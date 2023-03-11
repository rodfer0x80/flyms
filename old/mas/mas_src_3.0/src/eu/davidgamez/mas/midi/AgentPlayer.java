package eu.davidgamez.mas.midi;

//Java imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCPortOut;

import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.exception.MASException;


/** Class that runs as a separate thread to manage agents as they generate
     the MIDI notes. */
class AgentPlayer extends Thread {
	//=====================  ORDINARY VARIABLES  ====================
	/** MIDI sequencer. Used to start MIDI play when buffer is full */
	MASSequencer masSequencer;
	
	/** Controls run method */
	private boolean stop = true;
	
	/** Records if there has been an error */
	private boolean error  = false;
	
	/** Error message when an error has occurred */
	private String errorMessage = "";
	
	/** Records when agents have finished updating the buffer */
	HashMap<String, Boolean> agentsCompleteMap = new HashMap<String, Boolean>();
	
	/** Declare boolean once to save recreating it for every agent */
	Boolean updateComplete = new Boolean(true);
	
	/** Records time at which play started  - used for OSC messages */
	private long playStartTime_ms;
	
	private long packetPlayTime_ms;
	
	private ArrayList<ShortMessage> tmpArrayList;
	
	private TreeMap<Long, ArrayList<ShortMessage>> loadedBuffer;
	
	/** List of receivers that receive MIDI messages */
	ArrayList<OSCPortOut> oscPortOutList;
	
	
	/** Constructor */
	public AgentPlayer(MASSequencer masSequencer){
		this.masSequencer = masSequencer;
	}
	
	
	/*----------------------------------------------------------------*/
	/*-------                   PUBLIC METHODS                  ------*/
	/*----------------------------------------------------------------*/
	
	/** Returns error message */
	public String getErrorMessage() {
		return errorMessage;
	}


	/** Returns true if an error has occurred */
	public boolean isError(){
		return error;
	}

	
	/** Returns true if the player is in its run method */
	public boolean isPlaying(){
		return !stop;
	}
	
	
	/** Resets the state of the class - currently only clears the errors */
	public void reset(){
		clearError();
	}

	
	/** Main run method inherited from thread. */
	public void run() {
		// Set stop to false to enable main while loop
		stop = false;
		
		//Set thread to run on high priority
		this.setPriority(MIN_PRIORITY);
		
		//Clear error messages and state
		clearError();

		// Reset the MIDI buffer
		Buffer.reset();
		
		//Store reference to list of OSC receivers
		oscPortOutList = OSCDeviceManager.getOSCPortOutArrayList();

		// Reset the tracks and agents
		AgentHolder.resetAgents();
		TrackHolder.resetTracks();
		
		//Store the head start to save calling function repeatedly
		long headStart_buffers = Buffer.getHeadStart_buffers();
		
		//Local store of loadbuffer count to avoid function call overhead
		int loadBufferCount;
		
		//Record time at which play started
		playStartTime_ms = System.currentTimeMillis();
		
		try{
			/* Fill up head start of buffers
			   This needs to be done first so that the agents get ahead of the player,
			   No sleeping at this stage in between updates. */
			for(int i=0; i<headStart_buffers; ++i){
				// Call agents on current buffer
				agentBufferUpdate();
	
				// Move buffer to next position
				Buffer.advanceLoadBuffer();
				
				//Send any OSC messages - these access the loaded buffer
				//sendOSCMessages();
			}
			
			//Start MIDI sequencer playing
			masSequencer.play();
			
			/*
			 * Each buffer should last bufferLength_ticks * millisecsPerTick milliseconds. 
			 * Timing is less critical than in the sequencer because it will just bring
			 * forward or retard the filling up of a buffer by a couple of
			 * milliseconds.
			 */
			while (!stop) {
				// Call agents on current buffer
				agentBufferUpdate();
	
				// Move buffer to next position
				Buffer.advanceLoadBuffer();
				
				//Send OSC messages if there are receivers - these access the loaded buffer
				//sendOSCMessages();
				
				//Copy of load buffer count to avoid buffer access
				loadBufferCount = Buffer.getLoadBufferCount();
				
				//Sleep until the play buffer has advanced
				while(!stop && (loadBufferCount - headStart_buffers >= Buffer.getPlayBufferCount())){
					sleep(50);
				}
			}
		}
		catch(Exception ex){
			setError(ex.getMessage());
		}
		
		stop = true;
	}
	
	
	/** Causes thread to exit run method and terminate */
	public void stopThread(){
		stop = true;
	}
	
	
	/*----------------------------------------------------------------*/
	/*-------                  PRIVATE METHODS                  ------*/
	/*----------------------------------------------------------------*/
	
	/**  Update each agent requesting it to update the tracks that it is watching. 
	 	FIXME: TRACK RETURN VALUE AND CALL AGAIN IF NECESSARY. */
	private void agentBufferUpdate() throws InvalidMidiDataException {
		//Get reference to map of agents and order of agents
		HashMap<String, Agent> agentMap = AgentHolder.getAgentMap();
		ArrayList<String> agentOrderList = AgentHolder.getAgentOrderList();

		//Reset map recording agent's completion status
		agentsCompleteMap.clear();

		//Keep updating until all agents return true
		while(agentsCompleteMap.size() != agentMap.size()){
			
			//Work through all the agents in order and update them
			for (String agentID : agentOrderList) {
				// Request MIDI notes and agent messages from each agent
				if (agentMap.get(agentID).isEnabled()) {
					if(agentMap.get(agentID).updateTracks(Buffer.getLoadStart_ticks(), Buffer.getLoadEnd_ticks()))
						agentsCompleteMap.put(agentID, updateComplete);
				}
				else{
					agentsCompleteMap.put(agentID, updateComplete);
				}
			}
		}
	}


	/** Clears the error state */
	private void clearError(){
		errorMessage = "";
		error = false;
	}
	
	/** Sends OSC messages to all receivers */
	private void sendOSCMessages() throws IOException, MASException {
		//Return immediately if there are not any OSC receivers set
		
		loadedBuffer = Buffer.getLoadedBuffer();
		
		// Update the number of nanoseconds in each buffer. Refresh this value each loop in case tempo has changed
		double nanoSecPerTick = Globals.getNanoSecPerTick();
		
		//Work through current play buffer
		for(Long msgPosition_ticks : loadedBuffer.keySet()){
			
			//Get array of MIDI events to play at this tick
			tmpArrayList = loadedBuffer.get(msgPosition_ticks);
			
			//Calculate the time this event should be played in milliseconds
			packetPlayTime_ms = Math.round( ( msgPosition_ticks * nanoSecPerTick) / 1000000 ) + playStartTime_ms; 
			
			//Create bundle
			OSCBundle oscBundle = new OSCBundle(new Date(packetPlayTime_ms));
			//System.out.println("PACKET PLAY TIME MS: " + packetPlayTime_ms);
			
			//Add OSC packets to bundle
			for(ShortMessage midiMsg : tmpArrayList){
				oscBundle.addPacket(getOSCPacket(midiMsg));
			}
			
			//Send MIDI event to current receivers
			for (OSCPortOut tmpOscPortOut : oscPortOutList) { 
				tmpOscPortOut.send(oscBundle);
			}
		}
	}
	
	private OSCPacket getOSCPacket(ShortMessage midiMsg){
   	 	//Build contents of packet
		Object packetContents[] = new Object[2];
   	 	
   	 	//Note on message
   	 	if(midiMsg.getCommand() == 144){
   	 		packetContents[0] = new Integer(midiMsg.getData1());//Note
   	 		packetContents[1] = new Integer(midiMsg.getData2());//Velocity
   	 	}
   	 	//Note off message
   	 	else if(midiMsg.getCommand() == 128){
   	 		packetContents[0] = new Integer(midiMsg.getData1());//Note
   	 		packetContents[1] = new Integer(0);//Velocity set to zero for note off
   	 	}
   	 	else{
   	 		setError("Unrecognized command: " + midiMsg.getCommand());
   	 	}

   	 	//Return the packet
        return new OSCMessage("/mas/channel" + (midiMsg.getChannel()+1), packetContents);
		
	}
	
	
	/** Sets the error state */
	private void setError(String errorMessage){
		this.errorMessage = errorMessage;
		error = true;
		stop = true;
		System.out.println("AgentPlayer Error: " + errorMessage);
	}


}
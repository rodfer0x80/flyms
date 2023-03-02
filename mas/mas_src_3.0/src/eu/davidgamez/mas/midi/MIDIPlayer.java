package eu.davidgamez.mas.midi;

//Java imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPacket;
import com.illposed.osc.OSCPortOut;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.exception.MASException;


public class MIDIPlayer extends Thread implements Constants {
	/** Used to exit from run loop */
	private boolean stop = true;

	/** Records if an error has occurred */
	private boolean error = false;
	
	/** Message associated with an error */
	private String errorMessage = "";
	
	/** List of receivers that receive MIDI messages */
	ArrayList<Receiver> receiverArrayList;
	
	/** List of receivers that receive MIDI messages */
	ArrayList<OSCPortOut> oscPortOutList;

	/*------------------------------------------------------------*/
	/*-------                 PUBLIC METHODS               -------*/
	/*------------------------------------------------------------*/

	/** Run method of thread */
	public void run() {
		System.out.println("Starting MIDIPlayer.");
		
		//Record for entire application that play is in progress
		Globals.setPlaying(true);
		
		//Store reference to the current list of receivers
		receiverArrayList = MIDIDeviceManager.getReceiverArrayList();
		
		//Store reference to list of OSC receivers
		oscPortOutList = OSCDeviceManager.getOSCPortOutArrayList();
		
		// Keep running until we are told to stop
		stop = false;	
		
		//Clear any previous error messages
		clearError();
		
		//Check receivers have been set
		if( ( receiverArrayList == null || receiverArrayList.isEmpty() ) && ( oscPortOutList == null || oscPortOutList.isEmpty() ) )
			setError("No receivers set, cannot play without receivers");
		
		//Start main run loop
		try{
			play();
		}
		catch(MASException ex){
			setError(ex.getMessage());
		}
		catch(Exception ex){
			ex.printStackTrace();
			setError(ex.getMessage());
		}
		stop = true;
		
		//Record for entire application that play is complete
		Globals.setPlaying(false);
		
		//Play has been stopped or an error has occurred
		EventRouter.stopActionPerformed();	

		System.out.println("Stopping MIDIPlayer");
	}
	
	
	public void clearError(){
		error = false;
		errorMessage = "";
	}
	
	public String getErrorMessage(){
		return errorMessage;
	}
	
	public boolean isError(){
		return error;
	}
	
	public boolean isPlaying(){
		return !stop;
	}

	public void stopThread(){
		stop = true;
	}
	
	/*------------------------------------------------------------*/
	/*-------                 PRIVATE METHODS              -------*/
	/*------------------------------------------------------------*/
	
	/** Play works by loading the next list of midi events
	 * and waiting until they should be played.
	 * The time stamp associated with a message is in ticks, starting from the start of play.
	 */
	private void play() throws MASException, InterruptedException, IOException{
		// Declare tree map outside of main method so that they can just be
		// allocated when unpacking buffer
		ArrayList<ShortMessage> tmpArrayList;
		TreeMap<Long, ArrayList<ShortMessage>> playBuffer;
		
		//Set thread to run on high priority
		this.setPriority(MAX_PRIORITY);
			
		//Local copy to avoid calling Buffer method
		long bufferLength_ticks = Buffer.getLength_ticks();
		
		//The tick position of the last message to be played
		long lastMsg_ticks = 0;
		
		//Start of the buffer in ticks
		long startBuffer_ticks;
		
		//Time at which the last note in the buffer was played
		long endBuffer_ns = System.nanoTime();
		
		//General purpose integer to record the results of a delay calculation
		long delay_ms;
		
		//Play loop
		while (!stop) {
			
			// Update the number of nanoseconds in each tick. Refresh this value each loop in case tempo has changed
			double nanoSecPerTick = Globals.getNanoSecPerTick();
			
			//Get the current play buffer
			playBuffer = Buffer.getPlayBuffer();
						
			//Get the start of the next buffer in ticks
			startBuffer_ticks = bufferLength_ticks * Buffer.getPlayBufferCount();
			
			/* Calculate sleep time
			 * (startBuffer_ticks - lastMsg_ticks)*nanoSecPerTick //This is the nanoseconds left until the next buffer
			 * (System.nanoTime() - endBuffer_ns)//This is the time taken to advance the buffer.
			 */
			delay_ms = Math.round( ( (startBuffer_ticks - lastMsg_ticks)*nanoSecPerTick - (System.nanoTime() - endBuffer_ns) ) / 1000000 );
			if(delay_ms > 0)
				sleep(delay_ms);
			
			lastMsg_ticks = startBuffer_ticks;
					
			//Work through current play buffer
			for(Long msgPosition_ticks : playBuffer.keySet()){
				
				//Get array of MIDI events to play at this tick
				tmpArrayList = playBuffer.get(msgPosition_ticks);
				
				delay_ms = Math.round( ( (msgPosition_ticks - lastMsg_ticks)*nanoSecPerTick ) / 1000000 );
				if(delay_ms > 0)
					sleep(delay_ms);
				
				//Play the messages at this time
				for(ShortMessage midiMsg : tmpArrayList){
					//Send MIDI event to current receivers
					for (Receiver tmpRcv : receiverArrayList) { 
						tmpRcv.send(midiMsg, -1);
					}
					for (OSCPortOut tmpOscPortOut : oscPortOutList) { 
						tmpOscPortOut.send(getOSCPacket(midiMsg));
					}
					
				}
				lastMsg_ticks = msgPosition_ticks;
			}
			
			//Record the time at which the last note was played in the buffer.
			endBuffer_ns = System.nanoTime();
			
			//Advance the play buffer
			Buffer.advancePlayBuffer();
		}
	}
	
	private OSCPacket getOSCPacket(ShortMessage midiMsg){
   	 	//Build contents of packet
		Object packetContents[] = new Object[2];
   	 	
   	 	//Note on message
   	 	if(midiMsg.getCommand() == 144){
   	 		packetContents[0] = new Integer(midiMsg.getData1());//Note
   	 		packetContents[1] = new Float( midiMsg.getData2() / 127.0  );//Velocity
   	 	}
   	 	//Note off message
   	 	else if(midiMsg.getCommand() == 128){
   	 		packetContents[0] = new Integer(midiMsg.getData1());//Note
   	 		packetContents[1] = new Float(0.0);//Velocity set to zero for note off
   	 	}
   	 	else{
   	 		setError("Unrecognized command: " + midiMsg.getCommand());
   	 	}

   	 	//Return the packet
        return new OSCMessage("/mas/channel" + (midiMsg.getChannel()+1), packetContents);
		
	}

	/** Sets the thread into the error state, sets the thread to stop
	 * and stores the error message 
	 * @param errorMessage
	 */
	private void setError(String errorMessage){
		this.errorMessage = errorMessage;
		stop = true;
		error = true;
		System.out.println("MIDIPlayer Error: " + errorMessage);
	}
}

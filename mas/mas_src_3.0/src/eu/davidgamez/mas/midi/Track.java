package eu.davidgamez.mas.midi;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import javax.sound.midi.*;

import org.w3c.dom.Document;

import java.io.BufferedWriter;
import java.rmi.server.UID;

//MAS imports
import eu.davidgamez.mas.*;
import eu.davidgamez.mas.event.BufferListener;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.gui.MsgHandler;


public class Track implements BufferListener, Constants {
	//Name of this track
	private String name = "Untitled";

	//Midi channel of this track
	private int midiChannel = DEFAULT_MIDI_CHANNEL;

	//The start and end points of the buffer in the track that is being edited
	// private long bufferStart_ticks = -1, bufferEnd_ticks = -1;

	/* Buffer to hold the sequence that the agents edit at each time step.
     Short messages are stored with a key corresponding to their timestep.
     This buffer is emptied at each buffer advance with all notes being added and subtracted simultaneously from
     a global buffer that is held in the MIDISequencer */
	private TreeMap<Long, ArrayList<ShortMessage>> midiMessageMap = new TreeMap<Long, ArrayList<ShortMessage>>();

	/* Buffer to hold messages that agents send to one another. This buffer is emptied each time the buffer advances. */
	private TreeMap<Long, ArrayList<AgentMessage>> agentMessageMap = new TreeMap<Long, ArrayList<AgentMessage>>();

	/* Reference to the array of global buffers that hold all messages.
     This holds the same contents as the local buffers within each track, but by holding
      it in this way it saves copying the midi messages across later after they have been added. */
	private TreeMap<Long, ArrayList<ShortMessage>> [] globalBufferArray;

	//Unique id of the track
	private String uniqueID;

	//Is the track muted?
	private boolean mute = false;
	
	/** Static map holding all of the tracks that are currently soloed */
	private static HashMap<Track, Boolean> soloHashMap = new HashMap<Track, Boolean>(); 


	/** Standard constructor */
	public Track() {
		uniqueID = new UID().toString();

		//Register to listen for buffer advance events
		EventRouter.addBufferListener(this);
	}
	
	/** Constructor when loading from XML */
	public Track(String xmlStr) {
		//Load from xml
		loadFromXML(xmlStr);

		//Register to listen for buffer advance events
		EventRouter.addBufferListener(this);
	}
	

	//-------------------------------------------------------------------------------------
	//----------                         Transport Methods                      -----------
	//-------------------------------------------------------------------------------------

	/** Called just before the Buffer advances to the next load buffer */
	public void startLoadBufferAdvanceOccurred(long bufferCount){
		//Copy MIDI messages into the global buffer
		Buffer.addMidiMessages(midiMessageMap);

		//Empty message map and track buffer map here.
		agentMessageMap.clear();
		midiMessageMap.clear();
	}

	
	/** Called when the advance to the next buffer is complete */
	public void endLoadBufferAdvanceOccurred(long bufferCount){
	}

	
	/** Called when play is started to set buffer start and end to the correct initial values */
	public void reset(){
		//Clear buffers
		agentMessageMap.clear();
		midiMessageMap.clear();
	}


	//Unused methods inherited from BufferListener
	public void playBufferAdvanceOccurred(long bufferCount){
	}
	public void trackAdvanceOccurred(long beatCount){
	}


	//-------------------------------------------------------------------------------------
	//----------        Methods to Add and Delete Agent and MIDI Messages       -----------
	//-------------------------------------------------------------------------------------

	//Adds an agent message to the buffers
	public void addAgentMessage(long messageTime, AgentMessage agMsg) throws InvalidMidiDataException{
		//Check that track is not muted - add nothing if it is
		if(mute)
			return;
		
		// At least one track is soloed and this track is not in the list 
		if(!soloHashMap.isEmpty() && !soloHashMap.containsKey(this))
			return;
		

		//Check that message time is inside buffer
		if(messageTime < Buffer.getLoadStart_ticks() || messageTime >= Buffer.getLoadEnd_ticks())
			throw new InvalidMidiDataException ("Trying to add agent message outside buffer. Message time = " + messageTime + " bufferStart = " + Buffer.getLoadStart_ticks() + " bufferEnd = " + Buffer.getLoadEnd_ticks());

		//Create a key to access the maps
		Long messageKey = new Long(messageTime);

		//See if there is an array list at this message time and add message to existing array list or new array list
		if (agentMessageMap.containsKey(messageKey)){
			//Add agent message to this position
			ArrayList<AgentMessage>  tempArrayList =  agentMessageMap.get(messageKey);
			tempArrayList.add(agMsg);
		}
		else { //Create a new array list and add it at this position
			ArrayList tempArrayList = new ArrayList<AgentMessage>();
			tempArrayList.add(agMsg);
			agentMessageMap.put(messageKey, tempArrayList);
		}
	}


	//Adds a short MIDI message to the buffers
	public void addMidiMessage(long messageTime, ShortMessage shortMsg) throws InvalidMidiDataException{
		//Check that track is not muted - add nothing if it is
		if(mute)
			return;
		
		// At least one track is soloed and this track is not in the list 
		if(!soloHashMap.isEmpty() && !soloHashMap.containsKey(this))
			return;
		
		//Check that message time is inside buffer
		if(messageTime < Buffer.getLoadStart_ticks() || messageTime >= Buffer.getLoadEnd_ticks()){
			Utilities.printShortMessage(shortMsg);
			throw new InvalidMidiDataException(
					"Trying to add short message outside buffer. Message time = " +
					messageTime + " bufferStart = " + Buffer.getLoadStart_ticks() + " bufferEnd = " + Buffer.getLoadEnd_ticks());
		}

		//Create a key to access the maps
		Long messageKey = new Long(messageTime);

		//See if there is an array list at this message time and add message to existing array list or new array list
		if (midiMessageMap.containsKey(messageKey)){
			//Add agent message to this position
			ArrayList<ShortMessage>  tempArrayList =  midiMessageMap.get(messageKey);
			tempArrayList.add(shortMsg);
		}
		else { //Create a new array list and add it at this position
			ArrayList<ShortMessage> tempArrayList = new ArrayList<ShortMessage>();
			tempArrayList.add(shortMsg);
			midiMessageMap.put(messageKey, tempArrayList);
		}
	}


	/** Removes a short MIDI message */
	public boolean removeMidiMessage(Long messageTime, ShortMessage shortMessage){
		//Is there an entry for this time?
		if (!midiMessageMap.containsKey(messageTime))
			return false;

		//Does the array list at this time contain the message?
		if(!midiMessageMap.get(messageTime).contains(shortMessage))
			return false;

		//Remove the message
		midiMessageMap.get(messageTime).remove(shortMessage);
		return true;
	}


	//-------------------------------------------------------------------------------------
	//----------                  Saving and Loading Methods                    -----------
	//-------------------------------------------------------------------------------------

	/** Returns an XML string with the track's parameters */
	public String getXML(String indent){
		String trackStr = indent + "<midi_track>";
		trackStr += indent + "\t<name>" + name + "</name>";
		trackStr += indent + "\t<id>" + uniqueID + "</id>";
		trackStr += indent + "\t<channel>" + midiChannel + "</channel>";
		trackStr += indent + "</midi_track>";
		return trackStr;
	}

	
	/** Loads track parameters from the XML string. */
	public void loadFromXML(String xmlStr){
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			setName(Util.getStringParameter("name", xmlDoc));
			setUniqueID(Util.getStringParameter("id", xmlDoc));
			setChannel(Util.getIntParameter("channel", xmlDoc));
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}
	

	//-------------------------------------------------------------------------------------
	//----------                       Accessor Methods                         -----------
	//-------------------------------------------------------------------------------------

	//Returns all agent messages at a particular time
	public ArrayList<AgentMessage> getAgentMessages(long time){
		Long messageKey = new Long(time);
		if (agentMessageMap.containsKey(messageKey)){
			//Return list of messages at this position
			return (ArrayList<AgentMessage>) agentMessageMap.get(messageKey);
		}
		return new ArrayList<AgentMessage>();
	}


	//Gets the channel of the track
	public int getChannel(){
		return midiChannel;
	}


	//Returns the current buffer containing all of the agent messages
	public TreeMap<Long, ArrayList<AgentMessage>> getAgentMessages(){
		return agentMessageMap;
	}


	//Returns a buffer containing all of the MIDI messages
	public TreeMap<Long, ArrayList<ShortMessage>> getMidiMessages(){
		return midiMessageMap;
	}


	//Returns the name of the track
	public String getName(){
		return name;
	}


	//Returns all short MIDI messages at a particular time
	public ArrayList<ShortMessage> getMidiMessages(long time){
		Long messageKey = new Long(time);
		if (midiMessageMap.containsKey(messageKey)) {
			//Return list of messages at this position
			return (ArrayList<ShortMessage>) midiMessageMap.get(messageKey);
		}
		return new ArrayList<ShortMessage>();
	}


	//Returns the unique id of the track. #FIXME# NOT SURE IF THIS IS USED
	public String getID(){
		return uniqueID;
	}


	public boolean muted(){
		return mute;
	}
	
	
	/** Returns true if this track is soloed */
	public boolean soloed(){
		if(soloHashMap.containsKey(this))
			return true;
		return false;
	}

	
	/** Sets the soloed state of this track */
	public void setSoloed(boolean soloed){
		if(soloed)
			soloHashMap.put(this, new Boolean(true));
		else 
			soloHashMap.remove(this);
	}
	

	//Sets the channel of the track
	public void setChannel(int chNumber){
		midiChannel = chNumber;
	}
	
	
	/** Sets all of the MIDI messages  */
	public void setMidiMessageMap(TreeMap<Long, ArrayList<ShortMessage>> newMsgMap){
		this.midiMessageMap = newMsgMap;
	}


	public void setMuted(boolean mtd){
		mute = mtd;
	}

	//Sets the name of the track
	public void setName(String n){
		name = n;
	}

	public void setUniqueID(String uid){
		this.uniqueID = uid;
	}

	//-------------------------------------------------------------------------------------
	//----------                         Debug Methods                          -----------
	//-------------------------------------------------------------------------------------

	//Prints out information about the track
	public void printTrack(){
		System.out.println("Track name: " + name + "; Midi channel: " + midiChannel);
	}


}

package eu.davidgamez.mas.midi;

//Java imports
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.sound.midi.ShortMessage;

import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.ResetListener;
import eu.davidgamez.mas.exception.MASException;
import eu.davidgamez.mas.gui.MsgHandler;


/* ------------------------------ Buffer -----------------------------------
    Buffer containing the MIDI notes and messages.
    Only one buffer per application
   -----------------------------------------------------------------------------
 */
public class Buffer implements Constants {

	/** The length of the buffer in ticks*/
	private static long length_ticks = DEFAULT_BUFFER_LENGTH_BEATS * PPQ_RESOLUTION;

	/** The length of the buffer in beats */
	private static long length_beats = DEFAULT_BUFFER_LENGTH_BEATS;

	/** The start of the buffer in ticks. */
	private static long start_ticks;

	/** The number of load buffers that have elapsed */
	private static int loadBufferCount;

	/** The number of play buffers that have elapsed */
	private static int playBufferCount;

	/** The head start of the buffer in number of buffers */
	private static long headStart_buffers = DEFAULT_BUFFER_HEADSTART_BUFFERS;

	/** Buffers are managed on a rotating basis at any point in time one buffer
	   is being loaded whilst a different buffer is being played.
	   Play index is the buffer being played by the sequencer */
	private static int playIndex = 0;

	/** Load index is the buffer being filled with midi messages by the agents */
	private static int loadIndex = 0;
	
	private static int loadedIndex = -1;

	/** Rotating array of buffers.
	    Each buffer is a Tree map containing all of the midi events to be played.
	   	The key is the MIDI message time in ticks
	    The value is an array list containing all of the messages for that time */
	private static TreeMap<Long, ArrayList<ShortMessage>>[] midiBufferArray;	


	
	/*-----------------------------------------------------------*/
	/*-------              PUBLIC METHODS                --------*/
	/*-----------------------------------------------------------*/
	
	/** Returns an XML string describing the buffer properties */
	public static String getXML(String indent){
		String tmpXMLStr = indent + "<buffer_parameters>";
		tmpXMLStr += indent + "\t<length_beats>" + length_beats + "</length_beats>";
		tmpXMLStr += indent + "\t<headstart_buffers>" + headStart_buffers + "</headstart_buffers>";
		tmpXMLStr += indent + "</buffer_parameters>";
		return tmpXMLStr;
	}
	
	
	/** Loads buffer properties from the XML string */
	public static void loadFromXML(String xmlStr){
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			setLength_beats(Util.getIntParameter("length_beats", xmlDoc));
			setHeadStart_buffers(Util.getIntParameter("headstart_buffers", xmlDoc));
		}
		catch(Exception ex){
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}
	

	/** Initializes all of the buffers in the array */
	public synchronized static void init(){
		/** Set up the global buffer array. Has to be cast in this way because of some Java bug:
	  		http://forum.java.sun.com/thread.jspa?threadID=662450&messageID=3884829 */
		midiBufferArray = (TreeMap<Long, ArrayList<ShortMessage>>[]) new TreeMap[Globals.getNumberOfBuffers()];
		for (int i = 0; i < Globals.getNumberOfBuffers(); ++i) {
			midiBufferArray[i] = new TreeMap<Long, ArrayList<ShortMessage>>();
		}
		
		//Initialize variables
		reset();
	}

	
	/** Adds the supplied midi messages to the buffer in the correct place */
	public synchronized static void addMidiMessages(TreeMap<Long, ArrayList<ShortMessage>> midiMessageMap){

		//Work through all of the time steps in the message map
		for(Long msgTime : midiMessageMap.keySet()){
			
			/* If there is already an array list at this message time, add messages to existing array list*/
			if (midiBufferArray[loadIndex].containsKey(msgTime)){
				//Get existing array list at this time
				ArrayList<ShortMessage> tempArrayList = midiBufferArray[loadIndex].get(msgTime);
				
				//Add messages to list
				for(ShortMessage shortMsg : midiMessageMap.get(msgTime)){
					tempArrayList.add(shortMsg);
				}
			}
			
			/* Load buffer does not have an array list at this point in time, so add new array list */
			else { 
				//Add new array list to the current buffer
				ArrayList<ShortMessage> tmpArrayList = new ArrayList<ShortMessage>();
				for(ShortMessage shortMsg : midiMessageMap.get(msgTime)){
					tmpArrayList.add(shortMsg);
				}
				midiBufferArray[loadIndex].put(msgTime, tmpArrayList);
			}
		}
	}


	/** Advances the load buffer */
	public synchronized static void advanceLoadBuffer(){
		//Inform other classes that load buffer is about to start
		EventRouter.startLoadBufferAdvancePerformed(Buffer.loadIndex);
		
		start_ticks += length_ticks;
		++loadBufferCount;
		
		loadedIndex = loadIndex;
		
		++loadIndex;
		loadIndex %= Globals.getNumberOfBuffers();

		//Signal to other classes that play buffer has advanced
		EventRouter.endLoadBufferAdvancePerformed(Buffer.loadIndex);
	}


	/** Advances the play buffer  */
	public synchronized static void advancePlayBuffer(){
		//Clear the data in the current play buffer
		midiBufferArray[playIndex].clear();

		//Advance to the next play buffer
		++playBufferCount;
		++playIndex;
		playIndex %= Globals.getNumberOfBuffers();

		//Signal to other classes that play buffer has advanced
		EventRouter.playBufferAdvancePerformed(Buffer.playIndex);
		EventRouter.trackAdvancePerformed(playBufferCount*length_beats);
	}


	/** Resets the state of the buffer */
	public synchronized static void reset(){
		//Reset variables
		start_ticks = 0;
		loadBufferCount = 0;
		playBufferCount = 0;
		playIndex = 0;
		loadIndex = 0;
		loadedIndex = -1;
		
		//Reset buffers
		for (int i = 0; i < Globals.getNumberOfBuffers(); ++i) {
			midiBufferArray[i].clear();
		}
	}
	
	public static void printPlayBuffer(){
		System.out.println("Play buffer at index: " + playIndex);
		
		//Work through all of the times
		for(Long msgTime : midiBufferArray[playIndex].keySet()){
			System.out.println("Time " + msgTime);
			
			//Work through all the short messages at this time
			for(ShortMessage shortMsg : midiBufferArray[playIndex].get(msgTime)){
				printShortMessage(shortMsg);
			}
		}
	}
	
	public static void printLoadBuffer(){
		System.out.println("Load buffer at index: " + loadIndex);
		
		//Work through all of the times
		for(Long msgTime : midiBufferArray[loadIndex].keySet()){
			System.out.println("Time " + msgTime);
			
			//Work through all the short messages at this time
			for(ShortMessage shortMsg : midiBufferArray[loadIndex].get(msgTime)){
				printShortMessage(shortMsg);
			}
		}
	}
	
	public static void printShortMessage(ShortMessage shortMsg){
		System.out.print("\tShortMessage. CHANNEL: " + shortMsg.getChannel());
		System.out.print(" COMMAND: " + shortMsg.getCommand() + " DATA1: " + shortMsg.getData1());
		System.out.println(" DATA2: " + shortMsg.getData2());
	}
	
	
	/*-----------------------------------------------------------*/
	/*-------              PRIVATE METHODS               --------*/
	/*-----------------------------------------------------------*/
	
	/** Prints out a map containing the MIDI messages for debugging */
	public static void printMidiMessageMap(TreeMap<Long, ArrayList<ShortMessage>> midiMessageMap){
		System.out.println("==================  MIDI MESSAGE MAP  =================");
		for(Long msgTime : midiMessageMap.keySet()){
			System.out.print("Time " + msgTime);
			
			//Work through all the short messages at this time
			for(ShortMessage shortMsg : midiMessageMap.get(msgTime)){
				printShortMessage(shortMsg);
			}
		}
	}
	

	/*-----------------------------------------------------------*/
	/*-------                 ACCESSORS                  --------*/
	/*-----------------------------------------------------------*/

	public static int getLoadBufferCount(){
		return loadBufferCount;
	}

	public static int getPlayBufferCount(){
		return playBufferCount;
	}

	public static long getLoadStart_ticks(){
		return start_ticks;
	}

	public static long getLoadEnd_ticks(){
		return start_ticks + length_ticks;
	}

	//Accessor to get the current buffer length in beats
	public static long getLength_beats(){
		return length_beats;
	}

	//Accessor to get the current buffer length in ticks
	public static long getLength_ticks(){
		return length_ticks;
	}

	public static TreeMap<Long, ArrayList<ShortMessage>> getLoadBuffer(){
		return midiBufferArray[loadIndex];
	}
	
	public static TreeMap<Long, ArrayList<ShortMessage>> getLoadedBuffer() throws MASException {
		if(loadedIndex < 0)
			throw new MASException("Attempting to access loaded buffer that has not been initialized.");
		
		return midiBufferArray[loadedIndex];
	}
	
	
	public static TreeMap<Long, ArrayList<ShortMessage>> getPlayBuffer(){
		return midiBufferArray[playIndex];
	}

	//Accessor to set the current buffer length in beats
	public synchronized static void setLength_beats(long length_beats){
		Buffer.length_beats = length_beats;
		length_ticks = length_beats * PPQ_RESOLUTION;
	}


	public static long getHeadStart_buffers() {
		return headStart_buffers;
	}


	public synchronized static void setHeadStart_buffers(long headStartBuffers) {
		Buffer.headStart_buffers = headStartBuffers;
	}
	
}

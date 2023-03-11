package eu.davidgamez.mas.agents.timeinverter.midi;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Agent;
import eu.davidgamez.mas.midi.MIDIEvent;
import eu.davidgamez.mas.midi.Track;

/** Inverts the buffer temporally. If inversion leads to a note being
 	added at the end of the buffer it is shifted backwards by 1 tick.  */
public class TimeInverterAgent extends Agent {
	
	/** Controls whether inversion is applied to alternate buffers. */
	private boolean alternateBuffers = false;
	
	/** Tracks alternation of buffers */
	private int bufferCount = 0;

	/** Map linking a pitch with a time to record that a note on message was received
	 	at a particular time. */
	private HashMap<String, HashMap<Integer, Long> > noteOnMap = new HashMap<String, HashMap<Integer, Long> >();
	
	/** Map storing the note off messages associated with a particular channel */
	private HashMap<String, ArrayList<MIDIEvent> > noteOffMap = new HashMap<String, ArrayList<MIDIEvent> >();
	
	/** Map storing future events for each track */
	private HashMap<String, ArrayList<MIDIEvent> > storedEventMap = new HashMap<String, ArrayList<MIDIEvent> >();
	
	
	/** Constructor */
	public TimeInverterAgent() {
		super("Time Inverter", "Time Inverter", "TimeInverter");
	}


	/*--------------------------------------------------------------*/
	/*-------               PUBLIC METHODS                    ------*/
	/*--------------------------------------------------------------*/

	@Override
	public String getXML(String indent){
		String tmpStr = indent + "<midi_agent>";
		tmpStr += super.getXML(indent + "\t");
		tmpStr += indent + "\t<alternate_buffers>" + alternateBuffers + "</alternate_buffers>";
		tmpStr += indent + "</midi_agent>";
		return tmpStr;
	}
 
	
	@Override
	public void loadFromXML(String xmlStr) throws MASXmlException{
		super.loadFromXML(xmlStr);
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			alternateBuffers= Util.getBoolParameter("alternate_buffers", xmlDoc);
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}
	
	
	@Override
	protected void reset() {
		bufferCount = 0;
		storedEventMap.clear();
	}

	
	@Override
	protected boolean updateTracks(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException {
		//Output off messages left from a previous buffer
		outputStoredNotes(bufferStart_ppq, bufferEnd_ppq);
		
		//Do nothing if we are on an even number of buffers and alternating buffers
		++bufferCount;
		if(alternateBuffers && (bufferCount % 2 == 0))
			return true;

		//Reset note on and off maps
		noteOnMap.clear();
		noteOffMap.clear();
		
		//Goes through each track and invert the buffer in the track
		for(Track midiTrack : trackMap.values()){
			String trackID = midiTrack.getID();
			
			//Make sure there is an array list for this track
			noteOnMap.put(trackID, new HashMap<Integer, Long>());
			noteOffMap.put(trackID, new ArrayList<MIDIEvent>());
			
			//Get the current messages
			TreeMap<Long, ArrayList<ShortMessage>> midiMessageMap = midiTrack.getMidiMessages();
			
			//Replacement map
			TreeMap<Long, ArrayList<ShortMessage>> newMessageMap = new TreeMap<Long, ArrayList<ShortMessage> >();
			
			//Work through all of the message array lists
			for (Long time : midiMessageMap.keySet()) {
				//Get message array and create replacement
				ArrayList<ShortMessage> tmpMsgArr = midiMessageMap.get(time);
				ArrayList<ShortMessage> newMsgArray = new ArrayList<ShortMessage>();
								
				//Calculate new time for note on message array - ignore if it is at beginning of buffer
				long newTime;
				if(time == bufferStart_ppq)
					newTime = time;	
				else
					newTime = bufferEnd_ppq - time.longValue() + bufferStart_ppq;
				
				//Work through notes and create new array
				for(ShortMessage tmpMsg : tmpMsgArr){
					//Add note on messages to the new array
					if(tmpMsg.getCommand() == ShortMessage.NOTE_ON){
						newMsgArray.add(tmpMsg);
						noteOnMap.get(trackID).put(tmpMsg.getData1(), newTime - time);
					}
					else if(tmpMsg.getCommand() == ShortMessage.NOTE_OFF){
						if( noteOnMap.get(trackID).containsKey(tmpMsg.getData1()) ){
							long newNoteOffTime = time + noteOnMap.get(trackID).get(tmpMsg.getData1()).longValue();
							noteOffMap.get(trackID).add(new MIDIEvent(tmpMsg, newNoteOffTime));
							noteOnMap.get(trackID).remove(tmpMsg.getData1());
						}
					}
				}

				//Add note on array if we are not alternating buffers
				if(alternateBuffers && time == bufferStart_ppq)
					;//Do nothing -this would lead to the duplication of the note at the beginning of the buffer
				else{
					newMessageMap.put( newTime, newMsgArray );
				}
			}
			
			//Set the replacement map
			midiTrack.setMidiMessageMap(newMessageMap);
		}
		
		//Add the note off messages
		addNoteOffMessages(bufferStart_ppq, bufferEnd_ppq);
		
		return true;
	}

	
	/*---------------------------------------------------------*/
	/*-------               PRIVATE METHODS              ------*/
	/*---------------------------------------------------------*/
	
	/** Adds note off messages	 */
	private void addNoteOffMessages(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException{
		for(String trackID : noteOffMap.keySet()){
			ArrayList<MIDIEvent> tmpArr = noteOffMap.get(trackID);
			for(MIDIEvent tmpEvent : tmpArr){
				if(tmpEvent.getTimeStamp() < bufferStart_ppq){//Something funny happened here
					System.out.println("Event time falls before buffer??? time=" + tmpEvent.getTimeStamp());
				}
				else if(tmpEvent.getTimeStamp() < bufferEnd_ppq){//Add to current buffer
					if(trackMap.containsKey(trackID))//Check track has not been disconnected
						trackMap.get(trackID).addMidiMessage(tmpEvent.getTimeStamp(), tmpEvent.getMessage());
				}
				else{//Add to a later buffer
					if(!storedEventMap.containsKey(trackID))
						storedEventMap.put(trackID, new ArrayList<MIDIEvent>());
					storedEventMap.get(trackID).add(tmpEvent);
				}
			}
		}
	}


	/** Add any MIDI events that were generated in the previous cycle, but could not be added
		because they fell outside the buffer */
	private void outputStoredNotes(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException{
		//Work through all the stored events
		for(String trackID : storedEventMap.keySet()){
			Iterator<MIDIEvent> iter = storedEventMap.get(trackID).iterator();
			while (iter.hasNext()) {
				MIDIEvent tmpEvent = iter.next();
				
				//Perhaps agent has been disabled and enabled? Delete and ignore message if it is before start of buffer
				if (tmpEvent.getTimeStamp() < bufferStart_ppq){
					iter.remove();
				}
				
				//Add event to appropriate track
				else if (tmpEvent.getTimeStamp() < bufferEnd_ppq) { 
					if(trackMap.containsKey(trackID))//Check we are still connected to the track
						trackMap.get(trackID).addMidiMessage(tmpEvent.getTimeStamp(), tmpEvent.getMessage());
					
					//Remove off message after it has been added 
					iter.remove();
				}
			}
		}
	}

	
	/*---------------------------------------------------------*/
	/*-------               ACCESSORS                    ------*/
	/*---------------------------------------------------------*/

	/** Returns true if buffer alternation is set */
	public boolean isAlternatingBuffers(){
		return alternateBuffers;
	}
	
	public void setAlternatingBuffers(boolean altBuf){
		this.alternateBuffers = altBuf;
	}

}

package eu.davidgamez.mas.agents.randomeater.midi;

//Java imports
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.exception.MASAgentException;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Agent;
import eu.davidgamez.mas.midi.MIDIEvent;
import eu.davidgamez.mas.midi.Track;


public class RandomEater extends Agent implements Constants {
	
	/** Velocity used for all notes */
	final static int NOTE_VELOCITY = 80;

	/** The minimum pitch of the randomly generated notes. Ranges from 0-127 
	 	Must be <= maximumPitch. */
	private int minimumPitch = 0;
	
	/** The maximum pitch of the randomly generated notes. Ranges from 0-127. 
	 	Must be >=minimumPitch. */
	private int maximumPitch = 127;
	
	/** The frequency at which the random function will be evaluated */
	private int noteFrequency_ppq = PPQ_RESOLUTION;
	
	/** The probability that a note is generated each time the random function is evaluated */
	private double probability = 0.5;
		
	/** Stores the off notes that need to be located and removed after the on has been removed.
	 	Hash map key is the unique id of the midi track, each of which is associated with a hash map of off notes */
	private HashMap<String, HashMap<Integer, Boolean> > noteOffHashMap = new HashMap<String, HashMap<Integer, Boolean> >();
	
	/** Random number generator for the class */
	private Random randomGenerator = new Random();

	
	/** Constructor */
	public RandomEater(){
		super("Random Eater", "Random Eater", "RandomEater");
	}

	
	/*--------------------------------------------------------------*/
	/*-------               PUBLIC METHODS                    ------*/
	/*--------------------------------------------------------------*/
	
	@Override
	public String getXML(String indent){
		String tmpStr = indent + "<midi_agent>";
		tmpStr += super.getXML(indent + "\t");
		tmpStr += indent + "\t<min_pitch>" + minimumPitch + "</min_pitch>";
		tmpStr += indent + "\t<max_pitch>" + maximumPitch + "</max_pitch>";
		tmpStr += indent + "\t<note_frequency>" + noteFrequency_ppq + "</note_frequency>";
		tmpStr += indent + "\t<probability>" + probability + "</probability>";
		tmpStr += indent + "</midi_agent>";
		return tmpStr;
	}

	
	@Override
	public void loadFromXML(String xmlStr) throws MASXmlException {
		super.loadFromXML(xmlStr);
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			minimumPitch= Util.getIntParameter("min_pitch", xmlDoc);
			maximumPitch= Util.getIntParameter("max_pitch", xmlDoc);
			noteFrequency_ppq = Util.getIntParameter("note_frequency", xmlDoc);
			probability = Util.getDoubleParameter("probability", xmlDoc);
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}


	@Override
	public boolean updateTracks(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException{
		//Delete any off messages that could not be removed from the previous buffer
		deleteOffMessages(bufferStart_ppq, bufferEnd_ppq);
		
		//Identify the first point at which to evaluate the random function
		long noteDeletionPoint_ppq = 0;
		if(bufferStart_ppq % noteFrequency_ppq == 0)//Buffer start is an exact multiple of the note frequency
			noteDeletionPoint_ppq = bufferStart_ppq;
		else//Find the first point after buffer start that is an exact multiple of the note frequency
			noteDeletionPoint_ppq = bufferStart_ppq - (bufferStart_ppq % noteFrequency_ppq) + noteFrequency_ppq;
		
		//Work through each note insertion point
		while(noteDeletionPoint_ppq < bufferEnd_ppq){
			deleteNote(noteDeletionPoint_ppq, bufferEnd_ppq);
			noteDeletionPoint_ppq += noteFrequency_ppq;
		}
		
		//Agent has finished editing tracks
		return true;
	}

	
	@Override
	public void connectionStatusChanged(){
	}
	
	
	@Override
	public void enabledStatusChanged(){
	}
	
	
	/*--------------------------------------------------------------*/
	/*-------              PROTECTED METHODS                  ------*/
	/*--------------------------------------------------------------*/
	
	@Override
	protected void reset() {
		noteOffHashMap.clear();
	}

	
	/*--------------------------------------------------------------*/
	/*-------              PRIVATE METHODS                    ------*/
	/*--------------------------------------------------------------*/

	/** Deletes a note at random within the pitch range from all of the tracks it is connected to.
	   	Note off messages that fall outside the buffer are stored for deletion at a later point in time.	 */
	private void deleteNote(long noteStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException{
		//Decide whether to delete a note or not
		if(randomGenerator.nextDouble() > probability)
			return;

		//Work through all of the tracks
		for(Track midiTrack : trackMap.values()){
		
			//Look for a note to delete at this point by working through all the messages at this time step
			ArrayList<ShortMessage> tmpArrayList = (ArrayList<ShortMessage>) midiTrack.getMidiMessages(noteStart_ppq);
			Iterator<ShortMessage> msgIter = tmpArrayList.iterator();
			while(msgIter.hasNext()){
				ShortMessage tmpMsg = msgIter.next();

				//Delete the note if the pitch is in range
				if(tmpMsg.getCommand() == ShortMessage.NOTE_ON){
					
					int tmpPitch = tmpMsg.getData1();
					if(tmpPitch >= minimumPitch && tmpPitch <= maximumPitch){
						//Remove the note on from the track
						msgIter.remove();
						
						//Delete the associated note off message from the track
						deleteOffMessage(midiTrack.getID(), noteStart_ppq, tmpPitch);
						
						//Break out of loop
						break;
					}
				}
			}
		}
	}
	
		
	/** Deletes an off message from the track or store for later deletion if it cannot be found. */
	private void deleteOffMessage(String trackID, long noteStart_ppq, int pitch){
		//Look for note off with the specified pitch in remaining part of map
		SortedMap<Long, ArrayList<ShortMessage>> trackMsgMap = trackMap.get(trackID).getMidiMessages().tailMap(noteStart_ppq);
		for(Long key : trackMsgMap.keySet()){
			Iterator<ShortMessage> msgIter = trackMsgMap.get(key).iterator();
			while(msgIter.hasNext()){
				ShortMessage tmpMsg = msgIter.next();
				if(tmpMsg.getCommand() == ShortMessage.NOTE_OFF && tmpMsg.getData1() == pitch){
					msgIter.remove();
					return;
				}
			}
		}
		
		//If we have reached this point, note off has not been found in the current buffer - schedule for deletion later
		if(!noteOffHashMap.containsKey(trackID))
			noteOffHashMap.put(trackID, new HashMap<Integer, Boolean>());
		noteOffHashMap.get(trackID).put(new Integer(pitch), new Boolean(true));
	}
	
	
	/** Add any MIDI events that were generated in the previous cycle, but could not be added
      because they fell outside the buffer */
	private void deleteOffMessages(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException{
		//Work through all of the tracks
		for(Track midiTrack : trackMap.values()){
			
			//Only examine notes in track if there is a hash map with notes to delete
			if(noteOffHashMap.containsKey(midiTrack.getID())){
			
				//Work through all of the messages for each track
				TreeMap<Long, ArrayList<ShortMessage>> trackMsgs = midiTrack.getMidiMessages();
				for(ArrayList<ShortMessage> msgArrays : trackMsgs.values()){
	
					Iterator<ShortMessage> iter = msgArrays.iterator();	
					while(iter.hasNext()&& !noteOffHashMap.get(midiTrack.getID()).isEmpty()){
						ShortMessage msg = iter.next();
					
						if(msg.getCommand() == ShortMessage.NOTE_OFF){
							if(noteOffHashMap.get(midiTrack.getID()).containsKey(msg.getData1())){
								//Delete message from the track
								iter.remove();
								
								//Delete message from the noteOffHashMap
								noteOffHashMap.get(midiTrack.getID()).remove(msg.getData1());
							}
						}
					}
					
					if(noteOffHashMap.get(midiTrack.getID()).isEmpty())
						break;
				}
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*-------              ACCESSOR METHODS                   ------*/
	/*--------------------------------------------------------------*/
	
	public int getMinimumPitch() {
		return minimumPitch;
	}
	
	
	public int getMaximumPitch() {
		return maximumPitch;
	}


	public void setPitchRange(int minimumPitch, int maximumPitch) throws MASAgentException {
		//Check the data
		if(minimumPitch > 127 || minimumPitch < 0)
			throw new MASAgentException("RandomEater: Minimum pitch must be in the range 0-127");
		if(minimumPitch > maximumPitch)
			throw new MASAgentException("RandomEater: Minimum pitch must <= maximumPitch");
		if(maximumPitch > 127 || maximumPitch < 0)
			throw new MASAgentException("RandomEater: Maximum pitch must be in the range 0-127");
		if(maximumPitch < minimumPitch)
			throw new MASAgentException("RandomEater: Maximum pitch must >= minimumPitch");
		
		//Set the pitches
		this.maximumPitch = maximumPitch;
		this.minimumPitch = minimumPitch;
	}


	public int getNoteFrequency_ppq() {
		return noteFrequency_ppq;
	}


	public void setNoteFrequency_ppq(int noteFrequency_ppq) {
		this.noteFrequency_ppq = noteFrequency_ppq;
	}


	public double getProbability() {
		return probability;
	}


	public void setProbability(double prob) throws MASAgentException {
		if(prob < 0.0 || prob > 1.0)
			throw new MASAgentException("RandomEater: Probability must be in the range 0-1");
		this.probability = prob;
	}

}


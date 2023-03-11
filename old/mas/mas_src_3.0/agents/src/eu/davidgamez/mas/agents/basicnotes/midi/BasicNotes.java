package eu.davidgamez.mas.agents.basicnotes.midi;

//Java imports
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Iterator;
import java.io.BufferedWriter;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Agent;
import eu.davidgamez.mas.midi.AgentMessage;
import eu.davidgamez.mas.midi.MIDINote;
import eu.davidgamez.mas.midi.MIDIEvent;
import eu.davidgamez.mas.midi.Track;
import eu.davidgamez.mas.midi.Utilities;


public class BasicNotes extends Agent implements Constants {

	//Controls whether the notes are synched to bar markers or free running
	private boolean synchToBar = true;

	//If not synched to bar, need to define after which point will start emphasis sequence again
	private int sequenceLength_ppq = -1;

	//Counter keeping track of the current note insertion point
	private int noteCounter = 0;

	//Offset of the emphasis point for non-synch mode
	private long insertionPoint_ppq = -1;

	private boolean addNotes = false;

	private long bufferEnd_ppq = -1;


	//List of the notes. This should be the same length as the list of velocity percentage changes
	private int [] notePositionArray = new int [] {0, 2 * PPQ_RESOLUTION};//Default is place a note on the first and third beat in the bar
	private int [] noteLengthArray = new int [] {1*PPQ_RESOLUTION, 1*PPQ_RESOLUTION};//Notes are 1 beat long
	private int [] notePitchArray = new int [] {60, 70};//with pitch 60 and 70
	private int noteVelocity = 80;

	private ArrayList<MIDIEvent> midiEventArrayList = new ArrayList<MIDIEvent>();//TREE MIGHT BE BETTER SINCE IT IS STRUCTURED!


	public BasicNotes() {
		super("Basic Notes", "Basic Notes", "BasicNotes");
	}

	//Save agent data
	public String getXML(String indent){
		String tmpDesc = indent + "<midi_agent>";
		tmpDesc += super.getXML(indent + "\t");
		
		//Output synchronization
		tmpDesc += indent + "\t<synch_to_bar>" + synchToBar + "</synch_to_bar>";
		
		//Output note positions
		tmpDesc += indent + "\t<note_positions>";
		for (int i = 0; i < notePositionArray.length - 1; ++i)
			tmpDesc += notePositionArray[i] + ",";
		tmpDesc += notePositionArray[notePositionArray.length - 1] + "</note_positions>";
		
		//Output note lengths
		tmpDesc += indent + "\t<note_lengths>";
		for (int i = 0; i < noteLengthArray.length - 1; ++i)
			tmpDesc += noteLengthArray[i] + ",";
		tmpDesc += noteLengthArray[noteLengthArray.length - 1] + "</note_lengths>";
		
		//Output note pitches
		tmpDesc += indent + "\t<note_pitches>";
		for (int i = 0; i < notePitchArray.length - 1; ++i)
			tmpDesc += notePitchArray[i] + ",";
		tmpDesc += notePitchArray[notePitchArray.length - 1] + "</note_pitches>";
		
		//Output note velocity
		tmpDesc += indent + "\t<note_velocity>" + noteVelocity + "</note_velocity>";
		tmpDesc += indent + "</midi_agent>";
		return tmpDesc;
	}

	
	/** Loads agent parameters from the supplied XML string */
	public void loadFromXML(String xmlStr) throws MASXmlException{
		  super.loadFromXML(xmlStr);
		  try{
			  Document xmlDoc = Util.getXMLDocument(xmlStr);
			  synchToBar= Util.getBoolParameter("synch_to_bar", xmlDoc);
			  notePositionArray = Util.getIntArrayParameter("note_positions", xmlDoc);
			  noteLengthArray = Util.getIntArrayParameter("note_lengths", xmlDoc);
			  notePitchArray = Util.getIntArrayParameter("note_pitches", xmlDoc);
			  noteVelocity = Util.getIntParameter("note_velocity", xmlDoc);
		  }
		  catch(Exception ex){
			  System.out.println(xmlStr);
			  ex.printStackTrace();
			  MsgHandler.error(ex.getMessage());
			  return;
		  }

		//Check all arrays are the same length
		if(notePositionArray.length != notePitchArray.length || notePositionArray.length != noteLengthArray.length){
			throw new MASXmlException("Array Lengths do not match");
		}

		//Recalculate the sequence length
		calculateSequenceLength();

		//Reset counters and set insertionPoint_ppq to -1 to indicate that it must set its value to the first beat in buffer.
		resetAgent();
	}

	public void setNotes(TreeMap<Integer, MIDINote> noteList){
		//Copy into int arrays to save converting from Integers all the time at runtime
		notePositionArray = new int[noteList.size()];
		notePitchArray = new int[noteList.size()];
		noteLengthArray = new int[noteList.size()];

		int i=0;
		for(Integer tempInt : noteList.keySet()){
			notePositionArray[i] = tempInt.intValue();
			notePitchArray[i] = noteList.get(tempInt).getPitch();
			noteLengthArray[i] = (int)noteList.get(tempInt).getLength_ppq();
			++i;
		}

		//Recalculate the sequence length
		calculateSequenceLength();

		//Reset counters and set insertionPoint_ppq to -1 to indicate that it must set its value to the first beat in buffer.
		resetAgent();
	}

	//FIXME ADD REMAINING OFF NOTES TO TRACK TO STOP NOTES
	public void enabledStatusChanged(){
		if(!this.isEnabled())
			resetAgent();
	}

	public void connectionStatusChanged(){
		//If there are no connections left, need to re-initialise the agent so it can be restarted live
		if(trackMap.isEmpty()){
			resetAgent();
		}
	}

	public int [] getNotePositionArray(){
		return notePositionArray;
	}
	public int [] getNotePitchArray(){
		return notePitchArray;
	}

	public int [] getNoteLengthArray(){
		return noteLengthArray;
	}

	public void setSynchToBar(boolean val){
		synchToBar = val;
		resetAgent();
	}

	public boolean getSynchToBar(){
		return synchToBar;
	}


	//Main method called to request agent to add notes or messages to the buffer
	protected boolean updateTracks(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException {
		//Store buffer end for access by other methods
		this.bufferEnd_ppq = bufferEnd_ppq;

		//Go through each track
		for (Track  midiTrack : trackMap.values()) {

			/* Add any midi events that were generated in the previous cycle, but could not be added
        because they fell outside the buffer */
			Iterator<MIDIEvent> iter = midiEventArrayList.iterator();
			while (iter.hasNext()) {
				MIDIEvent tempMidiEvent = iter.next();
				if (tempMidiEvent.getTimeStamp() < bufferStart_ppq) //Perhaps track has been rewound, anyway error here
					iter.remove();
				else if (tempMidiEvent.getTimeStamp() < bufferEnd_ppq) { //Add to buffer
					midiTrack.addMidiMessage(tempMidiEvent.getTimeStamp(), tempMidiEvent.getMessage());
					iter.remove();
				}
			}
			//#FIXME# COULD BE A PROBLEM WITH MISSING NOTE OFF MESSAGES WHEN SYNCH IS CHANGED


			if (synchToBar) { //Restart notes on each bar marker

				//Work through the whole notes in the buffer
				//#ASSUMES# THAT BUFFER ADVANCES BY A WHOLE NUMBER OF BEATS
				for (long beat_ppq = Utilities.getFirstBeatInBuffer(bufferStart_ppq); beat_ppq < bufferEnd_ppq; beat_ppq += PPQ_RESOLUTION) {

					//===================================================================================
					//Check current beat for bar markers
					//===================================================================================
					//Extract the message buffer for the track
					ArrayList<AgentMessage> agMsgArrayList = midiTrack.getAgentMessages(beat_ppq);

					//Look through message map for a bar start
					for (AgentMessage agMsg : agMsgArrayList) {
						if (agMsg.getType() == AgentMessage.START_BAR) { //Have found a start bar message at this point
							noteCounter = 0; //Reset emphasis counter
							insertionPoint_ppq = beat_ppq; //All emphasis points will be offset from the bar marker
							addNotes = true;
						}
					}

					//========================================================================================
					//Add emphasis up to but not including the next beat in the buffer or up to the end
					//of the buffer if it does not contain another whole beat
					//========================================================================================
					if (addNotes) {
						long notePoint_ppq = notePositionArray[noteCounter] + insertionPoint_ppq;

						//Add notes to the current beat, where there may be a bar marker that resets everything
						while (notePoint_ppq >= beat_ppq && notePoint_ppq < beat_ppq + PPQ_RESOLUTION && notePoint_ppq < bufferEnd_ppq && addNotes) {
							addNote(notePoint_ppq, notePitchArray[noteCounter], noteLengthArray[noteCounter], midiTrack);

							//Increase array counter
							++noteCounter;
							if (noteCounter == notePositionArray.length) {
								noteCounter = 0;
								addNotes = false;
							}

							//Find next emphasis point
							notePoint_ppq = notePositionArray[noteCounter] + insertionPoint_ppq;
						}
					}
				}
			}
			else{//Run through notes in a loop
				//Initialise insertion point if necessary
				if(bufferStart_ppq == 0 || insertionPoint_ppq == -1){//We have just started the sequence or agent has been reset
					insertionPoint_ppq = Utilities.getFirstBeatInBuffer(bufferStart_ppq);
					noteCounter = 0;
				}
				long notePoint_ppq = notePositionArray[noteCounter] + insertionPoint_ppq;
				while(notePoint_ppq >= bufferStart_ppq && notePoint_ppq < bufferEnd_ppq){
					addNote(notePoint_ppq, notePitchArray[noteCounter], noteLengthArray[noteCounter], midiTrack);

					//Increase array counter in a circular way
					++noteCounter;
					noteCounter %= notePositionArray.length;

					//Increase insertonPoint if we have done a complete cycle of the arrays
					if (noteCounter == 0)
						insertionPoint_ppq += sequenceLength_ppq;

					//Find next note insertion point
					notePoint_ppq = notePositionArray[noteCounter] + insertionPoint_ppq;
				}
			}
		}
		return true;
	}

	private void addNote(long notePosition_ppq, int notePitch, int noteLength_ppq, Track midiTrack) throws InvalidMidiDataException{
		try{
			//Add note on message at beat_ppq. This will always be inside the buffer
			ShortMessage on = new ShortMessage();
			on.setMessage(ShortMessage.NOTE_ON, midiTrack.getChannel(), notePitch, noteVelocity);
			midiTrack.addMidiMessage(notePosition_ppq, on);

			//Add note off message. This may be outside the buffer, if so save it for the next update
			ShortMessage off = new ShortMessage();
			off.setMessage(ShortMessage.NOTE_OFF, midiTrack.getChannel(), notePitch, noteVelocity);

			if ( (notePosition_ppq + noteLength_ppq) >= bufferEnd_ppq) { //Off event will fall outside buffer
				midiEventArrayList.add(new MIDIEvent(off, notePosition_ppq + noteLength_ppq)); //Store for later update cycle
			}
			else { //Ok to add it to the current buffer
				midiTrack.addMidiMessage(notePosition_ppq + noteLength_ppq, off);
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	//Resets agent so it can restart successfully when update buffer is called on it at a later point
	private void resetAgent(){
		noteCounter = 0;
		insertionPoint_ppq = -1;
		addNotes = false;
		midiEventArrayList.clear();
	}


	//Works out the length in ticks of the sequence of notes held in this agent.
	private void calculateSequenceLength(){
		//Reset sequence length
		sequenceLength_ppq = 0;

		//Calculate length. It is the last whole note + the length of this note + 1 beat
		if(notePositionArray.length > 0){
			sequenceLength_ppq = (((notePositionArray[notePositionArray.length - 1] + noteLengthArray[notePositionArray.length - 1]) / PPQ_RESOLUTION) * PPQ_RESOLUTION) +  PPQ_RESOLUTION;
		}
		else{
			System.out.println("Note emphasis array length is zero!");
			sequenceLength_ppq = 0;
		}
	}

	private void printAgent(){
		System.out.println("------------------------ Basic Notes MIDI Agent -----------------------");
		for(int i=0; i<notePositionArray.length; ++i){
			System.out.println("Note position = " + notePositionArray[i] + "; note pitch = " + notePitchArray[i] + "; note length = " + noteLengthArray[i]);
		}
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}



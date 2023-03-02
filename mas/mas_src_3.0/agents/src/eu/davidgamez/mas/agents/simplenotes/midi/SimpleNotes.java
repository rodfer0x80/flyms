package eu.davidgamez.mas.agents.simplenotes.midi;

//Java imports
import java.util.Iterator;
import java.util.Vector;
import java.util.ArrayList;
import java.io.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Agent;
import eu.davidgamez.mas.midi.MIDIEvent;
import eu.davidgamez.mas.midi.Track;
import eu.davidgamez.mas.midi.Utilities;


public class SimpleNotes extends Agent implements Constants {

	private int notePitch = 60;
	private int noteVelocity = 80;
	private int noteFrequency_ppq = PPQ_RESOLUTION;
	private int noteLength_ppq = PPQ_RESOLUTION / 2;

	private long lastNote_ppq = -1;

	boolean frequencyChanged = false;
	int newNoteFrequency_ppq = -1;
	boolean connectionStatusChanged = false;
	boolean enabledStatusChanged = false;

	private ArrayList<MIDIEvent> midiEventArrayList = new ArrayList<MIDIEvent>();

	
	/** Constructor */
	public SimpleNotes(){
		super("Simple notes", "Simple Notes", "SimpleNotes");
	}

	
	/** Outputs agent parameters as an XML string */
	public String getXML(String indent){
		String tmpStr = indent + "<midi_agent>";
		tmpStr += super.getXML(indent + "\t");
		tmpStr += indent + "\t<note_pitch>" + notePitch + "</note_pitch>";
		tmpStr += indent + "\t<note_velocity>" + noteVelocity + "</note_velocity>";
		tmpStr += indent + "\t<note_frequency>" + noteFrequency_ppq + "</note_frequency>";
		tmpStr += indent + "\t<note_length>" + noteLength_ppq + "</note_length>";
		tmpStr += indent + "</midi_agent>";
		return tmpStr;
	}

	
	/** Loads the agent parameters from the supplied XML string */
	public void loadFromXML(String xmlStr) throws MASXmlException {
		super.loadFromXML(xmlStr);
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			notePitch= Util.getIntParameter("note_pitch", xmlDoc);
			noteVelocity = Util.getIntParameter("note_velocity", xmlDoc);
			noteFrequency_ppq = Util.getIntParameter("note_frequency", xmlDoc);
			noteLength_ppq = Util.getIntParameter("note_length", xmlDoc);
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}


	//Main method called to request agent to add notes to the buffer
	public boolean updateTracks(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException{
		//Update note frequency if it has been changed
		if(frequencyChanged){
			//Make sure that the next note is not outside of the bar. This can happen when increasing the frequency
			if (newNoteFrequency_ppq < noteFrequency_ppq)
				while (lastNote_ppq + newNoteFrequency_ppq < bufferStart_ppq)
					lastNote_ppq += newNoteFrequency_ppq;

			//Update the note frequency
			noteFrequency_ppq = newNoteFrequency_ppq;
		}

		//If connection status has been changed whilst playing set lastNote_ppq to the last whole note
		if(connectionStatusChanged || enabledStatusChanged){
			connectionStatusChanged = false;
			enabledStatusChanged = false;
			lastNote_ppq = Utilities.getFirstBeatInBuffer(bufferStart_ppq);
		}

		//Goes through each track
		for(Track midiTrack : trackMap.values()){

			/* Add any midi events that were generated in the previous cycle, but could not be added
            because they fell outside the buffer */
			Iterator<MIDIEvent> iter = midiEventArrayList.iterator();
			while (iter.hasNext()) {
				MIDIEvent tempMidiEvent = iter.next();
				if (tempMidiEvent.getTimeStamp() < bufferStart_ppq) //Perhaps track has been rewound, anyway error here
					iter.remove();
				else if (tempMidiEvent.getTimeStamp() < bufferEnd_ppq) { //Add to buffer
					midiTrack.addMidiMessage(tempMidiEvent.getTimeStamp(), (ShortMessage) tempMidiEvent.getMessage());
					iter.remove();
				}
			}

			//Initialise lastNote_ppq if necessary
			long nextNote_ppq;
			if(bufferStart_ppq == 0){
				lastNote_ppq = 0;
				nextNote_ppq = 0;
			}
			else{
				nextNote_ppq = lastNote_ppq + noteFrequency_ppq;
			}
			
			while (nextNote_ppq >= bufferStart_ppq && nextNote_ppq < bufferEnd_ppq) {
				addNote(nextNote_ppq, bufferEnd_ppq, midiTrack);

				//Get next note insertion point
				nextNote_ppq = lastNote_ppq + noteFrequency_ppq;
			}
		}
		return true;//Agent has finished editing tracks
	}

	public void connectionStatusChanged(){
		connectionStatusChanged = true;
	}

	public void enabledStatusChanged(){
		enabledStatusChanged = true;
	}

	//Adds the note to the buffer and stores any note off messages outside the buffer
	private void addNote(long noteStart_ppq, long bufferEnd_ppq, Track midiTrack)throws InvalidMidiDataException{
		//Add note on message. These will always be inside the buffer
		ShortMessage on = new ShortMessage();
		on.setMessage(ShortMessage.NOTE_ON, midiTrack.getChannel(), notePitch, noteVelocity);
		midiTrack.addMidiMessage(noteStart_ppq, on);

		//Store this notes's position
		lastNote_ppq = noteStart_ppq;

		//Add note off message. This may be outside the buffer, if so save it for the next update
		ShortMessage off = new ShortMessage();
		off.setMessage(ShortMessage.NOTE_OFF, midiTrack.getChannel(), notePitch, noteVelocity);

		if ( (noteStart_ppq + noteLength_ppq) >= bufferEnd_ppq) { //Off event will fall outside buffer
			midiEventArrayList.add(new MIDIEvent(off, noteStart_ppq + noteLength_ppq)); //Store for later update cycle
		}
		else { //Ok to add it to the current buffer
			midiTrack.addMidiMessage(noteStart_ppq + noteLength_ppq, off);
		}
	}

	public int getNotePitch(){
		return notePitch;
	}

	public void setNotePitch(int newPitch){
		notePitch = newPitch;
	}

	public int getNoteVelocity(){
		return noteVelocity;
	}

	public void setNoteVelocity(int newVelocity){
		noteVelocity = newVelocity;
	}

	public int getNoteLength(){
		return noteLength_ppq;
	}

	public void setNoteLength(int newLength){
		noteLength_ppq = newLength;
	}

	public int getNoteFrequency(){
		return noteFrequency_ppq;
	}

	public void setNoteFrequency(int newFrequency_ppq){
		if(Globals.isPlaying()){
			newNoteFrequency_ppq = newFrequency_ppq;
			frequencyChanged = true;
		}
		else
			noteFrequency_ppq = newFrequency_ppq;
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}


}

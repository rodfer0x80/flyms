package eu.davidgamez.mas.agents.pitchshifter.midi;

//Java imports
import java.util.Iterator;
import java.util.Vector;
import java.util.TreeMap;
import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.event.TrackBufferUpdateListener;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Agent;
import eu.davidgamez.mas.midi.Track;


public class PitchShifter extends Agent{

	//The amount the pitch is shifted by
	int pitchShiftAmount = 0;

	//Holds a list of classes that are updated whenever track buffer update is called
	ArrayList<TrackBufferUpdateListener> trackBufferUpdateListenerArray = new ArrayList<TrackBufferUpdateListener>();

	public PitchShifter(){
		super("Pitch Shifter", "Pitch Shifter", "PitchShifter");
	}

	//Save agent data
	public String getXML(String indent){
		String tmpStr = indent + "<midi_agent>";
		tmpStr += super.getXML(indent + "\t");
		tmpStr += indent + "\t<pitch_shift_amount>" + pitchShiftAmount + "</pitch_shift_amount>";
		tmpStr += indent + "</midi_agent>";
		return tmpStr;
	}

	public void loadFromXML(String xmlStr) throws MASXmlException{
		super.loadFromXML(xmlStr);
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			pitchShiftAmount= Util.getIntParameter("pitch_shift_amount", xmlDoc);
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}

	protected boolean updateTracks(long bufferStart, long bufferLength) throws InvalidMidiDataException{
		//Update any track buffer update listeners (probably the GUI in this case)
		for(TrackBufferUpdateListener tbul : trackBufferUpdateListenerArray)
			tbul.trackBufferUpdate();

		//Goes through each track and increases the pitch by pitchShiftAmount
		for(Track midiTrack : trackMap.values()){
			TreeMap<Long, ArrayList<ShortMessage>> midiMessageArrays = midiTrack.getMidiMessages();
			
			//Work through all of the message array lists
			for (ArrayList<ShortMessage> messageArray : midiMessageArrays.values()) {

				//Work through the array list and update the short messages in it
				for (ShortMessage tempMessage : messageArray) {
					if (tempMessage.getCommand() == ShortMessage.NOTE_ON || tempMessage.getCommand() == ShortMessage.NOTE_OFF) {
						//Work out the new note and make sure it is in range
						int newNote = tempMessage.getData1() + pitchShiftAmount;
						if (newNote > 127)
							newNote = 127;
						else if (newNote < 0)
							newNote = 0;

						//Set the data in the message
						tempMessage.setMessage(tempMessage.getCommand(), tempMessage.getChannel(), newNote, tempMessage.getData2());
					}
				}
			}
		}
		return true;//Agent has finished editing tracks
	}

	public void setPitchShiftAmount(int amnt){
		pitchShiftAmount = amnt;
	}

	public int getPitchShiftAmount(){
		return pitchShiftAmount;
	}


	public void addTrackBufferUpdateListener(TrackBufferUpdateListener tbul){
		trackBufferUpdateListenerArray.add(tbul);
	}

	public void removeTrackBufferUpdateListener(TrackBufferUpdateListener tbul){
		trackBufferUpdateListenerArray.remove(tbul);
	}

	@Override
	protected void reset() {
	}

}

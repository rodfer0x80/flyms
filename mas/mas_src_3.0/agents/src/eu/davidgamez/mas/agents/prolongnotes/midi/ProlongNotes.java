package eu.davidgamez.mas.agents.prolongnotes.midi;

//Java imports
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;

//MAS imports
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Agent;
import eu.davidgamez.mas.midi.Track;


public class ProlongNotes extends Agent {
	//Hash map key is the unique id of the midi track, each of which is associated with a hash map of off notes
	private HashMap<String, HashMap<Integer, Boolean> > noteOffHashMap = new HashMap<String, HashMap<Integer, Boolean> >();

	private boolean prolongNotes = false;

	public ProlongNotes() {
		super("Prolong Notes", "Prolong Notes", "ProlongNotes");
	}

	protected boolean updateTracks(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException{
		if(!prolongNotes && !noteOffHashMap.isEmpty()){
			addNoteOffMessages(bufferStart_ppq);
			return true;
		}
		else if(!prolongNotes){
			return true;
		}

		//Work through tracks and store any note off messages in the hash map and remove them from the track
		for(Track midiTrack : trackMap.values()){

			//Get the track buffer for the track
			TreeMap<Long, ArrayList<ShortMessage>> trackBufferMap = midiTrack.getMidiMessages();

			//Work through the tree map, each point is a separate tick
			for(Long bufferKey : trackBufferMap.keySet()){
				//Get the array list at the key position
				ArrayList<ShortMessage> tempArrayList = (ArrayList<ShortMessage>) trackBufferMap.get(bufferKey);

				//Work through the array list at the point looking for note off messages
				for(int i=0; i >=0 && i<tempArrayList.size(); ++i){
					ShortMessage tempShortMessage = tempArrayList.get(i);
					if(tempShortMessage.getCommand() == ShortMessage.NOTE_OFF){
						//Store note so that it can be turned off later
						noteOffHashMap.get(midiTrack.getID()).put(tempShortMessage.getData1(), new Boolean(true));

						//Remove note off from track buffer
						//if(!tempMidiTrack.removeShortMessage(bufferKey, tempShortMessage)){
						// throw new InvalidMidiDataException("Cannot delete short message");

						tempArrayList.remove(i);
						i--;
					}
				}
			}
		}
		return true;
	}


	public void enabledStatusChanged() {
	}

	//Synchronize the noteOffHashMap with the trackVector
	public void connectionStatusChanged(){
		//Keep a list of the current tracks to enable surplus tracks to be deleted
		HashMap<String, Boolean> currentTrackHashMap= new HashMap<String, Boolean>();

		//Add any tracks that were not there already
		for(Track midiTrack : trackMap.values()){
			currentTrackHashMap.put(midiTrack.getID(), new Boolean(true));
			if(!noteOffHashMap.containsKey(midiTrack.getID()))
				noteOffHashMap.put(midiTrack.getID(), new HashMap<Integer, Boolean>());
		}

		//Work through noteOffHashMap and remove any tracks that are not in the current track
		for(String trackKey : noteOffHashMap.keySet()){
			if(!currentTrackHashMap.containsKey(trackKey))
				noteOffHashMap.remove(trackKey);
		}
	}

	public void loadFromXML(String xmlStr) throws MASXmlException {
		super.loadFromXML(xmlStr);
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			setProlongNotes(Util.getBoolParameter("prolong_notes", xmlDoc));
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}


	public String getXML(String indent) {
		String tmpStr = "";
		tmpStr += indent + "<midi_agent>";
		tmpStr += super.getXML(indent + "\t");
		tmpStr += indent + "\t<prolong_notes>" + prolongNotes + "</prolong_notes>";
		tmpStr += indent + "</midi_agent>";
		return tmpStr;
	}

	public void setProlongNotes(boolean pn){
		prolongNotes = pn;
	}

	public boolean getProlongNotes(){
		return prolongNotes;
	}

	//Sends any note off messages that have been stored
	private void addNoteOffMessages(long bufferStart_ppq) throws InvalidMidiDataException {
		//Work through each of the tracks
		for(Track midiTrack : trackMap.values()){
			HashMap<Integer, Boolean> tempHashMap = noteOffHashMap.get(midiTrack.getID());

			//Add all of the notes in this hash map to the track
			if(tempHashMap != null){
				for(Integer tempInteger : tempHashMap.keySet()){
					ShortMessage off = new ShortMessage();
					off.setMessage(ShortMessage.NOTE_OFF, midiTrack.getChannel(), tempInteger.intValue(), 0);
					midiTrack.addMidiMessage(bufferStart_ppq, off);
				}
			}
		}
		//Remove all note offs from hash map
		for(String trackKey : noteOffHashMap.keySet()){
			noteOffHashMap.get(trackKey).clear();
		}
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}

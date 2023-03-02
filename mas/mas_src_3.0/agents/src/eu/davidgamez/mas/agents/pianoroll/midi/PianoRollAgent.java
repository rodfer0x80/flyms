package eu.davidgamez.mas.agents.pianoroll.midi;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.NoteEventListener;
import eu.davidgamez.mas.event.TransportListener;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Agent;
import eu.davidgamez.mas.midi.AgentMessage;
import eu.davidgamez.mas.midi.Buffer;
import eu.davidgamez.mas.midi.MIDIDeviceManager;
import eu.davidgamez.mas.midi.MIDIEvent;
import eu.davidgamez.mas.midi.MIDINote;
import eu.davidgamez.mas.midi.Track;
import eu.davidgamez.mas.midi.Utilities;


public class PianoRollAgent  extends Agent implements Constants, NoteEventListener, TransportListener {
	
	/** Velocity of the added notes */
	private static int NOTE_VELOCITY = 80;
	
	/** Default number of beats */
	public static int DEFAULT_NUMBER_OF_BEATS = 8;
	
	/** Map holding all of the notes that are to be played */
	TreeMap<Long, ArrayList<MIDINote>> noteMap = new TreeMap<Long, ArrayList<MIDINote>>(); 
	
	/** Map holding points at which there are bar markers */
	TreeMap<Long, Boolean> barMarkerMap = new TreeMap<Long, Boolean>();
	
	/** Controls whether its output restarts with each bar marking */
	boolean syncToBarMarker = false;
	
	/** Stores note off events that extend beyond the end of the bar */
	private ArrayList<MIDIEvent> midiEventArrayList = new ArrayList<MIDIEvent>();
	
	/** Point in the track at which the sequence has started playing.
	 	This jumps forward each time the sequence is complete. */
	private long sequenceStart_ppq = 0;
	
	/** Number of beats in the sequence */
	private int numberOfBeats = DEFAULT_NUMBER_OF_BEATS;

	/** Time stamp of the note that is currently being played from the sequence. */
	private long currentTimeStamp_ppq = -1;
	
	/** Time stamp of the previous note that was played from the sequence */
	private long previousTimeStamp_ppq = 0;
	
	/** Controls whether we are in recording mode or not */
	private boolean recordingMode = false;
	
	/** Map of received note on messages waiting for a corresponding note off message */
	private HashMap<Integer, MIDINote> noteOnRecordMap = new HashMap<Integer, MIDINote>();
	
	/** Stores start of play for record mode */
	private long playStart_ns = 0;
	
	/** Array of objects listening for actions - used when the state of this agent changes */
	private ArrayList<ChangeListener> changeListenerArray = new ArrayList<ChangeListener>();
	
	/** Offset to correct for sloppy MIDI timing */
	private int offset_ms = 500;
	
	/** Snap distance */
	private int snapDistance_ppq = PPQ_RESOLUTION/4;
	
	/** The position of the next bar marker */
	private long nextBarMarker_ppq = -1;
	
	private boolean firstTime = true;
	
	/** Restarts the sequence after the agent has been enabled. */
	private boolean restartSequence = true;
	
	/** Outputs debugging information */
	private boolean DEBUG = false;
	
	
	/** Constructor */
	public PianoRollAgent(){
		super("Piano Roll", "Piano Roll", "PianoRoll");
		EventRouter.addTransportListener(this);
	}
	

	/*--------------------------------------------------------------*/
	/*-------               PUBLIC METHODS                    ------*/
	/*--------------------------------------------------------------*/
	
	public void addChangeListener(ChangeListener listener){
		changeListenerArray.add(listener);
	}
	
	public void removeChangeListener(ChangeListener listener){
		changeListenerArray.remove(listener);
	}
	
	/** Adds a note to the agent */
	synchronized public void addNote(long timeStamp, MIDINote note){
		if(!noteMap.containsKey(timeStamp))
			noteMap.put(timeStamp, new ArrayList<MIDINote>());
		noteMap.get(timeStamp).add(note);
		
		//Record change of state
		Globals.setProjectSaved(false);
		updateChangeListeners();
	}
	
	
	/** Deletes a note from the agent and returns true if the note was found */
	synchronized public boolean deleteNote(long timeStamp, MIDINote note){
		if(!noteMap.containsKey(timeStamp))
			return false;
		Iterator<MIDINote> iter = noteMap.get(timeStamp).iterator();
		while(iter.hasNext()){
			MIDINote tmpNote = iter.next();
			if(tmpNote.equals(note)){
				iter.remove();
				return true;
			}	
		}
		return false;
	}
	
	
	/** Deletes all notes from the agent */
	synchronized public void deleteAllNotes(){
		noteMap.clear();
	}
	
	
	/** Returns the map containing all of the notes held by this agent */
	public TreeMap<Long, ArrayList<MIDINote>> getNotes(){
		return noteMap;
	}
	
	
	/** Returns the number of beats */
	public int getNumberOfBeats(){
		return numberOfBeats;
	}
	

	/** Returns the current offset for recording in milliseconds*/
	public int getOffset_ms(){
		return offset_ms;
	}
	
	
	/** Returns the current snap distance in PPQ */
	public int getSnapDistance_ppq(){
		return snapDistance_ppq;
	}
	
	
	/** Returns the synchronization setting */
	public boolean getSyncToBarMarker(){
		return syncToBarMarker;
	}
	
	//Save agent data
	public String getXML(String indent){
		String tmpDesc = indent + "<midi_agent>";
		tmpDesc += super.getXML(indent + "\t");
		
		//Output number of beats
		tmpDesc += indent + "\t<number_of_beats>" + numberOfBeats + "</number_of_beats>";
		
		//Output snap distance
		tmpDesc += indent + "\t<snap_distance>" + snapDistance_ppq + "</snap_distance>";
		
		//Output sync to bar markers
		tmpDesc += indent + "\t<sync_to_bar_markers>" + syncToBarMarker + "</sync_to_bar_markers>";
		
		//Output MIDI offset for recording
		tmpDesc += indent + "\t<offset>" + offset_ms + "</offset>";
		
		//Output notes
		tmpDesc += indent + "\t<note_map>";
		for(Long key : noteMap.keySet()){
			ArrayList<MIDINote> tmpArrList = noteMap.get(key);
			
			//Add note group
			if(!tmpArrList.isEmpty()){
				tmpDesc += indent + "\t\t<note_group>";
				tmpDesc += indent + "\t\t\t<time_stamp>" + key + "</time_stamp>"; 
				for(MIDINote note : tmpArrList){
					tmpDesc += indent + "\t\t\t<midi_note><pitch>" + note.pitch + "</pitch><length>"+note.length + "</length></midi_note>";
				}
				tmpDesc += indent + "\t\t</note_group>";
			}
		}
		tmpDesc += indent + "\t</note_map>";
		tmpDesc += indent + "</midi_agent>";
		return tmpDesc;
	}


	/** Returns true if we are in recording mode */
	public boolean isRecording(){
		return recordingMode;
	}
	
	
	/** Loads agent parameters from the supplied XML string */
	public void loadFromXML(String xmlStr) throws MASXmlException{
		super.loadFromXML(xmlStr);
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			numberOfBeats = Util.getIntParameter("number_of_beats", xmlDoc);
			snapDistance_ppq = Util.getIntParameter("snap_distance", xmlDoc);
			syncToBarMarker= Util.getBoolParameter("sync_to_bar_markers", xmlDoc);
			offset_ms= Util.getIntParameter("offset", xmlDoc);

			//Work through all of the note groups
			NodeList nodeList = xmlDoc.getElementsByTagName("note_group");
			for(int i=0; i<nodeList.getLength(); ++i){
				Node noteGroup = nodeList.item(i);
				NodeList arraysList = noteGroup.getChildNodes();

				//Extract the time stamp and list of notes for each note group
				long timeStamp = -1;
				int pitch = -1, length = -1;
				for(int j=0; j<arraysList.getLength(); ++j){
					Node noteArray = arraysList.item(j);
					if(noteArray.getNodeName().equals("time_stamp")){
						timeStamp = Long.parseLong(noteArray.getFirstChild().getNodeValue());
					}
					else if(noteArray.getNodeName().equals("midi_note")){
						NodeList midiNotesList = noteArray.getChildNodes();
						for(int k=0; k<midiNotesList.getLength(); ++k){
							Node midiNote = midiNotesList.item(k);
							if(midiNote.getNodeName().equals("pitch")){
								pitch = Integer.parseInt(midiNote.getFirstChild().getNodeValue());
							}
							else if(midiNote.getNodeName().equals("length")){
								length = Integer.parseInt(midiNote.getFirstChild().getNodeValue());
							}
						}
					}
					//Have loaded a complete note
					if(pitch > -1 && length > -1 && timeStamp > -1){
						addNote(timeStamp, new MIDINote(pitch, length));
						//Reset pitch and length
						pitch = -1;
						length = -1;
					}
				}
			}
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			MsgHandler.error(ex);
			return;
		}
	}

	
	@Override
	public void noteEventOcccurred(ShortMessage shrtMsg) {
		if(!recordingMode){
			MsgHandler.error("Note event received when not in recording mode.");
			return;
		}
		
		//Only record notes if system is playing
		if(!Globals.isPlaying())
			return;
		
		//Store note on information until we get the matching note off information. 
		if(shrtMsg.getCommand() == ShortMessage.NOTE_ON){
			//Store current time in MIDI note to calculate length and start time later
			MIDINote tmpNote = new MIDINote(shrtMsg.getData1(), 0);
			tmpNote.setData1(getPianoRollTime_ppq());//Start of note
			tmpNote.setData2(System.nanoTime());//Used for length of note
			noteOnRecordMap.put(shrtMsg.getData1(),  tmpNote);
		}
		else if(shrtMsg.getCommand() == ShortMessage.NOTE_OFF){
			//Only process note off if we have already got a note on message
			if(noteOnRecordMap.containsKey(shrtMsg.getData1())){			
				MIDINote tmpNote = noteOnRecordMap.get(shrtMsg.getData1());
				
				//Calculate length of note and make sure it does not exceed piano roll length
				long noteLength_ppq = Math.round( (System.nanoTime() - tmpNote.getData2()) /Globals.getNanoSecPerTick() );
				if( (tmpNote.getData1() + noteLength_ppq) > (numberOfBeats * PPQ_RESOLUTION) )
					noteLength_ppq = (numberOfBeats * PPQ_RESOLUTION) - tmpNote.getData1();
				tmpNote.setLength_ppq(noteLength_ppq);
				
				//Add the note and remove from record map
				addNote(tmpNote.getData1(), tmpNote);
				noteOnRecordMap.remove(tmpNote.getPitch());
			}
		}
	}
	
	
	/** Sets the number of beats **/
	public void setNumberOfBeats(int numBeats){
		this.numberOfBeats = numBeats;
		Globals.setProjectSaved(false);
	}
	
	
	/** Sets the recording offset in ms */
	public void setOffset(int offset_ms){
		this.offset_ms = offset_ms;
		Globals.setProjectSaved(false);
	}
	
	
	/** Sets the snap in ticks */
	public void setSnapDistance_ppq(int snapDistance_ppq){
		this.snapDistance_ppq = snapDistance_ppq;
		Globals.setProjectSaved(false);
	}
		
	
	/** Switches the recording mode on and off */
	public void setRecording(boolean newRecordingMode){
		//Switching recording on
		if(newRecordingMode)
			MIDIDeviceManager.getMidiInputHandler().addNoteEventListener(this);
		
		//Switching recording off
		else
			MIDIDeviceManager.getMidiInputHandler().removeNoteEventListener(this);

		//Store new recording mode
		this.recordingMode = newRecordingMode;
	}
	
	
	/** Sets whether agent restarts its output with each bar marking or not */
	public void setSyncToBarMarker(boolean sync){
		syncToBarMarker = sync;
		Globals.setProjectSaved(false);
	}
	

	/*--------------------------------------------------------------*/
	/*-------                PROTECTED METHODS                ------*/
	/*--------------------------------------------------------------*/
	
	@Override
	protected void enabledStatusChanged(){
		//Restart sequence if agent has been enabled
		if(this.isEnabled()){
			restartSequence = true;
		}
	}
	
	
	@Override
	protected void reset() {	
		sequenceStart_ppq = 0;
		currentTimeStamp_ppq = -1;
		previousTimeStamp_ppq = 0;
		noteOnRecordMap.clear();
		nextBarMarker_ppq = -1;
		firstTime = true;
		restartSequence = true;
	}

	
	@Override
	synchronized protected boolean updateTracks(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException {		
		//Output any note off messages from previous update
		outputStoredNotes(bufferStart_ppq, bufferEnd_ppq);
		
		//Do nothing if agent is empty
		if(noteMap.isEmpty()){
			return true;
		}
		
		//Load up the set of bar markings if we are synchronizing to the bar markers
		if(syncToBarMarker){
			storeBarMarkings(bufferStart_ppq, bufferEnd_ppq);
			checkBarMarker();
		}
		
		//Restart sequence if we are at the beginning or if flag has been set
		if(bufferStart_ppq == 0 || restartSequence){
			if(restartSequence){
				currentTimeStamp_ppq = -1;
				sequenceStart_ppq = bufferStart_ppq;
				restartSequence = false;
			}
			advanceTimeStamp();
		}
		
		//Get the next time stamp to be played
		while( (currentTimeStamp_ppq + sequenceStart_ppq) >= bufferStart_ppq && (currentTimeStamp_ppq + sequenceStart_ppq) < bufferEnd_ppq ){
			if(DEBUG) System.out.println("Updating tracks bufferStart_ppq=" + bufferStart_ppq + " currentTimeStamp_ppq=" + currentTimeStamp_ppq + " sequenceStart_ppq=" + sequenceStart_ppq);
			//Output the notes for the current time stamp
			ArrayList<MIDINote> noteArr = noteMap.get(currentTimeStamp_ppq);
			for(MIDINote note : noteArr){
				addNote(note, currentTimeStamp_ppq + sequenceStart_ppq, bufferEnd_ppq);
			}
			
			//Advance the time stamp in a circular fashion
			advanceTimeStamp();
		}
		
		//Have finished outputting notes.
		return true;
	}
	

	/*--------------------------------------------------------------*/
	/*-------                PRIVATE METHODS                  ------*/
	/*--------------------------------------------------------------*/

	/** Generates a random note and adds it to all tracks. 
   		Note off messages that fall outside the buffer are stored for a later point in time.	 */
	private void addNote(MIDINote note, long noteStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException{
		//Add note on message. These will always be inside the buffer
		ShortMessage on = new ShortMessage();
		for(Track midiTrack : trackMap.values()){
			on.setMessage(ShortMessage.NOTE_ON, midiTrack.getChannel(), note.pitch, NOTE_VELOCITY);
			midiTrack.addMidiMessage(noteStart_ppq, on);
		}

		//Create note off message. 
		ShortMessage off = new ShortMessage();

		//Save it for the next update if it is outside the buffer
		if ( (noteStart_ppq + note.length) >= bufferEnd_ppq) { //Off event will fall outside buffer
			off.setMessage(ShortMessage.NOTE_OFF, 0, note.pitch, NOTE_VELOCITY);
			midiEventArrayList.add(new MIDIEvent(off, noteStart_ppq + note.length)); //Store for later update cycle
		}
		//Add it to the current buffer of all the tracks
		else { 
			for(Track midiTrack : trackMap.values()){
				off.setMessage(ShortMessage.NOTE_OFF, midiTrack.getChannel(), note.pitch, NOTE_VELOCITY);
				midiTrack.addMidiMessage(noteStart_ppq + note.length, off);
			}
		}
	}
	
	
	/** Advances all of the time tracking variables */
	private void advanceTimeStamp(){
		//Move the current time stamp forward to point to the next note in the sequence - circular movement
		advanceNextNoteTimeStamp();
				
		checkBarMarker();
	}

	
	/** Checks to see if the current time stamp has passed the next bar marking? */
	private void checkBarMarker(){
		if(syncToBarMarker){
			if(DEBUG) System.out.println("EVALUATING. track position=" + (currentTimeStamp_ppq + sequenceStart_ppq) + " nextBarMarker=" + nextBarMarker_ppq);
			if(nextBarMarker_ppq >= 0 && (currentTimeStamp_ppq + sequenceStart_ppq) >= nextBarMarker_ppq){
				if(DEBUG) System.out.println("RESTART currentTimeStamp_ppq=" + currentTimeStamp_ppq + " sequenceStart=" + sequenceStart_ppq + " nextBarMarker=" + nextBarMarker_ppq);
				
				//Sequence is restarting at the next bar marker 
				sequenceStart_ppq = nextBarMarker_ppq;
				
				//Point the current time stamp to the first note in the bar
				currentTimeStamp_ppq = noteMap.firstKey().longValue();
				
				//Move the bar marker forward
				advanceBarMarker();
			}
		}
	}
	
	
	/** Moves the current time stamp forward to point to the next note in the sequence.
	 	This is circular. */
	private void advanceNextNoteTimeStamp(){
		previousTimeStamp_ppq = currentTimeStamp_ppq;
		
		//Get the next key, which may be higher or back to the beginning of the sequence
		Long tmpKey = noteMap.higherKey(currentTimeStamp_ppq);
		if(tmpKey == null){
			tmpKey = noteMap.firstKey();
		}
		currentTimeStamp_ppq = tmpKey.longValue();
	
		//Increase start of sequence if it has returned to the beginning or if there is only one note
		if(currentTimeStamp_ppq <= previousTimeStamp_ppq){
			sequenceStart_ppq += numberOfBeats * PPQ_RESOLUTION;
			if(DEBUG) System.out.println("Increasing sequence start: " + sequenceStart_ppq);
		}
	}
	
	
	/** Moves on to the next stored bar marking. */
	private void advanceBarMarker(){
		Long tmpKey = barMarkerMap.higherKey(nextBarMarker_ppq);
		if(tmpKey != null)
			nextBarMarker_ppq = tmpKey.longValue();
		else
			nextBarMarker_ppq = -1;
	}
	
	
	/** Checks for bar markings and resets start position if one is found **/
	private void storeBarMarkings(long bufferStart_ppq, long bufferEnd_ppq){
		//Reset bar markers and next bar marking
		barMarkerMap.clear();
		nextBarMarker_ppq = -1;
		
		if(DEBUG) System.out.println("---- Bar markings. BStart=" + bufferStart_ppq + " BEnd="+ bufferEnd_ppq + " ----");
		
		//Work through all the tracks. Bar markers are extracted from all the tracks that the agent is connected to
		boolean firstTime = true;
		for(Track midiTrack : trackMap.values()){
			for (long beat_ppq = Utilities.getFirstBeatInBuffer(bufferStart_ppq); beat_ppq < bufferEnd_ppq; beat_ppq += PPQ_RESOLUTION) {
				//Extract the message buffer for the track
				ArrayList<AgentMessage> agMsgArrayList = midiTrack.getAgentMessages(beat_ppq);
				
				//Look through message map for a bar start
				for (AgentMessage agMsg : agMsgArrayList) {
					if (agMsg.getType() == AgentMessage.START_BAR) { //Have found a start bar message at this point
						//Set next bar marker to the first bar marker that we find
						if(firstTime){
							nextBarMarker_ppq = beat_ppq;
							firstTime = false;
						}
						if(DEBUG) System.out.println("Bar Marker: " + beat_ppq);
						barMarkerMap.put(beat_ppq, true);
					}
				}
			}
		}
		if(DEBUG) System.out.println("-----------------------------------------------");
	}
	
	
	/** Returns the time within the piano roll when recording notes. */
	private long getPianoRollTime_ppq(){
		long ticksElapsed = (int)Math.round( (System.nanoTime() - playStart_ns - offset_ms*1000000) / Globals.getNanoSecPerTick());
		long tmpTime = ticksElapsed % (numberOfBeats * PPQ_RESOLUTION);
		return getSnapTimeStamp((int)tmpTime);
	}
	
	
	/** Returns the time stamp rounded to the nearest snap distance */
	private long getSnapTimeStamp(int timeStamp){	
		if(snapDistance_ppq <= 0)
			return timeStamp;
		
		int remainder = timeStamp % snapDistance_ppq;
		if(remainder == 0)
			return timeStamp;
		if( ((double)remainder / (double)snapDistance_ppq) > 0.5)
			return snapDistance_ppq * (timeStamp / snapDistance_ppq) + snapDistance_ppq;
		else
			return snapDistance_ppq * (timeStamp / snapDistance_ppq);
	}
	
	
	/** Add any MIDI events that were generated in the previous cycle, but could not be added
    		because they fell outside the buffer */
	private void outputStoredNotes(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException{
		Iterator<MIDIEvent> iter = midiEventArrayList.iterator();
		while (iter.hasNext()) {
			MIDIEvent tempMidiEvent = iter.next();
			
			//Perhaps track has been rewound, delete and ignore message if it is before start of buffer
			if (tempMidiEvent.getTimeStamp() < bufferStart_ppq){
				iter.remove();
			}
			
			//Add to buffer of all connected tracks
			else if (tempMidiEvent.getTimeStamp() < bufferEnd_ppq) { 
				for(Track midiTrack : trackMap.values()){
					//Set channel in the message
					ShortMessage shortMsg = tempMidiEvent.getMessage();
					shortMsg.setMessage(shortMsg.getCommand(), midiTrack.getChannel(), shortMsg.getData1(), shortMsg.getData2());
					
					//Add the message and remove it from the midi event array list
					midiTrack.addMidiMessage(tempMidiEvent.getTimeStamp(), shortMsg);
				}
				
				//Remove off message after it has been added to all tracks.
				iter.remove();
			}
		}
	}
	
	
	/** Outputs all stored notes */
	private void outputStoredNotes(){
		try{
			Iterator<MIDIEvent> iter = midiEventArrayList.iterator();
			while (iter.hasNext()) {
				MIDIEvent tempMidiEvent = iter.next();
				
				//Add to buffer of all connected tracks
				for(Track midiTrack : trackMap.values()){
					//Set channel in the message
					ShortMessage shortMsg = tempMidiEvent.getMessage();
					shortMsg.setMessage(shortMsg.getCommand(), midiTrack.getChannel(), shortMsg.getData1(), shortMsg.getData2());
					
					//Add the message and remove it from the midi event array list
					midiTrack.addMidiMessage(tempMidiEvent.getTimeStamp(), shortMsg);
				}
				
				//Remove off message after it has been added to all tracks.
				iter.remove();
			}
		}
		catch(InvalidMidiDataException ex){
			MsgHandler.critical("PianoRollAgentException: " + ex.getMessage());
			ex.printStackTrace();
		}
	}


	@Override
	public void killNotesActionPerformed() {
	}


	@Override
	public void playActionPerformed() {
		//Record time at which play started - used when recording
		playStart_ns = System.nanoTime();
	}


	@Override
	public void stopActionPerformed() {
	}

	private void updateChangeListeners(){
		for(ChangeListener changeListener : changeListenerArray)
			changeListener.stateChanged(new ChangeEvent(this));
	}
	
}

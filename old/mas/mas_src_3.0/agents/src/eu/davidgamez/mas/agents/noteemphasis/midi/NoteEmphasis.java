package eu.davidgamez.mas.agents.noteemphasis.midi;

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
import eu.davidgamez.mas.midi.Track;
import eu.davidgamez.mas.midi.Utilities;


public class NoteEmphasis extends Agent implements Constants{

	//Maximum and minimum possible MIDI velocities
	static final int maxVelocity = 127;
	static final int minVelocity = 0;

	//Controls whether the velocity changes are synched to bar markers or free running
	private boolean synchToBar = true;

	//If not synched to bar, need to define after which point will start emphasis sequence again
	private int sequenceLength_ppq;

	//Counter keeping track of the current emphasis point
	private int emphasisCounter = 0;

	//Offset of the emphasis point for non-synch mode
	private long insertionPoint_ppq = -1;

	private boolean addEmphasis = false;

	//List of the emphasis points. This should be the same length as the list of velocity percentage changes
	private int [] noteEmphasisArray = new int [] {0, 2 * PPQ_RESOLUTION};//Default is to emphasise every first and third beat in the bar
	private int [] velocityEmphasisArray = new int [] {120, 110};//by 120 and 110 %


	public NoteEmphasis() {
		super("Note Emphasis", "Note Emphasis", "NoteEmphasis");
	}


	/*--------------------------------------------------------------*/
	/*-------               PUBLIC METHODS                    ------*/
	/*--------------------------------------------------------------*/
	
	@Override
	public void connectionStatusChanged(){
		//If there are no connections left, need to re-initialise the agent so it can be restarted live
		if(trackMap.isEmpty()){
			reset();
		}
	}
	
	
	/** Returns an XML string describing the agent */
	public String getXML(String indent){
		String tmpStr = indent + "<midi_agent>";
		tmpStr += super.getXML(indent + "\t");

		//Output note emphasis array
		tmpStr += indent + "\t<note_emphasis_array>";
		for(int i=0; i < noteEmphasisArray.length - 1; ++i)
			tmpStr += noteEmphasisArray[i] + ",";
		tmpStr += noteEmphasisArray[noteEmphasisArray.length - 1] +  "</note_emphasis_array>";

		//Output velocity emphasis array
		tmpStr += indent + "\t<velocity_emphasis_array>";
		for (int i = 0; i < velocityEmphasisArray.length - 1; ++i)
			tmpStr += velocityEmphasisArray[i] + ",";
		tmpStr += velocityEmphasisArray[velocityEmphasisArray.length - 1] + "</velocity_emphasis_array>";

		//Output other parameters and close
		tmpStr += indent + "\t<synch_to_bar>" + synchToBar + "</synch_to_bar>";
		tmpStr += indent + "</midi_agent>";
		return tmpStr;
	}


	/** Loads the agent's state from the supplied XML string */
	public void loadFromXML(String xmlStr) throws MASXmlException {
		super.loadFromXML(xmlStr);
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			synchToBar= Util.getBoolParameter("synch_to_bar", xmlDoc);
			noteEmphasisArray = Util.getIntArrayParameter("note_emphasis_array", xmlDoc);
			velocityEmphasisArray = Util.getIntArrayParameter("velocity_emphasis_array", xmlDoc);
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
			return;
		}

		//Calculate sequence length
		calculateSequenceLength();

		//Reset counters and set insertionPoint_ppq to -1 to indicate that it must set its value to the first beat in buffer.
		reset();
	}


	/** Sets the emphasis created by the agent */
	public void setNoteEmphasis(TreeMap<Integer, Integer> noteList){
		//Copy into int arrays to save converting from Integers all the time at runtime
		noteEmphasisArray = new int[noteList.size()];
		velocityEmphasisArray = new int[noteList.size()];
		int i = 0;
		for (Integer tempInt : noteList.keySet()) {
			noteEmphasisArray[i] = tempInt.intValue();
			velocityEmphasisArray[i] = noteList.get(tempInt).intValue();
			++i;
		}
		//Calculate sequence length
		calculateSequenceLength();

		//Reset counters and set insertionPoint_ppq to -1 to indicate that it must set its value to the first beat in buffer.
		reset();
	}

	public int [] getNoteEmphasisArray(){
		return noteEmphasisArray;
	}
	public int [] getVelocityEmphasisArray(){
		return velocityEmphasisArray;
	}

	public void setSynchToBar(boolean val){
		synchToBar = val;
		reset();
	}

	public boolean getSynchToBar(){
		return synchToBar;
	}

	/*--------------------------------------------------------------*/
	/*-------               PROTECTED METHODS                 ------*/
	/*--------------------------------------------------------------*/

	@Override
	protected void enabledStatusChanged(){
		if(!this.isEnabled())
			reset();
	}
	

	//Main method called to request agent to add notes or messages to the buffer
	protected boolean updateTracks(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException{
		//Go through each track
		for (Track midiTrack : trackMap.values()) {
			if (synchToBar) { //Restart velocity emphasis on each bar marker

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
							emphasisCounter = 0; //Reset emphasis counter
							insertionPoint_ppq = beat_ppq; //All emphasis points will be offset from the bar marker
							addEmphasis = true;
						}
					}

					//========================================================================================
					//Add emphasis up to but not including the next beat in the buffer or up to the end
					//of the buffer if it does not contain another whole beat
					//========================================================================================
					if (addEmphasis) {
						long emphasisPoint_ppq = noteEmphasisArray[emphasisCounter] + insertionPoint_ppq;

						//Add emphasis points to the current beat, where there may be a bar marker that resets everything
						while (emphasisPoint_ppq >= beat_ppq && emphasisPoint_ppq < beat_ppq + PPQ_RESOLUTION && emphasisPoint_ppq < bufferEnd_ppq && addEmphasis) {
							emphasiseNotes(emphasisPoint_ppq, velocityEmphasisArray[emphasisCounter], midiTrack);

							//Increase array counter
							++emphasisCounter;
							if (emphasisCounter == noteEmphasisArray.length) {
								emphasisCounter = 0;
								addEmphasis = false;
							}

							//Find next emphasis point
							emphasisPoint_ppq = noteEmphasisArray[emphasisCounter] + insertionPoint_ppq;
						}
					}
				}
			}
			else{//Run through emphasis values in a loop
				//Initialise insertion point if necessary
				if(insertionPoint_ppq == -1)
					insertionPoint_ppq = Utilities.getFirstBeatInBuffer(bufferStart_ppq);

				long emphasisPoint_ppq = noteEmphasisArray[emphasisCounter] + insertionPoint_ppq;
				while(emphasisPoint_ppq >= bufferStart_ppq && emphasisPoint_ppq < bufferEnd_ppq){
					emphasiseNotes(emphasisPoint_ppq, velocityEmphasisArray[emphasisCounter], midiTrack);

					//Increase array counter in a circular way
					++emphasisCounter;
					emphasisCounter %= noteEmphasisArray.length;

					//Increase insertonPoint if we have done a complete cycle of the arrays
					if (emphasisCounter == 0)
						insertionPoint_ppq += sequenceLength_ppq;

					//Find next emphasis point
					emphasisPoint_ppq = noteEmphasisArray[emphasisCounter] + insertionPoint_ppq;
				}
			}
		}
		return true;
	}


	@Override
	protected void reset(){
		emphasisCounter = 0;
		insertionPoint_ppq = -1;
		addEmphasis = false;
	}
	

	/*--------------------------------------------------------------*/
	/*-------                 PRIVATE METHODS                 ------*/
	/*--------------------------------------------------------------*/
	
	/** Adds the emphasis to the notes */
	private void emphasiseNotes(long beat_ppq, int velocityPercentageChange, Track midiTrack) throws InvalidMidiDataException{
		//Work through the short messages and increase the velocity of any notes
		for (ShortMessage tempShortMsg : midiTrack.getMidiMessages(beat_ppq)) {
			if (tempShortMsg.getCommand() == ShortMessage.NOTE_ON) {

				//Work out new velocity
				int newVelocity =  tempShortMsg.getData2() * velocityPercentageChange / 100;

				//Check velocity is in range
				if (newVelocity > maxVelocity)
					newVelocity = maxVelocity;
				else if (newVelocity < minVelocity)
					newVelocity = minVelocity;

				//Set the new velocity in the message
				tempShortMsg.setMessage(ShortMessage.NOTE_ON, midiTrack.getChannel(), tempShortMsg.getData1(), newVelocity);
			}
		}
	}
	
	
	/** Work out the sequence length. It is the last whole note + 1 beat */
	//#FIXME# CHECK THIS IS OK WITH FRACTIONS
	private void calculateSequenceLength(){
		if(noteEmphasisArray.length > 0)
			sequenceLength_ppq = ((noteEmphasisArray[noteEmphasisArray.length - 1] / PPQ_RESOLUTION) * PPQ_RESOLUTION) +  PPQ_RESOLUTION;
		else{
			System.out.println("Note emphasis array length is zero!");
			sequenceLength_ppq = 0;
		}
	}



}

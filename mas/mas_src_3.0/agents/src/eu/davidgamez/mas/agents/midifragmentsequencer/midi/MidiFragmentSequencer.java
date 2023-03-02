package eu.davidgamez.mas.agents.midifragmentsequencer.midi;

//Java imports
import javax.sound.midi.*;

//Java imports
import java.util.Iterator;
import java.util.Vector;
import java.io.*;

//MAS imports
import eu.davidgamez.mas.exception.NoNoteOnFoundException;
import eu.davidgamez.mas.midi.Agent;
import eu.davidgamez.mas.midi.AgentMessage;


public class MidiFragmentSequencer extends Agent{

	private Vector midiFragments = new Vector();
	private MidiEvent [] midiSequence;
	private int sequenceCounter = 0;
	private long sequenceLength = -1;
	private long currentTrackPosition = 0;
	private long lastAddedEventTick = 0;
	private long lastNoteOnTickPoint = -1;

	public MidiFragmentSequencer() {
		super("MIDI Fragment Sequencer", "MIDI Fragment Sequencer", "MIDIFragmentSequencer");
	}

	//Save agent data
	public String getXML(String indent) {
		String tmpStr = "";
		tmpStr += indent + "<midi_agent>";
		tmpStr += super.getXML(indent + "\t");
		/*out.write("\n\t<note_pitch>" + String.valueOf(notePitch) + "<\\note_pitch>");
    out.write("\n\t<note_velocity>" + String.valueOf(noteVelocity) + "<\\note_velocity>");
    out.write("\n\t<note_frequency>" + String.valueOf(noteFrequency_ppq) + "<\\note_frequency>");
    out.write("\n\t<note_length>" + String.valueOf(noteLength_ppq) + "<\\note_length>");
		 */
		tmpStr += indent + "</midi_agent>";
		return tmpStr;
	}


	protected boolean updateTracks(long bufferStart, long bufferLength){
		//Need to set the track position to the start of the buffer.
		//Not very efficient, but with a sliding window things could get tricky without knowing the overlap.
		//So for the moment do it the crude way.
		/*    setTrackPosition(bufferStart);

    if(midiSequence != null && midiSequence.length > 0){
      //Goes through each track
      Iterator trackIterator = trackVector.iterator();
      while (trackIterator.hasNext()) {
        MidiTrack myTrack = (MidiTrack) trackIterator.next();
        Vector trackBuffer = myTrack.getTrackBuffer();
        try {
          int oldSequenceCounter = sequenceCounter - 1;
          MidiEvent previousMidiEvent = null;//Should only be used if one cycle of midi notes has been laid down
          while(oldSequenceCounter != sequenceCounter){//Exits when no more changes are made to track
            System.out.println("sequence counter is: " + sequenceCounter);
            oldSequenceCounter = sequenceCounter;
            MidiEvent nextMidiEvent = midiSequence[sequenceCounter];//This is the event that is to be added to the track
            if(sequenceCounter > 0){//Part way through sequence so previous midi event is earlier in sequence
              previousMidiEvent = midiSequence[sequenceCounter - 1];
              long nextMidiTickPoint = nextMidiEvent.getTick() - previousMidiEvent.getTick() + lastAddedEventTick;
              if (bufferStart <= nextMidiTickPoint && nextMidiTickPoint < bufferStart + bufferLength) {
                if ( (nextMidiEvent.getMessage() instanceof ShortMessage) && (previousMidiEvent.getMessage() instanceof ShortMessage)) {
                  ShortMessage message = (ShortMessage) nextMidiEvent.getMessage();
                  message.setMessage(message.getCommand(), myTrack.getChannel(),
                                     message.getData1(),
                                     message.getData2());
                  trackBuffer.add(new MidiEvent(message, nextMidiTickPoint));
                  System.out.println("Added event successfully=====================1 at tick point: " + nextMidiTickPoint);

                }
                else{ //#FIXME# Not sure if the track being ambiguous will be a problem with system exclusive
                  trackBuffer.add(new MidiEvent(nextMidiEvent.getMessage(), nextMidiTickPoint));
                  System.out.println("Added event successfully=====================2 at tick point: "+ nextMidiTickPoint);
                }
                lastAddedEventTick = nextMidiTickPoint;
                if(containsNoteOnMessage(nextMidiEvent)){//Need to record the last note on point so that the note on messages from the next fragment can be synchronized up to it.
                  lastNoteOnTickPoint = lastAddedEventTick;
                  System.out.println("sequenceCounte not zero: lastNOte onTIckpoint: __________________________ " + lastNoteOnTickPoint);
                }
                sequenceCounter = (sequenceCounter + 1) % midiSequence.length; //Will equal zero when it reaches the end of the sequence
              }
            }
            else { //Sequence counter = 0: Start of sequence so previous midi event is at the end of the sequence
              long nextMidiTickPoint;
              if (bufferStart == 0)
                nextMidiTickPoint = 0;
              else{
                previousMidiEvent = midiSequence[midiSequence.length - 1];
                nextMidiTickPoint = nextMidiEvent.getTick() + (lastNoteOnTickPoint / 960) * 960 + 960;
                System.out.println("nextMidiTickPoint: " + nextMidiTickPoint + " last note on tick point " + lastNoteOnTickPoint + " buffer start " + bufferStart);
              }
              if (bufferStart <= nextMidiTickPoint && nextMidiTickPoint < bufferStart + bufferLength) {
                if (nextMidiEvent.getMessage() instanceof ShortMessage) {
                  ShortMessage message = (ShortMessage) nextMidiEvent.getMessage();
                  message.setMessage(message.getCommand(), myTrack.getChannel(),
                                     message.getData1(),
                                     message.getData2());
                  trackBuffer.add(new MidiEvent(message, nextMidiTickPoint));
                  System.out.println("Added event successfully=====================3 at tick point: " + nextMidiTickPoint);
                }
                else {
                  trackBuffer.add(new MidiEvent(nextMidiEvent.getMessage(), nextMidiTickPoint));
                  System.out.println("Added event successfully=====================4 at tick point: " + nextMidiTickPoint);
                }
                lastAddedEventTick = nextMidiTickPoint;
                //Need to sort out so that at the end of sequence tick starts at whole number
                sequenceCounter = (sequenceCounter + 1) % midiSequence.length;
              }
            }
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }*/
		return true;//Agent has finished editing tracks
	}

	//Sets the sequence counter so that agent will start to play at the appropriate place.
	//This should be called whenever there is a rewind or position adjustment in track.
	private void setTrackPosition(long tick){
		long firstSequenceTickInBuffer = tick % sequenceLength;
		System.out.println("first sequence tick in buffer " + firstSequenceTickInBuffer + " tick  " + tick + " Sequence length "  + sequenceLength);
		for (int i = 0; i < midiSequence.length; i++) {
			if (midiSequence[i].getTick() >= firstSequenceTickInBuffer) {
				sequenceCounter = i;
				//Now need to find lastNoteOnTickPoint as well when we are at the beginning of the sequence
				if (sequenceCounter == 0) {
					for (int j = midiSequence.length - 1; j >= 0; j--) {
						if (containsNoteOnMessage(midiSequence[j])) { //Need to record the last note on point so that the note on messages from the next fragment can be synchronized up to it.
							lastNoteOnTickPoint = tick - (sequenceLength - midiSequence[j].getTick());
							System.out.println("Set track position lastNoteTickPoint " + lastNoteOnTickPoint);
							return;
						}
					}
				}
				return;
			}
		}
	}

	//Connects all the single fragments together into a single sequence
	public void connectFragments() throws NoNoteOnFoundException{
		Vector tempMidiEventVector = new Vector();

		//First add message indicating that it is the start of the sequence
		tempMidiEventVector.add(new AgentMessage(getAgentDescription(), getName(), 1, "Sequence start"));

		long tickOffset = 0, lastNoteOn_CF = -1;
		for(int i=0; i<midiFragments.size(); i++){//Maybe replace with iterator for speed?
			MidiEvent[] midiEventArray = (MidiEvent[]) midiFragments.get(i);

			//printMidiEvents(midiEventArray);
			for (int j = 0; j < midiEventArray.length; j++) {
				MidiEvent midiEvent = midiEventArray[j];
				if(midiEvent.getMessage() instanceof ShortMessage){//At present don't see any point in keeping these messages
					long nextTickPoint = midiEvent.getTick() + tickOffset;
					if (containsNoteOnMessage(midiEvent)) //Need to record the last note on point so that the note on messages from the next fragment can be synchronized up to it.
						lastNoteOn_CF = nextTickPoint;
					midiEvent.setTick(nextTickPoint);
					if (j == midiEventArray.length - 1) { //Last event in this fragment.
						//Need to round off to the nearest beat
						if (nextTickPoint > 0) { //Sometimes there is a stray track full of strange midi messages that need to be ignored
							System.out.print("next tick point: " + nextTickPoint + " old tick offset " + tickOffset);
							tickOffset = (lastNoteOn_CF / 960) * 960 + 960;
							System.out.println(" new tick offset: " + tickOffset);
						}
					}
					tempMidiEventVector.add(midiEvent);
				}
			}
		}
		//Add message indicating that it is the end of the sequence
		sequenceLength = (lastNoteOn_CF / 960) * 960 + 960;
		tempMidiEventVector.add(new AgentMessage(getAgentDescription(), getName(), 1, "Sequence end"));

		midiSequence = (MidiEvent[])tempMidiEventVector.toArray(new MidiEvent[1]);
		printMidiEvents(midiSequence);
		if(lastNoteOn_CF == -1)
			throw new NoNoteOnFoundException("No notes on in sequence");
	}

	public Vector getMidiFragmentsVector(){
		return midiFragments;
	}

	private boolean containsNoteOnMessage(MidiEvent event){
		if(event.getMessage() instanceof ShortMessage){
			if(((ShortMessage)event.getMessage()).getCommand() == ShortMessage.NOTE_ON)
				return true;
			else
				return false;
		}
		else return false;
	}

	public void printMidiEvents(MidiEvent[] midiEventArray) {
		System.out.println("========================================MIDI EVENTS=======================================");
		System.out.println("Midi event array length = " + midiEventArray.length);
		for (int j = 0; j < midiEventArray.length; j++) {
			if (midiEventArray[j].getMessage() instanceof ShortMessage) {
				System.out.print("Tick: " + midiEventArray[j].getTick());
				ShortMessage sm = (ShortMessage) midiEventArray[j].getMessage();
				switch (sm.getCommand()) {
				case (ShortMessage.NOTE_ON):
					System.out.print("; Note On");
				break;
				case (ShortMessage.NOTE_OFF):
					System.out.print("; Note Off");
				break;
				default:
					System.out.print("; Unrecognised");
				}
				System.out.println("; Channel: " + sm.getChannel() + "; Note: " + sm.getData1() + "; Velocity: " + sm.getData2());
			}
			// else if (midiEventArray[j].getMessage() instanceof AgentMessage) {
			//  System.out.print("Tick: " + midiEventArray[j].getTick());
			//  MidiAgentMessage mAm = (MidiAgentMessage) midiEventArray[j].getMessage();
			// System.out.println("; Agent name: \"" + mAm.getAgentName() + "\"; Agent description: \"" + mAm.getAgentDescription() + "\"; Message: \"" + mAm.getAgentMessage() + "\"");
			//   }
			else{
				System.out.print("Tick: " + midiEventArray[j].getTick());
				System.out.println("; Not a recognised message! ");
			}
		}
		System.out.println();
	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}

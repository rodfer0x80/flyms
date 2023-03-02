package eu.davidgamez.mas.midi;

//Java imports
import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Vector;

//MAS imports
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.TempoEvent;
import eu.davidgamez.mas.event.TempoListener;
import eu.davidgamez.mas.event.TransportListener;
import eu.davidgamez.mas.event.NoteEventListener;
import eu.davidgamez.mas.*;


/**----------------------------  MIDIInputHandler --------------------------------
    Receives MIDI events and passes them on to classes that have registered to
      listen to them. This class handles both controllers and synchronization signals.
   -------------------------------------------------------------------------------*/
public class MIDIInputHandler implements Receiver, Constants {

	/** Each controller number has an array list that holds the listeners for
	 	that controller number. */
	private ArrayList<ControllerEventListener>[] controllerEventListeners = new ArrayList[128];
	
	/** List of listeners for note events */
	private ArrayList<NoteEventListener> noteEventListeners = new ArrayList<NoteEventListener>();

	// Variables for synchronization.
	private int midiClockCount = 0;
	private long midiClockTime = 0;


	/** Constructor */
	public MIDIInputHandler() {
		/* Create an array list for each possible controller number. Registered
		 	controllerEventListeners will be added to the appropriate array list. */
		for (int i = 0; i <= 127; ++i)
			controllerEventListeners[i] = new ArrayList<ControllerEventListener>();
	}

	
	/**  Called when the receiver is closed.*/
	public void close() {
		System.out.println("Closing MIDIInputHandler");
	}

	
	/**Called when the receiver receives a MIDI message. */
	public void send(MidiMessage message, long timeStamp) {
		if (message instanceof ShortMessage) {
			ShortMessage shrtMsg = (ShortMessage) message;
						
			//Handle Note On and Note Off messages
			if(shrtMsg.getCommand() == ShortMessage.NOTE_ON || shrtMsg.getCommand() == ShortMessage.NOTE_OFF){
				//Change velocity 0 to a note off message
				try{
					if(shrtMsg.getCommand() == ShortMessage.NOTE_ON && shrtMsg.getData2() == 0)
						shrtMsg.setMessage(ShortMessage.NOTE_OFF, shrtMsg.getChannel(), shrtMsg.getData1(), 0);
				}
				catch(Exception ex){
					ex.printStackTrace();//Do nothing for the moment
				}
				
				//Pass message on to listeners
				for (NoteEventListener noteEventListnr : noteEventListeners)
					noteEventListnr.noteEventOcccurred(shrtMsg);
			}
			
			// Handle control change messages
			else if (shrtMsg.getCommand() == ShortMessage.CONTROL_CHANGE) {
				/*
				 * Need to send control message to all registered listeners Find
				 * the controller number of the message using
				 * "((ShortMessage) message).getData1()" and work through the
				 * appropriate ArrayList and call all of the registered
				 * ControllerEventListeners.
				 */
				for (ControllerEventListener contEventList : controllerEventListeners[((ShortMessage) message).getData1()])
					contEventList.controlChange((ShortMessage) message);
			}

			// Handle MIDI clock messages.
			else if (message.getStatus() == ShortMessage.TIMING_CLOCK && Globals.getMasterSyncMode() == MIDI_SYNC) {
				++midiClockCount;
				if (midiClockCount == 24) { // 24 MIDI clock signals per quarter note
					
					// This is time elapsed for 1 beat. Want to find out how
					// many beats there will be in 1 minute
					double newTempo = (double) (60000000000l / (System.nanoTime() - midiClockTime));

					// Create an event that sets the new tempo
					EventRouter.tempoActionPerformed(new TempoEvent(newTempo));

					// Reset the clock count and record the current time.
					midiClockCount = 0;
					midiClockTime = System.nanoTime();
				}
			}

			// Handle a start message when in midi clock mode
			else if (message.getStatus() == ShortMessage.START && Globals.getMasterSyncMode() == MIDI_SYNC) {
				// Initialise timing signals
				midiClockCount = 0;
				midiClockTime = System.nanoTime();

				// Fire a play event
				EventRouter.playActionPerformed();
			}

			// Handle a start message when in midi clock mode
			else if (message.getStatus() == ShortMessage.STOP && Globals.getMasterSyncMode() == MIDI_SYNC) {
				// Stop sequencer
				EventRouter.stopActionPerformed();
			}
		}
	}

	
	/*------------------------------------------------------------------------*/
	/*----------              EVENT RELATED METHODS                -----------*/
	/*------------------------------------------------------------------------*/

	/** Adds a listener for controller events of a particular number */
	public void addControllerEventListener(ControllerEventListener listener, int[] controllerNumbers) {
		// Work through the list of controller numbers
		for (int i = 0; i < controllerNumbers.length; ++i)
			// Add a new listener for that controller number
			controllerEventListeners[controllerNumbers[i]].add(listener);
	}


	/** Removes the specified listener from all controller numbers */
	public void removeControllerEventListener(ControllerEventListener listener) {
		// Work through the array of controller numbers
		for (int i = 0; i <= 127; ++i) {
			controllerEventListeners[i].remove(listener);
		}
	}

	/** Adds a listener for note events of a particular number */
	public void addNoteEventListener(NoteEventListener listener) {
		noteEventListeners.add(listener);
	}


	/** Removes the specified listener for note events */
	public void removeNoteEventListener(NoteEventListener listener) {
		noteEventListeners.remove(listener);
	}


	/*------------------------------------------------------------------------*/
	/*----------                GETTERS AND SETTERS                -----------*/
	/*------------------------------------------------------------------------*/

	// Get the controllers that this listener is listening for
	public int[] getControllerNumbers(ControllerEventListener listener) {
		// Create a vector to hold the controller numbers
		Vector numberVector = new Vector();

		// Find which controller numbers the listener is listening for.
		for (int i = 0; i <= 127; ++i) {
			if (controllerEventListeners[i].contains(listener))
				numberVector.add(new Integer(i));
		}

		// Return the vector as an int array
		int[] intArray = new int[numberVector.size()];
		int counter = 0;
		for (Object obj : numberVector) {
			intArray[counter] = ((Integer) obj).intValue();
			++counter;
		}
		return intArray;
	}

}

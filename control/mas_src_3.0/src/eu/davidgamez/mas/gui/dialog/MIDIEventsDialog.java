package eu.davidgamez.mas.gui.dialog;

//Java imports
import javax.sound.midi.Receiver;
import javax.sound.midi.MidiMessage;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.table.*;
import javax.swing.JTable;
import javax.sound.midi.*;
import java.awt.Dimension;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.TransportListener;
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.*;
import eu.davidgamez.mas.gui.model.MIDIEventsModel;


public class MIDIEventsDialog extends JDialog implements Receiver, TransportListener {
	
	/** Model for the events */
	private MIDIEventsModel midiEventsModel = new MIDIEventsModel();

	/** Constructor */
	public MIDIEventsDialog(MainFrame mainFrame) {
		super(mainFrame, "MIDI Events", true);

		//Listen for transport events to clear the list on play
		EventRouter.addTransportListener(this);

		//Dialog does not block application
		this.setModal(false);

		//Add table to layout
		JTable table = new JTable(midiEventsModel);
		JScrollPane scrollPane = new JScrollPane(table);
		this.getContentPane().add(scrollPane, BorderLayout.CENTER);

		this.setMinimumSize(new Dimension(500, 300));
		this.setLocation(Globals.getScreenWidth()/4, Globals.getScreenHeight()/4);
	}

	
	/** Unused method inherited from Receiver */
	public void close(){
	}


	/** Called when the receiver receives a MIDI message.
     	Adds the message to the event list. */
	public void send(MidiMessage message, long timeStamp) {
		ShortMessage sm = (ShortMessage)message;
		try{
			midiEventsModel.add(new MIDIEvent(timeStamp, sm.getCommand(), sm.getData1(), sm.getData2(), sm.getChannel()));
		}
		catch(InvalidMidiDataException ex){
			MsgHandler.error(ex.getMessage());
			ex.printStackTrace();
		}
	}


	public void showDialog(){
		//Start listening for MIDI events. This is only done when the dialog is shown
		MIDIDeviceManager.addReceiver(this);
		this.setVisible(true);
	}


	protected void hideDialog(){
		//Stop listening for MIDI events when the dialog is hidden.
		MIDIDeviceManager.removeReceiver(this);
		this.setVisible(false);
	}


	public void killNotesActionPerformed() {
		// TODO Auto-generated method stub
		
	}


	public void playActionPerformed() {
		//Clear the list of events
		midiEventsModel.initialize();
	}


	public void stopActionPerformed() {
	}

}


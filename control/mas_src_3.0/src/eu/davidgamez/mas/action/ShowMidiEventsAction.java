package eu.davidgamez.mas.action;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

//MAS imports
import eu.davidgamez.mas.gui.dialog.MIDIEventsDialog;


public class ShowMidiEventsAction extends AbstractAction{
	//========================  INJECTED VARIABLES  ===========================
	private MIDIEventsDialog midiEventsDialog;
	
	
	public ShowMidiEventsAction(){
		super();
		this.putValue(NAME, "MIDI Events");
	}
	
	public void actionPerformed(ActionEvent e){
        midiEventsDialog.showDialog();
	}


	public void setMidiEventsDialog(MIDIEventsDialog midiEventsDialog) {
		this.midiEventsDialog = midiEventsDialog;
	}
	
}

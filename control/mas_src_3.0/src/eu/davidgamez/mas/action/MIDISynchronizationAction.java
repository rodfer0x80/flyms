package eu.davidgamez.mas.action;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

//MAS imports
import eu.davidgamez.mas.gui.dialog.SynchronizationDialog;


public class MIDISynchronizationAction  extends AbstractAction {
	//========================  INJECTED VARIABLES  ==========================
	private SynchronizationDialog synchronizationDialog;
    
	public MIDISynchronizationAction(){
		super();
		this.putValue(NAME, "MIDI Synchronization");
	}
	
	public void actionPerformed(ActionEvent e) {
    	synchronizationDialog.showDialog();
    }
    
	
	public void setSynchronizationDialog(SynchronizationDialog synchronizationDialog) {
		this.synchronizationDialog = synchronizationDialog;
	}
}

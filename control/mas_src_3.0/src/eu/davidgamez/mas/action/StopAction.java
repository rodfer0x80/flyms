package eu.davidgamez.mas.action;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.ResetListener;
import eu.davidgamez.mas.event.TransportListener;
import eu.davidgamez.mas.gui.MASLookAndFeel;
import eu.davidgamez.mas.midi.MASSequencer;


public class StopAction extends AbstractAction{
	//=====================  INJECTED VARIABLES  ======================
	MASSequencer masSequencer;
	
	
	public StopAction(){
		super();
		this.putValue(NAME, "Stop");
        this.putValue(Action.SMALL_ICON, MASLookAndFeel.getStopIcon());
	}
    
    /* Inherited from AbstractAction */
	public void actionPerformed(ActionEvent e){
        if (!Globals.isPlaying())
            return;

        masSequencer.stop();
	}


	public void setMasSequencer(MASSequencer masSequencer) {
		this.masSequencer = masSequencer;
	}

	
}

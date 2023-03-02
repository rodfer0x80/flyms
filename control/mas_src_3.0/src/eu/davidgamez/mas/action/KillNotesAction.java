package eu.davidgamez.mas.action;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

//MAS imports
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.ResetListener;
import eu.davidgamez.mas.gui.MASLookAndFeel;


public class KillNotesAction extends AbstractAction implements ResetListener {
	
	public KillNotesAction(){  
		super();
		this.putValue(NAME, "Kill Notes");
		this.putValue(Action.SMALL_ICON, MASLookAndFeel.getKillNotesIcon());
	}
	
    public void actionPerformed(ActionEvent e) {
    	EventRouter.stopActionPerformed();
    	EventRouter.killNotesActionPerformed();
    }

	public void resetActionPerformed() {
		this.setEnabled(false);
	}
    
}

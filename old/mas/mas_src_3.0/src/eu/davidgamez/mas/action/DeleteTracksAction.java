package eu.davidgamez.mas.action;

//Java imports
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.gui.MainPanel;
import eu.davidgamez.mas.midi.AgentHolder;
import eu.davidgamez.mas.midi.TrackHolder;


public class DeleteTracksAction extends AbstractAction  {
	//========================  INJECTED VARIABLES  ===========================
    private MainPanel mainPanel;
	
    public DeleteTracksAction(){
    	super();
		this.putValue(NAME, "Delete Track(s)");
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('T', Event.SHIFT_MASK));
    }
    
    public void actionPerformed(ActionEvent e) {
		if (mainPanel.deleteSelectedTracks())
			Globals.setProjectSaved(false); // Project has changed

		if (TrackHolder.size() == 0 || AgentHolder.size() == 0)
			Globals.setReadyToPlay(false);
    }

    
	public void setMainPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
	}
}

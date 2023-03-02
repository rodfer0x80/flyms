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


public class DeleteAgentAction extends AbstractAction {
	// ======================== INJECTED VARIABLES ===========================
	private MainPanel mainPanel;

	public DeleteAgentAction(){
		super();
		this.putValue(NAME, "Delete Agent(s)");
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('G', Event.SHIFT_MASK));
	}
	
	public void actionPerformed(ActionEvent e) {
		if (mainPanel.deleteSelectedAgents())
			Globals.setProjectSaved(false); // Project has changed

		if (TrackHolder.size() == 0 || AgentHolder.size() == 0)
			Globals.setReadyToPlay(false);
	}


	public void setMainPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
	}
	
}

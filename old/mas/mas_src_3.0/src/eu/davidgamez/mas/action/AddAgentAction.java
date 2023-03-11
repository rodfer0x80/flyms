package eu.davidgamez.mas.action;

//Java imports
import java.awt.Event;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.gui.dialog.AgentSelectionDialog;


public class AddAgentAction extends AbstractAction {
	// ======================== INJECTED VARIABLES ===========================
	private AgentSelectionDialog agentSelectionDialog;
	
	
	public AddAgentAction(){
		super();
		this.putValue(NAME, "Add Agent");
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('G', Event.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e){
        agentSelectionDialog.showDialog();
        if(agentSelectionDialog.isAgentAdded())
        	Globals.setProjectSaved(false); //Project has changed
	}
	
	public void setAgentSelectionDialog(AgentSelectionDialog agentSelectionDialog) {
		this.agentSelectionDialog = agentSelectionDialog;
	}
	
}


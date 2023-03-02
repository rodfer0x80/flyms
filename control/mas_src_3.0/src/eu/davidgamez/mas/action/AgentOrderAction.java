package eu.davidgamez.mas.action;

//Java imports
import java.awt.Event;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.gui.MainPanel;
import eu.davidgamez.mas.gui.dialog.AgentOrderDialog;


public class AgentOrderAction extends AbstractAction {
	//========================  INJECTED VARIABLES  ===========================
	private AgentOrderDialog agentOrderDialog;
	
	public AgentOrderAction(){
		super();
		this.putValue(NAME, "Set Agent Order");
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('L', Event.CTRL_MASK));
	}
	
	public void actionPerformed(ActionEvent e){
        agentOrderDialog.showDialog();
	}


	public void setAgentOrderDialog(AgentOrderDialog agentOrderDialog) {
		this.agentOrderDialog = agentOrderDialog;
	}
	
}


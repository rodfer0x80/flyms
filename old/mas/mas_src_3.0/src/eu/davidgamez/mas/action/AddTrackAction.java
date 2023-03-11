package eu.davidgamez.mas.action;

//Java imports
import java.awt.Event;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.gui.MainPanel;


public class AddTrackAction extends AbstractAction  {
	//========================  INJECTED VARIABLES  ===========================
    private MainPanel mainPanel;
	
    
    public AddTrackAction(){
    	super();
		this.putValue(NAME, "Add Track");
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('T', Event.CTRL_MASK));
    }
    
    public void actionPerformed(ActionEvent e) {
    	mainPanel.addTrack();
    	Globals.setProjectSaved(false);
    }

    
	public void setMainPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
	}
    
}

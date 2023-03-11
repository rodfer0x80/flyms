package eu.davidgamez.mas.action;

//Java imports
import java.awt.Event;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.gui.MainFrame;


public class ExitAction extends AbstractAction{
	MainFrame mainFrame;
	
	public ExitAction(){
		this.putValue(NAME, "Exit");
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('Q', Event.CTRL_MASK));
	}
	
	public void actionPerformed(ActionEvent e){
		//Check to see if 
		
		// Check to see if user wants to save current project
		if (!Globals.isProjectSaved()) {
			int saveCurrentProjectResponse = JOptionPane.showConfirmDialog(
					mainFrame, "Current project has not been saved. Would you like to continue without saving your changes?", 
					"New Project",
					JOptionPane.YES_NO_CANCEL_OPTION
			);
			if (saveCurrentProjectResponse == JOptionPane.YES_OPTION) {
				// Call method triggered by window closing event
				mainFrame.applicationClosing();
			}
		}
		//Nothing needs saving, so just quit
		else{
			mainFrame.applicationClosing();
		}
	}

	
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
}

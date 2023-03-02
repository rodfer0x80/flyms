package eu.davidgamez.mas.action;

//MAS imports
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.gui.MainFrame;

//Java imports
import java.awt.Event;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import eu.davidgamez.mas.Globals;

public class NewProjectAction extends AbstractAction{
	private MainFrame mainFrame;
	
	public NewProjectAction(){
		super();
		this.putValue(NAME, "New Project");
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', Event.CTRL_MASK));
	}

	public void actionPerformed(ActionEvent e)  {
		// If current project is not saved, ask user if he or she wants to save it before proceeding.
		if (!Globals.isProjectSaved()) {
			int saveCurrentProjectResponse = JOptionPane.showConfirmDialog(mainFrame, "Current project has not been saved. Would you like to continue without saving your changes?", "New Project",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (saveCurrentProjectResponse != JOptionPane.YES_OPTION)
				return;
		}

		// Signal other classes to reset themselves
		EventRouter.resetActionPerformed();

		// Set information about project appropriately
		Globals.setProjectOpen(false);
		Globals.setProjectFile(null);
		Globals.setProjectSaved(true);
	}

	
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

}

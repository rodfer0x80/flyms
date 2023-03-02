package eu.davidgamez.mas.action;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.exception.MASFileException;
import eu.davidgamez.mas.file.ProjectFileManager;
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.AgentHandler;
import eu.davidgamez.mas.midi.MASSequencer;

public class OpenProjectAction extends AbstractAction {
	//=======================  INJECTED VARIABLES  =========================
	private MainFrame mainFrame;
	private JFileChooser projectFileChooser;
	private ProjectFileManager projectFileManager;
	
	
	public OpenProjectAction(){
		super();
		this.putValue(NAME, "Open Project");
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', Event.CTRL_MASK));
	}
	
	
	public void actionPerformed(ActionEvent e){
		// If current project is not saved, ask user if he or she wants to save it before proceeding.
		if (!Globals.isProjectSaved()) {
			int saveCurrentProjectResponse = JOptionPane.showConfirmDialog(mainFrame, "Current project has not been saved. Would you like to continue without saving your changes?", "Open Project",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (saveCurrentProjectResponse != JOptionPane.YES_OPTION)
				return;
		}
		
		// Open file chooser to select the project to be opened
		projectFileChooser.setCurrentDirectory(Globals.getProjectDirectory());
		projectFileChooser.rescanCurrentDirectory();
		if (projectFileChooser.showOpenDialog(mainFrame) != JFileChooser.CANCEL_OPTION) {
			File file = projectFileChooser.getSelectedFile();
			if (file != null) {
				String s = file.getName();
				if (s.endsWith(".proj")) {
					try {
						EventRouter.resetActionPerformed();
						projectFileManager.openProject(file);
						mainFrame.setTitle("MIDI Agent System - " + file.getName());
					} 
					catch (MASFileException ex) {
						MsgHandler.error("Error encountered loading project file. " + ex.getMessage());
						return;
					}
				} 
				else { // Show warning that it is the wrong file type and return
					MsgHandler.error("Please select a file with extension \".proj\"");
					return;
				}
			}
		}
	}

	
	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public void setProjectFileChooser(JFileChooser projectFileChooser) {
		this.projectFileChooser = projectFileChooser;
	}

	public void setProjectFileManager(ProjectFileManager projectFileManager) {
		this.projectFileManager = projectFileManager;
	}

}

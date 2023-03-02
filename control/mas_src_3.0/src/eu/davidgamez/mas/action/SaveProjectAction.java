package eu.davidgamez.mas.action;

import java.awt.Event;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.file.ProjectFileManager;

public class SaveProjectAction extends AbstractAction {
	//=========================  INJECTED VARIABLES  ==========================
	private ProjectFileManager projectFileManager;
	private SaveProjectAsAction saveProjectAsAction;
	
	
	public SaveProjectAction(){
		super();
		this.putValue(NAME, "Save Project");
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('S', Event.CTRL_MASK));
	}
	
	public void actionPerformed(ActionEvent e){
		if (Globals.isProjectOpen()) { // If a project is already open, just save it
			projectFileManager.saveProject(Globals.getProjectFile());
		} 
		else{
			saveProjectAsAction.actionPerformed(new ActionEvent(this, 0, "untitled"));
		}
	}

	public void setProjectFileManager(ProjectFileManager projectFileManager) {
		this.projectFileManager = projectFileManager;
	}

	public void setSaveProjectAsAction(SaveProjectAsAction saveProjectAsAction) {
		this.saveProjectAsAction = saveProjectAsAction;
	}
	
}

package eu.davidgamez.mas.action;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.file.ProjectFileManager;
import eu.davidgamez.mas.gui.MainFrame;

public class SaveProjectAsAction extends AbstractAction{
	//======================  INJECTED VARIABLES  ===========================
	private MainFrame mainFrame;
	private JFileChooser projectFileChooser;
	private ProjectFileManager projectFileManager;

	
	public SaveProjectAsAction(){
		super();
		this.putValue(NAME, "Save Project As");
	}
	
	
	public void actionPerformed(ActionEvent e){
		boolean fileSelected = false;
		boolean cancelled = false;
		File file = null;
		while(!(fileSelected || cancelled) ){
			
			//Display file chooser
			projectFileChooser.setCurrentDirectory(Globals.getProjectDirectory());
			projectFileChooser.rescanCurrentDirectory();
				
			//Check that user has selected a file
			if (projectFileChooser.showSaveDialog(mainFrame) != JFileChooser.CANCEL_OPTION) {
				file = projectFileChooser.getSelectedFile();
				if (file != null) {
					String s = file.getName();
					
					// First make sure file name is correct
					boolean fileNameOk = true;
					if (!s.endsWith(".proj")) { // Not correct file type
						if (s.indexOf('.') == -1) { // No file extension added
							file = new File(file.getAbsolutePath() + ".proj");
						} 
						else { // Show warning that it is the wrong file type and return
							JOptionPane.showMessageDialog(mainFrame, "Please select a file with extension \".proj\"", "Wrong File Type", JOptionPane.ERROR_MESSAGE);
							fileNameOk = false;
						}
					}
					
					// Check to see if file already exists
					if (fileNameOk && file.exists()) {
						// Check that they want to overwrite
						int optionPaneResult = JOptionPane.showConfirmDialog(mainFrame, "File exists. Overwrite?", "Confirm File Overwrite", JOptionPane.YES_NO_CANCEL_OPTION);
						if (optionPaneResult == JOptionPane.YES_OPTION) {
							fileSelected = true;
						} 
						else if (optionPaneResult == JOptionPane.NO_OPTION) {
							;//Do nothing we will go around loop again
						}
						//Dialog is cancelled
						else {
							cancelled = true;
						}
					}
					//File does not already exist
					else {
						fileSelected = true;
					}
				}
				else{
					JOptionPane.showMessageDialog(mainFrame, "Please select a file", "No File Selected", JOptionPane.ERROR_MESSAGE);
				}
			}
			//Selection is cancelled, so break out of loop
			else{
				cancelled = true;
			}
		}
		if(fileSelected && !cancelled)
			projectFileManager.saveProject(file);
	}

	
	public void setProjectFileChooser(JFileChooser projectFileChooser) {
		this.projectFileChooser = projectFileChooser;
	}

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public void setProjectFileManager(ProjectFileManager projectFileManager) {
		this.projectFileManager = projectFileManager;
	}
	
	
}

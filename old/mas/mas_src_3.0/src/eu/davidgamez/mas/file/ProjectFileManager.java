package eu.davidgamez.mas.file;

//Java imports
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.midi.Buffer;
import eu.davidgamez.mas.gui.MainPanel;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.*;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.TempoEvent;
import eu.davidgamez.mas.exception.*;


public class ProjectFileManager implements Constants {
	//========================  INJECTED VARIABLES  ==========================
	MainPanel mainPanel;


	/** Loads up a project from the specified file */
	public void openProject(File file) throws MASFileException {
		try {
			//Open file
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = in.readLine();
			
			//Read file
			while(line != null){
				
				//Load global parameters
				if (line.contains("<global_parameters>")) {
					String tmpStr = line + "\n";
					while (line != null && !line.equals("") && !line.contains("</global_parameters>")) {
						line = in.readLine();
						tmpStr += line + "\n";
					}
					//Load up the buffer properties from the loaded string
					Globals.loadFromXML(tmpStr);
				}
				
				//Load buffer parameters
				if (line.contains("<buffer_parameters>")) {
					String tmpStr = line + "\n";
					while (line != null && !line.equals("") && !line.contains("</buffer_parameters>")) {
						line = in.readLine();
						tmpStr += (line + "\n");
					}
					//Load buffer properties from the loaded string
					Buffer.loadFromXML(tmpStr);
				}
				
				//Load tracks
				if (line.contains("<tracks>")) {
					String tmpStr = line + "\n";
					while (line != null && !line.equals("") && !line.contains("</tracks>")) {
						line = in.readLine();
						tmpStr += (line + "\n");
					}
					//Load buffer properties from the loaded string
					mainPanel.loadTracksFromXML(tmpStr);
				}
				
				//Load agents
				if (line.contains("<agents>")) {
					String tmpStr = line + "\n";
					while (line != null && !line.equals("") && !line.contains("</agents>")) {
						line = in.readLine();
						tmpStr += (line + "\n");
					}
					//Load buffer properties from the loaded string
					mainPanel.loadAgentsFromXML(tmpStr);
				}
				
				//Load connections
				if (line.contains("<connections>")) {
					String tmpStr = line + "\n";
					while (line != null && !line.equals("") && !line.contains("</connections>")) {
						line = in.readLine();
						tmpStr += (line + "\n");
					}
					//Load buffer properties from the loaded string
					AgentHolder.loadConnectionsFromXML(tmpStr);
				}
				
				//Read next line
				line = in.readLine();
			}
			//Close file
			in.close();
        }
        catch (Exception ex) {
          MsgHandler.error("Error during the loading of project.", ex);
        }
        
        //Record new state of application in Globals
        mainPanel.repaint();
        Globals.setProjectOpen(true);
        Globals.setProjectFile(file);
        Globals.setProjectSaved(true);
    }


    /** At this stage all the checks on file should have been carried out.
    		It should be a file that exists with extension ".proj" */
    public void saveProject(File file){
    	try{
    		//Open output stream
    		BufferedWriter out = new BufferedWriter(new FileWriter(file));

    		//Write XML header
    		out.write(XML_HEADER);
    		out.write("\n<project>");

    		String indent = "\n\t";
    		
    		//Save global parameters
    		out.write(Globals.getXML(indent));
    		
    		//Save buffer parameters
    		out.write(Buffer.getXML(indent));
    		
	        //Save tracks
    		out.write(mainPanel.getTrackXML(indent));
	
	        //Save agents
    		out.write(mainPanel.getAgentXML(indent));
    		
    		//Save connections
    		out.write(AgentHolder.getConnectionXML(indent));
    		
    		//Finish off XML and close file
	        out.write("\n</project>\n");
	        out.close();
	        
	        //Record state of project
	        Globals.setProjectOpen(true);
	        Globals.setProjectSaved(true);
	        Globals.setProjectFile(file);
    	}
    	catch(Exception ex){
            MsgHandler.error("Error during the loading of project to file " + file.getAbsolutePath() + ".", ex);
    	}
    }
	
    
	public void setMainPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
	}
}

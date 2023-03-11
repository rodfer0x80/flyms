package eu.davidgamez.mas.file;

//Java imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.exception.MASAgentException;
import eu.davidgamez.mas.exception.MASException;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Agent;

public class AgentClassLoader_2 extends URLClassLoader implements Constants {
	
	HashMap<String, Class<Agent> > agentMap = new HashMap<String, Class<Agent> >();
	
	HashMap<String, Class<AgentPropertiesPanel> > panelMap = new HashMap<String, Class<AgentPropertiesPanel> >();
	
	/** Holds the icons associated with each type of agent */
	HashMap<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>();
	
	String currentElement = "";
	
	private String midiAgentClassStr = null;
	private String agentPanelClassStr = null;
	private String agentIconFileStr = null;
	
	boolean loadingAgent = false;

	/** Constructor - start with an empty URL array */
	public AgentClassLoader_2(){
		super(new URL [] {});
		
		//Add jars in plugin directory to class path
		try{
			File [] pluginFiles = new File("plugins").listFiles();
			for(int i=0; i<pluginFiles.length; ++i){
				if(pluginFiles[i].getAbsolutePath().endsWith(".jar")){
					System.out.println("ADDING PATH " + pluginFiles[i].getAbsolutePath());
					this.addURL(pluginFiles[i].toURI().toURL());
				}
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			MsgHandler.error("Error trying to add paths to jars in plugin directory: . Message:" + ex.getMessage());
		}
	}
	
	
	/** Loads up the current available agents */
	public void loadAgentClasses(){
		XMLInputFactory factory = XMLInputFactory.newInstance();

		try {
			String agentDefinitionFilePath = "plugins/agents.xml";
			XMLEventReader eventReader = factory.createXMLEventReader(new FileReader(agentDefinitionFilePath));
			
			//Work through file to obtain details about available agents
			while(eventReader.hasNext()){
				XMLEvent event = eventReader.nextEvent();
				int eventType = event.getEventType();
				
				//Reset everything
				if(eventType == XMLStreamConstants.START_DOCUMENT){
					reset();
				}
				else if(eventType == XMLStreamConstants.START_ELEMENT){
					String startElementName = event.asStartElement().getName().getLocalPart();
					if(startElementName.equals("agent"))
						loadingAgent = true;
					currentElement = startElementName;
				}
				else if(eventType == XMLStreamConstants.CHARACTERS){
					String characters = event.asCharacters().getData();
					if(loadingAgent && currentElement.equals("midi_agent"))
						midiAgentClassStr = characters;
					else if(loadingAgent && currentElement.equals("properties_panel"))
						agentPanelClassStr = characters;
					else if(loadingAgent && currentElement.equals("icon_file"))
						agentIconFileStr = characters;
					else if (!characters.trim().isEmpty())
						throw new MASXmlException("Unexpected characters: " + characters + ". Current element is " + currentElement);
				}
				else if(eventType == XMLStreamConstants.END_ELEMENT){
					String endElementName = event.asEndElement().getName().getLocalPart();
					if(endElementName.equals("agent")){
						addAgent();
					}
					currentElement = "";
				}
			}
		} 
		catch (FileNotFoundException e) {
			MsgHandler.error(e.getMessage());
		} 
		catch (XMLStreamException e) {
			MsgHandler.error(e.getMessage());
		}
		catch(MASException ex){
			MsgHandler.error(ex.getMessage());
		}
	}


	/** Returns a set containing the available agents */
	public Set<String> getAgentNames(){
		return agentMap.keySet();
	}
	
	
	/** Returns the properties panel corresponding to the agent with the specified name */
	public AgentPropertiesPanel getAgentPropertiesPanel(String agentType) throws MASException{
		if(!panelMap.containsKey(agentType))
			throw new MASException("Panel corresponding to agent type " + agentType + " cannot be found.");
		try {
			return panelMap.get(agentType).newInstance();
		} 
		catch (InstantiationException e) {
			e.printStackTrace();
			throw new MASException("InstantiationException thrown trying to get agent properties panel.");
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new MASException("IllegalAccessException thrown trying to get agent properties panel.");
		}
	}
	
	
	
	/** Returns an instance of the MIDI agent with the specified name */
	public Agent getMidiAgent(String agentType) throws MASException{
		if(!agentMap.containsKey(agentType))
			throw new MASException("MIDI agent with name " + agentType + " cannot be found.");
		try {
			Agent tmpAgent = agentMap.get(agentType).newInstance();
			return tmpAgent;
		} 
		catch (InstantiationException e) {
			e.printStackTrace();
			throw new MASException("InstantiationException thrown trying to get MIDI agent.");
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new MASException("IllegalAccessException thrown trying to get MIDI agent.");
		}
	}
	
	
	/** Returns the icon associated with a particular agent or the default icon if one has not 
	  	been specified */
	public ImageIcon getAgentIcon(String agentType) throws MASException {
		if(!iconMap.containsKey(agentType))
			throw new MASException("Icon for agent with name " + agentType + " cannot be found.");
		return iconMap.get(agentType);
	}
	
	
	/** Adds the agent using the information that should already have been loaded from the XML file. */
	private void addAgent() throws MASXmlException{
		if(midiAgentClassStr == null || agentPanelClassStr == null)
			throw new MASXmlException("Missing data for jar file, midi agent or agent panel");
		
		try{
			//Store the agent class
			Class<Agent> agentClass =  (Class<Agent>)this.loadClass(midiAgentClassStr);
			Agent agent = agentClass.newInstance();
			if(agent.getAgentType() == null)
				throw new MASAgentException ("Agent type has not been set for agent with class " + midiAgentClassStr);
			agentMap.put(agent.getAgentType(), agentClass);
			
			//Store the properties panel
			Class<AgentPropertiesPanel> panelClass =  (Class<AgentPropertiesPanel>)this.loadClass(agentPanelClassStr);
			AgentPropertiesPanel panel = panelClass.newInstance();
			if(panel.getAgentType() == null || !panel.getAgentType().equals(agent.getAgentType()))
				throw new MASAgentException("Panel agent type " + panel.getAgentType() + " does not match agent type " + agent.getAgentType());
			panelMap.put(agent.getAgentType(), panelClass);
			
			//Store the icon file
			ImageIcon icon = new ImageIcon(Globals.getImageDirectory() + "/" + DEFAULT_AGENT_ICON);
			if(agentIconFileStr != null){
				File iconFile = new File(Globals.getImageDirectory() + "/" + agentIconFileStr);
				if(iconFile.exists())
					icon = new ImageIcon(Globals.getImageDirectory() + "/" + agentIconFileStr);
			}
			iconMap.put(agent.getAgentType(), icon);
				
				
			//Reset the strings
			midiAgentClassStr = null;
			agentPanelClassStr = null;
			agentIconFileStr = null;
			loadingAgent = false;
		}
		catch(Exception ex){
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}
	
	
	/** Resets the information stored in the class */
	private void reset(){
		agentMap.clear();
		panelMap.clear();
		midiAgentClassStr = null;
		agentPanelClassStr = null;
	}
	
}

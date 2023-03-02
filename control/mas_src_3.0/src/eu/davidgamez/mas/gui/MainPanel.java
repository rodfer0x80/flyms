package eu.davidgamez.mas.gui;

//Java imports
import javax.swing.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.event.DisplayListener;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.ResetListener;
import eu.davidgamez.mas.exception.MASException;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.file.AgentClassLoader_2;
import eu.davidgamez.mas.gui.dialog.AgentPropertiesDialog;
import eu.davidgamez.mas.midi.*;

//Other imports
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;



public class MainPanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener, ResetListener, DisplayListener{
	//========================  INJECTED VARIABLES  ===========================
	private MainFrame mainFrame;
	private AgentClassLoader_2 agentClassLoader_2;


	//========================  ORDINARY VARIABLES  ===========================
	//Holds all the track GUIs. The key is the track's unique id. The value is the trackGUI
	private HashMap<String, TrackGUI> trackGUIHashMap = new HashMap<String, TrackGUI>();

	/* Holds all the AgentGUIs.
     These are stored in a hash map with the key being the unique ID of the associated MIDI agent
     and the data being the the AgentGUI. */
	private HashMap<String, AgentGUI> agentGUIHashMap= new HashMap<String, AgentGUI>();

	//Variables to help with mouse handling
	private String selectedConnection_TrackID = null;
	private String selectedConnection_AgentID = null;
	private int xPos = 0, yPos = 0, oldXPos, oldYPos;
	private int tempTrackNumber = -1;
	private int mouseButton = -1;

	//Popup menu for connections
	private JPopupMenu connectionPopup = new JPopupMenu();
	JMenuItem deleteConnectionMenuItem = new JMenuItem("Delete");

	//Popup menu for agent
	private JPopupMenu agentPopup = new JPopupMenu();
	JMenuItem enableAgentMenuItem = new JMenuItem("Enable");
	JMenuItem disableAgentMenuItem = new JMenuItem("Disable");

	//Records whether a track is moving
	private TrackGUI movingTrackGUI = null;

	//Records whether an agent is moving and the class of the moving agent when it is
	private AgentGUI movingAgentGUI = null;

	//The line that is painted when a connection is being drawn.
	private Line2D tempConnectionLine = null;
	private AgentGUI connectingAgentGUI = null;

	//The currently selected agent - used by the popup menu
	private AgentGUI selectedAgentGUI = null;

	//The button used to drag the track
	private int trackButton = 1;

	//Show a larger panel through a restricted view port
	private JViewport viewPort;
	private JScrollPane scrollPane;

	
	/** Constructor */
	public MainPanel() {
		agentPopup.setPreferredSize(new Dimension(100, 30));
		
		//Listen for events from agent popup menu
		enableAgentMenuItem.addActionListener(this);
		disableAgentMenuItem.addActionListener(this);

		//Set up connection popup menu
		connectionPopup.setPreferredSize(new Dimension(100, 30));
		deleteConnectionMenuItem.addActionListener(this);
		connectionPopup.add(deleteConnectionMenuItem);
	}


	/*-------------------------------------------------------*/
	/*------            PUBLIC METHODS                -------*/
	/*-------------------------------------------------------*/

	/** Sets up the panel */
	public void initialise(){
		//Set size of work area
		this.setPreferredSize(new Dimension(Globals.getWorkAreaWidth(), Globals.getWorkAreaHeight()));
		
		//Build scroll pane and view port
		scrollPane = new JScrollPane(this);
		scrollPane.setPreferredSize(new Dimension(200, 200));
		viewPort = scrollPane.getViewport();
		this.setAutoscrolls(true);

		//Set the look of the panel
		setBackground(MASLookAndFeel.getMainPanelBackground());

		//Add Listeners
		addMouseListener(this);
		addMouseMotionListener(this);
		EventRouter.addResetListener(this);


	}


	/** Adds an agent to the panel and application. */
	public void addAgent(AgentPropertiesPanel agPropPanel, ImageIcon icon){
		//Create graphical representation of agent
		AgentGUI tmpAgGUI = new AgentGUI(agPropPanel, icon);
		agentGUIHashMap.put(tmpAgGUI.getID(), tmpAgGUI);

		//Add MIDI agent to agent handler
		AgentHolder.addAgent(tmpAgGUI.getAgent());

		//Refresh
		this.repaint();
	}
	
	
	/** Adds an agent from the supplied XML String **/
	public void addAgent(String xmlStr){
		//Get substrings with the appropriate properties
		String panelStr = xmlStr.substring(xmlStr.indexOf("<agent_panel>"), xmlStr.indexOf("</agent_panel>") + 14);
		String midiAgStr = xmlStr.substring(xmlStr.indexOf("<midi_agent>"), xmlStr.indexOf("</midi_agent>") + 13);

		//Load up the agent classes
		agentClassLoader_2.loadAgentClasses();
		
		try{
			//Load panel
			Document xmlDoc = Util.getXMLDocument(panelStr);
			String panelAgType = Util.getStringParameter("type", xmlDoc);
			String panelClass = Util.getStringParameter("class", xmlDoc);
			AgentPropertiesPanel newPanel = agentClassLoader_2.getAgentPropertiesPanel(panelAgType);
			if(!newPanel.getClass().getCanonicalName().equals(panelClass))
				throw new MASXmlException("AgentPropertiesPanel class does not match the class associated with the agent type.");
			newPanel.loadFromXML(panelStr);
			
			//Load agent
			xmlDoc = Util.getXMLDocument(midiAgStr);
			String midiAgType = Util.getStringParameter("type", xmlDoc);
			String midiAgClass = Util.getStringParameter("class", xmlDoc);
			Agent newMidiAgent = agentClassLoader_2.getMidiAgent(midiAgType);
			if(!newMidiAgent.getClass().getCanonicalName().equals(midiAgClass))
				throw new MASXmlException("MIDI Agent class does not match the class associated with the agent type.");
			newMidiAgent.loadFromXML(midiAgStr);
			
			//Create graphical representation of agent
			AgentGUI tmpAgGUI = new AgentGUI(xmlStr, agentClassLoader_2.getAgentIcon(midiAgType));
			newPanel.setAgent(newMidiAgent);
			tmpAgGUI.setAgentPropertiesPanel(newPanel);
			agentGUIHashMap.put(newMidiAgent.getID(), tmpAgGUI);

			//Add MIDI agent to agent handler
			AgentHolder.addAgent(newMidiAgent);
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}


	/** Adds a new track to the panel and application. */
	public void addTrack(){
		//Create and add track
		TrackGUI tmpTrackGUI = new TrackGUI(new Track(), mainFrame);
		addTrack(tmpTrackGUI);
	}
	
	
	/** Adds the supplied track to the panel and application */
	public void addTrack(TrackGUI newTrackGUI){
		//Add to class
		trackGUIHashMap.put(newTrackGUI.getID(), newTrackGUI);

		//Add MIDI track to track holder
		TrackHolder.addTrack(newTrackGUI.getTrack());
		
		//Refresh
		this.repaint();
	}


	/** Deletes the agent's graphical representation and repaints panel */
	public void deleteAgent(String agentID){
		//Remove agent from the model
		try{
			AgentHolder.deleteAgent(agentID);
		}
		catch(MASException ex){
			MsgHandler.critical(ex.getMessage());
		}

		//Remove the agent's graphical representation
		agentGUIHashMap.remove(agentID);
		this.repaint();
	}


	/** Deletes the specified track */
	public void deleteTrack(String trackID){
		//Set solo state of track to false
		trackGUIHashMap.get(trackID).getTrack().setSoloed(false);
		
		//Remove track from the track GUI hash map
		trackGUIHashMap.remove(trackID);

		//Remove MIDI track from track holder
		try{
			TrackHolder.deleteTrack(trackID);
		}
		catch(MASException ex){
			MsgHandler.critical(ex.getMessage());
		}
	}


	//Deletes the selected agent(s). Returns true if agents have been deleted.
	public boolean deleteSelectedAgents(){
		//Get a list of agents to delete
		ArrayList<String> deleteList = new ArrayList<String>();
		for(AgentGUI tmpAgGUI : agentGUIHashMap.values()){
			if(tmpAgGUI.isSelected()){
				deleteList.add(tmpAgGUI.getID());
			}
		}
		//Delete the agents
		for(String tmpAgID : deleteList){
			deleteAgent(tmpAgID);
		}

		//Redraw graphics
		this.repaint();

		//Return true or false depending on whether agents have been deleted
		if(deleteList.isEmpty())
			return false;
		return true;
	}


	//Deletes the selected track from the project. Returns true if a track has been deleted
	public boolean deleteSelectedTracks(){
		//Get a list of tracks to delete
		ArrayList<String> deleteList = new ArrayList<String>();
		for(TrackGUI tmpTrkGUI : trackGUIHashMap.values()){
			if(tmpTrkGUI.isSelected()){
				deleteList.add(tmpTrkGUI.getID());
			}
		}
		//Delete the tracks
		for(String tmpTrkID : deleteList){
			deleteTrack(tmpTrkID);
		}

		//Redraw graphics
		this.repaint();

		//Return true or false depending on whether tracks have been deleted
		if(deleteList.isEmpty())
			return false;
		return true;
	}


	/** Removes all agents from the project */
	public void removeAllAgents(){
		agentGUIHashMap.clear();
		AgentHolder.reset();
		this.repaint();
	}


	/** Deletes all tracks from the project */
	public void removeAllTracks(){
		trackGUIHashMap.clear();
		TrackHolder.reset();
		this.repaint();
	}

	/** Resets the panel to its starting state. */
	public void reset(){
		removeAllAgents();
		removeAllTracks();
	}


	/** FIXME: NOT IMPLEMENTED AT PRESENT */
	public void zoomInActionPerformed(){

	}

	/** FIXME: NOT IMPLEMENTED AT PRESENT */
	public void zoomOutActionPerformed(){
	}


	//Paints everything in the window
	public void paint(Graphics g){
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D)g;
		
		//Paint unconnected agents
		for(AgentGUI tmpAgGUI : agentGUIHashMap.values()){
			if(!AgentHolder.isConnected(tmpAgGUI.getID()))
				tmpAgGUI.paintAgent(g2D);
		}

		//Paint tracks, connections and connected agents
		for(TrackGUI tmpTrackGUI : trackGUIHashMap.values()){
			//Get list of agents that are connected to this track
			if(tmpTrackGUI.showAgents()){
				Collection<String> trackConnections = AgentHolder.getTrackConnections(tmpTrackGUI.getID());
				for(String tmpAgID : trackConnections){
					AgentGUI tmpAgentGUI = agentGUIHashMap.get(tmpAgID);

					//Only want to paint connections if agents are painted
					if(tmpTrackGUI.showConnections()){
						g2D.setPaint(MASLookAndFeel.getConnectionColor());
						g2D.drawLine(tmpAgentGUI.getCentre().x, tmpAgentGUI.getCentre().y, tmpTrackGUI.getCentre().x, tmpTrackGUI.getCentre().y);
					}

					//Paint the agent (FIXME: CURRENTLY PAINTS TWICE CONNECTED AGENTS TWICE)
					tmpAgentGUI.paintAgent(g2D);
				}
			}
			//Paint the track
			tmpTrackGUI.paintTrack(g2D);
		}

		//Paint temporary connection line
		if(tempConnectionLine != null){
			g2D.setPaint(MASLookAndFeel.getConnectionColor());
			g2D.draw(tempConnectionLine);
		}
	}

	//-------------------------------------------------------------------------------------
	//----------                    Mouse Handling Methods                      -----------
	//-------------------------------------------------------------------------------------

	//Looks to see if an agent or track has been selected
	public void mouseClicked(MouseEvent e){
		xPos = e.getX();
		yPos = e.getY();

		//Check to see if click is anywhere inside a track
		for (TrackGUI tmpTrackGUI : trackGUIHashMap.values()) {
			if (tmpTrackGUI.processMouseClicked(xPos, yPos, e.getClickCount())) {
				this.repaint();
				return; //Have processed this event successfully
			}
		}
		
		//Check to see if click is anywhere inside an agent
		if (e.getClickCount() == 1){
			for (AgentGUI tmpAgGUI : agentGUIHashMap.values()) {
				if (tmpAgGUI.contains(xPos, yPos)) {
					//Middle mouse click enables or disables the agent
					if (e.getButton() == MouseEvent.BUTTON2) {
						if (tmpAgGUI.isEnabled())
							tmpAgGUI.setEnabled(false);
						else
							tmpAgGUI.setEnabled(true);
					}
					//Right mouse click shows popup menu for agent
					else if (e.getButton() == MouseEvent.BUTTON3) {
						//Set up menu appropriately
						agentPopup.removeAll();
						if (tmpAgGUI.isEnabled())
							agentPopup.add(disableAgentMenuItem);
						else
							agentPopup.add(enableAgentMenuItem);
						selectedAgentGUI = tmpAgGUI;
						agentPopup.show(this, xPos, yPos);
					}
					
					//Finished processing this event
					this.repaint();
					return;
				}
			}
		}
		
		//DOUBLE CLICK
		else if (e.getClickCount() == 2) {
			//Check to see if double click is anywhere inside an agent
			for (AgentGUI tmpAgGUI : agentGUIHashMap.values()) {
				if (tmpAgGUI.contains(xPos, yPos)) {
					new AgentPropertiesDialog(mainFrame, tmpAgGUI);
					
					//Finished processing this event
					this.repaint();
					return;
				}
			}
		}

		//Check that click is not near any of the connections
		if(e.getButton() == MouseEvent.BUTTON3){
			selectedConnection_TrackID = null;
			selectedConnection_AgentID = null;
			for(TrackGUI tmpTrackGUI : trackGUIHashMap.values()){
				//Get list of agents that are connected to this track
				if (tmpTrackGUI.showAgents()) {
					Collection<String> tmpAgIDArrayList = AgentHolder.getTrackConnections(tmpTrackGUI.getID());
					for (String tmpAgID : tmpAgIDArrayList) {
						AgentGUI tmpAgentGUI = agentGUIHashMap.get(tmpAgID);
	
						//Connection lines are only visible if agents are visible
						if (tmpTrackGUI.showConnections()) {
							Line2D.Double line = new Line2D.Double(tmpAgentGUI.getCentre().x, tmpAgentGUI.getCentre().y, tmpTrackGUI.getCentre().x, tmpTrackGUI.getCentre().y);
							if (line.ptLineDist(xPos, yPos) < 5) {
								selectedConnection_TrackID = tmpTrackGUI.getID();
								selectedConnection_AgentID = tmpAgentGUI.getID();
								connectionPopup.show(this, xPos, yPos);
								
								//Successfully processed this event
								this.repaint();
								return;
							}
						}
					}
				}
			}
		}
	}


	public void mouseEntered(MouseEvent e){
	}


	public void mouseExited(MouseEvent e){
		movingAgentGUI = null;
		movingTrackGUI = null;
		tempConnectionLine = null;
	}

	public void mousePressed(MouseEvent e){
		xPos = e.getX();
		yPos = e.getY();
		mouseButton = e.getButton();

		//Check to see if press is anywhere inside an agent
		boolean agentFound = false; //only want to select one agent at a time
		for (AgentGUI tmpAgGUI : agentGUIHashMap.values()) {
			if (tmpAgGUI.contains(xPos, yPos) && !agentFound) {
				agentFound = true;
				//CTRL + mouse press starts the drawing of a connection from the agent
				if (e.isControlDown() && e.getButton() == MouseEvent.BUTTON1) {
					tempConnectionLine = new Line2D.Double(xPos, yPos, xPos, yPos);
					connectingAgentGUI = tmpAgGUI;
				}
				//Left mouse press selects the agent
				else if (e.getButton() == MouseEvent.BUTTON1) {
					movingAgentGUI = tmpAgGUI;
					tmpAgGUI.setSelected(true);
				}
			}
			else {
				tmpAgGUI.setSelected(false);
			}
		}
		if (agentFound) {
			this.repaint();
			return; //Have processed this event
		}


		//Check to see if press is anywhere inside a track
		boolean trackFound = false; //Only want to select one track at a time
		for (TrackGUI tmpTrackGUI : trackGUIHashMap.values()) {
			if (!trackFound && tmpTrackGUI.contains(xPos, yPos)) {
				trackFound = true;
				tmpTrackGUI.setSelected(true);
				tmpTrackGUI.setMoving(true);
				movingTrackGUI = tmpTrackGUI;
				trackButton = e.getButton();
			}
			else{
				tmpTrackGUI.setSelected(false);
				tmpTrackGUI.setMoving(false);
			}
		}

		this.repaint();
	}

	public void mouseReleased(MouseEvent e){
		xPos = e.getX();
		yPos = e.getY();

		if(tempConnectionLine != null){
			//Find out if the mouse has been released inside a track
			boolean trackFound = false; //Only want to select one track at a time
			for (TrackGUI tmpTrackGUI : trackGUIHashMap.values()) {
				if (!trackFound && tmpTrackGUI.contains(xPos, yPos)) {
					trackFound = true;
					try{
						AgentHolder.addConnection(connectingAgentGUI.getID(), tmpTrackGUI.getID());
					}
					catch(MASException ex){
						MsgHandler.critical(ex.getMessage());
					}
				}
			}
		}

		movingTrackGUI = null;
		movingAgentGUI = null;
		tempConnectionLine = null;
		this.repaint();
	}


	public void mouseDragged(MouseEvent e){
		oldXPos = xPos;
		oldYPos = yPos;
		xPos = e.getX();
		yPos = e.getY();
		int xTrans = xPos - oldXPos;
		int yTrans = yPos - oldYPos;
		Rectangle panelBounds = this.getBounds();
		
		/* Middle mouse button drags working area around */
		if(mouseButton == MouseEvent.BUTTON2){
			Rectangle viewRect = viewPort.getViewRect();
			Rectangle newViewRect = new Rectangle(viewRect);
			newViewRect.x -= xTrans;
			newViewRect.y -= yTrans;

			//Make sure new position does not exceed boundaries
			if(newViewRect.x < 0)
				newViewRect.x = 0;
			if(newViewRect.y < 0)
				newViewRect.y = 0;
			if(newViewRect.x + newViewRect.width > panelBounds.width)
				newViewRect.x = panelBounds.width - newViewRect.width;
			if(newViewRect.y + newViewRect.height > panelBounds.height)
				newViewRect.y = panelBounds.height - newViewRect.height;
			
			//Set viewport to new position and return
			viewPort.setViewPosition(new Point(newViewRect.x, newViewRect.y));
			return;
		}
		
		//Moving an agent around
		if(movingAgentGUI != null){
			//Move the agent
			movingAgentGUI.translate(xTrans, yTrans, panelBounds);
			
			//Enable dragging to scroll view
			Rectangle viewRect = new Rectangle(movingAgentGUI.x, movingAgentGUI.y, movingAgentGUI.width + 1, movingAgentGUI.height + 1);
			scrollRectToVisible(viewRect);
			this.repaint();
			return;
		}
		
		//Moving track around
		if (movingTrackGUI != null) {
			//Move the track GUI
			movingTrackGUI.translate(xTrans, yTrans, panelBounds);
			
			//Translate agents as well if we are using the right mouse button
			if(trackButton == MouseEvent.BUTTON3){
				
				//Get list of agents that are connected to the moving track
				Collection<String> tmpAgIDArrayList = AgentHolder.getTrackConnections(movingTrackGUI.getID());
				for (String tmpAgID : tmpAgIDArrayList) {
					AgentGUI tmpAgentGUI = agentGUIHashMap.get(tmpAgID);
					if (AgentHolder.getNumberOfAgentConnections(tmpAgentGUI.getID()) == 1) {
						//Move the agent GUI 
						tmpAgentGUI.translate(xTrans, yTrans, panelBounds);
					}
				}
			}
			
			//Enable dragging to scroll view
			Rectangle viewRect = new Rectangle(movingTrackGUI.getX(), movingTrackGUI.getY(), movingTrackGUI.getWidth() + 1, movingTrackGUI.getHeight() + 1);
			scrollRectToVisible(viewRect);
			this.repaint();
			return;//Have processed the event
		}
		
		//Handle moving connection
		if(tempConnectionLine != null){
			tempConnectionLine.setLine(tempConnectionLine.getP1(), new Point2D.Double(xPos, yPos));
			repaint();
		}
	}

	public void mouseMoved(MouseEvent e){
	}


	//-------------------------------------------------------------------------------------
	//----------                   Action Performed Method                      -----------
	//-------------------------------------------------------------------------------------
	public void actionPerformed(ActionEvent e){
		//DELETE A CONNECTION
		if(e.getSource() == deleteConnectionMenuItem){
			if(selectedConnection_AgentID == null || selectedConnection_TrackID == null){//Run a safety check
				MsgHandler.error("SELECTED CONNECTION AGENT UID OR SELECTED TRACK UID UNEXPECTEDLY NULL!");
				return;
			}
			//Delete the connection
			AgentHolder.deleteConnection(selectedConnection_AgentID, selectedConnection_TrackID);
		}
		
		//ENABLE AN AGENT
		else if (e.getSource() == enableAgentMenuItem && selectedAgentGUI != null){
			selectedAgentGUI.setEnabled(true);
		}
		
		//DISABLE AN AGENT
		else if (e.getSource() == disableAgentMenuItem && selectedAgentGUI != null){
			selectedAgentGUI.setEnabled(false);
		}
		repaint();
	}


	/** Returns a string describing the agents in XML format.
	 	Agents are stored in the correct order so that they will be loaded in the same order. */
	public String getAgentXML(String indent){
		String agentStr = indent + "<agents>";
		ArrayList<String> agentOrderList = AgentHolder.getAgentOrderList();
		for(String agentID: agentOrderList){
			agentStr += agentGUIHashMap.get(agentID).getXML(indent + "\t");
		}
		agentStr += indent + "</agents>";
		return agentStr;
	}
	
	
	/** Loads agents from the supplied XML string */
	public void loadAgentsFromXML(String xmlStr){
		//Extract the agent XML strings, which will be used to configure the agents
		while(xmlStr.indexOf("</agent>") != -1){
			String agentStr = xmlStr.substring(xmlStr.indexOf("<agent>"), xmlStr.indexOf("</agent>") + 8);
			addAgent(agentStr);
			xmlStr = xmlStr.substring(xmlStr.indexOf("</agent>") + 8, xmlStr.length());
		}
	}

	
	/** Returns a string with all of the tracks' information in XML format */
	public String getTrackXML(String indent){
		String trackStr = indent + "<tracks>";
		for(TrackGUI trackGUI : trackGUIHashMap.values()){
			trackStr += trackGUI.getXML(indent + "\t ");
		}
		trackStr += indent + "</tracks>";  
		return trackStr;
	}

	
	/** Loads the track information from the XML string */
	public void loadTracksFromXML(String xmlStr){
		//Extract the track XML strings, which will be used to configure the tracks
		while(xmlStr.indexOf("</track>") != -1){
			String trackStr = xmlStr.substring(xmlStr.indexOf("<track>"), xmlStr.indexOf("</track>") + 8);
			TrackGUI newTrackGUI = new TrackGUI(trackStr, mainFrame);
			addTrack(newTrackGUI);
			xmlStr = xmlStr.substring(xmlStr.indexOf("</track>") + 8, xmlStr.length());
		}
	}


	public void resetActionPerformed() {
		this.reset();
	}


	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}


	public void setAgentClassLoader_2(AgentClassLoader_2 agentClassLoader_2) {
		this.agentClassLoader_2 = agentClassLoader_2;
	}
	

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

}

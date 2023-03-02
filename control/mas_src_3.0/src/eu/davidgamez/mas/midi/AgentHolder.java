package eu.davidgamez.mas.midi;

import java.util.ArrayList;
import java.util.Collection;
import java.rmi.server.UID;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.exception.MASAgentException;
import eu.davidgamez.mas.exception.MASException;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentGUI;
import eu.davidgamez.mas.gui.MsgHandler;

public class AgentHolder {
	/** Holds all of the agents.
	    The key is the agent id; the value is the agent class. */
	private static HashMap<String, Agent> agentMap = new HashMap<String, Agent>();
	
	private static ArrayList<String> agentOrderList = new ArrayList<String>();
	
	/** Holds the connections between each track and each agent. 
	    The key is the track id; the value is the map of agent ids. */
	private static HashMap<String, HashMap<String, Boolean> > trackAgentConnections = new HashMap<String, HashMap<String, Boolean> >();
	
	/** Holds the link between each agentGUI and the list of connected tracks.
	 	The key is the agent id; the value is a map of track ids. */
	private static HashMap<String, HashMap<String, Boolean> > agentTrackConnections = new HashMap<String, HashMap<String, Boolean> >();

	
	/** Adds a new connection between an agent and a track */
	public static void addConnection(String agentID, String trackID) throws MASException {
		//Check that ids are valid
		if(!AgentHolder.contains(agentID))
			throw new MASException("Failed to add connection. Agent ID does not exist: " + agentID);
		if(!TrackHolder.contains(trackID))
			throw new MASException("Failed to add connection. Track ID does not exist: " + agentID);
		
		//Store connections
		agentTrackConnections.get(agentID).put(trackID, true);
		trackAgentConnections.get(trackID).put(agentID, true);
		
		//Add connection to agent
		agentMap.get(agentID).addTrack(TrackHolder.getTrack(trackID));
		
		//Should be possible to play track if there is at least one connection
		Globals.setReadyToPlay(true);
	}
	
	
	/** Returns true if agent with the specified ID is stored by this class */
	public static boolean contains(String agentID){
		return agentMap.containsKey(agentID);
	}
	
	
	public synchronized static Collection<Agent> getAgents(){
		return agentMap.values();
	}
	
	public synchronized static HashMap<String, Agent> getAgentMap(){
		return agentMap;
	}
	
	
	public synchronized static void addAgent(Agent newAgent){
		agentMap.put(newAgent.getID(), newAgent);
		agentOrderList.add(newAgent.getID());
		agentTrackConnections.put(newAgent.getID(), new HashMap<String, Boolean>());
	}
	
	
	public synchronized static void deleteAgent(Agent agent) throws MASException {
		deleteAgent(agent.getID());
	}
	
	
	public synchronized static void deleteConnection(String agentID, String trackID){
		//Remove connection from track connections
		trackAgentConnections.get(trackID).remove(agentID);
		
		//Remove connection from agent connections
		agentTrackConnections.get(agentID).remove(trackID);
		
		//Remove connection from agent
		agentMap.get(agentID).deleteTrack(trackID);
		
		//Check to see if it is still possible to play system
		checkReadyToPlay();
	}
	
	public synchronized static void deleteAgent(String agentID) throws MASAgentException{
		if(!agentMap.containsKey(agentID))
			throw new MASAgentException("Attempting to remove agent which cannot be found.");
		
		//Remove agent from map and remove connections to and from this agent
		agentMap.remove(agentID);
		deleteAgentConnections(agentID);
		
		//Remove agent from agent order list
		int tmpIndex = 0, deleteIndex = -1;
		for(String tmpAgentID : agentOrderList){
			if(tmpAgentID.equals(agentID)){
				deleteIndex = tmpIndex;
				break;
			}
			++tmpIndex;
		}
		if(deleteIndex == -1)
			throw new MASAgentException("Trying to delete an agent from agent order list with an ID that cannot be found");
		agentOrderList.remove(deleteIndex);
		
		//Check ready to play status
		checkReadyToPlay();
	}
	
	private synchronized static void deleteAgentConnections(String agentID) throws MASAgentException {
		if(!agentTrackConnections.containsKey(agentID))
			throw new MASAgentException("Attempting to remove connections from an agent which cannot be found.");
		
		//Remove information about connections this agent makes to tracks
		agentTrackConnections.remove(agentID);
		
		//Work through all of the track connection information to remove connections to this agent
		for(HashMap<String, Boolean> tmpAgHashMap : trackAgentConnections.values()){
			tmpAgHashMap.remove(agentID);
		}
		
		//Check ready to play status
		checkReadyToPlay();
	}
	
	public synchronized static ArrayList<String> getAgentOrderList(){
		return agentOrderList;
	}
	
	public synchronized static int getNumberOfAgentConnections(String agentID){
		return agentTrackConnections.get(agentID).size();
	}
	
	
	/** Returns a list of the agents that the specified track is connected to  */
	public synchronized static Collection<String> getTrackConnections(String trackID){
		return trackAgentConnections.get(trackID).keySet();
	}
	
	
	public synchronized static boolean isConnected(String agentID){
		if(agentTrackConnections.get(agentID).isEmpty())
			return false;
		return true;
	}
	
	
	/** Returns a string describing the connections */
	public synchronized static String getConnectionXML(String indent){
		String tmpXMLStr = indent + "<connections>";
	
		//Work through the agents
		for(String agentID : agentOrderList){

			//Work through the tracks
			for(String trackID : agentTrackConnections.get(agentID).keySet()){
				 tmpXMLStr += indent + "\t<connection>";
				 tmpXMLStr += indent + "\t\t<agent_id>" + agentID + "</agent_id><track_id>" + trackID + "</track_id>";
				 tmpXMLStr += indent + "\t</connection>";
			 }
		}
		
		//Finish off and return string
		tmpXMLStr += indent + "</connections>";
		return tmpXMLStr;
	}
	
	
	/** Loads up connections from the supplied XML string */
	public synchronized static void loadConnectionsFromXML(String xmlStr){
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			
			//Work through all the connections
			NodeList conNodeList = xmlDoc.getElementsByTagName("connection");
			for(int i=0; i<conNodeList.getLength(); ++i){
				
				//Get the agent and track id strings
				NodeList tmpChildNodeList =conNodeList.item(i).getChildNodes();
				String agIdStr = null, trkIdStr = null;
				for(int j=0; j<tmpChildNodeList.getLength(); ++j){
					if(tmpChildNodeList.item(j).getNodeType() == Node.ELEMENT_NODE && tmpChildNodeList.item(j).getNodeName().equals("agent_id")){
						agIdStr = tmpChildNodeList.item(j).getFirstChild().getNodeValue();
					}
					if(tmpChildNodeList.item(j).getNodeType() == Node.ELEMENT_NODE && tmpChildNodeList.item(j).getNodeName().equals("track_id")){
						trkIdStr = tmpChildNodeList.item(j).getFirstChild().getNodeValue();
					}
				}
				
				//Add connection
				if(agIdStr == null || trkIdStr == null)
					throw new MASXmlException("Agent or track id cannot be found");
				addConnection(agIdStr, trkIdStr);
			}
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}
	
	
	public synchronized static void newTrack(String trackID){
		trackAgentConnections.put(trackID, new HashMap<String, Boolean>());
	}
	
	/** Changes the order with which agents are invoked by the agent handler */
	public synchronized static void changeAgentOrder(int fromIndex, int toIndex) throws MASAgentException{
		//Check indexes are in range
		if(fromIndex >= agentOrderList.size() || toIndex >= agentOrderList.size())
			throw new MASAgentException("Trying to change agent order with indexes that are out of bounds.");
		
		//Swap the from and to indexes
		String tmpAgID = agentOrderList.get(toIndex);
		agentOrderList.set(toIndex, agentOrderList.get(fromIndex));
		agentOrderList.set(fromIndex, tmpAgID);
		
		//Project has changed
		Globals.setProjectSaved(false);
	}
	
	
	public synchronized static void deleteTrackConnections(String trackID) throws MASException{
		if(!trackAgentConnections.containsKey(trackID))
			throw new MASException("Attempting to remove track connections to a track which cannot be found.");
		
		//Remove this track from all agents that connect to it
		for(String tmpAgID : trackAgentConnections.get(trackID).keySet()){
			agentMap.get(tmpAgID).deleteTrack(trackID);
		}
		
		//Remove the list of connections from this track to agents
		trackAgentConnections.remove(trackID);
		
		//Work through all of the agents and remove all connections to this track
		for(HashMap<String, Boolean> tmpTrkHashMap : agentTrackConnections.values()){
			tmpTrkHashMap.remove(trackID);
		}
		
		//Check ready to play status
		checkReadyToPlay();
	}

	
	public synchronized static void reset(){
		agentMap.clear();
		trackAgentConnections.clear();
		agentTrackConnections.clear();
		agentOrderList.clear();
		Globals.setReadyToPlay(false);
	}

	
	/** Invokes abstract reset method on all agents to put them in a state ready to play */
	public synchronized static void resetAgents(){
		for(Agent agent: agentMap.values()){
			agent.reset();
		}
	}
	
	public synchronized static int size(){
		return agentMap.size();
	}
	
	
	/** Checks whether system is capable of playing a track or not. 
	 * If there are no connected agents it will not be possible to play anything.
	 */
	private static void checkReadyToPlay(){
		if(trackAgentConnections.isEmpty())
			Globals.setReadyToPlay(false);
		else
			Globals.setReadyToPlay(true);
	}

}

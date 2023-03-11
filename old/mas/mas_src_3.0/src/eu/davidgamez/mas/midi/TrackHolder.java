package eu.davidgamez.mas.midi;

//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import eu.davidgamez.mas.exception.MASException;


public class TrackHolder {
	/** Holds all of the tracks 
	 	The key is the track id; the value is the track class. */
	private static HashMap<String, Track> trackMap = new HashMap<String, Track>();
	
	public synchronized static Collection<Track> getTracks(){
		return trackMap.values();
	}
	
	public synchronized static void addTrack(Track track){	
		trackMap.put(track.getID(), track);
		AgentHolder.newTrack(track.getID());
	}
	
	public synchronized static boolean contains(String trackID){
		return trackMap.containsKey(trackID);
	}
	
	public synchronized static Track getTrack(String trackID) throws MASException{
		if(!trackMap.containsKey(trackID))
			throw new MASException("Track cannot be found.");
		return trackMap.get(trackID);
	}
	
	
	public synchronized static void deleteTrack(Track track) throws MASException {
		if(!trackMap.containsKey(track.getID()))
			throw new MASException("Attempting to remove track which cannot be found.");
		trackMap.remove(track.getID());
		AgentHolder.deleteTrackConnections(track.getID());
	}
	
	public synchronized static void deleteTrack(String trackID) throws MASException{
		if(!trackMap.containsKey(trackID))
			throw new MASException("Attempting to remove track which cannot be found.");
		trackMap.remove(trackID);
		AgentHolder.deleteTrackConnections(trackID);
	}
	
	public synchronized static String getTrackXML(String indent){
		return "";
	}
	
	public synchronized static void loadTracksFromXML(String trackString){
	}
	
	public synchronized static void reset(){
		trackMap.clear();
	}
	
	public synchronized static void resetTracks(){
		for(Track track: trackMap.values()){
			track.reset();
		}
	}
	
	public synchronized static int size(){
		return trackMap.size();
	}

}

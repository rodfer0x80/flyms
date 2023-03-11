package eu.davidgamez.mas.agents.test.simplenotes.midi;

//Java imports
import java.util.ArrayList;
import java.util.TreeMap;
import javax.sound.midi.ShortMessage;

//MAS imports
import eu.davidgamez.mas.agents.simplenotes.midi.SimpleNotes;
import eu.davidgamez.mas.midi.Buffer;
import eu.davidgamez.mas.midi.Track;

//Other imports
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSimpleNotes {
	
	@Test public void testUpdateTracks(){
		System.out.println("TESTING UPDATE TRACKS");
		
		//Create track and add it to the simple notes class being tested
		Track testTrack = new Track();
		SimpleNotes simpleNotes = new SimpleNotes();
		simpleNotes.addTrack(testTrack);
		
		Buffer.init();
		
		//Call updateTracks twice on the agent
		try{
			//Invoke agent to update tracks
			simpleNotes.updateTracks(0, 1920);
			TreeMap<Long, ArrayList<ShortMessage>> msgMap = testTrack.getMidiMessages();
			
			//Check correct MIDI data has been inserted
			assertEquals("Incorrect number of time step points", 4, msgMap.size());
			assertEquals("Incorrect number of messages at position 0.", 1, msgMap.get(new Long(0)).size()); 
			assertEquals("Command at time step 0 incorrect", 144, msgMap.get(new Long(0)).get(0).getCommand());
			
			assertEquals("Incorrect number of messages at position 480.", 1, msgMap.get(new Long(480)).size()); 
			assertEquals("Command at time step 480 incorrect", 128, msgMap.get(new Long(480)).get(0).getCommand());
			
			assertEquals("Incorrect number of messages at position 960.", 1, msgMap.get(new Long(960)).size()); 
			assertEquals("Command at time step 960 incorrect", 144, msgMap.get(new Long(960)).get(0).getCommand());
			
			assertEquals("Incorrect number of messages at position 1440.", 1, msgMap.get(new Long(1440)).size()); 
			assertEquals("Command at time step 1440 incorrect", 128, msgMap.get(new Long(1440)).get(0).getCommand());
					
			//Advance buffer and invoke agent to update tracks
			Buffer.advanceLoadBuffer();
			simpleNotes.updateTracks(1920, 3840);//Should be two notes
			msgMap = testTrack.getMidiMessages();
			
			//Check correct MIDI data has been inserted
			assertEquals("Incorrect number of time step points", 4, msgMap.size());
			assertEquals("Incorrect number of messages at position 1920.", 1, msgMap.get(new Long(1920)).size()); 
			assertEquals("Command at time step 1920 incorrect", 144, msgMap.get(new Long(1920)).get(0).getCommand());
			
			assertEquals("Incorrect number of messages at position 480.", 1, msgMap.get(new Long(2400)).size()); 
			assertEquals("Command at time step 480 incorrect", 128, msgMap.get(new Long(2400)).get(0).getCommand());
			
			assertEquals("Incorrect number of messages at position 960.", 1, msgMap.get(new Long(2880)).size()); 
			assertEquals("Command at time step 960 incorrect", 144, msgMap.get(new Long(2880)).get(0).getCommand());
			
			assertEquals("Incorrect number of messages at position 3360.", 1, msgMap.get(new Long(3360)).size()); 
			assertEquals("Command at time step 3360 incorrect", 128, msgMap.get(new Long(3360)).get(0).getCommand());
			
			//printMidiMessages(testTrack.getMidiMessages());

		}
		catch(Exception ex){
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	

	}
	
	private void printMidiMessages(TreeMap<Long, ArrayList<ShortMessage>> msgMap){
		for(Long tmpTimeStamp : msgMap.keySet()){
			for(ShortMessage shortMsg : msgMap.get(tmpTimeStamp)){
				System.out.print("TIME: " + tmpTimeStamp + " CHANNEL: " + shortMsg.getChannel());
				System.out.print(" COMMAND: " + shortMsg.getCommand() + " DATA1: " + shortMsg.getData1());
				System.out.println(" DATA2: " + shortMsg.getData2());
			}
		}
	}
}

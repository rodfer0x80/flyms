package eu.davidgamez.mas.test.midi;

//Java imports
import java.util.ArrayList;
import java.util.TreeMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

//MAS imports
import eu.davidgamez.mas.midi.Buffer;

//Other imports
import org.junit.Test;
import static org.junit.Assert.*;


public class TestBuffer {
	
	@Test public void testAdvanceLoadBuffer(){
		Buffer.setLength_beats(2);
		Buffer.init();
		assertEquals("Load buffer count is incorrect.", 0, Buffer.getLoadBufferCount());
		assertEquals("Load buffer start at 0 is incorrect.", 0, Buffer.getLoadStart_ticks());
		assertEquals("Load buffer start at 0 is incorrect.", 1920, Buffer.getLoadEnd_ticks());

		Buffer.advanceLoadBuffer();
		assertEquals("Load buffer count is incorrect.", 1, Buffer.getLoadBufferCount());
		assertEquals("Load buffer start at 1 is incorrect.", 1920, Buffer.getLoadStart_ticks());
		assertEquals("Load buffer start at 1 is incorrect.", 3840, Buffer.getLoadEnd_ticks());
	}

	
	@Test public void testAddMidiMessages(){
		Buffer.setLength_beats(2);
		Buffer.init();
		try{
			ArrayList<ShortMessage> tmpArrayList = new ArrayList<ShortMessage>();
			tmpArrayList.add(getShortMessage(144, 3, 2, 7));
			TreeMap<Long, ArrayList<ShortMessage>> midiMessageMap = new TreeMap<Long, ArrayList<ShortMessage>>();
			
			//Add first messages at time 0
			midiMessageMap.put(new Long(0), tmpArrayList);
			Buffer.addMidiMessages(midiMessageMap);
			
			//Add second messages twice at time 50
			midiMessageMap.clear();
			midiMessageMap.put(new Long(50), tmpArrayList);
			Buffer.addMidiMessages(midiMessageMap);
			
			Buffer.addMidiMessages(midiMessageMap);
			
			//Play buffer index is same as load buffer index, so contents should match what we added
			TreeMap<Long, ArrayList<ShortMessage>> playTreeMap = Buffer.getPlayBuffer();
			assertEquals("Play buffer at 0 has wrong number of messages.", 1, playTreeMap.get(new Long(0)).size());
			assertEquals("Play buffer at 50 has wrong number of messages.", 2, playTreeMap.get(new Long(50)).size());
		}
		catch(Exception ex){
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}



	private ShortMessage getShortMessage(int command, int channel, int data1, int data2) throws InvalidMidiDataException {
		ShortMessage tmpShortMsg = new ShortMessage();
		tmpShortMsg.setMessage(command, channel, data1, data2);
		return tmpShortMsg;
	}

}
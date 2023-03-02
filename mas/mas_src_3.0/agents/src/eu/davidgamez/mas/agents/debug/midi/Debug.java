package eu.davidgamez.mas.agents.debug.midi;

//MAS imports
import javax.sound.midi.InvalidMidiDataException;

import eu.davidgamez.mas.midi.Agent;

public class Debug extends Agent {

	public Debug() {
		super("Debug", "Debug", "Debug");
	}

	@Override
	protected void reset() {
		System.out.println("Resetting agent");
	}

	@Override
	protected boolean updateTracks(long bufferStart_ppq, long bufferEnd_ppq) throws InvalidMidiDataException {
		long currentTime_ns = System.nanoTime();
		System.out.println("Time(ns): " + currentTime_ns + "Updating debug agent: bufferStart_ppq=" + bufferStart_ppq + "; bufferEnd_ppq=" + bufferEnd_ppq);
		return true;
	}
	
	//Save agent data
	public String getXML(String indent){
		String tmpStr = indent + "<midi_agent>";
		tmpStr += super.getXML(indent + "\t");
		tmpStr += indent + "</midi_agent>";
		return tmpStr;
	}


}

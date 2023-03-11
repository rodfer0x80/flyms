package eu.davidgamez.mas.event;

//Java imports
import javax.sound.midi.ShortMessage;


public interface NoteEventListener {
	
	void noteEventOcccurred(ShortMessage shortMessage);

	
}

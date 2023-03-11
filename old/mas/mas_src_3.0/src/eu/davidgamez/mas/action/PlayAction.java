package eu.davidgamez.mas.action;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.TransportListener;
import eu.davidgamez.mas.gui.MASLookAndFeel;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Buffer;
import eu.davidgamez.mas.midi.MASSequencer;
import eu.davidgamez.mas.midi.AgentHandler;
import eu.davidgamez.mas.midi.MIDIDeviceManager;


public class PlayAction extends AbstractAction implements TransportListener {
    
    public PlayAction(){
    	this.putValue(NAME, "Play");
    	this.putValue(Action.SMALL_ICON, MASLookAndFeel.getPlayOffIcon());
    	EventRouter.addTransportListener(this);
    }

	/* Inherited from AbstractAction */
	public void actionPerformed(ActionEvent e){
	    if (Globals.isPlaying())
	        return;
	    
		//Check receivers have been set
		if( ( MIDIDeviceManager.getReceiverArrayList() == null || MIDIDeviceManager.getReceiverArrayList().isEmpty() )){
			MsgHandler.error("No MIDI receivers set, cannot play without receivers");
			return;
		}
		
		//Check that we are now ready to play
	    if (Globals.isReadyToPlay()) {
	    	//Reset the buffer
	    	Buffer.reset();
	    	
	        //Signal to other classes that play has started
	        EventRouter.playActionPerformed();
	        Globals.setPlaying(true);
	        
	        //Change icon to play icon
	    	this.putValue(Action.SMALL_ICON, MASLookAndFeel.getPlayOnIcon());
	    }
	    else{
	    	MsgHandler.error("System is not in ready to play state, probably because no agents connected.");
	    }
	}

	@Override
	public void killNotesActionPerformed() {
	}

	@Override
	public void playActionPerformed() {
	}

	@Override
	public void stopActionPerformed() {
    	this.putValue(Action.SMALL_ICON, MASLookAndFeel.getPlayOffIcon());
	}

}

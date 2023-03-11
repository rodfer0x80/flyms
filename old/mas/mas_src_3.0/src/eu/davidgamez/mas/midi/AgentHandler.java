package eu.davidgamez.mas.midi;

//Java imports
import javax.sound.midi.*;
import java.util.*;
import java.io.BufferedWriter;

//MAS imports
import eu.davidgamez.mas.*;
import eu.davidgamez.mas.event.BufferListener;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.TempoEvent;
import eu.davidgamez.mas.event.TransportListener;
import eu.davidgamez.mas.event.TempoListener;
import eu.davidgamez.mas.gui.MsgHandler;


/**
 * ----------------------------- AgentHandler ----------------------------------
 * Holds all the MIDI agents and calls them in sequence at each buffer update so
 * that they can add notes or messages to the tracks.
 * -----------------------------------------------------------------------------
 */
public class AgentHandler implements TransportListener, Constants {
	//======================  INJECTED VARIABLES  =======================
	private MASSequencer masSequencer;
	
	//======================  ORDINARY VARIABLES  =======================
	private AgentPlayer agentPlayer = new AgentPlayer(null);

	
	/** Constructor */
	public AgentHandler() {
		// Make this listen to transport and tempo change events from the sequencer
		EventRouter.addTransportListener(this);
	}

	
	/*-------------------------------------------------------------------*/
	/*--------                   PUBLIC METHODS                  --------*/
	/*-------------------------------------------------------------------*/

	/** Part of the transport listener interface. Not used by this class */
	public void killNotesActionPerformed(){
	}
	
	
	/** Starts the system playing if it is not playing already */
	public void playActionPerformed() {
		if(!agentPlayer.isPlaying()){
			agentPlayer = new AgentPlayer(masSequencer);
			agentPlayer.start();
		}
			
	}
	
	
	/** Part of the transport listener interface. Stops the play */
	public void stopActionPerformed(){
		if(!agentPlayer.isPlaying())
			return;
		
		agentPlayer.stopThread();
		try {
			agentPlayer.join();
		}
		catch (Exception ex) {
			MsgHandler.critical(ex.getMessage());
		}
		if(agentPlayer.isError())
			MsgHandler.critical(agentPlayer.getErrorMessage());
	}
	
	
	/** Resets this class to initial state */
	public void reset() {
		if(agentPlayer.isPlaying()){
			MsgHandler.critical("Should not reset agent player when it is playing!");
			return;
		}
		agentPlayer.reset();
	}


	public void setMasSequencer(MASSequencer masSequencer) {
		this.masSequencer = masSequencer;
	}
	

}

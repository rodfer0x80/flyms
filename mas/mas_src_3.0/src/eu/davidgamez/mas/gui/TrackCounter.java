package eu.davidgamez.mas.gui;

//Java imports
import javax.swing.*;
import java.awt.*;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.event.BufferListener;
import eu.davidgamez.mas.event.ResetListener;
import eu.davidgamez.mas.event.TransportListener;
import eu.davidgamez.mas.midi.MASSequencer;
import eu.davidgamez.mas.event.EventRouter;


/** ----------------------------  TrackCounter ----------------------------------
    Displays how many beats have elapsed since play was pressed.
   -----------------------------------------------------------------------------*/
public class TrackCounter extends JTextField implements TransportListener, BufferListener, ResetListener{

	/** Constructor */
	public TrackCounter() {
		super();
		this.setColumns(5);
		this.setText("0");
		this.setEditable(false);
		this.setBackground(Color.black);
		this.setForeground(Color.yellow);
		this.setMaximumSize(new Dimension(30, 30));
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setColumns(5);
		this.setFocusable(false);
        
        //Tool tip
        this.setToolTipText("Track counter. The current beat of the track.");

		//Listen for transport and reset events
		EventRouter.addTransportListener(this);
		EventRouter.addResetListener(this);
		EventRouter.addBufferListener(this);
	}


	/** Unused transport listener method */
	public void killNotesActionPerformed() {
	}
	

	/** Unused transport listener method */
	public void playActionPerformed() {
	}

	
	/** Resets track counter to zero when track stops */
	public void stopActionPerformed() {
		this.setText("0");
	}


	/** Method from ResetListener */
	public void resetActionPerformed() {
		this.setText("0");
	}

	
	/** Unused BufferListener method */
	public void endLoadBufferAdvanceOccurred(long bufferCount) {
	}
	
	
	/** Unused BufferListener method */
	public void startLoadBufferAdvanceOccurred(long bufferCount) {
	}

	
	/** Unused BufferListener method */
	public void playBufferAdvanceOccurred(long bufferCount) {		
	}


	/** BufferListener method */
	public void trackAdvanceOccurred(long beatCount) {
		this.setText(Long.toString(beatCount));
	}

}


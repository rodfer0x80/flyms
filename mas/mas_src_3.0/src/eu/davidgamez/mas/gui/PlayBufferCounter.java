package eu.davidgamez.mas.gui;

//Java imports
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JTextField;

//MAS imports
import eu.davidgamez.mas.event.BufferListener;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.ResetListener;
import eu.davidgamez.mas.event.TransportListener;
import eu.davidgamez.mas.midi.AgentHandler;


public class PlayBufferCounter extends JTextField implements BufferListener, ResetListener, TransportListener{
	
	/** Constructor */
	public PlayBufferCounter() {
		super();

		// Set up the look of the counter
		this.setText("0");
		this.setEditable(false);
		this.setBackground(Color.black);
		this.setForeground(Color.yellow);
		this.setMaximumSize(new Dimension(30, 30));
		this.setHorizontalAlignment(SwingConstants.CENTER);
        this.setColumns(5);
        this.setFocusable(false);
        
        //Tool tip
        this.setToolTipText("Play buffer counter. The number of the buffer that is currently being played.");

		// Listen for buffer events from the agent handler
		EventRouter.addBufferListener(this);
		
		//Listen for reset events
		EventRouter.addResetListener(this);
		
		//Listen for transport events
		EventRouter.addTransportListener(this);
	}
	
	@Override
	public void killNotesActionPerformed() {
	}
	
	
	/** Unused transport listener method */
	public void playActionPerformed() {
	}

	
	/** Resets track counter to zero when track stops */
	public void stopActionPerformed() {
		this.setText("0");
	}

	public void resetActionPerformed() {
		this.setText("0");
	}


	public void endLoadBufferAdvanceOccurred(long bufferCount) {
	}
	
	
	public void startLoadBufferAdvanceOccurred(long bufferCount) {
	}


	public void playBufferAdvanceOccurred(long bufferCount) {
		this.setText(Long.toString(bufferCount));
	}


	public void trackAdvanceOccurred(long beatCount) {
	}
	
}

package eu.davidgamez.mas.gui;

//Java imports
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.ResetListener;
import eu.davidgamez.mas.event.TempoListener;
import eu.davidgamez.mas.event.TempoEvent;
import eu.davidgamez.mas.gui.dialog.TempoDialog;
import eu.davidgamez.mas.midi.MASSequencer;


/**  ----------------------------  TempoTextField --------------------------------
    Displays the current tempo and launches a dialog to adjust the tempo when
    pressed.
   -----------------------------------------------------------------------------*/
public class TempoTextField extends JTextField implements MouseListener, TempoListener {
	//========================  INJECTED VARIABLES  ===========================
	private TempoDialog tempoDialog;
	
	/** Constructor **/
	public TempoTextField() {
		super();
		this.setText(String.valueOf(Globals.getTempo()));
		this.setEditable(false);
		this.addMouseListener(this);
		this.setBackground(MASLookAndFeel.getTempoTextFieldBackground());
		this.setForeground(MASLookAndFeel.getTempoTextFieldForeground());
		this.setMaximumSize(new Dimension(30, 30));
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.setColumns(5);
		EventRouter.addTempoListener(this);
	}
	

	/** Shows a dialog that enables the tempo to be changed. */
	public void mouseClicked(MouseEvent e) {
		tempoDialog.showDialog();
	}
	
	/** Called when a tempo change occurs */
	public void tempoEventOccurred(TempoEvent tempoEvent){
		this.setText(String.valueOf(tempoEvent.getTempo()));
	}

	
	// Unused mouse listener methods.
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mousePressed(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}


	public void tempoActionPerformed(TempoEvent tempoEvent) {
		this.setText(String.valueOf(tempoEvent.getTempo()));	
	}

	public void setTempoDialog(TempoDialog tempoDialog) {
		this.tempoDialog = tempoDialog;
	}
	
}

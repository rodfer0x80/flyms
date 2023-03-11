package eu.davidgamez.mas.gui.dialog;

//Java imports
import java.awt.event.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;

//MAS imports
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.MASSequencer;
import eu.davidgamez.mas.*;


/**
 * ----------------------- MIDI Synchronisation Dialog -------------------------
 * Allows user to select synchronsation options for the MIDI playback.
 * -----------------------------------------------------------------------------
 */
public class SynchronizationDialog extends JDialog implements ActionListener, Constants {

	private JComboBox slaveCombo, masterCombo;
	private JButton okButton, cancelButton;
	private String[] masterSyncNames = { "Internal clock", "MIDI clock" };
	private String[] slaveSyncNames = { "No sync", "MIDI clock" };

	
	/** Constructor */
	public SynchronizationDialog(MainFrame mainFrame) {
		super(mainFrame);

		Box verticalBox = Box.createVerticalBox();
		verticalBox.add(Box.createVerticalStrut(10));

		// Create and add master sync box
		Box masterBox = Box.createHorizontalBox();
		masterBox.add(Box.createHorizontalStrut(10));
		masterBox.add(new JLabel("Master synchronisation"));
		masterBox.add(Box.createHorizontalStrut(10));
		masterCombo = new JComboBox(masterSyncNames);
		masterCombo.setMaximumSize(new Dimension(100, 20));
		masterCombo.setMinimumSize(new Dimension(100, 20));
		masterCombo.setPreferredSize(new Dimension(100, 20));
		masterBox.add(masterCombo);
		masterBox.add(Box.createHorizontalStrut(10));
		masterBox.add(Box.createHorizontalGlue());
		verticalBox.add(masterBox);
		verticalBox.add(Box.createVerticalStrut(10));

		// Create and add slave sync box
		Box slaveBox = Box.createHorizontalBox();
		slaveBox.add(Box.createHorizontalStrut(10));
		slaveBox.add(new JLabel("Slave synchronisation"));
		slaveBox.add(Box.createHorizontalStrut(10));
		slaveCombo = new JComboBox(slaveSyncNames);
		slaveCombo.setMaximumSize(new Dimension(100, 20));
		slaveCombo.setMinimumSize(new Dimension(100, 20));
		slaveCombo.setPreferredSize(new Dimension(100, 20));
		slaveBox.add(slaveCombo);
		slaveBox.add(Box.createHorizontalStrut(10));
		slaveBox.add(Box.createHorizontalGlue());
		verticalBox.add(slaveBox);
		verticalBox.add(Box.createVerticalStrut(10));

		// Set up buttons
		JPanel buttonPane = new JPanel();
		buttonPane.add(okButton = createButton("Ok"));
		buttonPane.add(cancelButton = createButton("Cancel"));

		// Finish off
		getContentPane().add(verticalBox, BorderLayout.CENTER);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		this.pack();
		this.setLocation(400, 300);
	}
	
	
	/** Displays the dialog to the user with up to date information */
	public void showDialog(){
		// Set master selection correctly
		if (Globals.getMasterSyncMode() == INTERNAL_CLOCK)
			masterCombo.setSelectedIndex(0);
		else if (Globals.getMasterSyncMode() == MIDI_SYNC)
			masterCombo.setSelectedIndex(1);
		
		// Set slave selection correctly
		if (Globals.getSlaveSyncMode() == NO_SYNC)
			slaveCombo.setSelectedIndex(0);
		else if (Globals.getSlaveSyncMode() == MIDI_SYNC)
			slaveCombo.setSelectedIndex(1);
		
		this.setVisible(true);
	}

	
	/* Inherited from abstract action */
	public void actionPerformed(ActionEvent ev) {
		//Ok
		if (ev.getSource() == okButton) {
			switch (masterCombo.getSelectedIndex()) {
			case (0):
				Globals.setMasterSyncMode(INTERNAL_CLOCK);
				break;
			case (1):
				Globals.setMasterSyncMode(MIDI_SYNC);
				break;
			default:
				MsgHandler.error("MASTER SYNCHRONISATION MODE NOT RECOGNISED");
			}
			switch (slaveCombo.getSelectedIndex()) {
			case (0):
				Globals.setSlaveSyncMode(NO_SYNC);
				break;
			case (1):
				Globals.setSlaveSyncMode(MIDI_SYNC);
				break;
			default:
				MsgHandler.error("SLAVE SYNCHRONISATION MODE NOT RECOGNISED");
			}
			this.setVisible(false);
		}
		//Cancel
		else if (ev.getSource() == cancelButton) {
			this.setVisible(false);
		}
	}

	/** Builds button */
	private JButton createButton(String label) {
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(80, 20));
		button.addActionListener(this);
		return button;
	}
}

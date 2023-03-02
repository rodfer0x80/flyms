package eu.davidgamez.mas.gui.dialog;

/* ----------------------------  ControllerDialog ------------------------------
    Selects a controller number.
 -----------------------------------------------------------------------------
 */

//Java imports
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.sound.midi.*;

//MAS imports
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.gui.ValidatedIntTextField;
import eu.davidgamez.mas.midi.*;

public class ControllerDialog extends JDialog implements ActionListener {

	//Buttons
	private JButton ok, apply, cancel;

	//Field to enter the controller number
	private ValidatedIntTextField controllerTextField;
	private JLabel controllerLabel;

	//Enables MIDI control
	private JCheckBox midiEnableCB;

	//Defaults
	private int defaultControllerNumber = 80;
	private int currentControllerNumber = -1;

	//Listener that will listen for the specified MIDI events
	private ControllerEventListener controllerEventListener;

	public ControllerDialog(ControllerEventListener contEvListener) {
		this.setTitle("MIDI Controller");
		this.setModal(true);

		//Store references
		controllerEventListener = contEvListener;

		//Create vertical box to organise dialog
		Box verticalBox = Box.createVerticalBox();
		verticalBox.add(Box.createVerticalStrut(10));

		//Add check box to decide whether midi controllers are to be used
		Box midiCBBox = Box.createHorizontalBox();
		midiCBBox.add(Box.createHorizontalStrut(10));
		midiEnableCB = new JCheckBox("MIDI control");
		midiEnableCB.addActionListener(this);
		midiCBBox.add(midiEnableCB);
		midiCBBox.add(Box.createHorizontalGlue());
		verticalBox.add(midiCBBox);
		verticalBox.add(Box.createVerticalStrut(10));

		//Add text field to control controller number
		Box controllerBox = Box.createHorizontalBox();
		controllerBox.add(Box.createHorizontalStrut(10));
		controllerLabel = new JLabel("Controller number");
		controllerBox.add(controllerLabel);
		controllerTextField = new ValidatedIntTextField(0, 127);
		controllerTextField.setMaximumSize(new Dimension(50, 20));
		controllerTextField.setMinimumSize(new Dimension(50, 20));
		controllerTextField.setPreferredSize(new Dimension(50, 20));
		controllerBox.add(controllerTextField);
		controllerBox.add(Box.createHorizontalGlue());

		verticalBox.add(controllerBox);
		verticalBox.add(Box.createVerticalStrut(10));

		//Find out if there are any controllers registered for this listener
		int[] conNumbers = MIDIDeviceManager.getMidiInputHandler().getControllerNumbers(controllerEventListener);

		//Should be registered for a maximum of 1 controller event.
		if (conNumbers.length > 1)
			JOptionPane.showMessageDialog(this, "This controller is registered for multiple controllers", "Error", JOptionPane.ERROR_MESSAGE);

		//Already registered to listen for 1 controller
		else if (conNumbers.length == 1) {
			controllerTextField.setEnabled(true);
			controllerLabel.setEnabled(true);
			midiEnableCB.setSelected(true);
			controllerTextField.setText(String.valueOf(conNumbers[0]));
			currentControllerNumber = conNumbers[0];
		}

		//Not currently registered for any controllers.
		else {
			controllerTextField.setEnabled(false);
			controllerLabel.setEnabled(false);
			midiEnableCB.setSelected(false);
			controllerTextField.setText(String.valueOf(defaultControllerNumber));
			currentControllerNumber = defaultControllerNumber;
		}

		//Set up buttons
		JPanel buttonPane = new JPanel();
		buttonPane.add(apply = createButton("Apply"));
		buttonPane.add(ok = createButton("Ok"));
		buttonPane.add(cancel = createButton("Cancel"));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		this.add(verticalBox, BorderLayout.CENTER);
		this.add(buttonPane, BorderLayout.SOUTH);

		this.pack();
	}

	public void showDialog(int xPos, int yPos) {
		this.setLocation(xPos, yPos);
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ok) {
			if (midiEnableCB.isSelected())
				connectMidiDevice();
			else
				disconnectMidiDevice();
			setVisible(false);
		}

		else if (e.getSource() == apply) {
			if (midiEnableCB.isSelected())
				connectMidiDevice();
			else
				disconnectMidiDevice();
		}

		else if (e.getSource() == cancel) {
			setVisible(false);
		}

		else if (e.getSource() == midiEnableCB) {
			if (midiEnableCB.isSelected()) {
				//Enable relevant components
				controllerLabel.setEnabled(true);
				controllerTextField.setEnabled(true);
			} else {
				//Disable relevant components
				controllerLabel.setEnabled(false);
				controllerTextField.setEnabled(false);
			}
		}
	}


	private void connectMidiDevice() {
		//Get the text
		String controllerText = controllerTextField.getText();
		if (!controllerText.equals("")) {
			int newControllerNumber = Integer.parseInt(controllerText);

			//Only update if it has changed
			if ((newControllerNumber != currentControllerNumber) || (newControllerNumber == defaultControllerNumber)) {

				//Check it is in range
				if (newControllerNumber >= 0 && newControllerNumber <= 127) {
					//Remove any previous listeners
					MIDIDeviceManager.getMidiInputHandler().removeControllerEventListener(controllerEventListener);

					//Connect the input handler up to the listener
					MIDIDeviceManager.getMidiInputHandler().addControllerEventListener(controllerEventListener, new int[] {newControllerNumber});

					//Record current midi controller
					currentControllerNumber = newControllerNumber;
				} else { //Controller number is out of range
					JOptionPane.showMessageDialog(this, "Please enter a valid controller number", "Invalid controller number", JOptionPane.ERROR_MESSAGE);
				}
			}
		} 
		else { //Blank entry in text field
			MsgHandler.error("Please enter a valid controller number");
		}
	}


	//Returns true if midi control is enabled.
	public boolean midiControllerEnabled() {
		return midiEnableCB.isSelected();
	}


	//Returns the current MIDI controller number for the controller listener
	public int getMidiController() {
		return currentControllerNumber;
	}


	//Disconnect the input handler from the listener
	private void disconnectMidiDevice() {
		MIDIDeviceManager.getMidiInputHandler().removeControllerEventListener(controllerEventListener);
	}


	private JButton createButton(String label) {
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(70, 20));
		button.addActionListener(this);
		return button;
	}

}

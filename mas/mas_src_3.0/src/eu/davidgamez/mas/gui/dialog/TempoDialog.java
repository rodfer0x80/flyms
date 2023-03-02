package eu.davidgamez.mas.gui.dialog;

//Java imports
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Vector;

//Project imports
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.event.TempoEvent;
import eu.davidgamez.mas.gui.DKnob;
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.ValidatedIntTextField;
import eu.davidgamez.mas.midi.*;
import eu.davidgamez.mas.*;


/* ----------------------------  TempoDialog -----------------------------------
Used to set the tempo.
FIXME: NEED TO SORT OUT THE CONTROLLER LISTENING. NEED TO PASS THE KNOB
DIRECTLY TO THE MIDIINPUTHANDLER AS A CONTROLLEREVENTLISTENER.
-----------------------------------------------------------------------------*/
public class TempoDialog extends JDialog implements ActionListener, ChangeListener, Constants {
	//Buttons
	private JButton ok, apply, cancel;

	//Text field that can only receive input within the specified range
	private ValidatedIntTextField tempoTextInputField = new ValidatedIntTextField(1, 500);

	//Knob to shift the tempo
	DKnob tempoShiftKnob = new DKnob();

	//Label of the amount of tempo shift
	JLabel tempoShiftLabel = new JLabel();

	//Controls the range within which the tempo is shifted.
	JComboBox tempoRangeCombo;

	//Controls whether the tempo shift knob is moved by controller input
	JCheckBox synchToControllerCB;

	//Variables holding the current amount tempo is shifted and the range of the knob
	int tempoShiftAmount = 0;
	float tempoRange = 100;

	//Keep a record of the last control value to avoid funny jumps when changing range
	int lastControlValue = 64;

	//Record the starting tempo when the dialog is launched
	double originalTempo_f = DEFAULT_TEMPO_BPM;


	/** Constructor */
	public TempoDialog(MainFrame mainFrame) {
		super(mainFrame, "Tempo", false);

		//Set up Panel
		//Create box to handle layout
		Box verticalBox = Box.createVerticalBox();
		verticalBox.add(Box.createVerticalStrut(10));

		//Arrange name line
		Box tempoBox = Box.createHorizontalBox();
		tempoBox.add(Box.createHorizontalStrut(10));
		tempoBox.add(new JLabel("Original tempo"));
		tempoTextInputField.setMaximumSize(new Dimension(50, 20));
		tempoTextInputField.setMinimumSize(new Dimension(50, 20));
		tempoTextInputField.setPreferredSize(new Dimension(50, 20));
		tempoBox.add(Box.createHorizontalStrut(5));
		tempoBox.add(tempoTextInputField);
		tempoBox.add(Box.createHorizontalGlue());
		verticalBox.add(tempoBox);
		verticalBox.add(Box.createVerticalStrut(10));

		//Set knob half way across range
		tempoShiftKnob.setValue(0.5f);
		tempoShiftLabel.setText("Tempo shift: 0");
		tempoShiftKnob.setRange((int) ( -1 * tempoRange), (int) tempoRange);

		// Add a change listener and double click listener to the knob
		tempoShiftKnob.addChangeListener(this);

		//Add knob and label to dialog
		JPanel tempPanel = new JPanel(new BorderLayout(3, 3));
		tempPanel.add(tempoShiftKnob, BorderLayout.NORTH);
		tempPanel.add(tempoShiftLabel, BorderLayout.CENTER);
		Box knobBox = Box.createHorizontalBox();
		knobBox.add(Box.createHorizontalStrut(80));
		knobBox.add(tempPanel);
		knobBox.add(Box.createHorizontalGlue());
		verticalBox.add(knobBox);
		verticalBox.add(Box.createVerticalStrut(10));

		//Add a combo box to select the pitch range
		Box rangeBox = Box.createHorizontalBox();
		Vector<String> tempVector = new Vector();
		tempVector.add("-50 / +50");
		tempVector.add("-100 / +100");
		tempVector.add("-150 / +150");
		tempVector.add("-200 / +200");
		tempVector.add("-250 / +250");
		tempoRangeCombo = new JComboBox(tempVector);
		tempoRangeCombo.setSelectedIndex(1);
		tempoRangeCombo.addActionListener(this);
		rangeBox.add(Box.createHorizontalStrut(10));
		rangeBox.add(new JLabel("Range "));
		rangeBox.add(tempoRangeCombo);
		synchToControllerCB = new JCheckBox("Synchronise to controller");
		synchToControllerCB.setSelected(true);
		rangeBox.add(synchToControllerCB);
		rangeBox.add(Box.createHorizontalStrut(10));
		verticalBox.add(rangeBox);
		verticalBox.add(Box.createVerticalStrut(10));

		//Set up buttons
		JPanel buttonPane = new JPanel();
		buttonPane.add(apply = createButton("Apply"));
		buttonPane.add(ok = createButton("Ok"));
		buttonPane.add(cancel = createButton("Cancel"));

		getContentPane().add(verticalBox, BorderLayout.CENTER);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		this.setLocation(300, 300);
		this.pack();
	}


	/* Called by other classes to make the dialog visible. */
	public void showDialog() {
		originalTempo_f = Globals.getTempo();
		String tempoString = Double.toString(originalTempo_f);

		//Get rid of decimal point
		tempoString = tempoString.substring(0, tempoString.indexOf("."));

		tempoTextInputField.setText(tempoString);

		this.setVisible(true);
	}


	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == ok) {
			setTempo();
			setVisible(false);
		}
		else if (source == apply) {
			setTempo();
		}
		else if (source == cancel) {
			//Reset tempo back to the value it was when the dialog was launched
			Globals.setTempo(originalTempo_f);
			EventRouter.tempoActionPerformed(new TempoEvent(originalTempo_f));
			//#FIXME# NEED TO RESET THE RESET OF THE DIALOG BACK TO STARTING VALUES SINCE IT IS NOT RECONSTRUCTED EACH TIME.
			setVisible(false);
		}
		else if (e.getSource() == tempoRangeCombo) {
			switch (tempoRangeCombo.getSelectedIndex()) {
			case (0):
				tempoRange = 50;
			break;
			case (1):
				tempoRange = 100;
			break;
			case (2):
				tempoRange = 150;
			break;
			case (3):
				tempoRange = 200;
			break;
			case (4):
				tempoRange = 250;
			break;
			default:
				System.out.println("Tempo range index not recognized");
			}
			if (synchToControllerCB.isSelected()) { //Set tempo shift amount to last received controller message
				tempoShiftAmount = Math.round(((float) lastControlValue - 63.5f) * (tempoRange / 63.5f));
			}
			else { //Need to make sure tempo shift amount is within range
				if ((float) tempoShiftAmount > tempoRange)
					tempoShiftAmount = (int) tempoRange;
				else if ((float) tempoShiftAmount < tempoRange * -1.0f)
					tempoShiftAmount = (int) (tempoRange * -1.0f);
			}
			tempoShiftKnob.setRange((int) ( -1 * tempoRange), (int) tempoRange);
			tempoShiftChanged();
		}
	}


	//Called when the knob changes state.
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == tempoShiftKnob) {
			tempoShiftAmount = (int) ((tempoRange * 2.0f) * (tempoShiftKnob.getValue() - 0.5f));
			tempoShiftLabel.setText("Tempo shift: " + tempoShiftAmount);
			setTempo();
		}
	}


	//Sets the tempo
	private void setTempo() {
		try {
			double newTempo = Double.parseDouble(tempoTextInputField.getText()) + (float) tempoShiftAmount;
			if (newTempo < 1f)
				newTempo = 1f;
			Globals.setTempo(newTempo);
			EventRouter.tempoActionPerformed(new TempoEvent(newTempo));
		}
		catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Tempo value of " + tempoTextInputField.getText() + " is not a float", "Data Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	private JButton createButton(String label) {
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(80, 20));
		button.addActionListener(this);
		return button;
	}

	//Method called when pitch shift amount has been changed
	private void tempoShiftChanged() {
		tempoShiftLabel.setText("Temopo shift: " + tempoShiftAmount);
		setTempo();
		tempoShiftKnob.removeChangeListener(this);
		tempoShiftKnob.setValue(((float) tempoShiftAmount + tempoRange) / (2.0f * tempoRange));
		tempoShiftKnob.addChangeListener(this);
	}

}

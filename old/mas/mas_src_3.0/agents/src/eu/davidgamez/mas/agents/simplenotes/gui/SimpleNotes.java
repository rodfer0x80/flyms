package eu.davidgamez.mas.agents.simplenotes.gui;

//Java imports
import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import java.awt.event.*;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;
import eu.davidgamez.mas.gui.ValidatedIntTextField;


public class SimpleNotes extends AgentPropertiesPanel implements ActionListener, Constants {

	private ValidatedIntTextField notePitchTF = new ValidatedIntTextField(1, 127);
	private ValidatedIntTextField noteVelocityTF = new ValidatedIntTextField(0, 127);
	private JComboBox noteFrequencyCB;//Holds the possible note frequencies as fractions of a bar
	private ValidatedIntTextField noteFrequencyTF = new ValidatedIntTextField(1, 5000);
	private ValidatedIntTextField noteLengthTF = new ValidatedIntTextField(1, 50000);
	private Box verticalBox;
	private String undefinedString = "--";
	private int freq1Index = 0;


	public SimpleNotes() {
		super("SimpleNotes");

		noteFrequencyCB = new JComboBox(getNoteFrequencyNames());
		noteFrequencyCB.setSelectedIndex(freq1Index);
		noteFrequencyCB.addActionListener(this);
		noteFrequencyTF.addActionListener(this);
		noteFrequencyTF.setPreferredSize(new Dimension(50, 15));

		verticalBox = Box.createVerticalBox();
		addVariable("Note pitch", notePitchTF);
		addVariable("Note velocity", noteVelocityTF);
		addVariables("Note frequency", noteFrequencyCB, noteFrequencyTF);
		addVariable("Note length (ticks)", noteLengthTF);
		verticalBox.add(Box.createVerticalStrut(10));

		//Finish everything off
		this.add(verticalBox, BorderLayout.CENTER);
	}

	public void loadAgentProperties(){
		notePitchTF.setText(String.valueOf(getSimpleNotesAgent().getNotePitch()));
		noteVelocityTF.setText(String.valueOf(getSimpleNotesAgent().getNoteVelocity()));
		noteLengthTF.setText(String.valueOf(getSimpleNotesAgent().getNoteLength()));
		noteFrequencyTF.setText(String.valueOf(getSimpleNotesAgent().getNoteFrequency()));
	}


	public boolean cancelButtonPressed(){
		return true;
	}


	public boolean okButtonPressed(){
		return setAgentProperties();
	}

	public boolean applyButtonPressed(){
		return setAgentProperties();
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == noteFrequencyCB){
			//Extract the ppq value of the string in the combo box and set the text field with this value
			String freqText = (String)noteFrequencyCB.getSelectedItem();
			if(freqText.indexOf(undefinedString) >= 0){//Do nothing
				return;
			}
			else if(freqText.indexOf("/") >= 0){//This is a fraction
				int denominator = Integer.parseInt(freqText.substring(freqText.indexOf("/") + 1, freqText.length()));
				noteFrequencyTF.setText(Integer.toString(PPQ_RESOLUTION / denominator));
			}
			else{//This is a whole number of beats
				noteFrequencyTF.setText(String.valueOf(Integer.parseInt(freqText) * PPQ_RESOLUTION));
			}
		}
		else if (e.getSource() == noteFrequencyTF){
			noteFrequencyCB.removeActionListener(this);
			noteFrequencyCB.setSelectedIndex(0);
			noteFrequencyCB.addActionListener(this);
		}
	}

	private boolean setAgentProperties(){
		//Add some checks that the note length is not longer than the note frequency. Otherwise things get horribly mixed up
		if(Integer.parseInt(noteLengthTF.getText()) >= Integer.parseInt(noteFrequencyTF.getText())){
			JOptionPane.showMessageDialog(null, "Note length cannot be greater than or equal to note frequency", "Overlapping Notes!", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		//Set simple parameters
		getSimpleNotesAgent().setNotePitch(Integer.parseInt(notePitchTF.getText()));
		getSimpleNotesAgent().setNoteVelocity(Integer.parseInt(noteVelocityTF.getText()));
		getSimpleNotesAgent().setNoteLength(Integer.parseInt(noteLengthTF.getText()));

		//Note frequency is more complicated
		String freqString = noteFrequencyTF.getText();
		getSimpleNotesAgent().setNoteFrequency(Integer.parseInt(freqString));

		return true;
	}

	private void addVariable(String labelText, JTextField textField){
		verticalBox.add(Box.createVerticalStrut(10));
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(Box.createHorizontalStrut(5));
		horizontalBox.add(new JLabel(labelText));
		textField.setColumns(5);
		textField.setMaximumSize(new Dimension(20, 30));
		horizontalBox.add(Box.createHorizontalStrut(10));
		horizontalBox.add(textField);
		horizontalBox.add(Box.createHorizontalGlue());
		verticalBox.add(horizontalBox);
	}

	private void addVariables(String labelText, JComboBox comboBox, JTextField textField){
		verticalBox.add(Box.createVerticalStrut(10));
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(Box.createHorizontalStrut(5));
		horizontalBox.add(new JLabel(labelText));
		comboBox.setMaximumSize(new Dimension(20, 30));
		horizontalBox.add(Box.createHorizontalStrut(10));
		horizontalBox.add(textField);
		horizontalBox.add(Box.createHorizontalBox());
		horizontalBox.add(comboBox);
		//horizontalBox.add(Box.createHorizontalGlue());
		verticalBox.add(horizontalBox);
	}

	private Vector getNoteFrequencyNames(){
		Vector<String> nameVector = new Vector<String>();
		//Add an undefined fraction sign
		nameVector.add(undefinedString);

		//Add Fractions
		for(int i=16; i>1; --i){
			nameVector.add("1/" + String.valueOf(i));
		}
		freq1Index = nameVector.size();
		for(int i=1; i<=16; ++i){
			nameVector.add(String.valueOf(i));
		}
		return nameVector;
	}


	private eu.davidgamez.mas.agents.simplenotes.midi.SimpleNotes getSimpleNotesAgent(){
		return (eu.davidgamez.mas.agents.simplenotes.midi.SimpleNotes)agent;

	}

	
	/** Returns an XML string with the parameters of the panel */
	public String getXML(String indent) {
		String panelStr = indent + "<agent_panel>";
		panelStr += super.getXML(indent + "\t");
		panelStr += indent + "</agent_panel>";
		return panelStr;
	}

	
	@Override
	public void loadFromXML(String arg0) throws MASXmlException {
	}

}

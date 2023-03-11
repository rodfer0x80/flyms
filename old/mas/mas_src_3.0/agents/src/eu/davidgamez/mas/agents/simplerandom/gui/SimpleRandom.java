package eu.davidgamez.mas.agents.simplerandom.gui;

//Java imports
import java.awt.*;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.ShortMessage;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Vector;
import java.awt.event.*;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.event.KnobDoubleClickListener;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;
import eu.davidgamez.mas.gui.DKnob;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.gui.ValidatedIntTextField;
import eu.davidgamez.mas.gui.dialog.ControllerDialog;
import eu.davidgamez.mas.midi.MIDIDeviceManager;


public class SimpleRandom extends AgentPropertiesPanel implements ActionListener, Constants, KnobDoubleClickListener, ControllerEventListener, ChangeListener{
	/** Holds the possible note frequencies as fractions of a bar.
	 	The frequency controls how often the random function is evaluated. */
	private JComboBox noteFrequencyCombo;
	
	/** Initial selection index of note frequency */
	private int noteFreqStartIndex = 0;
	
	/** Knob controlling the probability that a note will be produced. */
	private DKnob densityKnob = new DKnob();
	
	/** Label for the density knob */
	private JLabel densityLabel = new JLabel();
	
	/** The minimum pitch octave that will be selected from */
	private JComboBox minOctaveCombo;
	
	/** The minimum pitch note that will be selected from */
	private JComboBox minNoteCombo;
	
	/** The maximum pitch octave that will be selected from */
	private JComboBox maxOctaveCombo;
	
	/** The maximum pitch note that will be selected from */
	private JComboBox maxNoteCombo;
	
	/** The minimum note length */
	private ValidatedIntTextField minLengthTF = new ValidatedIntTextField(1, 50000);
	
	/** The maximum note length */
	private ValidatedIntTextField maxLengthTF = new ValidatedIntTextField(1, 50000);
	
	/** Vector of possible frequencies */
	private Vector<String> frequencyVector = new Vector<String>();
	
	
	/** Constructor */
	public SimpleRandom() {
		super("SimpleRandom");
		
		fillFrequencyVector();
		
		//Set up density knob
		densityKnob.setValue(0.5f);
		densityLabel.setText("Density: 0.5");
		densityKnob.setRange(0, 1);
		densityKnob.addDoubleClickListener(this);
		densityKnob.addChangeListener(this);
		
		//Set up frequency combo
		noteFrequencyCombo = new JComboBox(frequencyVector);
		noteFrequencyCombo.setSelectedIndex(noteFreqStartIndex);
		
		//Set up minimium note octave and pitch
		minOctaveCombo = new JComboBox(getOctaveNames());
		minNoteCombo = new JComboBox(getNoteNames());
		minOctaveCombo.addActionListener(this);
		minNoteCombo.addActionListener(this);

		//Set up maximum note octave and pitch
		maxOctaveCombo = new JComboBox(getOctaveNames());
		maxNoteCombo = new JComboBox(getNoteNames());
		maxOctaveCombo.addActionListener(this);
		maxNoteCombo.addActionListener(this);
		
		//Create layout		
		Box verticalBox = Box.createVerticalBox();
		
		//Add density knob
		JPanel tempPanel = new JPanel(new BorderLayout(3, 3));
		tempPanel.add(densityKnob , BorderLayout.NORTH);
		tempPanel.add(densityLabel, BorderLayout.CENTER);
		Box knobBox = Box.createHorizontalBox();
		knobBox.add(Box.createHorizontalStrut(50));
		knobBox.add(tempPanel);
		knobBox.add(Box.createHorizontalGlue());
		verticalBox.add(knobBox);
		verticalBox.add(Box.createVerticalStrut(10));
		
		//Add Frequency combo
		Box freqBox = Box.createHorizontalBox();
		freqBox.add(new JLabel("Note frequency: "));
		freqBox.add(noteFrequencyCombo);
		freqBox.add(Box.createGlue());
		freqBox.add(noteFrequencyCombo);
		verticalBox.add(freqBox);
		verticalBox.add(Box.createVerticalStrut(10));
		
		//Add min pitch combos
		Box minPitchBox = Box.createHorizontalBox();
		minPitchBox.add(new JLabel("Minimum pitch: "));
		minPitchBox.add(minOctaveCombo);
		minPitchBox.add(minNoteCombo);
		minPitchBox.add(Box.createGlue());
		verticalBox.add(minPitchBox);
		verticalBox.add(Box.createVerticalStrut(10));
		
		//Add max pitch combos
		Box maxPitchBox = Box.createHorizontalBox();
		maxPitchBox.add(new JLabel("Maximum pitch: "));
		maxPitchBox.add(maxOctaveCombo);
		maxPitchBox.add(maxNoteCombo);
		maxPitchBox.add(Box.createGlue());
		verticalBox.add(maxPitchBox);
		verticalBox.add(Box.createVerticalStrut(10));
		
		//Add min and maximum note length
		Box lengthBox = Box.createHorizontalBox();
		lengthBox.add(new JLabel("Note length from "));
		lengthBox.add(minLengthTF);
		lengthBox.add(new JLabel(" to "));
		lengthBox.add(maxLengthTF);
		lengthBox.add(Box.createGlue());
		verticalBox.add(lengthBox);
		verticalBox.add(Box.createVerticalStrut(10));
		
		//Finish everything off
		this.add(verticalBox, BorderLayout.CENTER);
	}
	
	
	/*--------------------------------------------------------------*/
	/*-------               PUBLIC METHODS                    ------*/
	/*--------------------------------------------------------------*/
	
	@Override
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == minOctaveCombo){
			if(minOctaveCombo.getSelectedIndex() > maxOctaveCombo.getSelectedIndex())
				maxOctaveCombo.setSelectedIndex(minOctaveCombo.getSelectedIndex());
		}
		else if (e.getSource() == minNoteCombo){
			if(minOctaveCombo.getSelectedIndex() == maxOctaveCombo.getSelectedIndex()){
				if(minNoteCombo.getSelectedIndex() > maxNoteCombo.getSelectedIndex()){
					maxNoteCombo.setSelectedIndex(minNoteCombo.getSelectedIndex());
				}
			}
		}
		else if (e.getSource() == maxOctaveCombo){
			if(maxOctaveCombo.getSelectedIndex() < minOctaveCombo.getSelectedIndex())
				minOctaveCombo.setSelectedIndex(maxOctaveCombo.getSelectedIndex());
		}
		else if(e.getSource() == maxNoteCombo){
			if(maxOctaveCombo.getSelectedIndex() == minOctaveCombo.getSelectedIndex()){
				if(maxNoteCombo.getSelectedIndex() < minNoteCombo.getSelectedIndex()){
					minNoteCombo.setSelectedIndex(maxNoteCombo.getSelectedIndex());
				}
			}	
		}
	}
		
	
	@Override
	public boolean applyButtonPressed(){
		return setAgentProperties();
	}
	
	
	@Override
	public boolean cancelButtonPressed(){
		return true;
	}
	

	@Override
	public void controlChange(ShortMessage shortMsg) {
		densityKnob.setValue((float)shortMsg.getData2() / 127f);
	}
	
	
	@Override
	public String getXML(String indent) {
		String panelStr = indent + "<agent_panel>";
		panelStr += super.getXML(indent + "\t");
		panelStr += indent + "</agent_panel>";
		return panelStr;
	}
	
	
	@Override
	public void loadAgentProperties(){
		eu.davidgamez.mas.agents.simplerandom.midi.SimpleRandom midiAgent = getSimpleRandomAgent();
		
		minOctaveCombo.setSelectedIndex( midiAgent.getMinimumPitch() / 12);
		minNoteCombo.setSelectedIndex( midiAgent.getMinimumPitch() % 12);
		maxOctaveCombo.setSelectedIndex( midiAgent.getMaximumPitch() / 12);
		maxNoteCombo.setSelectedIndex( midiAgent.getMaximumPitch() % 12);
		minLengthTF.setText( String.valueOf( midiAgent.getMinNoteLength_ppq() ) );
		maxLengthTF.setText( String.valueOf( midiAgent.getMaxNoteLength_ppq() ) );
		densityKnob.setValue((float)midiAgent.getDensity());
		try {
			noteFrequencyCombo.setSelectedIndex( getNoteFrequencyIndex( midiAgent.getNoteFrequency_ppq() ) );
		}
		catch(Exception ex){
			MsgHandler.error(ex);
		}
	}
	
	
	@Override
	public void loadFromXML(String arg0) throws MASXmlException {
	}
	

	@Override
	public void knobDoubleClicked(int arg0, int arg1) {
		ControllerDialog contDialog = new ControllerDialog(this);
		contDialog.showDialog( 400, 400 );
		if(contDialog.midiControllerEnabled()){
			densityKnob.setControllerText(String.valueOf(contDialog.getMidiController()));
			int densityControllerNumber = contDialog.getMidiController();
			MIDIDeviceManager.getMidiInputHandler().addControllerEventListener(this, new int[] {densityControllerNumber});
		}
		else{
			densityKnob.setControllerText("");
		}
	}
	
	
	@Override
	public boolean okButtonPressed(){
		return setAgentProperties();
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == densityKnob) {
			densityLabel.setText("Density: " + Util.round(densityKnob.getValue(), 2));
			try{
				getSimpleRandomAgent().setDensity(densityKnob.getValue());
			}
			catch(Exception ex){
				MsgHandler.error(ex);
			}
		}
	}
	
	
	/*--------------------------------------------------------------*/
	/*-------              PRIVATE METHODS                    ------*/
	/*--------------------------------------------------------------*/
	
	/** Returns the index of a particular note frequency in the noteFrequency combo */
	private int getNoteFrequencyIndex(int noteFrequency_ppq) throws Exception{
		for(int i=0; i< frequencyVector.size(); ++i){
			String lengthString = frequencyVector.get(i);
			if(lengthString.indexOf("/") >= 0){//This is a fraction
				int denominator = Integer.parseInt(lengthString.substring(lengthString.indexOf("/") + 1, lengthString.length()));
				if ((PPQ_RESOLUTION / denominator) == noteFrequency_ppq){
					return i;
				}
			}
			else { //This is a whole number of beats
				if((Integer.parseInt(lengthString) * PPQ_RESOLUTION) == noteFrequency_ppq){
					return i;
				}
			}
		}
		throw new Exception("Note frequency not found!");
	}
	
	
	/** Finds the value of a note frequency within the combo box */
	private int getNoteFrequency() {
		String tmpString = (String)noteFrequencyCombo.getSelectedItem();
		if (tmpString.indexOf("/") >= 0) { //This is a fraction
			int denominator = Integer.parseInt(tmpString.substring(tmpString.indexOf("/") + 1, tmpString.length()));
			return PPQ_RESOLUTION / denominator;
		}
		else { //This is a whole number of beats
			return Integer.parseInt(tmpString) * PPQ_RESOLUTION;
		}
	}
	
	
	/** Sets the MIDI agent's properties */
	private boolean setAgentProperties(){
		eu.davidgamez.mas.agents.simplerandom.midi.SimpleRandom midiAgent = getSimpleRandomAgent();
		try{
			//Check that text fields have sensible data
			if(minLengthTF.getText().isEmpty() || maxLengthTF.getText().isEmpty()){
				MsgHandler.error("Note length range not defined.");
				return false;
			}	
			
			//Set the properties of the MIDI agent
			midiAgent.setPitchRange(minOctaveCombo.getSelectedIndex() * 12 + minNoteCombo.getSelectedIndex(), maxOctaveCombo.getSelectedIndex() * 12 + maxNoteCombo.getSelectedIndex());
			midiAgent.setNoteLengthRange_ppq(Integer.parseInt(minLengthTF.getText()), Integer.parseInt(maxLengthTF.getText()));
			midiAgent.setDensity(densityKnob.getValue());
			midiAgent.setNoteFrequency_ppq(getNoteFrequency());
		}
		catch(Exception ex){
			MsgHandler.error(ex);
		}

		return true;
	}


	/** Returns a vector of names describing possible note frequencies */
	private void fillFrequencyVector(){
		//Add Fractions
		for(int i=16; i>1; --i){
			frequencyVector.add("1/" + String.valueOf(i));
		}
		noteFreqStartIndex = frequencyVector.size();
		for(int i=1; i<=16; ++i){
			frequencyVector.add(String.valueOf(i));
		}
	}
	
	
	/** Returns a vector of octave names */
	private Vector<String> getOctaveNames(){
		Vector<String> octaveVector = new Vector<String>();
		for(int i=0; i<=10; ++i)
			octaveVector.add(String.valueOf(i));
		return octaveVector;
	}
	
	
	/** Returns a vector of note names */
	private Vector<String> getNoteNames(){
		Vector<String> pitchVector = new Vector<String>();
		for (int i = 0; i < NOTE_NAME_ARRAY.length; ++i) {
			pitchVector.add(NOTE_NAME_ARRAY[i]);
		}
		return pitchVector;
	}


	/** Returns an appropriately cast reference to the MIDI agent associated with this interface */
	private eu.davidgamez.mas.agents.simplerandom.midi.SimpleRandom getSimpleRandomAgent(){
		return (eu.davidgamez.mas.agents.simplerandom.midi.SimpleRandom)agent;
	}


}


package eu.davidgamez.mas.agents.randomeater.gui;

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
import eu.davidgamez.mas.gui.dialog.ControllerDialog;
import eu.davidgamez.mas.midi.MIDIDeviceManager;


public class RandomEater extends AgentPropertiesPanel implements ActionListener, Constants, KnobDoubleClickListener, ControllerEventListener, ChangeListener{
	/** Holds the possible note frequencies as fractions of a bar.
	 	The frequency controls how often the random function is evaluated. */
	private JComboBox noteFrequencyCombo;
	
	/** Initial selection index of note frequency */
	private int noteFreqStartIndex = 0;
	
	/** Knob controlling the probability that a note will be produced. */
	private DKnob probKnob = new DKnob();
	
	/** Label for the density knob */
	private JLabel probKnobLabel = new JLabel();
	
	/** The minimum pitch that will be eaten from */
	private JComboBox minPitchCombo;
	
	/** The maximum pitch that will be eaten from */
	private JComboBox maxPitchCombo;
	
	/** Vector of possible frequencies */
	private Vector<String> frequencyVector = new Vector<String>();
	
	
	/** Constructor */
	public RandomEater() {
		super("RandomEater");
		
		fillFrequencyVector();
		
		//Set up density knob
		probKnob.setValue(0.5f);
		probKnobLabel.setText("Probability: 0.5");
		probKnob.setRange(0, 1);
		probKnob.addDoubleClickListener(this);
		probKnob.addChangeListener(this);
		
		//Set up frequency combo
		noteFrequencyCombo = new JComboBox(frequencyVector);
		noteFrequencyCombo.setSelectedIndex(noteFreqStartIndex);
		
		//Set up minimum and maximum pitch
		minPitchCombo = new JComboBox(getPitchNames());
		maxPitchCombo = new JComboBox(getPitchNames());
		minPitchCombo.addActionListener(this);
		maxPitchCombo.addActionListener(this);
	
		//Create layout		
		Box verticalBox = Box.createVerticalBox();
		
		//Add probability knob
		JPanel tempPanel = new JPanel(new BorderLayout(3, 3));
		tempPanel.add(probKnob , BorderLayout.NORTH);
		tempPanel.add(probKnobLabel, BorderLayout.CENTER);
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
		
		//Add min and max pitch combos
		Box pitchBox = Box.createHorizontalBox();
		pitchBox.add(new JLabel("Eat pitches from "));
		pitchBox.add(minPitchCombo);
		pitchBox.add(new JLabel(" to "));
		pitchBox.add(maxPitchCombo);
		pitchBox.add(Box.createGlue());
		verticalBox.add(pitchBox);
		verticalBox.add(Box.createVerticalStrut(10));
		
		//Finish everything off
		this.add(verticalBox, BorderLayout.CENTER);
	}
	
	
	/*--------------------------------------------------------------*/
	/*-------               PUBLIC METHODS                    ------*/
	/*--------------------------------------------------------------*/
	
	@Override
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == minPitchCombo){
			if(minPitchCombo.getSelectedIndex() > maxPitchCombo.getSelectedIndex())
				maxPitchCombo.setSelectedIndex(minPitchCombo.getSelectedIndex());
		}
		else if (e.getSource() == maxPitchCombo){
			if(maxPitchCombo.getSelectedIndex() < minPitchCombo.getSelectedIndex())
				minPitchCombo.setSelectedIndex(maxPitchCombo.getSelectedIndex());
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
		probKnob.setValue((float)shortMsg.getData2() / 127f);
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
		eu.davidgamez.mas.agents.randomeater.midi.RandomEater midiAgent = getRandomEaterAgent();
		
		minPitchCombo.setSelectedIndex( midiAgent.getMinimumPitch() );
		maxPitchCombo.setSelectedIndex( midiAgent.getMaximumPitch() );
		probKnob.setValue((float)midiAgent.getProbability());
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
			probKnob.setControllerText(String.valueOf(contDialog.getMidiController()));
			int densityControllerNumber = contDialog.getMidiController();
			MIDIDeviceManager.getMidiInputHandler().addControllerEventListener(this, new int[] {densityControllerNumber});
		}
		else{
			probKnob.setControllerText("");
		}
	}
	
	
	@Override
	public boolean okButtonPressed(){
		return setAgentProperties();
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == probKnob) {
			probKnobLabel.setText("Density: " + Util.round(probKnob.getValue(), 2));
			try{
				getRandomEaterAgent().setProbability(probKnob.getValue());
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
		eu.davidgamez.mas.agents.randomeater.midi.RandomEater midiAgent = getRandomEaterAgent();
		try{
		
			//Set the properties of the MIDI agent
			midiAgent.setPitchRange(minPitchCombo.getSelectedIndex(), maxPitchCombo.getSelectedIndex());
			midiAgent.setProbability(probKnob.getValue());
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
	
	
	/** Returns a vector of the names of all the pitches */
	private Vector<String> getPitchNames(){
		Vector<String> noteNameVector = new Vector<String>();
		for(int oct=0; oct<=10; ++oct){
			for (int noteIndex = 0; noteIndex < NOTE_NAME_ARRAY.length; ++noteIndex) {
				noteNameVector.add(NOTE_NAME_ARRAY[noteIndex] + oct);
			}
		}
		return noteNameVector;
	}


	/** Returns an appropriately cast reference to the MIDI agent associated with this interface */
	private eu.davidgamez.mas.agents.randomeater.midi.RandomEater getRandomEaterAgent(){
		return (eu.davidgamez.mas.agents.randomeater.midi.RandomEater)agent;
	}


}



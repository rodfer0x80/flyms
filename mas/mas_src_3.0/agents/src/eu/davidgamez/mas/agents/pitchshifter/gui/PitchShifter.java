package eu.davidgamez.mas.agents.pitchshifter.gui;

//Java imports
import javax.swing.*;
import java.util.Vector;
import javax.sound.midi.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.event.*;

import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.event.KnobDoubleClickListener;
import eu.davidgamez.mas.event.TrackBufferUpdateListener;
import eu.davidgamez.mas.exception.MASAgentException;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;
import eu.davidgamez.mas.gui.DKnob;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.gui.dialog.ControllerDialog;
import eu.davidgamez.mas.midi.Agent;
import eu.davidgamez.mas.midi.MIDIDeviceManager;


public class PitchShifter extends AgentPropertiesPanel implements ChangeListener, ActionListener, KnobDoubleClickListener, ControllerEventListener, TrackBufferUpdateListener{
	DKnob pitchShiftKnob = new DKnob();
	JLabel pitchShiftLabel = new JLabel();
	JCheckBox autoPitchChangeCB;
	JCheckBox synchToControllerCB;
	JComboBox pitchRangeCombo;

	//private ControllerDialog controllerDialog = new ControllerDialog(this);
	
	private int pitchShiftControllerNumber = -1;

	int pitchShiftAmount = 0;
	float pitchRange = 5;

	//Keep a record of the last control value to avoid funny jumps when changing range
	int lastControlValue = 64;

	public PitchShifter() {
		super("PitchShifter");
		
		//Create vertical box to organise dialog
		Box verticalBox = Box.createVerticalBox();

		//Add option to control whether emphasis points and synchronized to the bar
		Box contIncreaseBox = Box.createHorizontalBox();
		autoPitchChangeCB = new JCheckBox("Automatic pitch change");
		autoPitchChangeCB.addActionListener(this);
		contIncreaseBox.add(autoPitchChangeCB);
		contIncreaseBox.add(Box.createHorizontalGlue());
		verticalBox.add(contIncreaseBox);
		verticalBox.add(Box.createVerticalStrut(10));

		//Set knob half way across range
		pitchShiftKnob.setValue(0.5f);
		pitchShiftLabel.setText("Pitch shift: 0");
		pitchShiftKnob.setRange((int)(-1 * pitchRange), (int)pitchRange);

		// Add a change listener and double click listener to the knob
		pitchShiftKnob.addChangeListener(this);
		pitchShiftKnob.addDoubleClickListener(this);

		//Add knob and label to dialog
		JPanel tempPanel = new JPanel(new BorderLayout(3, 3));
		tempPanel.add(pitchShiftKnob , BorderLayout.NORTH);
		tempPanel.add(pitchShiftLabel, BorderLayout.CENTER);
		Box knobBox = Box.createHorizontalBox();
		knobBox.add(Box.createHorizontalStrut(50));
		knobBox.add(tempPanel);
		knobBox.add(Box.createHorizontalGlue());
		verticalBox.add(knobBox);
		verticalBox.add(Box.createVerticalStrut(10));

		//Add a combo box to select the pitch range
		Box rangeBox = Box.createHorizontalBox();
		Vector<String> tempVector = new Vector<String>();
		tempVector.add("-5 / +5");
		tempVector.add("-10 / +10");
		tempVector.add("-20 / +20");
		tempVector.add("-40 / +40");
		tempVector.add("-60 / +60");
		pitchRangeCombo = new JComboBox(tempVector);
		pitchRangeCombo.addActionListener(this);
		rangeBox.add(new JLabel("Range "));
		rangeBox.add(pitchRangeCombo);
		synchToControllerCB = new JCheckBox("Synchronise to controller");
		synchToControllerCB.setSelected(true);
		rangeBox.add(synchToControllerCB);
		verticalBox.add(rangeBox);

		//Finish everything off
		verticalBox.add(Box.createVerticalStrut(5));
		this.add(verticalBox, BorderLayout.CENTER);

	}

	//Should only be called when contIncreaseCB is checked
	public void trackBufferUpdate(){
		if(autoPitchChangeCB.isSelected()){
			if(pitchShiftAmount < pitchRange){
				++pitchShiftAmount;
				pitchShiftChanged();
			}
		}
	}

	public boolean cancelButtonPressed() {
		return true;
	}

	public boolean okButtonPressed() {
		return setAgentProperties();
	}

	public boolean applyButtonPressed() {
		return setAgentProperties();
	}

	public void loadAgentProperties(){

	}


	public String getXML(String indent){
		String tmpStr = "";
		tmpStr += indent + "<agent_panel>";
		
		//Get superclass XML
		tmpStr += super.getXML(indent + "\t");

		//Save last control value
		tmpStr += indent + "\t<last_control_value>" + String.valueOf(lastControlValue) + "</last_control_value>";

		//Save pitch shift amount
		tmpStr += indent + "\t<pitch_shift>" + String.valueOf(pitchShiftAmount) + "</pitch_shift>";

		//Save range
		tmpStr += indent + "\t<pitch_range>" + String.valueOf(pitchRange) + "</pitch_range>";

		//Save automatic pitch increase
		tmpStr += indent + "\t<auto_pitch_change>" + String.valueOf(autoPitchChangeCB.isSelected()) + "</auto_pitch_change>";

		//Save Synch to controllers cb
		tmpStr += indent + "\t<synch_to_controller>" + String.valueOf(synchToControllerCB.isSelected()) + "</synch_to_controller>";

		//Save controller number for the midi control
		tmpStr += indent + "\t<pitch_shift_controller>" + String.valueOf(pitchShiftControllerNumber) + "</pitch_shift_controller>";

		tmpStr += indent + "</agent_panel>";
		return tmpStr;
	}
	
	
	@Override
	public void loadFromXML(String xmlStr) throws MASXmlException {
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			lastControlValue= Util.getIntParameter("last_control_value", xmlDoc);
			pitchShiftAmount = Util.getIntParameter("pitch_shift", xmlDoc);
			pitchRange = Util.getFloatParameter("pitch_range", xmlDoc);
			autoPitchChangeCB.setSelected( Util.getBoolParameter("auto_pitch_change", xmlDoc) );
			synchToControllerCB.setSelected( Util.getBoolParameter("synch_to_controller", xmlDoc) );
			pitchShiftControllerNumber = Util.getIntParameter("pitch_shift_controller", xmlDoc);
			if(pitchShiftControllerNumber > 0){
				MIDIDeviceManager.getMidiInputHandler().addControllerEventListener(this, new int[] {pitchShiftControllerNumber});
				pitchShiftKnob.setControllerText(String.valueOf(pitchShiftControllerNumber));
			}
			
			//Update the state of the graphics
			updatePitchRangeCombo();
			pitchShiftChanged();
			
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}
	
	@Override
	public void setAgent(Agent ag) throws MASAgentException {
		super.setAgent(ag);
		getPitchShifterAgent().setPitchShiftAmount(pitchShiftAmount);
	}
	


	private boolean setAgentProperties(){
		return true;
	}


	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == pitchShiftKnob) {
			pitchShiftAmount = (int)((pitchRange * 2.0f) * (pitchShiftKnob.getValue() - 0.5f));
			pitchShiftLabel.setText("Pitch shift: " + pitchShiftAmount);
			getPitchShifterAgent().setPitchShiftAmount(pitchShiftAmount);
		}
	}

	public void actionPerformed(ActionEvent e){
		if (e.getSource() == autoPitchChangeCB) {
			if (autoPitchChangeCB.isSelected())
				getPitchShifterAgent().addTrackBufferUpdateListener(this);
			else
				getPitchShifterAgent().removeTrackBufferUpdateListener(this);
		}
		else if(e.getSource() == pitchRangeCombo){
			switch (pitchRangeCombo.getSelectedIndex()){
			case(0): pitchRange= 5;
			break;
			case(1): pitchRange = 10;
			break;
			case(2): pitchRange = 20;
			break;
			case(3): pitchRange= 40;
			break;
			case(4): pitchRange = 60;
			break;
			default: System.out.println("Pitch range index not recognized");
			}
			if(synchToControllerCB.isSelected()){//Set pitch shift amount to last received controller message
				pitchShiftAmount = Math.round( ( (float) lastControlValue - 63.5f) * (pitchRange / 63.5f));
			}
			else{//Need to make sure pitchShiftAmount is within range
				if((float)pitchShiftAmount > pitchRange)
					pitchShiftAmount = (int)pitchRange;
				else if((float)pitchShiftAmount < pitchRange * -1.0f)
					pitchShiftAmount = (int)(pitchRange * -1.0f);
			}
			pitchShiftKnob.setRange((int)(-1 * pitchRange), (int)pitchRange);
			pitchShiftChanged();
		}
	}

	public void controlChange(ShortMessage shortMessage){
		//Control message ranges from 0 to 127 so need to convert to range
		lastControlValue = shortMessage.getData2();
		pitchShiftAmount = Math.round(((float)lastControlValue - 63.5f) * (pitchRange / 63.5f));

		//Update panel
		pitchShiftChanged();
	}

	public void knobDoubleClicked(int xPos, int yPos){
		ControllerDialog contDialog = new ControllerDialog(this);
		contDialog.showDialog( 400, 400 );
		if(contDialog.midiControllerEnabled()){
			pitchShiftKnob.setControllerText(String.valueOf(contDialog.getMidiController()));
			pitchShiftControllerNumber = contDialog.getMidiController();
			MIDIDeviceManager.getMidiInputHandler().addControllerEventListener(this, new int[] {pitchShiftControllerNumber});
		}
		else{
			pitchShiftKnob.setControllerText("");
			pitchShiftControllerNumber = -1;
		}
	}

	private void updatePitchRangeCombo(){
		pitchRangeCombo.removeActionListener(this);
		switch ((int)pitchRange) {
		case (5):
			pitchRangeCombo.setSelectedIndex(0);
		break;
		case (10):
			pitchRangeCombo.setSelectedIndex(1);
		break;
		case (20):
			pitchRangeCombo.setSelectedIndex(2);
		break;
		case (40):
			pitchRangeCombo.setSelectedIndex(3);
		break;
		case (60):
			pitchRangeCombo.setSelectedIndex(4);
		break;
		default:
			System.out.println("Pitch range index not recognized");
		}
		pitchRangeCombo.addActionListener(this);
	}

	//Method called when pitch shift amount has been changed
	private void pitchShiftChanged(){
		pitchShiftLabel.setText("Pitch shift: " + pitchShiftAmount);
		if(agent != null)
			getPitchShifterAgent().setPitchShiftAmount(pitchShiftAmount);
		pitchShiftKnob.removeChangeListener(this);
		pitchShiftKnob.setValue(((float)pitchShiftAmount + pitchRange) / (2.0f * pitchRange));
		pitchShiftKnob.addChangeListener(this);
	}

	private eu.davidgamez.mas.agents.pitchshifter.midi.PitchShifter getPitchShifterAgent(){
		return (eu.davidgamez.mas.agents.pitchshifter.midi.PitchShifter)agent;
	}


}

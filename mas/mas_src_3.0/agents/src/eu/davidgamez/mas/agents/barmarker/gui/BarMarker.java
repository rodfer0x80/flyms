package eu.davidgamez.mas.agents.barmarker.gui;

//Java imports
import java.awt.BorderLayout;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.event.ChangeEvent;
import javax.sound.midi.ControllerEventListener;
import javax.swing.event.ChangeListener;
import javax.sound.midi.ShortMessage;

import org.w3c.dom.Document;

import java.awt.*;
import java.io.BufferedWriter;

//MAS imports
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;
import eu.davidgamez.mas.gui.DKnob;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.gui.dialog.ControllerDialog;
import eu.davidgamez.mas.midi.MIDIDeviceManager;
import eu.davidgamez.mas.event.KnobDoubleClickListener;
import eu.davidgamez.mas.exception.MASXmlException;


public class BarMarker extends AgentPropertiesPanel implements ActionListener, KeyListener, ChangeListener, KnobDoubleClickListener, ControllerEventListener{

	private Box leftVerticalBox;
	private JRadioButton fixedBarRadioButton, randomBarRadioButton;
	private int numFixedBars = 12;
	private ArrayList<JLabel> fixedComboLabels = new ArrayList<JLabel>();
	private ArrayList<JComboBox> fixedCombos = new ArrayList<JComboBox>();
	private JLabel minRanBarLengthLabel, maxRanBarLengthLabel;
	private JComboBox minRanBarLengthCombo, maxRanBarLengthCombo;
	private Vector fixedBarLengthsVector = new Vector();
	private Vector randomBarLengthsVector = new Vector();

	private int barClampControllerNumber = -1;

	DKnob barClampKnob = new DKnob();
	JLabel barClampLabel;
	int barClampAmount = 12;
	int lastControlValue = 0;


	public BarMarker() {
		super("BarMarker");
		
		//Left side of the panel contains the combo boxes and radio buttons
		JPanel leftPanel = new JPanel();
		leftVerticalBox = Box.createVerticalBox();

		//Add radio button for fixed bars
		fixedBarRadioButton = new JRadioButton("Fixed length bars");
		fixedBarRadioButton.setSelected(true);
		Box fixedButBox = Box.createHorizontalBox();
		fixedButBox.add(fixedBarRadioButton);
		fixedButBox.add(Box.createHorizontalGlue());
		leftVerticalBox.add(fixedButBox);

		//Add components to select the fixed bar lengths
		fillBarLengthsVector();
		for(int i=0; i<numFixedBars; ++i){
			Box horizontalBox = Box.createHorizontalBox();
			horizontalBox.add(Box.createHorizontalStrut(30));
			JLabel tempLabel = new JLabel("Bar length ");
			fixedComboLabels.add(tempLabel);
			horizontalBox.add(tempLabel);
			JComboBox tempCombo = new JComboBox(fixedBarLengthsVector);
			fixedCombos.add(tempCombo);
			horizontalBox.add(tempCombo);
			horizontalBox.add(Box.createHorizontalGlue());
			leftVerticalBox.add(horizontalBox);
		}

		leftVerticalBox.add(Box.createVerticalStrut(10));

		//Add radio button for random bar lengths
		randomBarRadioButton = new JRadioButton("Random length bars");
		randomBarRadioButton.setSelected(false);
		Box ranButBox = Box.createHorizontalBox();
		ranButBox.add(randomBarRadioButton);
		ranButBox.add(Box.createHorizontalGlue());
		leftVerticalBox.add(ranButBox);

		//Add components to select the minimum and maximum bar length
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(Box.createHorizontalStrut(30));
		minRanBarLengthLabel = new JLabel("Min ");
		horizontalBox.add(minRanBarLengthLabel);
		minRanBarLengthCombo = new JComboBox(randomBarLengthsVector);
		horizontalBox.add(minRanBarLengthCombo);
		horizontalBox.add(Box.createHorizontalStrut(10));
		maxRanBarLengthLabel = new JLabel("Max ");
		horizontalBox.add(maxRanBarLengthLabel);
		maxRanBarLengthCombo = new JComboBox(randomBarLengthsVector);
		horizontalBox.add(maxRanBarLengthCombo);
		horizontalBox.add(Box.createHorizontalGlue());
		leftVerticalBox.add(horizontalBox);

		//Disable random components
		minRanBarLengthLabel.setEnabled(false);
		maxRanBarLengthLabel.setEnabled(false);
		minRanBarLengthCombo.setEnabled(false);
		maxRanBarLengthCombo.setEnabled(false);

		//Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(fixedBarRadioButton);
		group.add(randomBarRadioButton);

		//Register a listener for the radio buttons to enable and disable relevant components
		fixedBarRadioButton.addActionListener(this);
		randomBarRadioButton.addActionListener(this);

		leftVerticalBox.add(Box.createVerticalStrut(5));
		leftPanel.add(leftVerticalBox, BorderLayout.CENTER);

		//Right side of panel contains knob for clamping bar length
		JPanel rightPanel = new JPanel();
		Box rightVerticalBox = Box.createVerticalBox();

		//Set up knob to clamp the length of a bar
		JPanel knobPanel = new JPanel(new BorderLayout());
		barClampKnob.setValue(1.0f);
		barClampKnob.setRange(1, 12);
		barClampKnob.addChangeListener(this);
		barClampKnob.addDoubleClickListener(this);
		knobPanel.add(barClampKnob, BorderLayout.CENTER);
		barClampLabel = new JLabel("Max bar length: 12");
		knobPanel.add(barClampLabel, BorderLayout.SOUTH);
		knobPanel.setMaximumSize(new Dimension(100, 100));
		knobPanel.setMinimumSize(new Dimension(100, 100));
		knobPanel.setPreferredSize(new Dimension(100, 100));
		rightVerticalBox.add(Box.createVerticalStrut(100));
		rightVerticalBox.add(knobPanel);
		rightVerticalBox.add(Box.createVerticalGlue());
		rightPanel.add(rightVerticalBox, BorderLayout.CENTER);

		//Finish everything off
		Box mainHorizontalBox = Box.createHorizontalBox();
		mainHorizontalBox.add(leftPanel);
		mainHorizontalBox.add(rightPanel);
		this.add(mainHorizontalBox, BorderLayout.CENTER);
	}



	public void keyPressed(KeyEvent e){

	}

	public void keyReleased(KeyEvent e){

	}

	public void keyTyped(KeyEvent e){
	}


	public void controlChange(ShortMessage shortMessage){
		//Control message ranges from 0 to 127 so need to convert to range
		lastControlValue = shortMessage.getData2();
		barClampAmount = Math.round(1f + ((float)(lastControlValue * 11f)/127f));

		//Update panel
		barClampChanged();
	}


	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == barClampKnob) {
			barClampAmount = Math.round(1f + (barClampKnob.getValue() * 11f));
			barClampLabel.setText("Max bar length: " + barClampAmount);
			getBarMarkerAgent().setMaxBarLength(barClampAmount);
		}
	}

	public void knobDoubleClicked(int xPos, int yPos){
		ControllerDialog contDialog = new ControllerDialog(this);
		contDialog.showDialog( 400, 400 );
		if(contDialog.midiControllerEnabled()){
			barClampControllerNumber = contDialog.getMidiController();
			barClampKnob.setControllerText(String.valueOf(contDialog.getMidiController()));
		}
		else{
			barClampControllerNumber = -1;
			barClampKnob.setControllerText("");
		}
	}

	public void loadAgentProperties() {
		//Set the radio buttons appropriately
		if(getBarMarkerAgent().fixedLengthBars()){
			setFixedLengthBars(true);
		}
		else{
			setFixedLengthBars(false);
		}

		//Load up the fixed arrays
		//Reset all combos to default
		for (JComboBox tempCombo : fixedCombos) {
			tempCombo.setSelectedIndex(0);
		}

		//Set combos appropriately
		int[] barLengths = getBarMarkerAgent().getFixedBarLengths();
		int maxIndexValue = barLengths.length;
		if (barLengths.length > fixedCombos.size())
			maxIndexValue = fixedCombos.size();
		for (int i = 0; i < maxIndexValue; ++i) { //Work through the bar lengths
			fixedCombos.get(i).setSelectedIndex(barLengths[i]); //Numbers range from 0 to 12 inclusive so can go directly from bar length to index
		}

		//Load up the random ranges
		//Random bar lengths should vary from 1 -12 so subtract 1 to convert from bar length to index
		minRanBarLengthCombo.setSelectedIndex(getBarMarkerAgent().getMinRandomBarLength() - 1);
		maxRanBarLengthCombo.setSelectedIndex(getBarMarkerAgent().getMaxRandomBarLength() - 1);
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

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == fixedBarRadioButton){
			setFixedLengthBars(true);
		}
		else if (e.getSource() == randomBarRadioButton){
			setFixedLengthBars(false);
		}
	}

	/** Returns an XML string with the parameters of the panel */
	public String getXML(String indent) {
		String panelStr = indent + "<agent_panel>";
		panelStr += super.getXML(indent + "\t");
		panelStr += indent + "\t<bar_clamp_controller>" + barClampControllerNumber + "</bar_clamp_controller>";
		panelStr += indent + "</agent_panel>";
		return panelStr;
	}


	@Override
	public void loadFromXML(String xmlStr) throws MASXmlException {
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			barClampControllerNumber = Util.getIntParameter("bar_clamp_controller", xmlDoc);
			if(barClampControllerNumber > 0){
				MIDIDeviceManager.getMidiInputHandler().addControllerEventListener(this, new int[] {barClampControllerNumber});
				barClampKnob.setControllerText(String.valueOf(barClampControllerNumber));
			}
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}
	
	
	private void setFixedLengthBars(boolean fixed){
		if(fixed){ //Enable fixed length labels and combos
			fixedBarRadioButton.setSelected(true);
			for (JComboBox tempCombo : fixedCombos) {
				tempCombo.setEnabled(true);
			}
			for (JLabel tempLabel : fixedComboLabels) {
				tempLabel.setEnabled(true);
			}
			//Disable random stuff
			minRanBarLengthLabel.setEnabled(false);
			maxRanBarLengthLabel.setEnabled(false);
			minRanBarLengthCombo.setEnabled(false);
			maxRanBarLengthCombo.setEnabled(false);
		}
		else{
			randomBarRadioButton.setSelected(true);
			//Disable fixed length combos
			for (JComboBox tempCombo : fixedCombos) {
				tempCombo.setEnabled(false);
			}
			for (JLabel tempLabel : fixedComboLabels) {
				tempLabel.setEnabled(false);
			}
			//Enable random stuff
			minRanBarLengthLabel.setEnabled(true);
			maxRanBarLengthLabel.setEnabled(true);
			minRanBarLengthCombo.setEnabled(true);
			maxRanBarLengthCombo.setEnabled(true);
		}
	}



	private boolean setAgentProperties(){
		if(fixedBarRadioButton.isSelected()){
			ArrayList<Integer> tempArrayList = new ArrayList<Integer>();
			//Work through combos and add the lengths of any that are not selected
			for(JComboBox tempCombo : fixedCombos){
				if(((Integer)tempCombo.getSelectedItem()).intValue() != 0)
					tempArrayList.add((Integer)tempCombo.getSelectedItem());
			}
			getBarMarkerAgent().setFixedBarLengths(tempArrayList);
			return true;
		}
		else{
			//Run a check that max range is not less than min range
			int minRange = ((Integer)minRanBarLengthCombo.getSelectedItem()).intValue();
			int maxRange = ((Integer)maxRanBarLengthCombo.getSelectedItem()).intValue();
			if(maxRange < minRange){
				JOptionPane.showMessageDialog(null, "Maximum range cannot be less than the minimum", "Range error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else{
				getBarMarkerAgent().setRandomBarLengths(minRange, maxRange);
				return true;
			}
		}
	}

	//Bar lengths can vary from 0 (no bar marker) to 12
	private void fillBarLengthsVector(){
		for(int i=0; i<=12; ++i){
			fixedBarLengthsVector.add(new Integer(i));
			if(i>=1)//Random bar lengths need to be greater than or equal to 1
				randomBarLengthsVector.add(new Integer(i));
		}
	}

	//Method called when pitch shift amount has been changed
	private void barClampChanged() {
		barClampLabel.setText("Max bar length: " + barClampAmount);
		getBarMarkerAgent().setMaxBarLength(barClampAmount);
		barClampKnob.removeChangeListener(this);
		barClampKnob.setValue( ( (float) (barClampAmount - 1f)/11f));
		barClampKnob.addChangeListener(this);
	}

	private eu.davidgamez.mas.agents.barmarker.midi.BarMarker getBarMarkerAgent(){
		return (eu.davidgamez.mas.agents.barmarker.midi.BarMarker) agent;
	}

}

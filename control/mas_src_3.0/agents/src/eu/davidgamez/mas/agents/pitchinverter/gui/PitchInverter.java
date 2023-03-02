package eu.davidgamez.mas.agents.pitchinverter.gui;

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


public class PitchInverter extends AgentPropertiesPanel implements ActionListener, Constants, KnobDoubleClickListener, ControllerEventListener, ChangeListener{
	
	/** Knob controlling the probability that a note will be produced. */
	private DKnob noteKnob = new DKnob();
	
	/** Label for the density knob */
	private JLabel noteKnobLabel = new JLabel();
	
	/** The maximum pitch note that will be selected from */
	private JComboBox noteCombo;
	

	
	/** Constructor */
	public PitchInverter() {
		super("PitchInverter");
		
		//Set up density knob
		noteKnob.setValue(0.5f);
		noteKnobLabel.setText("Inversion pitch: 60");
		noteKnob.setRange(0, 127);
		noteKnob.addDoubleClickListener(this);
		noteKnob.addChangeListener(this);
			
		//Set up note octave and pitch
		noteCombo = new JComboBox(getNoteNames());
		noteCombo.addActionListener(this);
		
		//Create layout		
		Box verticalBox = Box.createVerticalBox();
		
		//Add density knob
		JPanel tempPanel = new JPanel(new BorderLayout(3, 3));
		tempPanel.add(noteKnob , BorderLayout.NORTH);
		tempPanel.add(noteKnobLabel, BorderLayout.CENTER);
		Box knobBox = Box.createHorizontalBox();
		knobBox.add(Box.createHorizontalStrut(50));
		knobBox.add(tempPanel);
		knobBox.add(Box.createHorizontalGlue());
		verticalBox.add(knobBox);
		verticalBox.add(Box.createVerticalStrut(10));
			
		//Add note combo
		Box pitchBox = Box.createHorizontalBox();
		pitchBox.add(new JLabel("Inversion pitch: "));
		pitchBox.add(noteCombo);
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
		if(e.getSource() == noteCombo){
			noteKnob.setValue( (float)noteCombo.getSelectedIndex() / 127f);
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
		noteKnob.setValue((float)shortMsg.getData2() / 127f);
		noteCombo.setSelectedIndex(shortMsg.getData2());
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
		eu.davidgamez.mas.agents.pitchinverter.midi.PitchInverter midiAgent = getInverterAgent();
		noteKnob.setValue((float)midiAgent.getInversionPitch() / 127f);
		noteCombo.setSelectedIndex(midiAgent.getInversionPitch());
	}
	
	
	@Override
	public void loadFromXML(String arg0) throws MASXmlException {
	}
	

	@Override
	public void knobDoubleClicked(int arg0, int arg1) {
		ControllerDialog contDialog = new ControllerDialog(this);
		contDialog.showDialog( 400, 400 );
		if(contDialog.midiControllerEnabled()){
			noteKnob.setControllerText(String.valueOf(contDialog.getMidiController()));
			int densityControllerNumber = contDialog.getMidiController();
			MIDIDeviceManager.getMidiInputHandler().addControllerEventListener(this, new int[] {densityControllerNumber});
		}
		else{
			noteKnob.setControllerText("");
		}
	}
	
	
	@Override
	public boolean okButtonPressed(){
		return setAgentProperties();
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == noteKnob) {
			int newInversionPitch = Math.round(noteKnob.getValue() * 127);
			noteKnobLabel.setText("Inversion pitch: " + newInversionPitch);
			noteCombo.setSelectedIndex(newInversionPitch);
			try{
				getInverterAgent().setInversionPitch(newInversionPitch);
			}
			catch(Exception ex){
				MsgHandler.error(ex);
			}
		}
	}
	
	
	/*--------------------------------------------------------------*/
	/*-------              PRIVATE METHODS                    ------*/
	/*--------------------------------------------------------------*/
	
	/** Sets the MIDI agent's properties */
	private boolean setAgentProperties(){
		try{
			getInverterAgent().setInversionPitch(noteCombo.getSelectedIndex());
		}
		catch(Exception ex){
			MsgHandler.error(ex);
		}

		return true;
	}
	
	
	/** Returns a vector of note names */
	private Vector<String> getNoteNames(){
		Vector<String> noteNameVector = new Vector<String>();
		for(int oct=0; oct<=10; ++oct){
			for (int noteIndex = 0; noteIndex < NOTE_NAME_ARRAY.length; ++noteIndex) {
				noteNameVector.add(NOTE_NAME_ARRAY[noteIndex] + oct);
			}
		}
		return noteNameVector;
	}


	/** Returns an appropriately cast reference to the MIDI agent associated with this interface */
	private eu.davidgamez.mas.agents.pitchinverter.midi.PitchInverter getInverterAgent(){
		return (eu.davidgamez.mas.agents.pitchinverter.midi.PitchInverter)agent;
	}


}


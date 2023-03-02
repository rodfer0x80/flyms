package eu.davidgamez.mas.agents.timeinverter.gui;

//Java imports
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JCheckBox;

//MAS imports
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;


public class TimeInverterPanel extends AgentPropertiesPanel implements ActionListener{

	/** Sets whether the time inversion is switched on every other buffer */
	private JCheckBox alternateCB = new JCheckBox("Alternate buffers");
	
	public TimeInverterPanel() {
		super("TimeInverter");
		
		//Add check box
		alternateCB.addActionListener(this);
		Box mainVBox = Box.createVerticalBox();
		mainVBox.add(alternateCB);
		
		//Finish everything off
		this.add(mainVBox, BorderLayout.CENTER);
	}

	
	/*--------------------------------------------------------------*/
	/*-------               PUBLIC METHODS                    ------*/
	/*--------------------------------------------------------------*/
	
	@Override
	public void actionPerformed(ActionEvent ev){
		if(ev.getSource() == alternateCB){
			getTimeInverterAgent().setAlternatingBuffers(alternateCB.isSelected());
		}
	}
	
	
	@Override
	public boolean applyButtonPressed() {
		return true;
	}

	
	@Override
	public boolean cancelButtonPressed() {
		return true;
	}
	
	
	@Override
	public String getXML(String indent) {
		String panelStr = indent + "<agent_panel>";
		panelStr += super.getXML(indent + "\t");
		panelStr += indent + "</agent_panel>";
		return panelStr;
	}

	
	@Override
	public void loadAgentProperties() throws Exception {
		if(getTimeInverterAgent().isAlternatingBuffers())
			alternateCB.setSelected(true);
		else
			alternateCB.setSelected(false);
	}

	
	@Override
	public void loadFromXML(String arg0) throws MASXmlException {	
	}

	
	@Override
	public boolean okButtonPressed() {
		return true;
	}
	
	
	/*--------------------------------------------------------------*/
	/*-------               PRIVATE METHODS                   ------*/
	/*--------------------------------------------------------------*/

	/** Returns the agent cast as a time inverter agent. */
	private eu.davidgamez.mas.agents.timeinverter.midi.TimeInverterAgent getTimeInverterAgent(){
		return (eu.davidgamez.mas.agents.timeinverter.midi.TimeInverterAgent) agent;
	}

	
}

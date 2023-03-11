package eu.davidgamez.mas.agents.prolongnotes.gui;

//Java imports
import java.awt.event.*;
import java.io.BufferedWriter;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Dimension;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;


public class ProlongNotes  extends AgentPropertiesPanel implements Constants, ActionListener{

	private JToggleButton prolongNotesButton;
	
	public ProlongNotes() {
		super("ProlongNotes");
		prolongNotesButton = new JToggleButton("Prolong Notes");
		prolongNotesButton.setPreferredSize(new Dimension(160,  20));
		prolongNotesButton.addActionListener(this);
		this.add(prolongNotesButton, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent event){
		if(event.getSource() == prolongNotesButton){
			getProlongNotesAgent().setProlongNotes(prolongNotesButton.isSelected());
		}
	}


	public void loadAgentProperties(){
		prolongNotesButton.setSelected(getProlongNotesAgent().getProlongNotes());
	}

	public boolean okButtonPressed(){
		return true;
	}

	public boolean applyButtonPressed(){
		return true;
	}

	public boolean cancelButtonPressed(){
		return true;
	}
	
	private eu.davidgamez.mas.agents.prolongnotes.midi.ProlongNotes getProlongNotesAgent(){
		 return (eu.davidgamez.mas.agents.prolongnotes.midi.ProlongNotes)agent;
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

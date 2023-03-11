package eu.davidgamez.mas.agents.debug.gui;

import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;

public class Debug extends AgentPropertiesPanel {
	
	public Debug(){
		super("Debug");
	}

	@Override
	public boolean applyButtonPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean cancelButtonPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	
	/** Returns an XML string with the parameters of the panel */
	public String getXML(String indent) {
		String panelStr = indent + "<agent_panel>";
		panelStr += super.getXML(indent + "\t");
		panelStr += indent + "</agent_panel>";
		return panelStr;
	}

	@Override
	public void loadFromXML(String xmlStr) throws MASXmlException {	
	}


	@Override
	public boolean okButtonPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void loadAgentProperties() throws Exception {
		// TODO Auto-generated method stub
		
	}

}

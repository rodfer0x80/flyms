package eu.davidgamez.mas.gui;
/* ***************************************************************************
 *                     Midi Agent System - 2                                 *
 *                     David Gamez: david@davidgamez.eu.                     *
 *                     Copyright(c): David Gamez 2004                        *
 *                     Version 0.1                                           *
 *****************************************************************************
 ------------------------- Agent Properties Panel ----------------------------
   Abstract class that defines the interface for the panel that is used to
   edit an agent's properties. DefaultAgentPropertiesPanel is a default
   implementation of this class.
 -----------------------------------------------------------------------------
 */

//Java imports
import javax.swing.*;
import java.io.BufferedWriter;

//Project imports
import eu.davidgamez.mas.exception.MASAgentException;
import eu.davidgamez.mas.exception.MASException;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.midi.Agent;


public abstract class AgentPropertiesPanel extends JPanel {

  //The agent whose properties are edited by this panel
  protected Agent agent;

  //The type of agent that this panel works with
  private String agentType = null;

  
  //Constructor
  public AgentPropertiesPanel(String agentType) {
	  this.agentType = agentType;
  }


  /* Called when the apply button is pressed on the associated dialog. */
  public abstract boolean applyButtonPressed();


  /* Called when the cancel button is pressed on the associated dialog. */
  public abstract boolean cancelButtonPressed();


  /* Configures the panel by extracting information from its associated
     Agent. Used whenever the panel is displayed. */
  public abstract void loadAgentProperties() throws Exception;


  /* Loads up the configuration of the panel from the supplied string, setting
     checkboxes, comboboxes etc. in their correct position. Used when loading
     a project from file. */
  public abstract void loadFromXML(String agentPropertiesString) throws MASXmlException;


  /* Called when the ok button is pressed on the associated dialog. */
  public abstract boolean okButtonPressed();


  /* Writes the panels state to the BufferedWriter when saving a project. */
  public String getXML(String indent){
		String panelStr = indent + "<type>" + getAgentType() + "</type>";
		panelStr += indent + "<class>" + getClass().getCanonicalName() + "</class>";
		return panelStr;
  }


  //-------------------------------------------------------------------------------------
  //----------                      Accessor methods                          -----------
  //-------------------------------------------------------------------------------------

  //Returns the MIDIAgent associated with the panel
  public Agent getAgent(){
    return agent;
  }

  /* Sets the MIDIAgent associated with this panel. */
  public void setAgent(Agent ag) throws MASAgentException {
	  if(ag.getAgentType() != agentType)
		  throw new MASAgentException("Panel agent type " + agentType + " does not match agent type " + ag.getAgentType());
    this.agent = ag;
  }
  
  
  public String getAgentType(){
	  return agentType;
  }

}

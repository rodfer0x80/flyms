package eu.davidgamez.mas.gui;

//Java imports
import java.awt.*;

import javax.swing.*;

import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.event.DisplayListener;
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.midi.*;


public class AgentGUI extends Rectangle {

	//Records whether the graphical element is selected by the user
	private boolean selected = false;
	private Font agentFont;
	
	//MIDI agent that that this is graphically representing
	private Agent agent;
	
	/** Icon representing the agent in the graphical display */
	ImageIcon icon;

	/* Properties panel is used to edit the agent's properties.
     This can be the default panel with just a name, or a class that inherits from this */
	private AgentPropertiesPanel agentPropertiesPanel;

	
	/** Standard constructor */
	public AgentGUI(AgentPropertiesPanel panel, ImageIcon icon) {
		agent = panel.getAgent();
		agentPropertiesPanel = panel;
		this.icon = icon;
		this.width = icon.getIconWidth();
		this.height = icon.getIconHeight();
		
		setPosition(50, 20);
	}

	
	/** Constructor when loading from a file */
	public AgentGUI(String xmlStr, ImageIcon icon){
		//Set width and height based on icon
		this.icon = icon;
		this.width = icon.getIconWidth();
		this.height = icon.getIconHeight();
		
		//Load position from XML string
		loadFromXML(xmlStr);
	}


	/** Paints a representation of the agent using the supplied graphics environment */
	public void paintAgent( Graphics2D g2D){
		g2D.drawImage(icon.getImage(), this.x, this.y, null);
	
		//Draw agent name
		g2D.setPaint(Color.white);
		g2D.setFont(agentFont);
		g2D.drawString(agent.getName(), this.x , this.y + this.height + 15);
		
		if(selected){
			g2D.setPaint(Color.red);
			g2D.drawRect(this.x - 2, this.y - 2, icon.getIconWidth() + 2, icon.getIconHeight() + 2);
		}

		if(!agent.isEnabled()){
			g2D.setPaint(new Color(50, 50, 50, 150));
			g2D.fill3DRect(this.x, this.y, icon.getIconWidth(), icon.getIconHeight(), true);
		}
	}
	
	/** Translates the agent GUI with checks to make sure it does not go outside of the bounding rectangle */
	public void translate(int dx, int dy, Rectangle boundingRectangle){
		x += dx;
		y += dy;
		
		//Make sure new position does not exceed boundaries
		if(x < 0)
			x = 0;
		if(y < 0)
			y = 0;
		if(x + width > boundingRectangle.width)
			x = boundingRectangle.width -width;
		if(y + height > boundingRectangle.height)
			y = boundingRectangle.height - height;
	}
	

	//-------------------------------------------------------------------------------------
	//----------                      Accessor methods                          -----------
	//-------------------------------------------------------------------------------------

	protected void setSelected(boolean s){
		selected = s;
	}


	protected boolean isSelected(){
		return selected;
	}

	protected Point getCentre(){
		return new Point(this.x + this.width / 2, this.y + this.height/2);
	}

	protected char[] getName(){
		return agent.getName().toCharArray();
	}

	public Agent getAgent(){
		return agent;
	}

	public String getID(){
		return agent.getID();
	}


	public AgentPropertiesPanel getAgentPropertiesPanel(){
		return agentPropertiesPanel;
	}
	
	public void setAgentPropertiesPanel(AgentPropertiesPanel panel){
		this.agentPropertiesPanel = panel;
		agent = panel.getAgent();
	}


	public boolean isEnabled(){
		return agent.isEnabled();
	}


	public void setEnabled(boolean enabld){
		agent.setEnabled(enabld);
	}


	/** Returns a string containing an XML description of the graphical and MIDI aspects of the agent */
	public String getXML(String indent){
		String agentStr = indent + "<agent>";
		agentStr += indent + "\t<position>";
		agentStr += indent + "\t\t<x>" + this.x + "</x><y>" + this.y + "</y>";
		agentStr += indent + "\t</position>";
		agentStr += agentPropertiesPanel.getXML(indent + "\t");
		agentStr += agent.getXML(indent + "\t");
		agentStr += indent + "</agent>";
		return agentStr;
	}

	
	/** Loads parameters from the supplied XML string */
	public void loadFromXML(String xmlStr){
		try{
			Document xmlDoc = Util.getXMLDocument(xmlStr);
			int newXPos = Util.getIntParameter("x", xmlDoc);
			int newYPos = Util.getIntParameter("y", xmlDoc);
			setPosition(newXPos, newYPos);
		}
		catch(Exception ex){
			System.out.println(xmlStr);
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}

	
	public void setPosition(int x, int y){
		this.x = x;
		this.y = y;
	}


}

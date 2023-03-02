package eu.davidgamez.mas.gui;

//Java imports
import java.awt.*;

import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.gui.dialog.TrackDialog;
import eu.davidgamez.mas.midi.*;


public class TrackGUI {

	/** The track managed by this GUI */
	private Track midiTrack;

	/** MainFrame stored for launching track dialog */
	private MainFrame mainFrame;

	//Is the track selected?
	private boolean selected = false;

	//Is the track moving, i.e. being dragged around?
	private boolean moving = false;

	//The location of the centre track, which corresponds to its centre.
	private int xPos = 50, yPos = 50;

	//The size of the track
	private int size;
	private int buttonSize;

	//Create the rectangles with no size - this will be set later using the zoom level
	private Rectangle muteRect = new Rectangle(0, 0, 1, 1);
	private Rectangle soloRect = new Rectangle(0, 0, 1, 1);
	private Rectangle agVisRect = new Rectangle(0, 0, 1, 1);
	private Rectangle connVisRect = new Rectangle(0, 0, 1, 1);


	//Tweaks to get everything in the right position
	private int nameYOffset;
	private int buttonYOffset;
	private int channelYOffset;
	private int buttonLetterOffset;
	private int letterWidth;
	private int smallLetterWidth;
	private int border;

	//Variables controlling the zoom of this component
	/* Number of zoom levels The minimum zoom level is the furthest out, i.e.
     the smallest value for the size of the component. */
	final static int NUMBER_OF_ZOOM_LEVELS = 10;

	//The current zoom level
	private int zoomLevel = 5;

	//The state of different things.
	private boolean agentsVisible = true;
	private boolean connectionsVisible = true;

	private Font trackFont;
	private Font smallTrackFont;

	private static Color trackColor = new Color(197, 197, 0);

	/** Standard constructor */
	public TrackGUI(Track track, MainFrame mainFrame) {
		this.midiTrack = track;
		this.mainFrame = mainFrame;

		//Set up all the sizes and offsets to the current zoom level
		setZoomLevel();
	}
	
	
	/** Constructor when loading from a file */
	public TrackGUI(String xmlStr, MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		
		//Load up track parameters and MIDI track parameters from XML
		loadFromXML(xmlStr);

		//Set up all the sizes and offsets to the current zoom level
		setZoomLevel();
	}

	public void setBackgroundColour(Color newColor) {
		trackColor = newColor;
	}


	/** Returns true if the class contains the click position. */
	public boolean contains(int x, int y){
		double distanceFromCenter = Math.pow((double)x - (xPos + size/2), 2.0) + Math.pow((double)y - (yPos + size/2), 2.0);
		distanceFromCenter = Math.pow(distanceFromCenter, 0.5);
		if (distanceFromCenter < size / 2)
			return true;
		return false;
	}


	public boolean processMouseClicked(int mouseXPos, int mouseYPos, int clickCount){	
		//Check to see if the click position is inside any of the buttons.
		if(muteRect.contains(mouseXPos, mouseYPos)){
			if(midiTrack.muted())
				midiTrack.setMuted(false);
			else
				midiTrack.setMuted(true);
			return true;
		}
		else if(soloRect.contains(mouseXPos, mouseYPos)){
			if(midiTrack.soloed()){
				midiTrack.setSoloed(false);
			}
			else{
				midiTrack.setSoloed(true);
			}
			return true;
		}
		else if(agVisRect.contains(mouseXPos, mouseYPos)){
			if(agentsVisible){
				agentsVisible = false;
				connectionsVisible = false;//No point in drawing connections to invisible agents
			}
			else{
				agentsVisible = true;
				connectionsVisible = true;
			}
			return true;
		}
		else if(connVisRect.contains(mouseXPos, mouseYPos)){
			if(connectionsVisible)
				connectionsVisible = false;
			else if(agentsVisible)
				connectionsVisible = true;
			return true;
		}
		
		//Process double click event
		if (clickCount == 2) {
			//See if the click is within the circle
			if (this.contains(mouseXPos, mouseYPos)) {
				new TrackDialog(mainFrame, midiTrack, xPos, yPos);
				return true;
			} 
			else
				return false;
		}
		
		//Accept event if it is within this component's boundaries
		else if (this.contains(mouseXPos, mouseYPos))
			return true;
		
		//Event does not lie anywhere within this class
		return false;
	}


	public void setPosition(int newXPos, int newYPos){
		xPos = newXPos;
		yPos = newYPos;
		updateButtonRectangles();
	}


	//-------------------------------------------------------------------------------------
	//----------                        View methods                            -----------
	//-------------------------------------------------------------------------------------

	private void setZoomLevel(){
		size = zoomLevel * 20;

		nameYOffset = (int)Math.round(zoomLevel * -2.5);
		buttonYOffset = -1 * zoomLevel;
		channelYOffset = 6 * zoomLevel;
		buttonLetterOffset = (int)Math.round(zoomLevel / 1.2);
		letterWidth = (int)Math.round(zoomLevel * 1.2);
		smallLetterWidth = (int)Math.round(zoomLevel * 0.85);
		border = (int)Math.round(zoomLevel / 2.0);

		muteRect.width = (int)Math.round(zoomLevel * 3.4);
		muteRect.height = (int)Math.round(zoomLevel * 3.4);
		soloRect.width = (int)Math.round(zoomLevel * 3.4);
		soloRect.height = (int)Math.round(zoomLevel * 3.4);
		agVisRect.width = (int)Math.round(zoomLevel * 3.4);
		agVisRect.height = (int)Math.round(zoomLevel * 3.4);
		connVisRect.width = (int)Math.round(zoomLevel * 3.4);
		connVisRect.height = (int)Math.round(zoomLevel * 3.4);

		//Set up fonts
		trackFont = new Font("Arial", Font.PLAIN, (int)Math.round(zoomLevel * 2.5));
		smallTrackFont = new Font("Arial", Font.PLAIN, (int)Math.round(zoomLevel * 2.0));

		updateButtonRectangles();
	}
	
	public void translate(int dx, int dy, Rectangle boundingRectangle){
		xPos += dx;
		yPos += dy;

		//Prevent from going out of bounds
		if(xPos < 0)
			xPos = 0;
		if(yPos < 0)
			yPos = 0;
		if(xPos + size > boundingRectangle.width)
			xPos = boundingRectangle.width - size;
		if(yPos + size > boundingRectangle.height)
			yPos = boundingRectangle.height - size;
		
		updateButtonRectangles();
	}
	

	//-------------------------------------------------------------------------------------
	//----------                      Accessor methods                          -----------
	//-------------------------------------------------------------------------------------

	protected Point getCentre(){
		return new Point(xPos + size / 2, yPos + size/2);
	}

	//Returns the MIDI track associated with this track GUI
	protected Track getTrack() {
		return midiTrack;
	}

	public int getX(){
		return xPos;
	}

	public int getY(){
		return yPos;
	}
	
	public int getWidth(){
		return size;
	}

	public int getHeight(){
		return size;
	}


	//Returns the unique ID shared by this track GUI and its associated MIDI track
	protected String getID(){
		return midiTrack.getID();
	}


	protected boolean isMoving(){
		return moving;
	}

	public boolean isSelected(){
		return selected;
	}

	public void setMoving(boolean mving){
		moving = mving;
	}

	public void setSelected(boolean b){
		selected = b;
	}

	public boolean showAgents(){
		return agentsVisible;
	}

	public boolean showConnections(){
		return connectionsVisible;
	}

	//-------------------------------------------------------------------------------------
	//----------                        Paint Method                            -----------
	//-------------------------------------------------------------------------------------

	/* Paints the track using the provided graphics environment. */
	protected void paintTrack(Graphics2D g2D){
		//Draw track background
		if(selected)
			g2D.setColor(Color.red);
		else
			g2D.setColor(Color.blue);
		g2D.fillOval(xPos, yPos, size, size);
		g2D.setColor(trackColor);
		g2D.fillOval(xPos + border, yPos + border, size - border*2, size - border*2);

		//Draw track name in centre
		g2D.setColor(Color.black);
		int nameLength = midiTrack.getName().length();
		if(nameLength > 12){
			g2D.setFont(smallTrackFont);
			g2D.drawString(midiTrack.getName(), xPos + size/2 - nameLength/2 * smallLetterWidth , yPos + size/2 + nameYOffset);
		}
		else{
			g2D.setFont(trackFont);
			g2D.drawString(midiTrack.getName(), xPos + size/2 - nameLength/2 * letterWidth , yPos + size/2 + nameYOffset);
		}

		//Draw buttons
		g2D.setFont(trackFont);
		if(midiTrack.muted())
			g2D.setColor(MASLookAndFeel.getTrackMuteOnColor());
		else
			g2D.setColor(MASLookAndFeel.getTrackMuteOffColor());
		g2D.fillRect(muteRect.x, muteRect.y, muteRect.width, muteRect.height);
		g2D.setColor(Color.white);
		g2D.drawString("M", muteRect.x + buttonLetterOffset, muteRect.y + muteRect.height - buttonLetterOffset);

		if(midiTrack.soloed())
			g2D.setColor(MASLookAndFeel.getTrackSoloOnColor());
		else
			g2D.setColor(MASLookAndFeel.getTrackSoloOffColor());
		g2D.fillRect(soloRect.x, soloRect.y, soloRect.width, soloRect.height);
		g2D.setColor(Color.white);
		g2D.drawString("S", soloRect.x + buttonLetterOffset, soloRect.y + soloRect.height - buttonLetterOffset);

		g2D.setColor(MASLookAndFeel.getTrackAgentVisibleColor());
		g2D.fillRect(agVisRect.x, agVisRect.y, agVisRect.width, agVisRect.height);
		g2D.setColor(Color.white);
		g2D.drawString("A", agVisRect.x + buttonLetterOffset, agVisRect.y + agVisRect.height - buttonLetterOffset);
		if(!agentsVisible){
			g2D.setColor(Color.red);
			g2D.drawLine(agVisRect.x, agVisRect.y, agVisRect.x + agVisRect.width, agVisRect.y + agVisRect.height);
			g2D.drawLine(agVisRect.x + agVisRect.width, agVisRect.y, agVisRect.x, agVisRect.y + agVisRect.height);
		}

		g2D.setColor(MASLookAndFeel.getTrackConnectionVisibleColor());
		g2D.fillRect(connVisRect.x, connVisRect.y, connVisRect.width, connVisRect.height);
		g2D.setColor(Color.white);
		g2D.drawString("C", connVisRect.x + buttonLetterOffset, connVisRect.y + connVisRect.height - buttonLetterOffset);
		if(!connectionsVisible){
			g2D.setColor(Color.red);
			g2D.drawLine(connVisRect.x, connVisRect.y, connVisRect.x + connVisRect.width, connVisRect.y + connVisRect.height);
			g2D.drawLine(connVisRect.x + connVisRect.width, connVisRect.y, connVisRect.x, connVisRect.y + connVisRect.height);
		}

		//Draw MIDI track number
		g2D.setColor(Color.black);
		int midiChannel = midiTrack.getChannel() + 1;//MIDI values are from 0-15; display values are from 1-16.
		if(midiChannel < 10)
			g2D.drawString(Integer.toString(midiChannel), xPos + size/2 - 3, yPos + size/2 + channelYOffset);
		else
			g2D.drawString(Integer.toString(midiChannel), xPos + size/2 - 5, yPos + size/2 + channelYOffset);

	}


	//-------------------------------------------------------------------------------------
	//----------                  Saving and Loading Methods                    -----------
	//-------------------------------------------------------------------------------------

	/** Returns the GUI and MIDI track information in XML format */
	public String getXML(String indent){
		String trackStr = indent + "<track>";
		trackStr += indent + "\t<position>";
		trackStr += indent + "\t\t<x>" + this.xPos + "</x><y>" + this.yPos + "</y>";
		trackStr += indent + "\t</position>";
		trackStr += midiTrack.getXML(indent  + "\t");
		trackStr += indent + "</track>";
		return trackStr;
	}
	
	
	/** Loads the track GUI's parameters from the XML string and uses XML to create MIDI track */
	public void loadFromXML(String xmlStr){
		//Load the details about the MIDI track
		String midiTrackStr = xmlStr.substring(xmlStr.indexOf("<midi_track>"), xmlStr.indexOf("</midi_track>") + 13);
		midiTrack = new Track(midiTrackStr);
		
		//Load GUI parameters
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


	//-------------------------------------------------------------------------------------
	//----------                    Other Private Methods                       -----------
	//-------------------------------------------------------------------------------------

	private void updateButtonRectangles(){
		int yLoc = yPos + size / 2 + buttonYOffset;
		muteRect.x = xPos + size/2 - muteRect.width - soloRect.width;
		muteRect.y = yLoc;

		soloRect.x = xPos + size/2 - soloRect.width;
		soloRect.y = yLoc;

		agVisRect.x = xPos + size/2;
		agVisRect.y = yLoc;

		connVisRect.x = xPos + size/2 + agVisRect.width;
		connVisRect.y = yLoc;
	}

}



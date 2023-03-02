package eu.davidgamez.mas.agents.pianoroll.gui;

//Java imports
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.agents.pianoroll.midi.PianoRollAgent;
import eu.davidgamez.mas.exception.MASAgentException;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.MIDINote;


public class PianoRollEditor extends JPanel implements Constants, MouseListener, MouseMotionListener, ChangeListener {

	/** Mode in which pencil is used to add notes */
	static public final int PENCIL_MODE = 0;
	
	/** Mode in which eraser is used to erase notes */
	static public final int ERASER_MODE = 1;
	
	/** The minimum length a note is allowed to be  */
	static private final int MIN_NOTE_LENGTH_PPQ = 20;
	
	/** Number of beats visible for editing */
	private int numberOfBeats = PianoRollAgent.DEFAULT_NUMBER_OF_BEATS;
	
	/** The current tool mode */
	private int toolMode = PENCIL_MODE;
	
	/** The length of the notes that are added */
	private int noteLength_ppq = 960;
	
	/** Array of possible vertical zoom levels */
	private int [] verticalZoomArray = {5,10,15,20,25,30,35};
	
	/** The current vertical zoom level */ 
	private int verticalZoomLevel = 3;
	
	/** Array of possible horizontal zoom levels */
	private int [] horizontalZoomArray = {25,50,75,100,150,200,250};
	
	/** The current horizontal zoom level */
	private int horizontalZoomLevel = 3;
	
	/** Height of the top header containing the beat numbers */
	private int TOP_HEADER_HEIGHT = 20;
	
	/** Width of the left header containing the note names */
	private int LEFT_HEADER_WIDTH = 30;
	
	/** Current width of the panel */
	private int width = 10;
	
	/** Current height of the panel */
	private int height = 10;
	
	/** Font used for writing the beat numbers */
	private Font beatFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	
	/** Font used for writing the pitches */
	private Font pitchFont = new Font(Font.SERIF, Font.PLAIN, 12);
	
	/** Whether we are editing a note or not */
	private boolean noteEditMode = false;
	
	/** The note being edited in edit mode */
	private MIDINote editNote;
	
	/** Time stamp of the note being edited */
	private long editNoteTimeStamp = -1;
	
	/** The type of note edit being carried out */
	private int editType = 0;
	
	/** Editing the side of the note */
	static final private int LEFT_EDIT = 1;
	
	/** Editing the side of the note */
	static final private int TOP_EDIT = 2;
	
	/** Editing the side of the note */
	static final private int RIGHT_EDIT = 3;
	
	/** Editing the side of the note */
	static final private int BOTTOM_EDIT = 4;
	
	/** Agent whose notes are being edited */
	private PianoRollAgent agent = null;
	
	/** Degree of approximation when deciding if mouse is over a note */
	static private final int NOTE_EDIT_APPROXIMATION = 2;
	
	/** Start position on X axis for measuring mouse drags */
	int startXPos;
	
	/** Start position on Y axis for measuring mouse drags */
	int startYPos;
	
	/** Cursor used in pencil mode */
	private Cursor pencilCursor;
	
	/** Cursor used in eraser mode */
	private Cursor eraserCursor;
	
	
	/** Constructor */
	public PianoRollEditor(){
		setPreferredSize(new Dimension(width, height));
		setBackground(Color.white);
		calculateSize();
		addMouseListener(this);
		addMouseMotionListener(this);
		
		//Create cursors
		try{
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			pencilCursor = toolkit.createCustomCursor( toolkit.getImage(Globals.getImageDirectory().getPath() + "/pencil_cursor.gif"), new Point (0,30), "PencilCursor");
			eraserCursor = toolkit.createCustomCursor( toolkit.getImage(Globals.getImageDirectory().getPath() + "/eraser_cursor.gif"), new Point (0,30), "EraserCursor");
		}
		catch(Exception ex){
			MsgHandler.error(ex);
		}
	}
	
	
	/*--------------------------------------------------------------*/
	/*-------               PUBLIC METHODS                    ------*/
	/*--------------------------------------------------------------*/
	
	/** Works out and sets the size of the panel given the zoom level */
	private void calculateSize(){
		width = LEFT_HEADER_WIDTH + numberOfBeats * horizontalZoomArray[horizontalZoomLevel];
		height = TOP_HEADER_HEIGHT + 127 * verticalZoomArray[verticalZoomLevel];
		setPreferredSize(new Dimension(width, height));
		this.revalidate();
		this.repaint();
	}
	
	
	/** Deletes all notes */
	public void deleteAllNotes(){
		agent.deleteAllNotes();
		this.repaint();
	}
	
	
	@Override 
	public void paint(Graphics g){
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D)g;

		//Work out the current width and height of the lines
		int currentPitchHeight = verticalZoomArray[verticalZoomLevel];
		int currentBeatWidth = horizontalZoomArray[horizontalZoomLevel];
		
		//Draw beat numbers
		g2D.setPaint(Color.red);
		g2D.setFont(beatFont);
		for(int i=0; i<numberOfBeats; ++i){
			g2D.drawString(String.valueOf(i+1), LEFT_HEADER_WIDTH + i*currentBeatWidth - 4 , TOP_HEADER_HEIGHT - 2);
		}
		
		//Draw pitch names
		g2D.setPaint(Color.black);
		g2D.setFont(pitchFont);
		for(int i=0; i<127; ++i){
			int yPos = (127-i)*currentPitchHeight  + TOP_HEADER_HEIGHT - 2;
			g2D.drawString(NOTE_NAME_ARRAY[i%12] + i/12, 1, yPos);
		}
		
		//Draw horizontal lines
		g2D.setPaint(Color.black);
		for(int i=0; i<=127; ++i){
			g2D.drawLine(LEFT_HEADER_WIDTH, TOP_HEADER_HEIGHT + i*currentPitchHeight, width, TOP_HEADER_HEIGHT + i*currentPitchHeight);
		}
		
		//Draw vertical lines
		g2D.setPaint(Color.red);
		for(int i=0; i<=numberOfBeats; ++i){
			g2D.drawLine(LEFT_HEADER_WIDTH + i*currentBeatWidth, TOP_HEADER_HEIGHT, LEFT_HEADER_WIDTH +  i*currentBeatWidth, height);
		}
		
		//Check agent has been set
		if(agent == null){
			System.out.println("AGENT MISSING");
			return;
		}
		
		//Draw notes
		g2D.setPaint(new Color(255, 0, 255));
		TreeMap<Long, ArrayList<MIDINote>>  noteMap = agent.getNotes();
		for(Long timeStamp : noteMap.keySet()){
			int noteXPos = (int) Math.round( LEFT_HEADER_WIDTH + currentBeatWidth * (timeStamp.doubleValue() / PPQ_RESOLUTION) );
			ArrayList<MIDINote> tmpList = noteMap.get(timeStamp);
			for(MIDINote note : tmpList){
				int noteYPos = TOP_HEADER_HEIGHT + (126-note.pitch) * currentPitchHeight;
				int noteWidth = (int) Math.round( currentBeatWidth * ( (double)note.length / (double)PPQ_RESOLUTION) );
				g2D.fillRect(noteXPos+1, noteYPos+1, noteWidth-1, currentPitchHeight-1);
			}
		}
	}
	
	
	/** Passes the agent held in the panel to this class */
	public void setAgent(PianoRollAgent agent){
		this.agent = agent;
		agent.addChangeListener(this);
	}
	
	
	/** Sets the horizontal zoom level */
	public void setHorizontalZoomLevel(int zoomLevel) throws MASAgentException{
		if(zoomLevel > horizontalZoomArray.length)
			throw new MASAgentException("Horizontal zoom level out of range");
		horizontalZoomLevel = zoomLevel;
		calculateSize();
	}
	
	
	/** Sets the length of notes created by the pencil tool */
	public void setNoteLength(int noteLength_ppq){
		this.noteLength_ppq = noteLength_ppq;
	}
	
	
	/** Sets the number of beats in the piano roll. Deletes beats greater than this number if they exist */
	public void setNumberOfBeats(int numberOfBeats){
		this.numberOfBeats = numberOfBeats;
		calculateSize();
		
		//Ignore deleting beats if we are in the constructor
		if(agent == null)
			return;
		
		//Erase any notes that start outside of the beat window
		TreeMap<Long, ArrayList<MIDINote>>  noteMap = agent.getNotes();
		Iterator<Long> keyIter = noteMap.keySet().iterator();
		while(keyIter.hasNext()){
			Long key = keyIter.next();
			if(key.intValue() / PPQ_RESOLUTION > (numberOfBeats-1) )
				keyIter.remove();
		}
		
		//Erase any notes that finish outside of the beat window
		for(Long timeStamp : noteMap.keySet()){
			ArrayList<MIDINote> tmpArrList = noteMap.get(timeStamp);
			Iterator<MIDINote> iter = tmpArrList.iterator();
			while(iter.hasNext()){
				MIDINote tmpNote = iter.next();
				if( (timeStamp + tmpNote.length)/PPQ_RESOLUTION > (numberOfBeats) ){
					System.out.println("first tib: " + ((timeStamp + tmpNote.length)/PPQ_RESOLUTION) + " second bit " + (numberOfBeats));
					iter.remove();
				}
			}
		}
	}
	
	
	/** Sets the offset in ms */
	public void setOffset_ms(int offset_ms){
		agent.setOffset(offset_ms);
	}
	
	
	/** Sets the snap used  in ppq */
	public void setSnap(int snapDistance_ppq){
		agent.setSnapDistance_ppq(snapDistance_ppq);
	}
	
	
	/** Controls whether the pencil or eraser is used and sets the cursor appropriately */
	public void setToolMode(int newToolMode) {
		if(newToolMode != PENCIL_MODE && newToolMode != ERASER_MODE)
			MsgHandler.error("Tool mode does not exist.");
		this.toolMode = newToolMode;
		
		//Set the cursor appropriate to the mode
		if(toolMode == PENCIL_MODE)
			setCursor(pencilCursor);
		else if(toolMode == ERASER_MODE)
			setCursor(eraserCursor);
	}
	
	
	/** Sets the vertical zoom level */
	public void setVerticalZoomLevel(int zoomLevel) throws MASAgentException{
		if(zoomLevel > verticalZoomArray.length)
			throw new MASAgentException("Vertical zoom level out of range");
		verticalZoomLevel = zoomLevel;
		calculateSize();
	}
	
	
	/*--------------------------------------------------------------*/
	/*-------            MOUSE-HANDLING METHODS               ------*/
	/*--------------------------------------------------------------*/
	
	@Override
	public void mouseClicked(MouseEvent ev) {
		int xPos = ev.getX();
		int yPos = ev.getY();
		int currentPitchHeight = verticalZoomArray[verticalZoomLevel];
		int currentBeatWidth = horizontalZoomArray[horizontalZoomLevel];
	
		//Handle case where we are editing a note
		if(noteEditMode){
			return;
		}
		
		//Handle case where we are in pencil mode
		if(toolMode == PENCIL_MODE){
			long timeStamp = getTimeStamp(xPos);
			int pitch = getPitch(yPos);
			if(timeStamp >=0 && (timeStamp + noteLength_ppq) < (PPQ_RESOLUTION * numberOfBeats) && pitch >=0 && pitch <= 127)
				agent.addNote(getSnapTimeStamp((int)timeStamp), new MIDINote(pitch, noteLength_ppq));
			return;
		}
		
		//Handle case where we are in eraser mode
		if(toolMode == ERASER_MODE){	
			//Look for note
			TreeMap<Long, ArrayList<MIDINote>>  noteMap = agent.getNotes();
			for(Long timeStamp : noteMap.keySet()){
				int noteXPos = (int) Math.round( LEFT_HEADER_WIDTH + currentBeatWidth * (timeStamp.doubleValue() / PPQ_RESOLUTION) );
				ArrayList<MIDINote> tmpList = noteMap.get(timeStamp);
				Iterator<MIDINote> iter = tmpList.iterator();
				while(iter.hasNext()){
					MIDINote note = iter.next();
					int noteYPos = TOP_HEADER_HEIGHT + (126-note.pitch) * currentPitchHeight;
					int noteWidth = (int) Math.round( currentBeatWidth * ( (double)note.length / (double)PPQ_RESOLUTION) );
					if(xPos > noteXPos && xPos < (noteXPos + noteWidth) && yPos > noteYPos && yPos < (noteYPos + currentPitchHeight)){
						iter.remove();
						this.repaint();
						return;
					}
				}
			}
			//No note found, so just return
			return;
		}
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
	}


	@Override
	public void mouseExited(MouseEvent arg0) {
	}


	@Override
	public void mousePressed(MouseEvent ev) {
	}


	@Override
	public void mouseReleased(MouseEvent arg0) {
	}


	@Override
	public void mouseDragged(MouseEvent ev) {
		int xPos = ev.getX();
		int yPos = ev.getY();
		if(xPos < LEFT_HEADER_WIDTH)
			xPos = LEFT_HEADER_WIDTH;
		
		if(noteEditMode){
			switch(editType){
				case LEFT_EDIT:				
					//Find the note that we are editing
					ArrayList<MIDINote> tmpArrayList = agent.getNotes().get(editNoteTimeStamp);
					if(tmpArrayList == null || tmpArrayList.isEmpty()){
						MsgHandler.error("Cannot find MIDI note for specified time step");
						return;
					}
					
					//Find the note and move it
					Iterator<MIDINote> iter = tmpArrayList.iterator();
					while(iter.hasNext()){
						MIDINote note = iter.next();
						if(note == editNote){
							long newTimeStamp = getSnapTimeStamp((int)getTimeStamp(xPos));
							if(newTimeStamp >=0 && newTimeStamp + note.length <= (PPQ_RESOLUTION * numberOfBeats)){
								iter.remove();
								agent.addNote(newTimeStamp, note);
								editNoteTimeStamp = newTimeStamp;
							}
							break;
						}
					}
				break;
				case RIGHT_EDIT:
					long noteLengthChange_ppq = toPPQ(xPos - startXPos);
					if(editNote.length + noteLengthChange_ppq > MIN_NOTE_LENGTH_PPQ)
						editNote.length += noteLengthChange_ppq;
					break;
				case TOP_EDIT:
				case BOTTOM_EDIT: {
					int newPitch = getPitch(yPos);
					if(newPitch >= 0 && newPitch <= 127)
						editNote.pitch = newPitch;

				}
				break;
				default: 
					MsgHandler.error("Edit mode is not recognized.");
			}
		}
		startXPos = xPos;
		startYPos = yPos;
		this.repaint();
	}


	@Override
	public void mouseMoved(MouseEvent ev) {
		startXPos = ev.getX();
		startYPos = ev.getY();
		
		//Determine if we should enter the note edit mode
		checkNoteIntersection(ev.getX(), ev.getY());
		
		if(noteEditMode){
			switch(editType){
				case LEFT_EDIT:
					setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
				break;
				case RIGHT_EDIT:
					setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
					break;
				case TOP_EDIT:
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				break;
				case BOTTOM_EDIT:
					setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
				break;
				default: 
					MsgHandler.error("Edit mode is not recognized.");
			}
		}
		else{
			setToolMode(toolMode);
		}
	}
	
	
	/*--------------------------------------------------------------*/
	/*-------               PRIVATE METHODS                   ------*/
	/*--------------------------------------------------------------*/
	
	/** Checks to see if the mouse is over one of the note edges */
	private void checkNoteIntersection(int xPos, int yPos){
		//Work out the current width and height of the lines
		int currentPitchHeight = verticalZoomArray[verticalZoomLevel];
		int currentBeatWidth = horizontalZoomArray[horizontalZoomLevel];
		
		//Reset note edit mode - will be set to true if we have an intersection with a note
		noteEditMode = false;
		editType = 0;
		editNote = null;
		editNoteTimeStamp = -1;
		
		TreeMap<Long, ArrayList<MIDINote>>  noteMap = agent.getNotes();
		for(Long timeStamp : noteMap.keySet()){
			int noteXPos = (int) Math.round( LEFT_HEADER_WIDTH + currentBeatWidth * (timeStamp.doubleValue() / PPQ_RESOLUTION) );
			ArrayList<MIDINote> tmpList = noteMap.get(timeStamp);
			for(MIDINote note : tmpList){
				int noteYPos = TOP_HEADER_HEIGHT + (126-note.pitch) * currentPitchHeight;
				int noteWidth = (int) Math.round( currentBeatWidth * ( (double)note.length / (double)PPQ_RESOLUTION) );
				
				//Left edge of note
				if( ( xPos >= (noteXPos-NOTE_EDIT_APPROXIMATION) && xPos <= (noteXPos+NOTE_EDIT_APPROXIMATION) ) && ( yPos >= noteYPos && yPos <= (noteYPos+currentPitchHeight) ) ){
					noteEditMode = true;
					editNote = note;
					editNoteTimeStamp = timeStamp;
					editType = LEFT_EDIT;
					return;
				}
				//Right edge of note
				else if( ( xPos >= (noteXPos+noteWidth-NOTE_EDIT_APPROXIMATION) && xPos <= (noteXPos+noteWidth+NOTE_EDIT_APPROXIMATION) )  && ( yPos >= noteYPos && yPos <= (noteYPos+currentPitchHeight) ) ){
					noteEditMode = true;
					editNote = note;
					editNoteTimeStamp = timeStamp;
					editType = RIGHT_EDIT;
					return;
				}
				//Top edge of note
				else if( ( xPos >= (noteXPos) && xPos <= (noteXPos+noteWidth) )  && ( yPos >= (noteYPos-NOTE_EDIT_APPROXIMATION) && yPos <= (noteYPos+NOTE_EDIT_APPROXIMATION) ) ){
					noteEditMode = true;
					editNote = note;
					editNoteTimeStamp = timeStamp;
					editType = TOP_EDIT;
					return;
				}
				//Bottom edge of note
				else if( ( xPos >= (noteXPos) && xPos <= (noteXPos+noteWidth) )  && ( yPos >= (noteYPos+currentPitchHeight-NOTE_EDIT_APPROXIMATION) && yPos <= (noteYPos+currentPitchHeight+NOTE_EDIT_APPROXIMATION) ) ){
					noteEditMode = true;
					editNote = note;
					editNoteTimeStamp = timeStamp;
					editType = BOTTOM_EDIT;
					return;
				}
			}
		}
	}

	
	/** Returns the pitch for a y position */
	private int getPitch(int yPos){
		if(yPos < TOP_HEADER_HEIGHT)
			return -1;
		int currentPitchHeight = verticalZoomArray[verticalZoomLevel];
		return 126 - (int) Math.floor ( ( (double)yPos - TOP_HEADER_HEIGHT) / currentPitchHeight);
	}
	
	
	/** Returns the time stamp rounded to the nearest snap distance */
	private long getSnapTimeStamp(int timeStamp){
		int snapDistance_ppq = agent.getSnapDistance_ppq();
		
		if(snapDistance_ppq <= 0)
			return timeStamp;
		
		int remainder = timeStamp % snapDistance_ppq;
		if(remainder == 0)
			return timeStamp;
		if( ((double)remainder / (double)snapDistance_ppq) > 0.5)
			return snapDistance_ppq * (timeStamp / snapDistance_ppq) + snapDistance_ppq;
		else
			return snapDistance_ppq * (timeStamp / snapDistance_ppq);
	}
	
	
	/** Returns the time stamp in ppq for an x position */
	private long getTimeStamp(int xPos){
		if(xPos < LEFT_HEADER_WIDTH)
			return -1;
		int currentBeatWidth = horizontalZoomArray[horizontalZoomLevel];
		return Math.round( PPQ_RESOLUTION  * ( ( (double)xPos - LEFT_HEADER_WIDTH )  / (double)currentBeatWidth ) );
	}


	/** Converts a distance on the x axis into a PPQ value */
	private long toPPQ(int xDistance){
		int currentBeatWidth = horizontalZoomArray[horizontalZoomLevel];
		return Math.round( PPQ_RESOLUTION  * ( (double)xDistance  / (double)currentBeatWidth ) );
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		repaint();
	}
	
}

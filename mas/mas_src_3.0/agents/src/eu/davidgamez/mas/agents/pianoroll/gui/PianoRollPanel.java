package eu.davidgamez.mas.agents.pianoroll.gui;

//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
 
//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.agents.pianoroll.midi.PianoRollAgent;
import eu.davidgamez.mas.exception.MASAgentException;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Agent;


public class PianoRollPanel extends AgentPropertiesPanel implements ActionListener, Constants{

	/** Maximum number of beats */
	private static final int MAX_NUMBER_OF_BEATS = 48;
	
	/** Sets the number of editable beats */
	private JComboBox beatCombo = new JComboBox();
	
	/** Sets whether the agent synchronizes its output to the bar markers */
	private JCheckBox syncToBarMarkerCB = new JCheckBox("Sync to bar markers");
	
	/** Graphical editor of the notes in the agent */
	private PianoRollEditor pianoRollEditor = new PianoRollEditor();
	
	/** Allows scrolling around the piano roll editor */
	private JScrollPane scrollPane = null;
	
	/** Sets the vertical zoom of the piano roll editor */
	private JComboBox verticalZoomCombo = new JComboBox();
	
	/** Sets the horizontal zoom of the piano roll editor */
	private JComboBox horizontalZoomCombo = new JComboBox();
	
	/** Controls the snapping of the piano roll editor */
	private JComboBox snapCombo = new JComboBox();

	/** Sets the length of note that is added in the piano roll editor */
	private JComboBox noteLengthCombo = new JComboBox();

	/** Sets the tool mode to pencil */
	private JButton pencilButton;
	
	/** Sets the tool mode to erase */
	private JButton eraserButton;
	
	/** Deletes all notes in the piano roll editor */
	private JButton deleteAllNotesButton;
	
	/** Toggles record mode */
	private JButton recordButton;
	
	/** Sets properties of panel */
	private JButton propertiesButton;
	

	/** Constructor */
	public PianoRollPanel() {
		super("PianoRoll");
		
		//Organize whole layout
		Box mainBox = Box.createVerticalBox();
		
		//Fill beat combo with numbers to select length of piano roll
		for(int i=1; i< MAX_NUMBER_OF_BEATS; ++i)
			beatCombo.addItem(i);
		beatCombo.setSelectedIndex(PianoRollAgent.DEFAULT_NUMBER_OF_BEATS - 1);
		beatCombo.addActionListener(this);
		
		//Set up zoom combos
		addZoomLevels(verticalZoomCombo);
		verticalZoomCombo.addActionListener(this);
		addZoomLevels(horizontalZoomCombo);
		horizontalZoomCombo.addActionListener(this);
		
		//Set up note length combo
		for(int i=16; i>1; --i){	//Add Fractions
			noteLengthCombo.addItem("1/" + String.valueOf(i));
		}
		int noteLengthStartIndex = noteLengthCombo.getItemCount();
		for(int i=1; i<=16; ++i){
			noteLengthCombo.addItem(String.valueOf(i));
		}
		noteLengthCombo.setSelectedIndex(noteLengthStartIndex);
		noteLengthCombo.addActionListener(this);
		
		//Set up sync to bar marker check box
		syncToBarMarkerCB.addActionListener(this);
		
		//Set up snap combo
		int selectIndex = 0;
		snapCombo.addItem("off");
		for(int i=16; i>1; --i){	//Add Fractions
			snapCombo.addItem("1/" + String.valueOf(i));
			if(i==4)
				selectIndex = snapCombo.getItemCount()-1;
		}
		snapCombo.setSelectedIndex(selectIndex);
		snapCombo.addItem("1");
		snapCombo.addActionListener(this);
				
		//Set up tools
		pencilButton = new JButton(new ImageIcon(Globals.getImageDirectory().getPath() + "/pencil_cursor_small.gif"));
		pencilButton.setBackground(Color.red);
		pencilButton.addActionListener(this);
		eraserButton = new JButton(new ImageIcon(Globals.getImageDirectory().getPath() + "/eraser_cursor_small.gif"));
		eraserButton.addActionListener(this);
		eraserButton.setBackground(Color.white);
		deleteAllNotesButton = new JButton(new ImageIcon(Globals.getImageDirectory().getPath() + "/delete_all_icon.gif"));
		deleteAllNotesButton.addActionListener(this);
		
		//Set up record button
		recordButton = new JButton(new ImageIcon(Globals.getImageDirectory().getPath() + "/record_icon.png"));
		recordButton.addActionListener(this);
		
		//Set up properties button
		propertiesButton = new JButton("P");
		propertiesButton.addActionListener(this);
		
		//Add tools to top of panel
		Box comboBox = Box.createHorizontalBox();
		comboBox.add(new JLabel(" Beats: "));
		comboBox.add(beatCombo);
		comboBox.add(Box.createHorizontalStrut(10));
		comboBox.add(pencilButton);
		comboBox.add(eraserButton);
		comboBox.add(deleteAllNotesButton);
		comboBox.add(new JLabel(" Snap:"));
		comboBox.add(snapCombo);
		comboBox.add(new JLabel(" Note length:"));
		comboBox.add(noteLengthCombo);
		comboBox.add(Box.createHorizontalStrut(10));
		comboBox.add(syncToBarMarkerCB);
		comboBox.add(Box.createHorizontalStrut(10));
		comboBox.add(new JLabel(" V-Zoom: "));
		comboBox.add(verticalZoomCombo);
		comboBox.add(new JLabel(" H-Zoom: "));
		comboBox.add(horizontalZoomCombo);
		comboBox.add(Box.createHorizontalStrut(10));
		comboBox.add(recordButton);
		comboBox.add(Box.createHorizontalStrut(10));
		comboBox.add(propertiesButton);
		comboBox.add(Box.createHorizontalGlue());
		mainBox.add(comboBox);
		mainBox.add(Box.createVerticalStrut(10));
		
		//Add editor to main part of panel
		scrollPane = new JScrollPane(pianoRollEditor);
		scrollPane.setPreferredSize(new Dimension(1000, 800));
		mainBox.add(scrollPane);
		
		//Finish everything off
		this.add(mainBox, BorderLayout.CENTER);
	}

	
	/*--------------------------------------------------------------*/
	/*-------               PUBLIC METHODS                    ------*/
	/*--------------------------------------------------------------*/

	@Override
	public void actionPerformed(ActionEvent e) {
		try{
			if(e.getSource() == beatCombo){
				//Set number of beats in the piano roll editor
				pianoRollEditor.setNumberOfBeats(beatCombo.getSelectedIndex() + 1);
				
				//Set number of beats in the agent
				getPianoRollAgent().setNumberOfBeats(beatCombo.getSelectedIndex() + 1);
			}
			else if(e.getSource() == verticalZoomCombo){
				pianoRollEditor.setVerticalZoomLevel(verticalZoomCombo.getSelectedIndex());
			}
			else if(e.getSource() == horizontalZoomCombo){
				pianoRollEditor.setHorizontalZoomLevel(horizontalZoomCombo.getSelectedIndex());
			}
			else if(e.getSource() == pencilButton){
				pianoRollEditor.setToolMode(PianoRollEditor.PENCIL_MODE);
				pencilButton.setBackground(Color.red);
				eraserButton.setBackground(Color.white);
			}
			else if(e.getSource() == eraserButton){
				pianoRollEditor.setToolMode(PianoRollEditor.ERASER_MODE);
				pencilButton.setBackground(Color.white);
				eraserButton.setBackground(Color.red);
			}
			else if(e.getSource() == deleteAllNotesButton){
				int saveCurrentProjectResponse = JOptionPane.showConfirmDialog(this, "Do you want to delete all notes?", "Delete All Notes",
						JOptionPane.YES_NO_CANCEL_OPTION);
				if (saveCurrentProjectResponse == JOptionPane.YES_OPTION)
					pianoRollEditor.deleteAllNotes();
			}
			else if(e.getSource() == noteLengthCombo){
				pianoRollEditor.setNoteLength(getNoteLength());
			}
			else if (e.getSource() == snapCombo){
				int tmpSnapDist = getSnapPPQ();
				getPianoRollAgent().setSnapDistance_ppq(tmpSnapDist);
			}
			else if (e.getSource() == syncToBarMarkerCB){
				getPianoRollAgent().setSyncToBarMarker(syncToBarMarkerCB.isSelected());
			}
			else if (e.getSource() == recordButton){
				if(getPianoRollAgent().isRecording()){
					getPianoRollAgent().setRecording(false);
					recordButton.setBackground(Color.black);
					recordButton.setForeground(Color.black);
				}
				else{
					getPianoRollAgent().setRecording(true);
					recordButton.setBackground(Color.red);
					recordButton.setForeground(Color.red);
				}
			}
			else if(e.getSource() == propertiesButton){
				PropertiesDialog propDlg = new PropertiesDialog(getPianoRollAgent().getOffset_ms());
				if(propDlg.wasAccepted()){
					getPianoRollAgent().setOffset(propDlg.getOffset_ms());	
				}
			}
		}
		catch(Exception ex){
			MsgHandler.error(ex);
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
		//Load up number of beats
		int numBeats = getPianoRollAgent().getNumberOfBeats();
		if(numBeats < 1 || numBeats > MAX_NUMBER_OF_BEATS)
			throw new MASAgentException("Number of beats is out of range: " + numBeats);
		beatCombo.removeActionListener(this);//Avoid triggering a save event
		beatCombo.setSelectedIndex(numBeats - 1);
		pianoRollEditor.setNumberOfBeats(numBeats);
		beatCombo.addActionListener(this);
		
		//Load up snap distance
		snapCombo.removeActionListener(this);
		setSnapDistance(getPianoRollAgent().getSnapDistance_ppq());
		snapCombo.addActionListener(this);
		
		//Load up sync to bar marker
		syncToBarMarkerCB.removeActionListener(this);
		if(getPianoRollAgent().getSyncToBarMarker())
			syncToBarMarkerCB.setSelected(true);
		else
			syncToBarMarkerCB.setSelected(false);
		syncToBarMarkerCB.addActionListener(this);
	}


	@Override
	public void loadFromXML(String arg0) throws MASXmlException {	
	}


	@Override
	public boolean okButtonPressed() {
		return true;
	}
	
	
	@Override
	 public void setAgent(Agent agent) throws MASAgentException {
		 super.setAgent(agent);
		 pianoRollEditor.setAgent(getPianoRollAgent());
	 }
	

	/*--------------------------------------------------------------*/
	/*-------               PRIVATE METHODS                   ------*/
	/*--------------------------------------------------------------*/
	
	/** Adds zoom levels to the specified combo box */
	private void addZoomLevels(JComboBox combo){
		combo.addItem("25%");
		combo.addItem("50%");
		combo.addItem("75%");
		combo.addItem("100%");
		combo.addItem("150%");
		combo.addItem("200%");
		combo.addItem("250%");
		combo.setSelectedIndex(3);
	}
	
	
	/** Finds the value of a note frequency within the combo box */
	private int getNoteLength() {
		String tmpString = (String)noteLengthCombo.getSelectedItem();
		if (tmpString.indexOf("/") >= 0) { //This is a fraction
			int denominator = Integer.parseInt(tmpString.substring(tmpString.indexOf("/") + 1, tmpString.length()));
			return PPQ_RESOLUTION / denominator;
		}
		else { //This is a whole number of beats
			return Integer.parseInt(tmpString) * PPQ_RESOLUTION;
		}
	}
	
	
	/** Returns an appropriately cast reference to the MIDI agent associated with this interface */
	private eu.davidgamez.mas.agents.pianoroll.midi.PianoRollAgent getPianoRollAgent(){
		return (eu.davidgamez.mas.agents.pianoroll.midi.PianoRollAgent) agent;
	}

	
	/** Returns the snap distance in PPQ resolution */
	private int getSnapPPQ(){
		String snapStr = (String)snapCombo.getSelectedItem();
		if(snapStr.equals("off"))
				return -1;
		else if (snapStr.equals("1"))
			return PPQ_RESOLUTION;
		
		//Should be a fraction
		int denominator = Integer.parseInt(snapStr.substring(snapStr.indexOf("/") + 1, snapStr.length()));
		return PPQ_RESOLUTION / denominator;
	}
	
	
	/** Sets the combo to the specified snap distance */
	private void setSnapDistance(int distance){
		//Snap is off
		if(distance < 0){
			snapCombo.setSelectedIndex(0);
			return;
		}
		
		//Distance is 1
		if(distance == PPQ_RESOLUTION){
			snapCombo.setSelectedIndex(snapCombo.getItemCount() - 1);
			return;
		}
		
		//Find matching snap distance
		for(int i=16; i>1; --i){
			if( (PPQ_RESOLUTION / i) == distance){
				snapCombo.setSelectedIndex(17-i);
				return;
			}
		}
		MsgHandler.error("Cannot find snap combo index for snap distance: " + distance);
	}
	
}

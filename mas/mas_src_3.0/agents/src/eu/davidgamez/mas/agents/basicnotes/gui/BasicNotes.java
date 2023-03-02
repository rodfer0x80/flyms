package eu.davidgamez.mas.agents.basicnotes.gui;

//Java imports
import javax.swing.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.TreeMap;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.BufferedWriter;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;
import eu.davidgamez.mas.midi.MIDINote;


public class BasicNotes extends AgentPropertiesPanel implements Constants, ActionListener {
	private Box verticalBox;
	private JButton reloadButton, clearButton;

	private ArrayList<JComboBox> wholeNoteComboList = new ArrayList<JComboBox>();
	private ArrayList<JComboBox> fractionComboList = new ArrayList<JComboBox>();
	private ArrayList<JComboBox> pitchComboList = new ArrayList<JComboBox>();
	private ArrayList<JComboBox> octaveComboList = new ArrayList<JComboBox>();
	private ArrayList<JComboBox> lengthComboList = new ArrayList<JComboBox>();

	private Vector<String> wholeNoteVector = new Vector<String> ();
	private Vector<String> fractionVector = new Vector<String> ();
	private Vector<String> pitchVector = new Vector<String> ();
	private Vector<String> octaveVector = new Vector<String> ();
	private Vector<String> lengthVector = new Vector<String> ();

	//Note pitch 0 is C so start with this
	String notePitchArray[] = new String[] {"C", "Db", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"};
	private int numNotes = 12;
	private JCheckBox synchroniseToBarCB;
	int noteLength1Index = 0;

	
	public BasicNotes() {
		super("BasicNotes");
		
		//Create vertical box to organise dialog
		verticalBox = Box.createVerticalBox();

		//Add option to control whether emphasis points and synchronized to the bar
		Box synchHBox = Box.createHorizontalBox();
		synchroniseToBarCB = new JCheckBox("Synchronise notes to bar markers");
		synchHBox.add(synchroniseToBarCB);
		synchHBox.add(Box.createHorizontalGlue());
		verticalBox.add(synchHBox);
		verticalBox.add(Box.createVerticalStrut(10));

		//Fill vectors defining the possible note lengths and velocity changes
		fillNotePositionVectors();
		fillNotePitchVector();
		fillNoteLengthVector();

		//Add combo boxes to select the note emphasis points
		for (int i = 0; i < numNotes; ++i) {
			Box horizontalBox = Box.createHorizontalBox();
			horizontalBox.add(Box.createHorizontalStrut(30));
			JLabel tempLabel = new JLabel("Beat ");
			horizontalBox.add(tempLabel);

			JComboBox tempWholeCombo = new JComboBox(wholeNoteVector);
			wholeNoteComboList.add(tempWholeCombo);
			horizontalBox.add(tempWholeCombo);

			JComboBox tempFractionCombo = new JComboBox(fractionVector);
			fractionComboList.add(tempFractionCombo);
			horizontalBox.add(tempFractionCombo);

			JComboBox tempPitchCombo = new JComboBox(pitchVector);
			pitchComboList.add(tempPitchCombo);
			horizontalBox.add(tempPitchCombo);

			JComboBox tempOctaveCombo = new JComboBox(octaveVector);
			octaveComboList.add(tempOctaveCombo);
			horizontalBox.add(tempOctaveCombo);

			JComboBox tempLengthCombo = new JComboBox(lengthVector);
			lengthComboList.add(tempLengthCombo);
			horizontalBox.add(tempLengthCombo);
			horizontalBox.add(Box.createHorizontalGlue());
			verticalBox.add(horizontalBox);
		}

		//Add reset button
		verticalBox.add(Box.createVerticalStrut(10));
		Box resetButtonBox = Box.createHorizontalBox();
		reloadButton = createButton("Reload");
		resetButtonBox.add(reloadButton);
		clearButton = createButton("Clear");
		resetButtonBox.add(clearButton);
		verticalBox.add(resetButtonBox);

		//Finish everything off
		verticalBox.add(Box.createVerticalStrut(5));
		this.add(verticalBox, BorderLayout.CENTER);

	}


	public void loadAgentProperties() throws Exception {
		//Reset all combos to default
		resetPanel();

		//Set synchronise to bar check box
		synchroniseToBarCB.setSelected(getBasicNotesAgent().getSynchToBar());

		//Get the arrays from the agent
		int[] notePositionArray = getBasicNotesAgent().getNotePositionArray();
		int[] pitchArray = getBasicNotesAgent().getNotePitchArray();
		int[] noteLengthArray = getBasicNotesAgent().getNoteLengthArray();

		//Run a couple of checks
		if (notePositionArray.length > wholeNoteComboList.size())
			throw new Exception("Note array is greater than number of combos in agent panel!");
		if(pitchArray.length != notePositionArray.length || pitchArray.length != noteLengthArray.length)
			throw new Exception("Array lengths do not match up");

		//Set the combos appropriately
		for (int i = 0; i < notePositionArray.length; ++i) {
			//Set the whole note emphasis combo
			int wholeNoteComponent_beats = notePositionArray[i] / PPQ_RESOLUTION;
			wholeNoteComboList.get(i).setSelectedIndex(wholeNoteComponent_beats + 1); //Numbers range from - to 12 so add 1 to go from bar length to index

			//Set the fraction emphasis combo
			int fractionComponent_ppq = notePositionArray[i] % PPQ_RESOLUTION;
			fractionComboList.get(i).setSelectedIndex(getFractionIndexFromPPQ(fractionComponent_ppq));

			//Set the pitch and octave combo
			pitchComboList.get(i).setSelectedIndex(getPitchIndex(pitchArray[i]));
			octaveComboList.get(i).setSelectedIndex(getOctaveIndex(pitchArray[i]));

			//Set the length combo
			lengthComboList.get(i).setSelectedIndex(getNoteLengthIndex(noteLengthArray[i]));
		}
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
		if(e.getSource() == reloadButton){
			try{
				this.loadAgentProperties();
			}
			catch(Exception exception){
				exception.printStackTrace();
			}
			return;
		}
		//Works through combos and resets everything to index 0 and 100% velocity
		else if(e.getSource() == clearButton){
			resetPanel();
		}
	}


	private boolean setAgentProperties(){
		//Get a list of ppq points that are to be emphasised along with the degree of emphasis
		TreeMap<Integer, MIDINote> noteTreeMap = new TreeMap<Integer, MIDINote>();

		//Check that array lists are the same length
		try{
			if (wholeNoteComboList.size() != fractionComboList.size() || wholeNoteComboList.size() != pitchComboList.size() || wholeNoteComboList.size() != octaveComboList.size() || wholeNoteComboList.size() != lengthComboList.size())
				throw new Exception("Combo lists not the same size");
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}

		//Work through combos and add the lengths of any that are not selected
		for (int i = 0; i < wholeNoteComboList.size(); ++i) {
			//Get the indexes from the combos
			int pitchIndex = pitchComboList.get(i).getSelectedIndex();
			int octaveIndex = octaveComboList.get(i).getSelectedIndex();
			int wholeNoteIndex = wholeNoteComboList.get(i).getSelectedIndex();

			//Check that a beat has been selected
			if (wholeNoteIndex != 0) {

				//#FIXME# Could run a few checks at a later point

				//Get the emphasis point
				int notePoint_ppq = getNotePoint_ppq(wholeNoteComboList.get(i), fractionComboList.get(i));

				//Add note to tree list
				MIDINote tempMIDINote = new MIDINote(pitchIndex + octaveIndex * 12, getNoteLength((String)lengthComboList.get(i).getSelectedItem()));
				noteTreeMap.put(new Integer(notePoint_ppq), tempMIDINote);
			}
		}

		//Set the note emphasis points in the midi agent
		getBasicNotesAgent().setNotes(noteTreeMap);

		//Set the synchronise to bar staus
		getBasicNotesAgent().setSynchToBar(synchroniseToBarCB.isSelected());
		return true;
	}


	private void fillNotePositionVectors(){
		//Add an unselected value
		wholeNoteVector.add("-");

		//Add whole notes to whole note vectors
		for (int i = 1; i <= 12; ++i) {
			wholeNoteVector.add(String.valueOf(i));
		}

		/*Want to add fraction note intervals of 1/4, 1/3,1/2, 3/4,  2/3 etc. */
		fractionVector.add("0");
		fractionVector.add("1/16");
		fractionVector.add("1/9");
		fractionVector.add("3/16");
		fractionVector.add("1/8");
		fractionVector.add("5/16");
		fractionVector.add("1/6");
		fractionVector.add("1/4");
		fractionVector.add("1/3");
		fractionVector.add("7/16");
		fractionVector.add("1/2");
		fractionVector.add("5/8");
		fractionVector.add("9/16");
		fractionVector.add("2/3");
		fractionVector.add("11/16");
		fractionVector.add("5/6");
		fractionVector.add("3/4");
		fractionVector.add("13/16");
		fractionVector.add("7/8");
		fractionVector.add("8/9");
		fractionVector.add("15/16");
	}

	//Finds a location of the fraction in the fraction vector
	private int getFractionIndexFromPPQ(int fractionComponent_ppq) throws Exception{
		if(fractionComponent_ppq == 0)
			return 0;

		//Work through fraction vector looking for a fraction that matches the fraction component
		for(int i=1; i< fractionVector.size(); i++){
			String fractionText = (String) fractionVector.get(i);
			int numerator = Integer.parseInt(fractionText.substring(0, fractionText.indexOf("/")));
			int denominator = Integer.parseInt(fractionText.substring(fractionText.indexOf("/") + 1, fractionText.length()));
			if((numerator * PPQ_RESOLUTION) / denominator == fractionComponent_ppq){
				return i;
			}
		}
		throw new Exception ("Fraction component not found: " + fractionComponent_ppq);
	}

	//Finds whether the note pitch number is A, B, C# etc.
	/* Notes start at 0 with C, then 12, then 24 etc up to 60 */
	private int getPitchIndex(int notePitch){
		return notePitch % 12;
	}

	private int getOctaveIndex(int notePitch){
		return notePitch / 12;
	}


	private void fillNotePitchVector() {
		//Add descriptions of the notes to the vector
		for (int i = 0; i < notePitchArray.length; ++i) {
			pitchVector.add(notePitchArray[i]);
		}
		//Fill octave vector
		for(int i=0; i<=9; ++i)
			octaveVector.add(String.valueOf(i));
	}

	private int getNoteLengthIndex(int noteLength) throws Exception{
		for(int i=0; i< lengthVector.size(); ++i){
			String lengthString = (String) lengthVector.get(i);
			if(lengthString.indexOf("/") >= 0){//This is a fraction
				int denominator = Integer.parseInt(lengthString.substring(lengthString.indexOf("/") + 1, lengthString.length()));
				if ((PPQ_RESOLUTION / denominator) == noteLength){
					return i;
				}
			}
			else { //This is a whole number of beats
				if((Integer.parseInt(lengthString) * PPQ_RESOLUTION) == noteLength){
					return i;
				}
			}
		}
		throw new Exception("Note length not found!");
	}

	//Finds the value of a note length
	private int getNoteLength(String lengthString) {
		if (lengthString.indexOf("/") >= 0) { //This is a fraction
			int denominator = Integer.parseInt(lengthString.substring(lengthString.indexOf("/") + 1, lengthString.length()));
			return PPQ_RESOLUTION / denominator;
		}
		else { //This is a whole number of beats
			return Integer.parseInt(lengthString) * PPQ_RESOLUTION;
		}
	}

	private void fillNoteLengthVector(){
		//Add Fractions
		for (int i = 16; i > 1; --i) {
			lengthVector.add("1/" + String.valueOf(i));
		}
		noteLength1Index = lengthVector.size();
		for (int i = 1; i <= 16; ++i) {
			lengthVector.add(String.valueOf(i));
		}
	}

	/* Note: this uses integer division, so will round off */
	private int getNotePoint_ppq(JComboBox wholeNoteCombo, JComboBox fractionCombo) { //Extract the ppq value of the string in the combo box and set the text field with this value
		int wholeNote = Integer.parseInt( (String) wholeNoteCombo.getSelectedItem());
		//Subtract 1 because basic notes midi agents works in terms of offset from the beat
		wholeNote--;
		String fractionText = (String) fractionCombo.getSelectedItem();
		if (fractionText.indexOf("/") >= 0) { //This is a fraction, not zero
			int numerator = Integer.parseInt(fractionText.substring(0, fractionText.indexOf("/")));
			int denominator = Integer.parseInt(fractionText.substring(fractionText.indexOf("/") + 1, fractionText.length()));
			return wholeNote * PPQ_RESOLUTION + (numerator * PPQ_RESOLUTION) / denominator;
		}
		else { //This is a whole number of beats
			return wholeNote * PPQ_RESOLUTION;
		}
	}

	private void resetPanel(){
		for (JComboBox tempCombo : wholeNoteComboList) {
			tempCombo.setSelectedIndex(0);
		}
		for (JComboBox tempCombo : fractionComboList) {
			tempCombo.setSelectedIndex(0);
		}
		for (JComboBox tempCombo : pitchComboList) {
			tempCombo.setSelectedIndex(0);
		}
		for(JComboBox tempCombo : octaveComboList){
			tempCombo.setSelectedIndex(5);
		}
		for(JComboBox tempCombo : lengthComboList){
			tempCombo.setSelectedIndex(noteLength1Index);
		}
	}


	private JButton createButton (String label){
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(80,  20));
		button.addActionListener(this);
		return button;
	}
	
	private eu.davidgamez.mas.agents.basicnotes.midi.BasicNotes getBasicNotesAgent(){
		return (eu.davidgamez.mas.agents.basicnotes.midi.BasicNotes)agent;
	}

	/** Returns an XML string with the parameters of the panel */
	public String getXML(String indent) {
		String panelStr = indent + "<agent_panel>";
		panelStr += super.getXML(indent + "\t");
		panelStr += indent + "</agent_panel>";
		return panelStr;
	}
	
	
	public void loadFromXML(String agentPropertiesString) throws MASXmlException{
	}

}

package eu.davidgamez.mas.agents.noteemphasis.gui;

//Java imports
import javax.swing.*;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JRadioButton;
import java.util.TreeMap;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.BufferedWriter;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;


public class NoteEmphasis extends AgentPropertiesPanel implements ActionListener, Constants {
	private Box verticalBox;
	private JButton reloadButton, clearButton;
	private ArrayList<JComboBox> wholeNoteComboList = new ArrayList<JComboBox>();
	private ArrayList<JComboBox> fractionComboList = new ArrayList<JComboBox>();
	private ArrayList<JComboBox> velocityComboList = new ArrayList<JComboBox>();
	private Vector<String> wholeNoteVector = new Vector<String>();
	private Vector<String> fractionVector = new Vector<String>();
	private Vector<String> velocityVector = new Vector<String>();
	private int velocity100Index = -1;//Index of 100 % in the velocity vector
	private int numEmphasisPoints = 12;
	private JCheckBox synchroniseToBarCB;

	
	public NoteEmphasis() {
		super("NoteEmphasis");
		
		//Create vertical box to organise dialog
		verticalBox = Box.createVerticalBox();

		//Add option to control whether emphasis points and synchronized to the bar
		Box synchHBox = Box.createHorizontalBox();
		synchroniseToBarCB = new JCheckBox("Synchronise emphasis to bar markers");
		synchHBox.add(synchroniseToBarCB);
		synchHBox.add(Box.createHorizontalGlue());
		verticalBox.add(synchHBox);
		verticalBox.add(Box.createVerticalStrut(10));

		//Fill vectors defining the possible note lengths and velocity changes
		fillNoteEmphasisVectors();
		fillVelocityVector();

		//Add combo boxes to select the note emphasis points
		for (int i = 0; i < numEmphasisPoints; ++i) {
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
			JComboBox tempVelocityCombo = new JComboBox(velocityVector);
			velocityComboList.add(tempVelocityCombo);
			horizontalBox.add(tempVelocityCombo);
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
		synchroniseToBarCB.setSelected(getNoteEmphasisAgent().getSynchToBar());

		//Set combos appropriately
		int[] noteEmphasisArray = getNoteEmphasisAgent().getNoteEmphasisArray();
		int[] velocityEmphasisArray = getNoteEmphasisAgent().getVelocityEmphasisArray();
		if (noteEmphasisArray.length > wholeNoteComboList.size())
			throw new Exception("Note emphasis array is greater than number of combos in agent panel!");
		if(velocityEmphasisArray.length != noteEmphasisArray.length)
			throw new Exception("Velocity emphasis array length does not match note emphasis array length");

		for (int i = 0; i < noteEmphasisArray.length; ++i) {
			//Set the whole note emphasis combo
			int wholeNoteComponent_beats = noteEmphasisArray[i] / PPQ_RESOLUTION;
			wholeNoteComboList.get(i).setSelectedIndex(wholeNoteComponent_beats); //Numbers range from 0 to 11 so can go directly from bar length to index

			//Set the fraction emphasis combo
			int fractionComponent_ppq = noteEmphasisArray[i] % PPQ_RESOLUTION;
			fractionComboList.get(i).setSelectedIndex(getFractionIndexFromPPQ(fractionComponent_ppq));

			//Set the velocity emphasis combo
			velocityComboList.get(i).setSelectedIndex(getVelocityIndex(velocityEmphasisArray[i]));
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
		TreeMap<Integer, Integer> noteEmphasisPoints = new TreeMap<Integer, Integer>();

		//Check that array lists are the same length
		try{
			if (wholeNoteComboList.size() != fractionComboList.size())
				throw new Exception("Whole note and combo lists not the same size");
			if(wholeNoteComboList.size() != velocityComboList.size())
				throw new Exception("Whole note list and velocity percentage lists are not the same size");
		}
		catch(Exception e){
			e.printStackTrace();
		}

		//Work through combos and add the lengths of any that are not selected
		for (int i = 0; i < wholeNoteComboList.size(); ++i) {
			int emphasisPoint_ppq = getEmphasisPoint_ppq(wholeNoteComboList.get(i), fractionComboList.get(i));
			//Find the velocity for this emphasis point
			String velPercentText = (String) velocityComboList.get(i).getSelectedItem();
			if (velPercentText.indexOf("100") < 0) {
				//Check that emphasis point has not already been added to the array
				for (Integer tempInt : noteEmphasisPoints.keySet()) {
					if (tempInt.intValue() == emphasisPoint_ppq) {
						JOptionPane.showMessageDialog(null, "Cannot add two emphasis points in the same place!", "Duplicate values error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
				//Add emphasis point
				int tempInt = Integer.parseInt(velPercentText.substring(0, velPercentText.length() - 1));
				noteEmphasisPoints.put(new Integer(emphasisPoint_ppq), new Integer(tempInt));
			}
		}

		//Set the note emphasis points in the midi agent
		getNoteEmphasisAgent().setNoteEmphasis(noteEmphasisPoints);

		//Set the synchronise to bar staus
		getNoteEmphasisAgent().setSynchToBar(synchroniseToBarCB.isSelected());
		return true;
	}


	private void fillNoteEmphasisVectors(){
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
		throw new Exception ("Fraction component not found");
	}

	private int getVelocityIndex(int velocityValue) throws Exception{
		String searchVelText = String.valueOf(velocityValue);
		for(int i=0; i<velocityVector.size(); ++i){
			String velocityText = (String)velocityVector.get(i);
			if(velocityText.substring(0, velocityText.length() -1).equals(searchVelText))
				return i;
		}
		throw new Exception("Velocity text not found");
	}

	private void fillVelocityVector(){
		for(int i=10; i< 200; i += 10){
			velocityVector.add(String.valueOf(i) + "%");
			if(i == 100)
				velocity100Index = velocityVector.size() - 1;
		}
	}

	/* Note: this uses integer division, so will round off */
	private int getEmphasisPoint_ppq(JComboBox wholeNoteCombo, JComboBox fractionCombo) { //Extract the ppq value of the string in the combo box and set the text field with this value
		int wholeNote = Integer.parseInt( (String) wholeNoteCombo.getSelectedItem());
		wholeNote = wholeNote -1;//First note in the bar is actually marked as zero not 1
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
		for (JComboBox tempCombo : velocityComboList) {
			tempCombo.setSelectedIndex(velocity100Index);
		}
	}


	private JButton createButton (String label){
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(80,  20));
		button.addActionListener(this);
		return button;
	}
	
	
	private eu.davidgamez.mas.agents.noteemphasis.midi.NoteEmphasis getNoteEmphasisAgent(){
		return (eu.davidgamez.mas.agents.noteemphasis.midi.NoteEmphasis)agent;
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


}

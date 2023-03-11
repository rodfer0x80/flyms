package eu.davidgamez.mas.agents.barmarker.midi;

//Java imports
import java.util.ArrayList;

import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Util;
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.Agent;
import eu.davidgamez.mas.midi.AgentMessage;
import eu.davidgamez.mas.midi.Track;
import eu.davidgamez.mas.midi.Utilities;


public class BarMarker extends Agent implements Constants{

	/** Length of the current bar initialised to the default*/
	private int barLength_beats = 4; 
	
	/** Count within the current bar */
	private int beatCount = 0; 
	
	/** Controls whether bars are fixed to determinate lenghts or random lengths */
	private boolean fixedLengthBars = true; 
	
	/** Array of bar lengths */
	private int [] fixedBarLengthsArray = new int []{4};
	
	/** Tracks which bar length we are currently using */
	private int fixedBarLengthCounter = 0;
	
	/** Minimum random bar length */
	private int minRandomBarLength = 2;
	
	/** Maximum random bar length */
	private int maxRandomBarLength = 2;

	/** The maximum possible bar length */
	private int maxBarLength_beats = 12;

	
	/** Constructor */
	public BarMarker() {
		super("Bar marker", "Bar marker", "BarMarker");
	}

	
	@Override
	public String getXML(String indent){
		String tmpStr = indent + "<midi_agent>";
		tmpStr += super.getXML(indent + "\t");
		tmpStr += indent + "\t<fixed_length_bars>" + fixedLengthBars + "</fixed_length_bars>";
		
		//Output fixed bar lengths
		tmpStr += indent + "\t<fixed_bar_lengths>";
		for(int i=0; i<fixedBarLengthsArray.length - 1; ++i)
			tmpStr += fixedBarLengthsArray[i] + ",";
		tmpStr += fixedBarLengthsArray[fixedBarLengthsArray.length - 1] +  "</fixed_bar_lengths>";
		
		//Output min and max random bar length
		tmpStr += indent + "\t<min_random_bar_length>" + minRandomBarLength + "</min_random_bar_length>";
		tmpStr += indent + "\t<max_random_bar_length>" + maxRandomBarLength + "</max_random_bar_length>";
		tmpStr += indent + "</midi_agent>";
		return tmpStr;
	}


	@Override
	public void loadFromXML(String xmlStr) throws MASXmlException {
		  super.loadFromXML(xmlStr);
		  try{
			  Document xmlDoc = Util.getXMLDocument(xmlStr);
			  fixedLengthBars= Util.getBoolParameter("fixed_length_bars", xmlDoc);
			  fixedBarLengthsArray = Util.getIntArrayParameter("fixed_bar_lengths", xmlDoc);
			  minRandomBarLength = Util.getIntParameter("min_random_bar_length", xmlDoc);
			  maxRandomBarLength = Util.getIntParameter("max_random_bar_length", xmlDoc);
			  if(fixedBarLengthsArray.length > 0)
				  barLength_beats = fixedBarLengthsArray[0];
		  }
		  catch(Exception ex){
			  System.out.println(xmlStr);
			  ex.printStackTrace();
			  MsgHandler.error(ex.getMessage());
		  }
	}

	
	/** Sets the array of fixed bar lengths */
	public void setFixedBarLengths(ArrayList<Integer> tempArrayList){
		//First check to see if there are any bar lengths, if not, disable the agent
		if(tempArrayList.size() == 0){
			this.setEnabled(false);
			return;
		}
		//Array list contains bar lengths, so load them into an int array for quick and easy access
		fixedLengthBars = true;
		fixedBarLengthCounter = 0;
		fixedBarLengthsArray = new int[tempArrayList.size()];
		for(int i=0; i<tempArrayList.size(); ++i)
			fixedBarLengthsArray[i] = tempArrayList.get(i).intValue();
		barLength_beats = fixedBarLengthsArray[0];
	}

	
	public void setMaxBarLength(int maxBarLen){
		maxBarLength_beats = maxBarLen;
	}

	public void setRandomBarLengths(int minLen, int maxLen){
		fixedLengthBars = false;
		minRandomBarLength = minLen;
		maxRandomBarLength = maxLen;
	}

	public int [] getFixedBarLengths(){
		return fixedBarLengthsArray;
	}

	public int getMaxRandomBarLength(){
		return maxRandomBarLength;
	}

	public int getMinRandomBarLength(){
		return minRandomBarLength;
	}

	public boolean fixedLengthBars(){
		return fixedLengthBars;
	}


	//Main method called to request agent to add notes or messages to the buffer
	protected boolean updateTracks(long bufferStart_ppq, long bufferEnd_ppq){
		//Assume it starts with bufferStart_ppq = 0, so want to add first bar marker at this point
		if (bufferStart_ppq == 0)
			addBarMarker(0);

		//Need to find the first whole beat in the buffer
		long firstBeat_ppq = Utilities.getFirstBeatInBuffer(bufferStart_ppq);

		//Work through the buffer adding bar markers after every barLength beats
		for (long j = firstBeat_ppq; j < bufferEnd_ppq; j += PPQ_RESOLUTION) {
			//Add a bar marker if the beat count reaches the bar length
			if (beatCount == barLength_beats){
				addBarMarker(j);
				adjustBarLength();
			}

			//Increase the beat count
			beatCount++;
		}
		return true;
	}

	
	/** Adds a bar marker at a particular point in the buffer */
	private void addBarMarker(long trackPosition_ppq){
		//Goes through each track
		for(Track midiTrack : trackMap.values()){
			try {
				AgentMessage tempMessage = new AgentMessage(AgentMessage.START_BAR);
				tempMessage.setContents(Integer.toString(barLength_beats));
				midiTrack.addAgentMessage(trackPosition_ppq, tempMessage);
				beatCount = 0;//Reset beat count
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void adjustBarLength(){
		if(fixedLengthBars){
			++fixedBarLengthCounter;
			fixedBarLengthCounter %= fixedBarLengthsArray.length;
			barLength_beats = fixedBarLengthsArray[fixedBarLengthCounter];
			if(barLength_beats > maxBarLength_beats)
				barLength_beats = maxBarLength_beats;
		}
		else{
			double randomNum = Math.random();
			barLength_beats = (int)Math.rint((double)minRandomBarLength + ((double)maxRandomBarLength - (double)minRandomBarLength) * randomNum);
			if(barLength_beats > maxBarLength_beats)
				barLength_beats = maxBarLength_beats;
		}
	}


	@Override
	protected void reset() {
	}

}

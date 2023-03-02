package eu.davidgamez.mas;

/*----------------------------  StaticVariables ------------------------------*/
/** Holds variables that apply across the whole application. */
/* --------------------------------------------------------------------------- */
public interface Constants {
	
	/** Default screen width in pixels */
	final static int DEFAULT_WORK_AREA_WIDTH = 1200;
	
	/** Default screen height in pixels */
	final static int DEFAULT_WORK_AREA_HEIGHT = 800;
	
	/** Maximum zoom level of the working area */
	final static int ZOOM_RANGE = 6;
	
	/** Maximum zoom level of the working area */
	final static int MIN_ZOOM_LEVEL = 6; 
	
	/** Location of the configuration file */
	final static String CONFIG_FILE_LOCATION = "mas.conf";
	
	/** Location of default agent icon */
	final static String DEFAULT_AGENT_ICON = "DefaultAgentIcon.gif";

	/** Timing resolution expressed in pulses per quarter note */
	final static int PPQ_RESOLUTION = 960;

	/** Default tempo of the application */
	final static float DEFAULT_TEMPO_BPM = 120;

	// NOT SURE WHAT THESE ARE FOR OR WHETHER THEY ARE STILL NECESSARY!
	// final static byte BUFFER_UPDATE_MSG = 1;
	// final static byte MIDI_CLOCK_MSG = 2;

	/** Default buffer length  */
	final static int DEFAULT_BUFFER_LENGTH_BEATS = 2;
	
	/** Default number of buffers between load and play */
	final static int DEFAULT_BUFFER_HEADSTART_BUFFERS = 2;

	/** Default MIDI channel for creating tracks */
	final static int DEFAULT_MIDI_CHANNEL = 0;

	/** Number of buffers in the rotating buffer */
	final static int DEFAULT_NUMBER_OF_BUFFERS = 100;

	/** Synchronise MIDI to external source */
	final static int MIDI_SYNC = 1;

	/** Synchronise MIDI to internal clock */
	final static int INTERNAL_CLOCK = 2;
	
	/** No MIDI synchronisation */
	final static int NO_SYNC = 3;
	
	final static String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";

	/** Maximum value of OSC port */
	final static int OSC_PORT_MAX = 30000;
	
	/** Minimum value of an OSC port */
	final static int OSC_PORT_MIN = 1;
	 
	/** Default value of osc port */
	final static int DEFAULT_OSC_PORT = 10000;
	
	/** Array of note pitches */
	final static String NOTE_NAME_ARRAY[] = new String[] {"C", "Db", "D", "Eb", "E", "F", "F#", "G", "G#", "A", "Bb", "B"};
	
}

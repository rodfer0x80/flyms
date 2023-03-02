package eu.davidgamez.mas;

//Java imports
import java.awt.Rectangle;
import java.io.File;
import org.w3c.dom.Document;

//MAS imports
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.exception.MASFileException;
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.MsgHandler;


/*----------------------------  Globals ------------------------------*/
/** Holds dynamic variables that apply across the whole application. */
/*------------------------------------------------------------------ */
public class Globals implements Constants{
	
	/** Default screen width and height - can be altered by the configuration file */
	private static Rectangle workArea = new Rectangle(0, 0, DEFAULT_WORK_AREA_WIDTH, DEFAULT_WORK_AREA_HEIGHT);

	/** Current screen width */
	private static int screenWidth = 100;
	
	/** Current screen height */
	private static int screenHeight = 100;
	
	/** Records whether the project has been saved or not */
	private static boolean projectSaved = true;
	
	//FIXME: NOT SURE IF THIS IS NEEDED
	private static boolean projectOpen = false;

	/** Location of file containing stored settings */
	private static File configFile = new File(CONFIG_FILE_LOCATION);
	
	/** File that project is saved to. Should be set to null by a new project until project has been saved. */
	private static File projectFile = null;
	
	/** Location of default directory where project files are stored */
	private static File projectDirectory;
	
	/** Location of images for the application */
	private static File imageDirectory = new File("images");

	/** Default tempo. Can be adjusted using the configuration file */
	private static double defaultTempo = DEFAULT_TEMPO_BPM;
	
	/** Tempo of the sequencer */
	private static double tempo = DEFAULT_TEMPO_BPM;

	/** Records whether MAS can be played */
	private static boolean readyToPlay = false;
	
	/** Records whether MAS is actually playing */
	private static boolean playing = false;
	
	/** The number of buffers used by the application */
	private static int numberOfBuffers = DEFAULT_NUMBER_OF_BUFFERS;
	
	/** The number of nano seconds in each tick */
	private static double nanoSecPerTick = 60000000000l / (DEFAULT_TEMPO_BPM * PPQ_RESOLUTION);
		
	/** Master synchronisation mode of the sequencer 
	 	What the synchronizer synchronises to */
	private static int masterSyncMode = INTERNAL_CLOCK;
	
	/** Slave synchronization mode;
	 	What the synchronizer synchronizes from */
	private static int slaveSyncMode = NO_SYNC;
	
	/** Reference to the main frame for modal dialogs created within agents */
	private static MainFrame mainFrame;
		
	/*-----------------------------------------------------------------------*/
	/*------                    SAVING AND LOADING                     ------*/
	/*-----------------------------------------------------------------------*/
	
	/** Returns an XML string containing the global parameters */
	public static String getXML(String indent){
		//Start of Globals XML string
		String tmpXMLStr = indent + "<global_parameters>";
		
		//Save tempo
		tmpXMLStr += indent + "\t<tempo>" + getTempo() + "</tempo>";
	
		//Save synchronization
		tmpXMLStr += indent + "\t<master_sync>" + getMasterSyncMode() + "</master_sync>";
		tmpXMLStr += indent + "\t<slave_sync>" + getSlaveSyncMode() + "</slave_sync>";
		
		//Finish off and return string
		tmpXMLStr += indent + "</global_parameters>";
		return tmpXMLStr;
	}
	
	
	/** Loads the global parameters from the specified XML string */
	public static void loadFromXML(String xmlString){
		try{
			Document xmlDoc = Util.getXMLDocument(xmlString);
			setTempo(Util.getDoubleParameter("tempo", xmlDoc));
			setMasterSyncMode(Util.getIntParameter("master_sync", xmlDoc));
			setSlaveSyncMode(Util.getIntParameter("slave_sync", xmlDoc));
		}
		catch(Exception ex){
			ex.printStackTrace();
			MsgHandler.error(ex.getMessage());
		}
	}
	
	
	
    /*-----------------------------------------------------------------------*/
    /*------                      ACCESSOR METHODS                     ------*/
    /*-----------------------------------------------------------------------*/
	
	public static void setMasterSyncMode(int mode) {
		if(mode != MIDI_SYNC && mode != INTERNAL_CLOCK ){
			MsgHandler.error("Master MIDI Synchronization mode not recognized: " + mode);
			return;
		}
		masterSyncMode = mode;
	}
	
	public static void setSlaveSyncMode(int mode) {
		if(mode != MIDI_SYNC && mode != NO_SYNC){
			MsgHandler.error("Slave MIDI Synchronization mode not recognized: " + mode);
			return;
		}
		slaveSyncMode = mode;
	}
	
	
	public static MainFrame getMainFrame(){
		return mainFrame;
	}
	
	public static void setMainFrame(MainFrame mainFrame){
		Globals.mainFrame = mainFrame;
	}
	
	public static int getMasterSyncMode() {
		return masterSyncMode;
	}
	
	public static int getSlaveSyncMode() {
		return slaveSyncMode;
	}
	
	public static double getTempo() {
		return tempo;
	}

	public static void setTempo(double tempo) {
		Globals.tempo = tempo;
		Globals.nanoSecPerTick = 60000000000l / (tempo * PPQ_RESOLUTION);
	}

	public static double getNanoSecPerTick() {
		return nanoSecPerTick;
	}

	public static void setNanoSecPerTick(double nanoSecPerTick) {
		Globals.nanoSecPerTick = nanoSecPerTick;
	}

	public static int getNumberOfBuffers() {
		return numberOfBuffers;
	}

	public static void setNumberOfBuffers(int numberOfBuffers) {
		Globals.numberOfBuffers = numberOfBuffers;
	}

	public static boolean isPlaying(){
		return playing;
	}
	
	public static void setPlaying(boolean playing){
		Globals.playing =playing;
	}
	
	public static boolean isReadyToPlay () {
		return readyToPlay;
	}
	
	public static void setReadyToPlay(boolean readyToPlay) {
		Globals.readyToPlay = readyToPlay;
	}

	public static File getConfigFile() {
		return configFile;
	}

	public static void setConfigFile(File configFile) {
		Globals.configFile = configFile;
	}

	public static int getWorkAreaWidth() {
		return workArea.width;
	}
	

	public static void setWorkAreaWidth(int workAreaWidth) {
		Globals.workArea.width= workAreaWidth;
	}

	public static int getWorkAreaHeight() {
		return workArea.height;
	}
	

	public static void setWorkAreaHeight(int workAreaHeight) {
		Globals.workArea.height = workAreaHeight;
	}
	

	public static Rectangle getWorkArea(){
		return workArea;
	}
	

	public static double getDefaultTempo() {
		return defaultTempo;
	}

	public static void setDefaultTempo(double defaultTempo) {
		Globals.defaultTempo = defaultTempo;
	}

	public static int getScreenWidth() {
		return screenWidth;
	}

	public static void setScreenWidth(int screenWidth) {
		Globals.screenWidth = screenWidth;
	}

	public static int getScreenHeight() {
		return screenHeight;
	}

	public static void setScreenHeight(int screenHeight) {
		Globals.screenHeight = screenHeight;
	}

	public static boolean isProjectSaved() {
		return projectSaved;
	}

	public static void setProjectSaved(boolean projectSaved) {
		Globals.projectSaved = projectSaved;
	}

	public static boolean isProjectOpen() {
		return projectOpen;
	}

	public static void setProjectOpen(boolean projectOpen) {
		Globals.projectOpen = projectOpen;
	}

	public static File getProjectFile() {
		return projectFile;
	}

	public static void setProjectFile(File projectFile) {
		Globals.projectFile = projectFile;
	}

	public static File getProjectDirectory() {
		return projectDirectory;
	}

	public static void setProjectDirectory(File projectDirectory) {
		Globals.projectDirectory = projectDirectory;
	}

	public static File getImageDirectory() {
		return imageDirectory;
	}

	public static void setImageDirectory(File imageDirectory) {
		Globals.imageDirectory = imageDirectory;
	}
	
}

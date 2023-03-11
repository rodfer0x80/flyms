package eu.davidgamez.mas.gui;

//Java imports
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;

//MAS imports
import eu.davidgamez.mas.file.*;
import eu.davidgamez.mas.midi.*;
import eu.davidgamez.mas.*;
import eu.davidgamez.mas.action.*;
import eu.davidgamez.mas.event.ResetListener;


public class MainFrame extends JFrame implements ResetListener{
	//===================== INJECTED VARIABLES  ======================
	//The main panel where agents and tracks are displayed
	private MainPanel mainPanel;

	//Actions
	private AddAgentAction addAgentAction;
	private AddTrackAction addTrackAction;
	private AgentOrderAction agentOrderAction;
	private BufferPropertiesAction bufferPropertiesAction;
	private DeleteAgentAction deleteAgentAction;
	private DeleteTracksAction deleteTracksAction;
	private DevicePropertiesAction devicePropertiesAction;
	private EditOSCDevicesAction editOSCDevicesAction;
	private ExitAction exitAction;
	private KillNotesAction killNotesAction;
	private MIDISynchronizationAction midiSynchronizationAction;
	private NewProjectAction newProjectAction;
	private OpenProjectAction openProjectAction;
	private PlayAction playAction;
	private SaveProjectAction saveProjectAction;
	private SaveProjectAsAction saveProjectAsAction;
	private ShowMidiEventsAction showMidiEventsAction;
	private StopAction stopAction;
	private ZoomInAction zoomInAction;
	private ZoomOutAction zoomOutAction;

	//Toolbar
	private TrackCounter trackCounter;//Displays the progress with playing back the MIDI notes
	private LoadBufferCounter loadBufferCounter;//Displays the progress with filling the buffer
	private PlayBufferCounter playBufferCounter;//Displays the progress with filling the buffer
	private TempoTextField tempoTextField;//The tempo

	//File handling
	private ConfigLoader configLoader;

	//Injected here so that we can configure its properties
	private JFileChooser projectFileChooser;


	//============================  ORDINARY VARIABLES  ===========================
	//Menu of the frame and menu bar
	private JMenu fileMenu = new JMenu("File"), insertMenu = new JMenu("Track"), agentMenu = new JMenu("Agent"), midiMenu = new JMenu("MIDI"), viewMenu = new JMenu("View");
	private JMenu oscMenu = new JMenu("OSC");
	private JMenuBar menuBar = new JMenuBar();

	//Empty constructor of the main frame
	public MainFrame() {
	}

	public void showApplication(){
		//Set title and store reference to the main frame class
		this.setTitle("MIDI Agent System");

		//Pass reference to main frame to the static message handler
		MsgHandler.setMainFrame(this);

		//Load up configuration information from the settings file
		try{
			configLoader.loadSettings();
		}
		catch(IOException ex){
			MsgHandler.critical("Failed to load configuration from file, application will now exit. " + ex.getMessage());
			return;
		}

		//Set the look and feel to match the system look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) {
			MsgHandler.critical(e.getMessage());
		}

		//Set the look of the main frame
		this.setBackground(MASLookAndFeel.getBackground());
		this.setForeground(MASLookAndFeel.getForeground());
		this.setIconImage(MASLookAndFeel.getApplicationIcon().getImage());

		//Set up listener to handle when the frame is closed
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				applicationClosing();
			}
		});

		//Store width and height of application frame and set the initial size 
		Globals.setScreenWidth(Toolkit.getDefaultToolkit().getScreenSize().width);
		Globals.setScreenHeight(Toolkit.getDefaultToolkit().getScreenSize().height);
		this.setMinimumSize(new Dimension(Globals.getScreenWidth()/2, Globals.getScreenHeight()/2));
		
		//Store reference to the frame in globals for modal dialogs launched within agents
		Globals.setMainFrame(this);
		
		//this.setExtendedState(MAXIMIZED_BOTH);
		
		//Set up menus
		this.setJMenuBar(menuBar);
		menuBar.setBackground(MASLookAndFeel.getMenuBackground());
		menuBar.setForeground(MASLookAndFeel.getMenuForeground());

		setUpMenu(fileMenu);
		menuBar.add(fileMenu);
		fileMenu.add(getJMenuItem(newProjectAction));
		fileMenu.add(getJMenuItem(openProjectAction));
		fileMenu.addSeparator();
		fileMenu.add(getJMenuItem(saveProjectAction));
		fileMenu.add(getJMenuItem(saveProjectAsAction));
		fileMenu.addSeparator();
		fileMenu.add(getJMenuItem(exitAction));

		setUpMenu(insertMenu);
		menuBar.add(insertMenu);
		insertMenu.add(getJMenuItem(addTrackAction));
		insertMenu.add(getJMenuItem(deleteTracksAction));

		setUpMenu(agentMenu);
		menuBar.add(agentMenu);
		agentMenu.add(getJMenuItem(addAgentAction));
		agentMenu.add(getJMenuItem(deleteAgentAction));
		agentMenu.addSeparator();
		agentMenu.add(getJMenuItem(agentOrderAction));

		setUpMenu(midiMenu);
		menuBar.add(midiMenu);
		midiMenu.add(getJMenuItem(bufferPropertiesAction));
		midiMenu.addSeparator();
		midiMenu.add(getJMenuItem(devicePropertiesAction));
		midiMenu.add(getJMenuItem(midiSynchronizationAction));

		setUpMenu(oscMenu);
		menuBar.add(oscMenu);
		oscMenu.add(getJMenuItem(editOSCDevicesAction));
		
		setUpMenu(viewMenu);
		menuBar.add(viewMenu);
		viewMenu.add(getJMenuItem(showMidiEventsAction));

		//Set up tool bar
		JToolBar toolBar = new JToolBar();
		toolBar.setBackground(MASLookAndFeel.getToolBarColor());
		toolBar.setForeground(MASLookAndFeel.getToolBarColor());
		Box toolBarBox = Box.createHorizontalBox();
		toolBarBox.add(Box.createHorizontalStrut(5));
		toolBarBox.add(createJButton(playAction));
		toolBarBox.add(Box.createHorizontalStrut(2));
		toolBarBox.add(createJButton(stopAction));
		toolBarBox.add(Box.createHorizontalStrut(2));
		toolBarBox.add(createJButton(killNotesAction));
		toolBarBox.add(Box.createHorizontalStrut(2));
		toolBarBox.add(trackCounter);
		toolBarBox.add(Box.createHorizontalStrut(2));
		toolBarBox.add(loadBufferCounter);
		toolBarBox.add(Box.createHorizontalStrut(2));
		toolBarBox.add(playBufferCounter);
		toolBarBox.add(Box.createHorizontalStrut(2));
		toolBarBox.add(tempoTextField);
		//toolBarBox.add(Box.createHorizontalStrut(5));
		//toolBarBox.add(createJButton(zoomOutAction));
		//toolBarBox.add(Box.createHorizontalStrut(2));
		//toolBarBox.add(createJButton(zoomInAction));
		toolBarBox.add(Box.createHorizontalGlue());
		toolBar.add(toolBarBox);
		this.getContentPane().add(toolBar, BorderLayout.NORTH);

		//Set up main panel and scroll pane to hold it
		mainPanel.initialise();
		this.getContentPane().add(mainPanel.getScrollPane(), BorderLayout.CENTER);

		//Set up file chooser
		projectFileChooser.addChoosableFileFilter(new MASFileFilter(".proj", "Project files"));

		//Make visible
		this.setVisible(true);
	}


	/*------------------------------------------------------------------------*/
	/*-------                EVENT HANDLING METHODS                  ---------*/
	/*------------------------------------------------------------------------*/

	/** Repaints everything in response to reset action */    
	public void resetActionPerformed(){
		this.repaint();
	}

	/* Called when the window is closed.
         Carries out any final clearing up and saving */
	public void applicationClosing() {
		//Check to see if user wants to exit whilst application is playing
		if (Globals.isPlaying()) {
			if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this, "MAS is playing. Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION)) {
				return; //User has not clicked on yes, so do not want to continue with cleaning up
			}
		}
		
		//Check to see if user wants to exit without saving
		if (!Globals.isProjectSaved()) {
			if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(this, "Project has not been saved. Exit without saving?", "Project not saved", JOptionPane.YES_NO_OPTION)) {
				return; //User has not clicked on yes, so do not want to continue with cleaning up
			}
		}

		try {
			//Save settings changed by user, such as input and output ports
			configLoader.saveSettings();

			//Close MIDI devices
			MIDIDeviceManager.close();
		}
		catch (Exception e) {
			MsgHandler.critical("Error shutting down application: " + e.getMessage());
			e.printStackTrace();
		}

		//Exit from application
		this.dispose();
		System.exit(0);
	}


	/*-----------------------------------------------------------*/
	/*------                 PRIVATE METHODS              -------*/
	/*-----------------------------------------------------------*/

	/** Formats a menu */
	private void setUpMenu(JMenu menu){
		menu.setForeground(MASLookAndFeel.getMenuForeground());
		menu.setBackground(MASLookAndFeel.getMenuBackground());
		menu.getPopupMenu().setForeground(MASLookAndFeel.getPopupMenuForeground());
		menu.getPopupMenu().setBackground(MASLookAndFeel.getPopupMenuBackground());
		menu.getPopupMenu().setBorderPainted(MASLookAndFeel.isMenuBorderPainted());
	}


	/** Gets a button from an action */
	private JButton createJButton(Action action){
		JButton button = new JButton(action);
		button.setText("");
		button.setBackground(MASLookAndFeel.getButtonBackground());
		button.setForeground(MASLookAndFeel.getButtonForeground());
		button.setBorder(MASLookAndFeel.getButtonBorder());
		button.setFocusPainted(false);
		button.setFocusable(false);
		return button;
	}


	//Gets a menu item from an action.
	private JMenuItem getJMenuItem(Action action){
		JMenuItem menuItem = new JMenuItem((String)action.getValue("NAME"));
		menuItem.setBackground(MASLookAndFeel.getMenuItemBackground());
		menuItem.setForeground(MASLookAndFeel.getMenuItemForeground());
		menuItem.setAction(action);
		return menuItem;
	}


	/*-----------------------------------------------------------*/
	/*------                    ACCESSORS                 -------*/
	/*-----------------------------------------------------------*/

	public void setMainPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public void setAddAgentAction(AddAgentAction addAgentAction) {
		this.addAgentAction = addAgentAction;
	}

	public void setAddTrackAction(AddTrackAction addTrackAction) {
		this.addTrackAction = addTrackAction;
	}

	public void setAgentOrderAction(AgentOrderAction agentOrderAction) {
		this.agentOrderAction = agentOrderAction;
	}

	public void setBufferPropertiesAction(BufferPropertiesAction bufferPropertiesAction) {
		this.bufferPropertiesAction = bufferPropertiesAction;
	}

	public void setDeleteAgentAction(DeleteAgentAction deleteAgentAction) {
		this.deleteAgentAction = deleteAgentAction;
	}

	public void setDeleteTracksAction(DeleteTracksAction deleteTracksAction) {
		this.deleteTracksAction = deleteTracksAction;
	}

	public void setDevicePropertiesAction(DevicePropertiesAction devicePropertiesAction) {
		this.devicePropertiesAction = devicePropertiesAction;
	}

	public void setEditOSCDevicesAction(EditOSCDevicesAction editOSCDevicesAction){
		this.editOSCDevicesAction = editOSCDevicesAction;
	}
	
	public void setExitAction(ExitAction exitAction) {
		this.exitAction = exitAction;
	}

	public void setKillNotesAction(KillNotesAction killNotesAction) {
		this.killNotesAction = killNotesAction;
	}

	public void setMidiSynchronizationAction(MIDISynchronizationAction midiSynchronizationAction) {
		this.midiSynchronizationAction = midiSynchronizationAction;
	}

	public void setNewProjectAction(NewProjectAction newProjectAction) {
		this.newProjectAction = newProjectAction;
	}

	public void setOpenProjectAction(OpenProjectAction openProjectAction) {
		this.openProjectAction = openProjectAction;
	}

	public void setPlayAction(PlayAction playAction) {
		this.playAction = playAction;
	}

	public void setSaveProjectAction(SaveProjectAction saveProjectAction) {
		this.saveProjectAction = saveProjectAction;
	}

	public void setSaveProjectAsAction(SaveProjectAsAction saveProjectAsAction) {
		this.saveProjectAsAction = saveProjectAsAction;
	}

	public void setShowMidiEventsAction(ShowMidiEventsAction showMidiEventsAction) {
		this.showMidiEventsAction = showMidiEventsAction;
	}

	public void setStopAction(StopAction stopAction) {
		this.stopAction = stopAction;
	}

	public void setZoomInAction(ZoomInAction zoomInAction) {
		this.zoomInAction = zoomInAction;
	}

	public void setZoomOutAction(ZoomOutAction zoomOutAction) {
		this.zoomOutAction = zoomOutAction;
	}

	public void setTrackCounter(TrackCounter trackCounter) {
		this.trackCounter = trackCounter;
	}

	public void setLoadBufferCounter(LoadBufferCounter loadBufferCounter) {
		this.loadBufferCounter = loadBufferCounter;
	}

	public void setPlayBufferCounter(PlayBufferCounter playBufferCounter) {
		this.playBufferCounter = playBufferCounter;
	}

	public void setTempoTextField(TempoTextField tempoTextField) {
		this.tempoTextField = tempoTextField;
	}

	public void setConfigLoader(ConfigLoader configLoader) {
		this.configLoader = configLoader;
	}

	public void setProjectFileChooser(JFileChooser projectFileChooser) {
		this.projectFileChooser = projectFileChooser;
	}

}

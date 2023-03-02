package eu.davidgamez.mas.gui.dialog;

//Java imports
import javax.swing.*;
import javax.sound.midi.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Vector;

//MAS imports
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.midi.*;
import eu.davidgamez.mas.Globals;


public class DeviceDialog extends JDialog implements ActionListener{

	//Buttons for dialog
	private JButton ok, cancel;

	//List of input and output devices
	private JList inputDeviceList, outputDeviceList;


	//Constructor
	public DeviceDialog(MainFrame mainFrame) {
		super(mainFrame, "MIDI Device Selection", true);

		//Set up dialog
		this.setBackground(Color.white);

		//Set up buttons
		JPanel buttonPane = new JPanel();
		buttonPane.add(ok = createButton("Ok"));
		buttonPane.add(cancel = createButton("Cancel"));
		buttonPane.setBackground(Color.white);
		getContentPane().add(buttonPane,  BorderLayout.SOUTH);

		//Set up list of input devices
		inputDeviceList = new JList();
		inputDeviceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane inputScrollPane = new JScrollPane(inputDeviceList);

		//Set up list of output devices
		outputDeviceList = new JList();
		outputDeviceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane outputScrollPane = new JScrollPane(outputDeviceList);

		//Add lists to main panel of this dialog.
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(new GridLayout(1, 2));
		mainPanel.setBackground(Color.white);

		//Create the panel for the input devices
		JPanel inputPanel = new JPanel();
		inputPanel.setBorder(BorderFactory.createTitledBorder("Input Devices"));
		inputPanel.setLayout(new GridLayout(1, 1));
		inputPanel.add(inputScrollPane);
		inputPanel.setBackground(Color.white);
		mainPanel.add(inputPanel);

		//Create and add the panel for the output devices
		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(BorderFactory.createTitledBorder("Output Devices"));
		outputPanel.setLayout(new GridLayout(1, 1));
		outputPanel.add(outputScrollPane);
		outputPanel.setBackground(Color.white);
		mainPanel.add(outputPanel);

		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.setBackground(Color.white);
		this.setPreferredSize(new Dimension(500, 300));
		this.pack();
	}
	

	/* Loads the dialog with current MIDI devices and selects the ones that are currently
     in use. */
	public void showDialog(){
		//Fill lists with input and output devices
		inputDeviceList.setListData(MIDIDeviceManager.getInputDeviceNames());
		inputDeviceList.clearSelection();
		outputDeviceList.setListData(MIDIDeviceManager.getOutputDeviceNames());
		outputDeviceList.clearSelection();

		//Select the devices that are currently in use
		Vector<String> inputDeviceVector = MIDIDeviceManager.getOpenInputDeviceNames();
		for(String inDevName : inputDeviceVector)
			addInputSelection(inDevName);
		Vector<String> outputDeviceVector = MIDIDeviceManager.getOpenOutputDeviceNames();
		for (String outDevName : outputDeviceVector)
			addOutputSelection(outDevName);

		//Make the dialog visible
		this.setLocation(Globals.getScreenWidth() /4, Globals.getScreenHeight()/4);
		this.setVisible(true);
	}


	//-------------------------------------------------------------------------------------
	//----------                 Event Handling Methods                         -----------
	//-------------------------------------------------------------------------------------

	//Called when ok or cancel are pressed
	public void actionPerformed(ActionEvent e){
		Object source = e.getSource();
		if(source == ok){
			//Set input devices.
			setInputDevices();

			//Set output devices.
			setOutputDevices();

			//Hide the dialog
			setVisible(false);
		}
		else if(source == cancel){
			//Hide the dialog
			setVisible(false);
		}
	}

	public void addInputSelection(String deviceName){
		//Work through list and set selection when it is found
		for(int i=0; i<inputDeviceList.getModel().getSize(); ++i){
			if(((String)inputDeviceList.getModel().getElementAt(i)).equals(deviceName)){
				inputDeviceList.addSelectionInterval(i, i);
			}
		}
	}

	public void addOutputSelection(String deviceName) {
		//Work through list and set selection when it is found
		for (int i = 0; i < outputDeviceList.getModel().getSize(); ++i) {
			if ( ( (String) outputDeviceList.getModel().getElementAt(i)).equals(deviceName)) {
				outputDeviceList.addSelectionInterval(i, i);
			}
		}
	}



	//-------------------------------------------------------------------------------------
	//-------------                  Private Methods                         --------------
	//-------------------------------------------------------------------------------------

	//Creates a button in the appropriate style for the dialog
	private JButton createButton (String label){
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(150,  20));
		button.addActionListener(this);
		return button;
	}


	//Instructs the sequencer to open up the selected input devices
	private void setInputDevices(){
		//Close all current midi input devices
		MIDIDeviceManager.closeInputDevices();

		//Work through selection array and open up input devices
		int [] inputSelectionArray =  inputDeviceList.getSelectedIndices();
		for (int i = 0; i < inputSelectionArray.length; i++) {
			String deviceName = (String) inputDeviceList.getModel().getElementAt(inputSelectionArray[i]);
			try {
				MIDIDeviceManager.openInputDevice(deviceName);
			}
			catch (MidiUnavailableException ex) {
				JOptionPane.showMessageDialog(this, "Midi Input Device not available: " + deviceName,  "Input Device Error", JOptionPane.ERROR_MESSAGE);
				inputDeviceList.clearSelection();
				MIDIDeviceManager.closeInputDevices();
				ex.printStackTrace();
				break;
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}


	//Instructs the sequencer to open up the selected output devices
	private void setOutputDevices() {
		//Close all output devices
		MIDIDeviceManager.closeOutputDevices();

		//Work through selection array and open out output devices
		int[] outputSelectionArray = outputDeviceList.getSelectedIndices();
		for (int i = 0; i < outputSelectionArray.length; i++) {
			String deviceName = (String) outputDeviceList.getModel().getElementAt(outputSelectionArray[i]);
			try {
				MIDIDeviceManager.openOutputDevice(deviceName);
			}
			catch (MidiUnavailableException ex) {
				JOptionPane.showMessageDialog(this, "Midi Output Port not available: " + deviceName, "Output Port Error", JOptionPane.ERROR_MESSAGE);
				outputDeviceList.clearSelection();
				MIDIDeviceManager.closeOutputDevices();
				ex.printStackTrace();
				break;
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}


}



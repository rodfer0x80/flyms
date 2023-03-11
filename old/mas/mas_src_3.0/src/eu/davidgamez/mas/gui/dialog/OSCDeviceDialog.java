package eu.davidgamez.mas.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.illposed.osc.OSCPortOut;

import eu.davidgamez.mas.Constants;
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.gui.ValidatedIntTextField;
import eu.davidgamez.mas.midi.OSCDeviceManager;

public class OSCDeviceDialog extends JDialog implements ActionListener, Constants {

	//Buttons for dialog
	private JButton okButton, cancelButton, addButton, deleteButton;
	
	private JList oscDeviceList;
	private JTextField nameTextField = new JTextField();
	private JTextField ipTextField = new JTextField();
	private ValidatedIntTextField portTextField = new ValidatedIntTextField(1, 30000);

	//Constructor
	public OSCDeviceDialog(MainFrame mainFrame) {
		super(mainFrame, "OSC Devices", true);
		
		//Set up dialog
		this.setBackground(Color.white);
		
		//Add fields to add more devices
	    Box newDevBox = Box.createHorizontalBox();
	    newDevBox.add(new JLabel("Name: "));
	    newDevBox.add(nameTextField);
	    newDevBox.add(Box.createHorizontalStrut(5));
	    newDevBox.add(new JLabel("IP Address: "));
	    newDevBox.add(ipTextField);
	    newDevBox.add(Box.createHorizontalStrut(5));
	    newDevBox.add(new JLabel("Port:"));
	    portTextField.setText(String.valueOf(DEFAULT_OSC_PORT));
	    portTextField.setColumns(5);
	    newDevBox.add(portTextField);
	    newDevBox.add(Box.createHorizontalStrut(5));
	    newDevBox.add(addButton = createButton("Add OSC Device"));
	    getContentPane().add(newDevBox, BorderLayout.NORTH);
	
		//Add list holding current devices.
	    oscDeviceList = new JList();
	    oscDeviceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane inputScrollPane = new JScrollPane(oscDeviceList);
		getContentPane().add(inputScrollPane,  BorderLayout.CENTER);
		
		//Set up buttons
		JPanel buttonPane = new JPanel();
		buttonPane.add(cancelButton = createButton("Cancel"));
		buttonPane.add(deleteButton = createButton("Delete Selected Devices"));
		buttonPane.add(okButton = createButton("Ok"));
		buttonPane.setBackground(Color.white);
		getContentPane().add(buttonPane,  BorderLayout.SOUTH);
		
		//Set preferred size
		this.setMinimumSize(new Dimension(700, 300));
		this.pack();
	}
	
	
	//Called when ok or cancel are pressed
	public void actionPerformed(ActionEvent e){
		Object source = e.getSource();
		if(source == okButton || source == cancelButton){
			//Hide the dialog
			setVisible(false);
		}
		else if(source == addButton){
			addOSCDevice();
		}
		else if (source == deleteButton){
			deleteSelectedDevices();
		}
			
	}
	
	
	/** Makes the dialog visible */
	public void showDialog(){
		loadOSCDevices();
		this.setLocation(Globals.getScreenWidth() /4, Globals.getScreenHeight()/4);
		this.setVisible(true);
	}
	
	
	/** Loads up the current set of OSC devices */
	private void loadOSCDevices(){
		Vector<String> oscDevNames = new Vector<String>();
		try{
			ArrayList<OSCPortOut> oscPortOutArrayList = OSCDeviceManager.getOSCPortOutArrayList();
			for (OSCPortOut oscOutPrt : oscPortOutArrayList){
				InetAddress portAddress = oscOutPrt.getAddress();
				if(portAddress.equals(InetAddress.getLocalHost()))
					oscDevNames.add(oscOutPrt.getName() + "@localhost:" + oscOutPrt.getPort() + "\n");
				else
					oscDevNames.add(oscOutPrt.getName() + "@" + portAddress.getHostAddress() + ":" + oscOutPrt.getPort() + "\n");
			}
		}
		catch(Exception ex){
			MsgHandler.error(ex);
		}
		oscDeviceList.setListData(oscDevNames);
	}
	
	
	/** Opens up a new device */
	private void addOSCDevice(){
		//Check user input
		if(nameTextField.getText().equals(""))
			nameTextField.setText("Unnamed");
		if(ipTextField.getText().equals("")){
			MsgHandler.error("IP Address must be specified");
			return;
		}
		if(portTextField.getText().equals("")){
			MsgHandler.error("Port must be specified");
			return;
		}
		int portNum = Integer.parseInt(portTextField.getText());
		if(portNum < OSC_PORT_MIN || portNum > OSC_PORT_MAX){
			MsgHandler.error("Port number is out of range");
			return;
		}
		
		//Try to add port
		try{
			OSCDeviceManager.openOSCOutputPort(nameTextField.getText(), ipTextField.getText(), portNum);
		}
		catch(Exception ex){
			MsgHandler.error("Cannot open device with ip address " + ipTextField.getText() + " and port " + portNum);
		}
		
		//Reload list of OSC Devices
		loadOSCDevices();
	}
	
	/** Deletes all selected devices */
	private void deleteSelectedDevices(){
		//Get all the objects that need to be removed
		ArrayList<OSCPortOut> tmpPortList = OSCDeviceManager.getOSCPortOutArrayList();
		ArrayList<OSCPortOut> removalList = new ArrayList<OSCPortOut>();
		
		int [] selDevArray = oscDeviceList.getSelectedIndices();
		for(int i=0; i<selDevArray.length; ++i)
			removalList.add(tmpPortList.get(selDevArray[i]));
		
		//Remove the objects
		for(OSCPortOut tmpPortOut : removalList)
			OSCDeviceManager.removeOSCPortOut(tmpPortOut);
		
		//Reload the list of devices
		loadOSCDevices();
	}
	

	//Creates a button in the appropriate style for the dialog
	private JButton createButton (String label){
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(200,  20));
		button.addActionListener(this);
		return button;
	}
	
	
}

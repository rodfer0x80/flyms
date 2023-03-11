package eu.davidgamez.mas.action;

import java.awt.Event;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.dialog.DeviceDialog;
import eu.davidgamez.mas.gui.dialog.OSCDeviceDialog;

public class EditOSCDevicesAction extends AbstractAction{
	//====================  INJECTED VARIABLES  =======================
	private OSCDeviceDialog deviceDialog;
	
	public EditOSCDevicesAction(){
		this.putValue(NAME, "OSC Devices");
		//this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', Event.CTRL_MASK));
	}
	
	public void actionPerformed(ActionEvent e){
    	deviceDialog.showDialog();
	}
	
	public void setDeviceDialog(OSCDeviceDialog deviceDialog) {
		this.deviceDialog = deviceDialog;
	}

}

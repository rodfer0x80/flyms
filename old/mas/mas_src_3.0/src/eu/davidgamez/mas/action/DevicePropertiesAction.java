package eu.davidgamez.mas.action;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

//MAS imports
import eu.davidgamez.mas.gui.dialog.DeviceDialog;


public class DevicePropertiesAction  extends AbstractAction {
	//====================  INJECTED VARIABLES  =======================
	private DeviceDialog deviceDialog;
    
	public DevicePropertiesAction(){
		super();
		this.putValue(NAME, "MIDI Devices");
	}
	
	public void actionPerformed(ActionEvent e) {
    	deviceDialog.showDialog();
    }	
	
	
	public void setDeviceDialog(DeviceDialog deviceDialog) {
		this.deviceDialog = deviceDialog;
	}
}

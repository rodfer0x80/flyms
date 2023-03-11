package eu.davidgamez.mas.action;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

//MAS imports
import eu.davidgamez.mas.gui.dialog.BufferPropertiesDialog;


public class BufferPropertiesAction  extends AbstractAction {
    private BufferPropertiesDialog bufferPropertiesDialog;
    
    public BufferPropertiesAction(){
    	super();
		this.putValue(NAME, "Buffer Properties");
    }
    
    public void actionPerformed(ActionEvent e) {
    	bufferPropertiesDialog.showDialog();
    }
    
    
    public void setBufferPropertiesDialog(BufferPropertiesDialog bufferPropertiesDialog) {
		this.bufferPropertiesDialog = bufferPropertiesDialog;
	}

}

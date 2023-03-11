package eu.davidgamez.mas.action;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

//MAS imports
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.gui.MASLookAndFeel;


public class ZoomOutAction extends AbstractAction{
	
	public ZoomOutAction(){
		super();
		this.putValue(NAME, "Zoom Out");
		this.putValue(Action.SMALL_ICON, MASLookAndFeel.getZoomOutIcon());
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('-'));
	}
	
	public void actionPerformed(ActionEvent e){
        EventRouter.zoomOutActionPerformed();
	}
	
}

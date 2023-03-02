package eu.davidgamez.mas.action;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

//MAS imports
import eu.davidgamez.mas.event.EventRouter;
import eu.davidgamez.mas.gui.MASLookAndFeel;


public class ZoomInAction extends AbstractAction{

	public ZoomInAction(){
		super();
		this.putValue(NAME, "Zoom In");
		this.putValue(Action.SMALL_ICON, MASLookAndFeel.getZoomInIcon());
		this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('='));
	}
	
	public void actionPerformed(ActionEvent e){
		EventRouter.zoomInActionPerformed();
	}
	
}

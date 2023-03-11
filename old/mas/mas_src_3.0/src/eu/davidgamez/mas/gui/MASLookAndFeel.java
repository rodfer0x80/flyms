package eu.davidgamez.mas.gui;

//Java imports
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.border.Border;

//MAS imports
import eu.davidgamez.mas.Globals;

public class MASLookAndFeel {

    private static Color toolBarColor = new Color(20, 20, 20);//Colour of the toolbar
    
    //Overall styles
    private static Color background = Color.black;
	private static Color foreground = Color.white;
    
    //Button styles
	private static Color buttonBackground = Color.lightGray;
    private static Color buttonForeground = toolBarColor;    
    private static Border buttonBorder = BorderFactory.createLoweredBevelBorder();
    private static Color playButtonBackground = Color.lightGray;
	private static Color playButtonForeground = Color.white;
    private static Border playButtonBorder = BorderFactory.createLoweredBevelBorder();
    
    //Menu styles
    private static Color menuForeground = Color.white;
	private static Color menuBackground = Color.black;
    private static Color popupMenuBackground = Color.white;
    private static Color popupMenuForeground = Color.black;
    private static Color menuItemBackground = Color.white;
	private static Color menuItemForeground = Color.black;
    private static boolean menuBorderPainted = false;
    
	//Icons
    private static ImageIcon applicationIcon = new ImageIcon(Globals.getImageDirectory().getPath() + "/mas-icon.png");
    private static ImageIcon playOnIcon = new ImageIcon(Globals.getImageDirectory().getPath() + "/PlayIcon_on.gif");
	private static ImageIcon playOffIcon = new ImageIcon(Globals.getImageDirectory().getPath() + "/PlayIcon_off.gif");
	private static ImageIcon stopIcon = new ImageIcon(Globals.getImageDirectory().getPath() + "/StopIcon.gif");
	private static ImageIcon killNotesIcon = new ImageIcon(Globals.getImageDirectory().getPath() + "/KillNotesIcon.gif");
	private static ImageIcon zoomInIcon = new ImageIcon(Globals.getImageDirectory().getPath() + "/ZoomIn.gif");
	private static ImageIcon zoomOutIcon = new ImageIcon(Globals.getImageDirectory().getPath() + "/ZoomOut.gif");
	
	//Connections
	private static Color connectionColor = new Color(197, 197, 0);
	
	//Main panel
	private static Color mainPanelBackground = Color.darkGray;
	
	//Track GUI
	private static Color trackMuteOffColor = new Color(44, 44, 44);
	private static Color trackMuteOnColor = new Color(255, 0, 0);
	private static Color trackSoloOnColor = new Color(0, 255, 0);
	private static Color trackSoloOffColor = new Color(44, 44, 44);
	private static Color trackAgentVisibleColor = new Color(80, 80, 80);
	private static Color trackConnectionVisibleColor = new Color(80, 80, 80);
	
	//Tempo text field
	private static Color tempoTextFieldBackground = Color.BLACK;
	private static Color tempoTextFieldForeground = Color.YELLOW;
	
	//Agent order dialog
	private static Color agentOrderBackground = Color.BLACK;
	private static Color agentOrderForeground = Color.WHITE;
	private static Color agentOrderSelectionBackground = Color.YELLOW;
	private static Color agentOrderSelectionForeground = Color.BLACK;
	private static Color agentOrderDragSelectionForeground = Color.lightGray;
	private static Color agentOrderDragSelectionBackground = Color.white;
	

	//Getters and setters
	public static Color getAgentOrderBackground() {
		return agentOrderBackground;
	}
	public static Color getAgentOrderSelectionBackground() {
		return agentOrderSelectionBackground;
	}
	public static Color getAgentOrderSelectionForeground() {
		return agentOrderSelectionForeground;
	}
	public static Color getAgentOrderDragSelectionForeground() {
		return agentOrderDragSelectionForeground;
	}
	public static Color getAgentOrderDragSelectionBackground() {
		return agentOrderDragSelectionBackground;
	}
	public static Color getAgentOrderForeground() {
		return agentOrderForeground;
	}
	
	
	public static Color getTempoTextFieldBackground() {
		return tempoTextFieldBackground;
	}
	public static Color getTempoTextFieldForeground() {
		return tempoTextFieldForeground;
	}
	public static Color getTrackMuteOffColor() {
		return trackMuteOffColor;
	}
	public static Color getTrackMuteOnColor() {
		return trackMuteOnColor;
	}
	public static Color getTrackSoloOnColor() {
		return trackSoloOnColor;
	}
	public static Color getTrackSoloOffColor() {
		return trackSoloOffColor;
	}
	public static Color getTrackAgentVisibleColor() {
		return trackAgentVisibleColor;
	}
	public static Color getTrackConnectionVisibleColor() {
		return trackConnectionVisibleColor;
	}
	public static Color getMainPanelBackground() {
		return mainPanelBackground;
	}
	public static Color getConnectionColor() {
		return connectionColor;
	}
    public static Color getMenuItemBackground() {
		return menuItemBackground;
	}
	public static Color getMenuItemForeground() {
		return menuItemForeground;
	}
	public static ImageIcon getStopIcon() {
		return stopIcon;
	}
	public static ImageIcon getKillNotesIcon() {
		return killNotesIcon;
	}
	public static ImageIcon getZoomInIcon() {
		return zoomInIcon;
	}
	public static ImageIcon getZoomOutIcon() {
		return zoomOutIcon;
	}
    public static ImageIcon getApplicationIcon() {
		return applicationIcon;
	}
    public static Color getBackground() {
		return background;
	}
	public static Color getForeground() {
		return foreground;
	}
    public static Color getPlayButtonBackground() {
		return playButtonBackground;
	}
	public static Color getPlayButtonForeground() {
		return playButtonForeground;
	}
	public static Border getPlayButtonBorder() {
		return playButtonBorder;
	}
    public static ImageIcon getPlayOnIcon() {
		return playOnIcon;
	}
	public static ImageIcon getPlayOffIcon() {
		return playOffIcon;
	}
	public static Color getToolBarColor() {
		return toolBarColor;
	}
	public static Color getButtonBackground() {
		return buttonBackground;
	}
	public static Color getButtonForeground() {
		return buttonForeground;
	}   
    public static Border getButtonBorder(){
    	return buttonBorder;
    }
    public static Color getMenuForeground() {
		return menuForeground;
	}
	public static Color getMenuBackground() {
		return menuBackground;
	}
	public static Color getPopupMenuBackground() {
		return popupMenuBackground;
	}
	public static Color getPopupMenuForeground() {
		return popupMenuForeground;
	}
    public static boolean isMenuBorderPainted() {
		return menuBorderPainted;
	}
}

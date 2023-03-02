package eu.davidgamez.mas.gui.dialog;

//Java imports
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.exception.MASAgentException;
import eu.davidgamez.mas.gui.MASLookAndFeel;
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.*;


public class AgentOrderDialog  extends JDialog implements DragGestureListener, DropTargetListener, DragSourceListener, ListSelectionListener{
	private boolean agentOrderChanged = false;
	
	/** Displays a list of agent names that can be re-ordered */
	private JList agentList = new JList();
	
	/** The names of the agents */
	private Vector<String> agentNames = new Vector<String>();

	//Drag and drop stuff
	private boolean isDragging = false;
	private int selectedAgentIndex = -1;
	private String selectedAgentDescription;
	private int startDragIndex = -1;
	
	
	/** Constructor */
	public AgentOrderDialog(MainFrame mainFrame) {
		super(mainFrame, "Agent Order", true);

		//Set up dialog
		this.setModal(false);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().setView(agentList);
		this.getContentPane().add(scrollPane, BorderLayout.CENTER);

		//Set up agent list
		agentList.setBackground(MASLookAndFeel.getAgentOrderBackground());
		agentList.setForeground(MASLookAndFeel.getAgentOrderForeground());
		agentList.setSelectionBackground(MASLookAndFeel.getAgentOrderSelectionBackground());
		agentList.addListSelectionListener(this);

		//Set up drag and drop for source
		DragSource dragSource = DragSource.getDefaultDragSource();
		
		//Create the recognizer for drag gestures
		dragSource.createDefaultDragGestureRecognizer(
				agentList, // component where drag originates
				DnDConstants.ACTION_COPY_OR_MOVE, // actions
				this
		); // drag gesture listener

		//Set up drag and drop for target
		DropTarget dropTarget = new DropTarget(agentList, // component
				DnDConstants.ACTION_COPY_OR_MOVE, // actions
				this
		); // DropTargetListener
	}

	
	/** Loads up the agent order into the list and displays the dialog */
	public void showDialog(){
		//Load up the list of agent names in their correct order
		loadAgentList();

		//Set size of dialog
		this.setBounds(Globals.getScreenWidth() / 4, Globals.getScreenHeight() / 4, Globals.getScreenWidth() / 4, Globals.getScreenHeight() / 2);
		this.setVisible(true);
	}

	
	/** Loads up the list of agents into the graphical list in the current order. */
	private void loadAgentList(){		
		//Local references to agent map and agent order list
		HashMap<String, Agent> agentMap = AgentHolder.getAgentMap();
		ArrayList<String> agentOrderList = AgentHolder.getAgentOrderList();

		agentNames.clear();
		for(String agentID : agentOrderList){
			agentNames.add(agentMap.get(agentID).getName() + "      [" + agentMap.get(agentID).getAgentType() + "]");
		}
		agentList.setListData(agentNames);
	}

	
	public void valueChanged(ListSelectionEvent e){
		if(isDragging){
			agentList.setSelectedIndex(selectedAgentIndex);
		}
	}

	//Drag methods for source
	public void dragGestureRecognized(DragGestureEvent e) {
		selectedAgentDescription = (String)agentList.getSelectedValue();
		startDragIndex = agentList.getSelectedIndex();
		agentList.setSelectionForeground(MASLookAndFeel.getAgentOrderDragSelectionForeground());
		agentList.setSelectionBackground(MASLookAndFeel.getAgentOrderDragSelectionBackground());
		e.startDrag(DragSource.DefaultCopyDrop, // cursor
				new StringSelection(selectedAgentDescription), // transferable
				this
		); 
	}


	//Drag methods for target
	public void drop(DropTargetDropEvent e) {
		agentList.setSelectionForeground(MASLookAndFeel.getAgentOrderSelectionForeground());
		agentList.setSelectionBackground(MASLookAndFeel.getAgentOrderSelectionBackground());
		
		//Get parameters of the drop point
		Point dropPoint = e.getLocation();
		int dropIndex = agentList.locationToIndex(dropPoint);
		
		//Change the agent order
		try{
			AgentHolder.changeAgentOrder(startDragIndex, dropIndex);
		}
		catch(MASAgentException ex){
			MsgHandler.error(ex);
		}
		
		//Refresh the display
		loadAgentList();
	}
	
	
	public void dragOver(DropTargetDragEvent e) {
		int index = agentList.locationToIndex(e.getLocation());
		agentList.setSelectedIndex(index);
	}

	
	//Unused drag methods
	public void dragExit(DropTargetEvent e){}
	public void dragEnter(DropTargetDragEvent e){}
	public void dropActionChanged(DropTargetDragEvent e){}
	public void dragDropEnd(DragSourceDropEvent e){}
	public void dragEnter(DragSourceDragEvent e){}
	public void dragExit(DragSourceEvent e){}
	public void dragOver(DragSourceDragEvent e){}
	public void dropActionChanged(DragSourceDragEvent e){}

	//Unused mouse methods
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
	public void mouseDragged(MouseEvent e){}
	public void mouseMoved(MouseEvent e){}
}

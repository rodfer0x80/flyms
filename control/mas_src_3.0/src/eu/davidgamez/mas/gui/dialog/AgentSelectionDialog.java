package eu.davidgamez.mas.gui.dialog;

//Java imports
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.TreeSet;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.file.AgentClassLoader_2;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.MainPanel;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.*;


public class AgentSelectionDialog extends JDialog implements ActionListener, KeyListener{
	//========================  INJECTED VARIABLES  =======================
	private AgentClassLoader_2 agentClassLoader_2;
	private MainPanel mainPanel;

	//========================  ORDINARY VARIABLES  =======================
	//Buttons for the dialog
	private JButton ok, cancel;

	//Used to select the type of agent
	private JComboBox agentTypeCombo;

	//Used to set the name of the new agent
	private JTextField nameTextField = new JTextField();

	//Default name for agents
	private String defaultAgentName = "Untitled";

	//Records whether an agent has been selected by the dialog after it has been shown.
	private boolean agentAdded = false;


	//Constructor
	public AgentSelectionDialog(MainFrame mainFrame) {
		super(mainFrame, "Select Agent Type", true);

		//Set up buttons
		JPanel buttonPane = new JPanel();
		buttonPane.add(ok = createButton("Ok"));
		buttonPane.add(cancel = createButton("Cancel"));
		getContentPane().add(buttonPane,  BorderLayout.SOUTH);

		//Then set up list with the list of agent types
		agentTypeCombo = new JComboBox();
		agentTypeCombo.setBackground(Color.white);
		agentTypeCombo.addKeyListener(this);
		Box topBox = Box.createVerticalBox();
		topBox.add(agentTypeCombo);
		topBox.add(Box.createVerticalStrut(10));
		Box nameBox = Box.createHorizontalBox();
		nameBox.add(new JLabel("Name"));
		nameBox.add(Box.createHorizontalStrut(10));
		nameBox.add(nameTextField);
		topBox.add(nameBox);
		getContentPane().add(topBox, BorderLayout.NORTH);

		//Set up name text field
		nameTextField.setText(defaultAgentName);
		nameTextField.addKeyListener(this);
	}


	/*-------------------------------------------------------------------*/
	/*-------                    PUBLIC METHODS                   -------*/
	/*-------------------------------------------------------------------*/

	//Refreshes agent list and makes dialog visible.
	public void showDialog(){
		agentAdded = false;
		agentTypeCombo.removeAllItems();
		
		//Load current agent classes and store names in combo
		agentClassLoader_2.loadAgentClasses();
		TreeSet<String> tempAgentTypes = new TreeSet<String>(agentClassLoader_2.getAgentNames());
		for(String agName : tempAgentTypes)
			agentTypeCombo.addItem(agName);
		
		//Finish off dialog
		nameTextField.setText("Untitled");
		agentTypeCombo.requestFocus();

		//Set size of dialog and display it
		this.setBounds(Globals.getScreenWidth() / 8, Globals.getScreenHeight() / 8, Globals.getScreenWidth() / 4, Globals.getScreenHeight() / 4);
		setVisible(true);
	}

	
	//Action performed method
	public void actionPerformed(ActionEvent e){
		Object source = e.getSource();
		if(source == ok){
			addAgent();
			agentAdded = true;
		}
		else if(source == cancel){
			setVisible(false);
		}
	}

	//Links pressing of enter to adding agent - same as ok button.
	public void keyPressed(KeyEvent e){
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			addAgent();
	}


	//Unused key listener methods
	public void keyTyped(KeyEvent e) {
	}
	public void keyReleased(KeyEvent e){
	}
	
	
	/*-------------------------------------------------------------------*/
	/*-------                    PRIVATE METHODS                  -------*/
	/*-------------------------------------------------------------------*/

	/** Adds an agent to the project */
	private void addAgent(){
		try {
			if(agentTypeCombo.getSelectedIndex() == -1){
				MsgHandler.error("No agent selected");
				return;
			}
			
			//Get the selected agent and panel
			String agentType = (String)agentTypeCombo.getSelectedItem();
			Agent midiAgent = agentClassLoader_2.getMidiAgent(agentType);
			AgentPropertiesPanel propertiesPanel = agentClassLoader_2.getAgentPropertiesPanel(agentType);
			

			//Set up midi agent
			Agent newMidiAgent = (Agent) midiAgent.getClass().newInstance();
	
			//If user has added a new name, use it, otherwise leave default, which is the type of agent
			if(!nameTextField.getText().equals(defaultAgentName))
				newMidiAgent.setName(nameTextField.getText());

			//Set up the properties panel for the agent
			AgentPropertiesPanel newPropertiesPanel = (AgentPropertiesPanel) propertiesPanel.getClass().newInstance();
			newPropertiesPanel.setAgent(newMidiAgent);//Sets up reference so that panel can be used to edit the agent's properties
			
			//Get the icon associated with the agent
			ImageIcon icon = agentClassLoader_2.getAgentIcon(agentType);

			//Add the agent and the properties panel
			mainPanel.addAgent(newPropertiesPanel, icon);
		}
		catch (Exception ex) {
			MsgHandler.error(ex.getMessage());
		}
		setVisible(false);
	}

	
	/** Builds a button with appropriate settings */
	private JButton createButton (String label){
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(80,  20));
		button.addActionListener(this);
		return button;
	}

	
	/*-------------------------------------------------------------------*/
	/*-------                    ACCESSORS                        -------*/
	/*-------------------------------------------------------------------*/

	public boolean isAgentAdded(){
		return agentAdded;
	}

	public void setAgentClassLoader_2(AgentClassLoader_2 agentClassLoader_2) {
		this.agentClassLoader_2 = agentClassLoader_2;
	}

	public void setMainPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

}

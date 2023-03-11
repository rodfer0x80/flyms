package eu.davidgamez.mas.gui.dialog;
/*------------------------ Agent Properties Dialog ----------------------------
   Displays the agent panel that is used to edit the agent's properties.
 -----------------------------------------------------------------------------
 */

//Java imports
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

//Project imports
import eu.davidgamez.mas.gui.AgentGUI;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;
//import eu.davidgamez.mas.gui.DefaultAgentPropertiesPanel;
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.MsgHandler;
import eu.davidgamez.mas.midi.*;
import eu.davidgamez.mas.Globals;


public class AgentPropertiesDialog extends JDialog{
	//GUI variables
	private JButton okButton, applyButton, cancelButton;
	private JTextField nameTextField = new JTextField();
	private AgentPropertiesPanel agentPropertiesPanel;
	private JPanel holdingPanel = new JPanel();

	//Agent variables
	private Agent midiAgent;

	public AgentPropertiesDialog(final MainFrame mainFrame, AgentGUI agentGUI){
		super(mainFrame, "Agent Properties", false);
		midiAgent = agentGUI.getAgent();
		
		//Set up layout
		holdingPanel.setLayout(new BorderLayout());

		//Set up buttons
		JPanel buttonPane = new JPanel();
		buttonPane.add(applyButton = createButton("Apply"));
		buttonPane.add(okButton = createButton("Ok"));
		buttonPane.add(cancelButton = createButton("Cancel"));
		holdingPanel.add(buttonPane,  BorderLayout.SOUTH);

		//Arrange name line in panel
		nameTextField.setText(midiAgent.getName());
		Box nameBox = Box.createHorizontalBox();
		nameBox.add(new JLabel("Name"));
		nameBox.add(Box.createHorizontalStrut(5));
		nameBox.add(nameTextField);
		nameBox.add(Box.createHorizontalGlue());
		holdingPanel.add(nameBox,  BorderLayout.NORTH);

		//Add default properties panel
		agentPropertiesPanel = agentGUI.getAgentPropertiesPanel();
		agentPropertiesPanel.setBorder(BorderFactory.createTitledBorder(midiAgent.getAgentDescription()));
		holdingPanel.add(agentPropertiesPanel, BorderLayout.CENTER);
		holdingPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		getContentPane().add(holdingPanel, BorderLayout.CENTER);

		//Set up actions for the buttons
		Action cancelAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if(agentPropertiesPanel.cancelButtonPressed())
					setVisible(false);
			}
		};

		Action applyAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				midiAgent.setName(nameTextField.getText());
				agentPropertiesPanel.applyButtonPressed();
			}
		};

		Action okAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				midiAgent.setName(nameTextField.getText());
				if (agentPropertiesPanel.okButtonPressed())
					setVisible(false);
			}
		};

		//Connect actions with keystrokes
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		KeyStroke shiftEnterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK);
		KeyStroke ctrlEnterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK);

		applyButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlEnterKey, "ApplyAction");
		applyButton.getActionMap().put("ApplyAction", applyAction);
		applyButton.addActionListener(applyAction);

		okButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(shiftEnterKey, "OkAction");
		okButton.getActionMap().put("OkAction", okAction);
		okButton.addActionListener(okAction);

		cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, "CancelAction");
		cancelButton.getActionMap().put("CancelAction", cancelAction);
		cancelButton.addActionListener(cancelAction);
		
		//Load agent properties
		try{
			agentPropertiesPanel.loadAgentProperties();
		}
		catch (Exception ex){
			MsgHandler.error(ex.getMessage());
			return;
		}
		
		//Make visible
		this.pack();
		this.setLocation(Globals.getScreenWidth()/8, Globals.getScreenHeight()/8);
		this.setVisible(true);

	}


	private JButton createButton (String label){
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(80,  20));
		return button;
	}

}

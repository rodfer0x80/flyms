package eu.davidgamez.mas.gui.dialog;

//Java imports
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

//MAS imports
import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.gui.ValidatedIntTextField;
import eu.davidgamez.mas.midi.*;
import eu.davidgamez.mas.Globals;


/** ----------------------- BufferPropertiesDialog ------------------------------
    Sets the properties of the buffer
 -----------------------------------------------------------------------------*/
public class BufferPropertiesDialog extends JDialog implements ActionListener, KeyListener{
	//Buttons for the dialog
	private JButton ok, cancel;

	//GUI components
	private ValidatedIntTextField bufferLengthTF = new ValidatedIntTextField(1, 1000);//Length of the buffer
	private ValidatedIntTextField bufferHeadStartTF = new ValidatedIntTextField(0, 1000);//Difference in beats between the start of the buffer and start of playback

	//Constructor
	public BufferPropertiesDialog(MainFrame mainFrame) {
	    super(mainFrame, "Buffer Properties", true);
	
	    //Set up GUI
	    Box verticalBox = Box.createVerticalBox();
	
	    //Set up buffer length box
	    Box bufLenBox = Box.createHorizontalBox();
	    bufLenBox.add(new JLabel("Buffer Length (beats)"));
	    bufLenBox.add(Box.createHorizontalStrut(5));
	    bufferLengthTF.setColumns(5);
	    bufferLengthTF.setMaximumSize(new Dimension(20, 30));
	    bufLenBox.add(bufferLengthTF);
	    bufLenBox.add(Box.createHorizontalGlue());
	    verticalBox.add(bufLenBox);
	
	    //Set up parameters to display how far ahead of the play position the agents should be working
	    verticalBox.add(Box.createVerticalStrut(15));
	    Box bufHeadStartBox = Box.createHorizontalBox();
	    bufHeadStartBox.add(new JLabel("Buffer headstart (buffers)"));
	    bufHeadStartBox.add(Box.createHorizontalStrut(5));
	    bufferHeadStartTF.setColumns(5);
	    bufferHeadStartTF.setMaximumSize(new Dimension(20, 30));
	    bufHeadStartBox.add(bufferHeadStartTF);
	    bufHeadStartBox.add(Box.createHorizontalGlue());
	    verticalBox.add(bufHeadStartBox);
	
	    //Set up ok and cancel buttons
	    JPanel buttonPane = new JPanel();
	    buttonPane.add(ok = createButton("Ok"));
	    buttonPane.add(cancel = createButton("Cancel"));
	    verticalBox.add(Box.createVerticalStrut(20));
	    verticalBox.add(buttonPane);
	
	    //Finish everything off
	    JPanel panel = new JPanel();
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    panel.add(verticalBox, BorderLayout.NORTH);
	    getContentPane().add(panel, BorderLayout.CENTER);
	    this.setLocation(Globals.getScreenWidth()/4, Globals.getScreenHeight()/4);
	    this.pack();
	}

	
	//-------------------------------------------------------------------------------------
	//------------                     Public Methods                         -------------
	//-------------------------------------------------------------------------------------

	//Called to show dialog
	public void showDialog() {
		//Get buffer properties from static BufferInfo
		bufferLengthTF.setText( Long.toString(Buffer.getLength_beats()));
		bufferHeadStartTF.setText(Long.toString(Buffer.getHeadStart_buffers()));

		//Make dialog visible
		this.setVisible(true);
	}


  	//-------------------------------------------------------------------------------------
  	//----------                 Event Handling Methods                         -----------
  	//-------------------------------------------------------------------------------------

  	// Handles events from buttons
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == ok) {
			if (setBufferProperties())
				setVisible(false);
		} 
		else if (source == cancel) {
			setVisible(false);
		}
	}

	// Handles events from keyboard
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			setBufferProperties();
		}
	}

	// Unused keylistener methods
	public void keyTyped(KeyEvent e) {
	}
	public void keyReleased(KeyEvent e) {
	}


  	//-------------------------------------------------------------------------------------
  	//-------------                  Private Methods                         --------------
  	//-------------------------------------------------------------------------------------

  	//Creates a button with the appropriate style for the dialog.
	private JButton createButton(String label) {
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(80, 20));
		button.addActionListener(this);
		return button;
	}


	// Sets the properties of the buffer in BufferInfo
	private boolean setBufferProperties() {
		// Sort out buffer length text field
		Buffer.setLength_beats(Long.valueOf(bufferLengthTF.getText()));

		// Sort out buffer head start text field
		Buffer.setHeadStart_buffers(Long.valueOf(bufferHeadStartTF.getText()));
		return true;
	}


}//End of class


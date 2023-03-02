package eu.davidgamez.mas.agents.pianoroll.gui;

//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//MAS imports
import eu.davidgamez.mas.Globals;
import eu.davidgamez.mas.gui.ValidatedIntTextField;


public class PropertiesDialog extends JDialog implements ActionListener {
	
	/** Offset for recording */
	private ValidatedIntTextField offsetTextField = new ValidatedIntTextField(1, 1000);
	
	/** Ok button */
	private JButton okButton;
	
	/** Cancel button */
	private JButton cancelButton;
	
	/** Value of offset is stored here when ok is pressed */
	int offset_ms = 0;
	
	/** Records whether dialog has been accepted */
	boolean accepted = false;
	
	public PropertiesDialog(int offset_ms){
		super(Globals.getMainFrame(), "Piano Roll Properties", true);
		
	    //Set up GUI
	    Box mainVBox = Box.createVerticalBox();
	    mainVBox.add(Box.createVerticalStrut(15));
	    
	    //Add offset attribute
	    Box offsetBox = Box.createHorizontalBox();
	    offsetBox.add(new JLabel("Record offset (ms)"));
	    offsetBox.add(Box.createHorizontalStrut(5));
	    offsetTextField.setText(String.valueOf(offset_ms));
	    offsetTextField.setColumns(5);
	    offsetTextField.setMaximumSize(new Dimension(20, 30));
	    offsetBox.add(offsetTextField);
	    offsetBox.add(Box.createHorizontalGlue());
	    mainVBox.add(offsetBox);
	    
	    //Ok and cancel buttons
	    JPanel buttonPane = new JPanel();
	    buttonPane.add(okButton = createButton("Ok"));
	    buttonPane.add(cancelButton = createButton("Cancel"));
	    mainVBox.add(Box.createVerticalStrut(20));
	    mainVBox.add(buttonPane);
	    
	    //Finish everything off
	    JPanel panel = new JPanel();
	    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	    panel.add(mainVBox, BorderLayout.NORTH);
	    getContentPane().add(panel, BorderLayout.CENTER);
	    this.setLocation(Globals.getScreenWidth()/4, Globals.getScreenHeight()/4);
	    this.pack();
	    setVisible(true);
	}
	
	
	public void actionPerformed(ActionEvent ev){
		if(ev.getSource() == okButton){
			if(offsetTextField.getText().isEmpty()){
				JOptionPane.showMessageDialog(Globals.getMainFrame(), "Offset text is empty.", "Offset Error", JOptionPane.ERROR_MESSAGE);
			}
			
			offset_ms = Integer.parseInt(offsetTextField.getText());
			accepted = true;
			this.setVisible(false);
		}
		else if(ev.getSource() == cancelButton){
			accepted = false;
			this.setVisible(false);
		}
	}
	
	
	public boolean wasAccepted(){
		return accepted;
	}
		
	
	public int getOffset_ms(){
		return offset_ms;
	}
	
	
	//Creates a button with the appropriate style for the dialog.
	private JButton createButton(String label) {
		JButton button = new JButton(label);
		button.setPreferredSize(new Dimension(80, 20));
		button.addActionListener(this);
		return button;
	}
}

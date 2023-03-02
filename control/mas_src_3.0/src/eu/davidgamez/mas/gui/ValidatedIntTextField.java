package eu.davidgamez.mas.gui;

//Java imports
import javax.swing.*;
import java.awt.event.*;


/** ------------------------ ValidatedIntTextField ------------------------------
   Validates a text field to make sure it is an integer within the specified
     range.
   -----------------------------------------------------------------------------
 */
public class ValidatedIntTextField extends JTextField implements KeyListener {

    /** The minimum value that can be entered into the field */
    private int lowRange;
    
    /** The maximum value that can be entered into the field */
    private int highRange;

    //Constructor
    public ValidatedIntTextField(int lr, int hr) {
        lowRange = lr;
        highRange = hr;
        addKeyListener(this);
    }

    //Called when a key is typed and checks the value in the field is within range.
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        //Run a first check that it is a digit
        if (!((Character.isDigit(c) || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE)))) {
            getToolkit().beep();
            e.consume();
            return;
        }

        //Should be a digit if have reached this stage. Now check that it does not exceed the range
        if (getText().equals(""))
            return;

        int textFieldValue = Integer.parseInt(getText());
        if (textFieldValue < lowRange) {
            getToolkit().beep();
            e.consume();
            setText(Integer.toString(lowRange));
            return;
        } else if (textFieldValue > highRange) {
            getToolkit().beep();
            e.consume();
            setText(Integer.toString(highRange));
            return;
        }
    }


    //Unused key listener methods
    public void keyPressed(KeyEvent e) {
    }
    public void keyReleased(KeyEvent e) {
    }
}

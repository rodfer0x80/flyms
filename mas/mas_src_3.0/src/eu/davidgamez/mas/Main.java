package eu.davidgamez.mas;

//MAS imports
import java.net.InetAddress;

import eu.davidgamez.mas.gui.MainFrame;
import eu.davidgamez.mas.midi.MIDIDeviceManager;

//Spring imports
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.illposed.osc.*;

public class Main {

	public static void main(String cmdArgs[]) {
		
		// Create ApplicationContext, which is also a BeanFactory (via inheritance)
		ApplicationContext context = new FileSystemXmlApplicationContext(new String[] { "spring/gui.xml", "spring/midi.xml", "spring/actions.xml", "spring/file.xml" });
		BeanFactory factory = (BeanFactory) context;

		// Instantiate the main frame of the application
		MainFrame mainFrame = (MainFrame) factory.getBean("mainFrame");
		
		
		try{

		}	         
		catch (Exception e) {
            e.printStackTrace();
     }
		 
		 mainFrame.showApplication();
	}
}


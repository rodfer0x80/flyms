package eu.davidgamez.mas.midi;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.illposed.osc.OSCPortOut;

public class OSCDeviceManager {

	
	/** List of OSC ports for the OSC messages */
	private static ArrayList<OSCPortOut> oscPortOutArrayList = new ArrayList<OSCPortOut>(); 
	
	
	/** Device processing OSC data coming into the system. */
	private static OSCInputHandler oscInputHandler = new OSCInputHandler();
	
	
	public static ArrayList<OSCPortOut> getOSCPortOutArrayList(){
		return oscPortOutArrayList;
	}
	
	public synchronized static void addOSCPortOut(OSCPortOut oscPortOut){
		oscPortOutArrayList.add(oscPortOut);
	}
	
	
	public synchronized static void removeOSCPortOut(OSCPortOut oscPortOut){
		oscPortOutArrayList.remove(oscPortOut);
	}
	
	public static OSCInputHandler getOSCInputHandler(){
		return oscInputHandler;
	}
	
	public synchronized static void close(){
		closeOSCOutputPorts();
	}

	
	/** Closes all of the OSC Output ports */
	public synchronized static void closeOSCOutputPorts(){
		for (OSCPortOut tmpOSCOut : oscPortOutArrayList) {
			try {
				System.out.println("Closing OSC output port");
				tmpOSCOut.close();
				System.out.println("OSC output port closed");
			} 
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		oscPortOutArrayList.clear();
	}
	
	
	/** Opens up and OSC Output Port with the specified parameters */
	public synchronized static void openOSCOutputPort(String name, String ipAddress, int port) throws SocketException, UnknownHostException {
		OSCPortOut oscPortOut;
		
		//Special case for local host
		if(ipAddress.equals("localhost"))
			oscPortOut = new OSCPortOut(InetAddress.getLocalHost(), port);
		else
			oscPortOut = new OSCPortOut(InetAddress.getByName(ipAddress), port);
		 
		//Add the port
		oscPortOut.setName(name);
		addOSCPortOut(oscPortOut);
	}
	
}

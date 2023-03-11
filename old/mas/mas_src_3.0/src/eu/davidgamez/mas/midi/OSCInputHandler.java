package eu.davidgamez.mas.midi;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

public class OSCInputHandler implements OSCListener {
	
	private OSCPortIn receiver;
	
	OSCPortOut sender;
	ArrayList<OSCPortOut> oscPortOutList = OSCDeviceManager.getOSCPortOutArrayList();
	public OSCInputHandler(){ 
		
		
		try{
			receiver = new OSCPortIn(10001);
			receiver.addListener("#time", this);
			receiver.startListening();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	
    public void acceptMessage(java.util.Date time, OSCMessage message) {
        System.out.println("Message received!");
        
    	long millisecs = System.currentTimeMillis();
		long secsSince1970 = (long) (millisecs / 1000);
		long secs = secsSince1970 + OSCBundle.SECONDS_FROM_1900_to_1970.longValue();
			// the next line was cribbed from jakarta commons-net's NTP TimeStamp code
		long fraction = ((millisecs % 1000) * 0x100000000L) / 1000;
        
        Object args2[] = new Object[2];
   	 	args2[0] = new Integer((int)secs);
   	 	args2[1] = new Integer((int)fraction);
        OSCMessage msg = new OSCMessage("#time", args2);
 
        
        //Send MIDI event to current receivers
        try{
        	for (OSCPortOut tmpOscPortOut : oscPortOutList) { 
        		tmpOscPortOut.send(msg);
        	}
        	System.out.println("Message sent with time stamp");
        }
        catch(Exception ex){
        	ex.printStackTrace();
        }

       
}

}

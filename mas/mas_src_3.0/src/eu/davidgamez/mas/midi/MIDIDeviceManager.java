package eu.davidgamez.mas.midi;

//Java imports
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import com.illposed.osc.OSCPortOut;

//MAS Imports
import eu.davidgamez.mas.exception.MASException;

public class MIDIDeviceManager {
	/** The MIDI input devices currently in use */
	private static ArrayList<MidiDevice> midiInputArrayList = new ArrayList<MidiDevice>();
	
	/** The MIDI output devices currently in use */
	private static ArrayList<MidiDevice> midiOutputArrayList = new ArrayList<MidiDevice>();

	/** Class that handles MIDI input */
	private static MIDIInputHandler midiInputHandler = new MIDIInputHandler();

	/** List of Receivers that receive the MIDI messages */
	private static ArrayList<Receiver> receiverArrayList = new ArrayList<Receiver>();
	
	public static ArrayList<Receiver> getReceiverArrayList() {
		return receiverArrayList;
	}
	
	public synchronized static void addReceiver(Receiver rcvr) {
		receiverArrayList.add(rcvr);
	}

	public synchronized static void removeReceiver(Receiver rcvr) {
		receiverArrayList.remove(rcvr);
	}
	

	public static MIDIInputHandler getMidiInputHandler() {
		return midiInputHandler;
	}


	/** Closes all MIDI devices */
	public synchronized static void close() {
		closeInputDevices();
		closeOutputDevices();
	}

	
	/** Closes all MIDI Input Devices. */
	public synchronized static void closeInputDevices() {
		for (MidiDevice tempDev : midiInputArrayList) {
			try {
				tempDev.getTransmitter().close();
				tempDev.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		midiInputArrayList.clear();
	}
	

	/** Closes all MIDI Output devices */
	public synchronized static void closeOutputDevices() {
		for (MidiDevice tempDev : midiOutputArrayList) {
			try {
				System.out.println("Closing receiver");
				tempDev.getReceiver().close();
				System.out.println("Receiver closed");
				tempDev.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		midiOutputArrayList.clear();
		receiverArrayList.clear();
	}
	
	
	/**
	 * Opens an input port, connects it to the MIDIInput handler and stores it
	 * so that it can be closed later. */
	public synchronized static void openInputDevice(String deviceName) throws MidiUnavailableException {
		boolean deviceFound = false;

		// Get a list of available MIDI devices
		MidiDevice.Info[] deviceInfo = MidiSystem.getMidiDeviceInfo();
		for (int i = 0; i < deviceInfo.length; i++) { // Work through this list
			if (deviceInfo[i].getName().equals(deviceName)) { // Have found a device with a matching name
				MidiDevice midiDevice = MidiSystem.getMidiDevice(deviceInfo[i]);
				if (midiDevice.getClass().toString().indexOf("com.sun.media.sound.MidiInDevice") > -1) {
					// The device with this name is an input device
					deviceFound = true;

					// Open up device and connect it to midi input handler
					midiDevice.open();
					Transmitter inputPortTransmitter = midiDevice.getTransmitter();
					inputPortTransmitter.setReceiver(midiInputHandler);
					midiInputArrayList.add(midiDevice);
					return;
				}
			}
		}

		// Check that we have found the device
		if (!deviceFound)
			throw new MidiUnavailableException("MIDISequencer: MIDI Input device could not be found: " + deviceName);
	}

	/** Opens the specified output device, usually an output port. */
	public synchronized static void openOutputDevice(String deviceName) throws MidiUnavailableException {
		boolean deviceFound = false;

		// Get a list of available MIDI devices
		MidiDevice.Info[] deviceInfo = MidiSystem.getMidiDeviceInfo();
		
		// Work through this list
		for (int i = 0; i < deviceInfo.length; i++) {
			
			// Have found the device we are looking for
			if (deviceInfo[i].getName().equals(deviceName)) { 
				MidiDevice midiDevice = MidiSystem.getMidiDevice(deviceInfo[i]);
				if (midiDevice.getClass().toString().indexOf("com.sun.media.sound.MidiOutDevice") > -1) {
					// Have found device and it is an output device
					deviceFound = true;

					// Open up the device and add it to the list of midi receivers
					midiDevice.open();
					Receiver outputPortReceiver = midiDevice.getReceiver();
					receiverArrayList.add(outputPortReceiver);
					midiOutputArrayList.add(midiDevice);
				}
			}
		}

		// Check that we have found the device. An exception is too major for
		// this type of error
		if (!deviceFound)
			throw new MidiUnavailableException("MIDI Output device could not be found: " + deviceName);
	}

	
	
	// Returns a list of descriptions of the currently available input ports
	public synchronized static Vector<String> getInputDeviceNames() {
		MidiDevice.Info[] deviceInfo = MidiSystem.getMidiDeviceInfo();
		MidiDevice device;
		Vector<String> descriptionVector = new Vector();
		for (int i = 0; i < deviceInfo.length; i++) {
			try {
				device = MidiSystem.getMidiDevice(deviceInfo[i]);
				if (device.getClass().toString().indexOf("com.sun.media.sound.MidiInDevice") > -1) {
					descriptionVector.add(deviceInfo[i].getName());
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
		return descriptionVector;
	}

	// Returns a list of the current input devices
	public synchronized static Vector<String> getOpenInputDeviceNames() {
		Vector<String> inVect = new Vector<String>();
		for (MidiDevice tempInDev : midiInputArrayList) {
			inVect.add(tempInDev.getDeviceInfo().getName());
		}
		return inVect;
	}

	// Returns a list of descriptions of the currently available output ports
	public synchronized static Vector<String> getOutputDeviceNames() {
		MidiDevice.Info[] deviceInfo = MidiSystem.getMidiDeviceInfo();
		MidiDevice device;
		Vector<String> descriptionVector = new Vector();
		for (int i = 0; i < deviceInfo.length; i++) {
			try {
				device = MidiSystem.getMidiDevice(deviceInfo[i]);
				if (device.getClass().toString().indexOf("com.sun.media.sound.MidiOutDevice") > -1) {
					descriptionVector.add(deviceInfo[i].getName());
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
		return descriptionVector;
	}

	/** Returns a list of the current output devices */
	public synchronized static Vector<String> getOpenOutputDeviceNames() {
		Vector<String> outVect = new Vector<String>();
		for (MidiDevice tempInDev : midiOutputArrayList) {
			outVect.add(tempInDev.getDeviceInfo().getName());
		}
		return outVect;
	}

}

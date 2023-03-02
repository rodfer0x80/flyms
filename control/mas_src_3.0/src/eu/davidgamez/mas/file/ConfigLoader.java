package eu.davidgamez.mas.file;

//Java imports
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.net.InetAddress;

import javax.sound.midi.MidiUnavailableException;

import com.illposed.osc.OSCPortOut;

//Project imports
import eu.davidgamez.mas.*;
import eu.davidgamez.mas.midi.*;
import eu.davidgamez.mas.exception.MASFileException;


public class ConfigLoader {

	//Constructor
	public ConfigLoader() {
	}


	//-------------------------------------------------------------------------------------
	//----------                Loading and Saving Methods                      -----------
	//-------------------------------------------------------------------------------------

	//Loads up settings such as midi devices from a file
	public void loadSettings() throws MASFileException, IOException{


		//Load up settings from file.
		if (Globals.getConfigFile().exists()) {
			//Open file
			BufferedReader in = new BufferedReader(new FileReader(Globals.getConfigFile()));
			String line = in.readLine();
			while(line != null){
				//Try to open input devices
				if(line.equals("#MIDI input devices")){
					line = in.readLine();
					while(line != null && !line.equals("") && !line.contains("#")){
						//Open input device
						try{
							MIDIDeviceManager.openInputDevice(line);
						}
						catch (MidiUnavailableException ex){
							System.out.println("Could not open input device: " + line);
						}

						//Read in the next line
						line = in.readLine();
					}
				}

				//Try to open output devices
				if (line.equals("#MIDI output devices")) {
					line = in.readLine();
					while (line != null &&  !line.equals("") && !line.contains("#") ) {
						//Open output device
						try{
							MIDIDeviceManager.openOutputDevice(line);
						}
						catch(MidiUnavailableException ex){
							System.out.println("Could not open output device: " + line);
						}

						//Read in the next line
						line = in.readLine();
					}
				}
				
				//Try to open output devices
				if (line.equals("#OSC output ports")) {
					line = in.readLine();
					while (line != null &&  !line.equals("") && !line.contains("#") ) {
						//Open output device
						try{
							String tmpName = line.substring(0, line.indexOf('@'));
							String tmpIPAddr = line.substring(line.indexOf('@') + 1, line.indexOf(':') );
							int port = Integer.parseInt(line.substring(line.indexOf(':') + 1, line.length()));
							System.out.println("tmp ip addr: '" + tmpIPAddr + "', port '" + port + "'");
							OSCDeviceManager.openOSCOutputPort(tmpName, tmpIPAddr, port);
						}
						catch(Exception ex){
							ex.printStackTrace();
							System.out.println("Could not open OSC output port: " + line);
						}
						
						//Read in the next line
						line = in.readLine();
					}
				}

				if (line.equals("#Project directory")) {
					line = in.readLine();
					while (line != null &&  !line.equals("") && !line.contains("#") ) {
						//Store the output devices
						Globals.setProjectDirectory(new File(line));

						//Read in the next line
						line = in.readLine();
					}
				}

				if (line.equals("#Default tempo")) {
					line = in.readLine();
					while (line != null &&  !line.equals("") && !line.contains("#") ) {
						//Store the output devices
						Globals.setDefaultTempo(Double.parseDouble(line));

						//Read in the next line
						line = in.readLine();
					}
				}

				if (line.equals("#Work area width")) {
					line = in.readLine();
					while (line != null &&  !line.equals("") && !line.contains("#") ) {
						//Store the default width
						Globals.setWorkAreaWidth(Integer.parseInt(line));

						//Read in the next line
						line = in.readLine();
					}
				}

				if (line.equals("#Work area height")) {
					line = in.readLine();
					while (line != null &&  !line.equals("") && !line.contains("#") ) {
						//Store the output devices
						Globals.setWorkAreaHeight(Integer.parseInt(line));

						//Read in the next line
						line = in.readLine();
					}
				}

				//Read in the next line
				line = in.readLine();
			}
			//Close file
			in.close();
		}
		else{
			throw new MASFileException("ConfigLoader: Cannot find config file: " + Globals.getConfigFile().toString());
		}
	}


	//Saves the settings to the config file
	public void saveSettings() throws Exception {
		//Open output stream
		BufferedWriter out = new BufferedWriter(new FileWriter(Globals.getConfigFile()));

		out.write("#Configuration file for MAS-2\n");

		//Save list of MIDI input devices
		out.write("\n#MIDI input devices\n");
		Vector<String> inputDevVect = MIDIDeviceManager.getOpenInputDeviceNames();
		for (String inDevName : inputDevVect)
			out.write(inDevName + "\n");

		//Save list of MIDI output devices
		out.write("\n#MIDI output devices\n");
		Vector<String> outputDevVect = MIDIDeviceManager.getOpenOutputDeviceNames();
		for (String outDevName : outputDevVect)
			out.write(outDevName + "\n");

		//Save list of OSC output ports
		out.write("\n#OSC output ports\n");
		ArrayList<OSCPortOut> oscPortOutArrayList = OSCDeviceManager.getOSCPortOutArrayList();
		for (OSCPortOut oscOutPrt : oscPortOutArrayList){
			InetAddress portAddress = oscOutPrt.getAddress();
			if(portAddress.equals(InetAddress.getLocalHost()))
				out.write(oscOutPrt.getName() + "@localhost:" + oscOutPrt.getPort() + "\n");
			else
				out.write(oscOutPrt.getName() + "@" + portAddress.getHostAddress() + ":" + oscOutPrt.getPort() + "\n");
		}
		
		//Save project directory
		out.write("\n#Project directory\n");
		out.write(Globals.getProjectDirectory() + "\n");

		//Save default tempo
		out.write("\n#Default tempo\n");
		out.write(Globals.getDefaultTempo() + "\n");

		//Save default width and height
		out.write("\n#Work area width\n");
		out.write(Globals.getWorkAreaWidth() + "\n");
		out.write("\n#Work area height\n");
		out.write(Globals.getWorkAreaHeight() + "\n");

		//Close file
		out.close();

	}


	//-------------------------------------------------------------------------------------
	//----------                      Private methods                          -----------
	//-------------------------------------------------------------------------------------

	private static void printConfig(){
		System.out.println("--------------------------- Config Loader ----------------------------");
		System.out.println("Config file: " + Globals.getConfigFile());
		System.out.println("--------------------------  MIDI Input Devices  ------------------------");
		Vector<String> inputDevices  = MIDIDeviceManager.getInputDeviceNames();
		for (String inDev : inputDevices)
			System.out.println(inDev);
		System.out.println();
		System.out.println("--------------------------  MIDI Output Devices  ------------------------");
		Vector<String> outputDevices  = MIDIDeviceManager.getOutputDeviceNames();
		for (String outDev : outputDevices)
			System.out.println(outDev);
		System.out.println();
		System.out.println("Project directory: " + Globals.getProjectDirectory());
		System.out.println("Default tempo: " + Globals.getDefaultTempo());
		System.out.println();
	}

}

package eu.davidgamez.mas.midi;


/**
 * ---------------------------- Agent Message ----------------------------------
 * Used to send messages between agents.
 * ----------------------------------------------------------------------------- */
public class AgentMessage {

	// Public definitions of message types
	static final public int START_BAR = 1;
	static final public int END_BAR = 2;

	// Private variables
	private String senderDescription = "";
	private String senderName = "";
	private String contents = "";
	private int type = -1;

	// Constructor
	public AgentMessage(String senderDesc, String senNam, int tp, String msg) {
		senderDescription = senderDesc;
		senderName = senNam;
		contents = msg;
		type = tp;
	}

	// Constructor
	public AgentMessage(int tp) {
		type = tp;
	}

	// Gets the name of the sender of the message
	public String getAgentName() {
		return senderName;
	}

	// Gets a description of the sender of the message
	public String getAgentDescription() {
		return senderDescription;
	}

	// Returns contents of message
	public String getContents() {
		return contents;
	}

	// Returns type of message
	public int getType() {
		return type;
	}

	// Prints out the message
	public void print() {
		switch (type) {
		case (START_BAR):
			System.out.println("Agent message START_BAR. Bar length = " + contents + " from " + senderName + " " + senderDescription);
			break;
		case (END_BAR):
			System.out.println("Agent message END_BAR. Contents = \"" + contents + "\" from " + senderName + " " + senderDescription);
			break;
		default:
			System.out.println("Agent message type not recognised:  \"" + contents + "\" from " + senderName + " " + senderDescription + " type = " + type);
		}
	}

	// Sets the contents of the message
	public void setContents(String messCont) {
		contents = messCont;
	}

	// Sets the type of the message
	public void setType(int messType) {
		type = messType;
	}
}

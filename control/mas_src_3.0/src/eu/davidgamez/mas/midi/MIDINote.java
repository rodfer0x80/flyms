package eu.davidgamez.mas.midi;

public class MIDINote {
	/** The pitch of the note between 0 and 127 */
	public int pitch;
	
	/** The length of the note */
	public long length;
	
	/** Field for storing data */
	private long data1 = 0;
	
	/** Field for storing data */
	private long data2 = 0;

	
	public MIDINote(int pitch, int length) {
		this.pitch = pitch;
		this.length = length;
	}
	
	
	public MIDINote(int pitch, int length, long data1, long data2) {
		this.pitch = pitch;
		this.length = length;
		this.data1 = data1;
		this.data2 = data2;
	}
	
	public boolean equals(MIDINote note){
		if(pitch == note.pitch && length == note.length && data1 == note.data1)
			return true;
		return false;
	}
	
	public long getData1(){
		return data1;
	}
	
	public long getLength_ppq(){
		return length;
	}
	
	public void setLength_ppq(long length_ppq){
		this.length = length_ppq;
	}
	
	public int getPitch(){
		return pitch;
	}
	
	public void setPitch(int pitch){
		this.pitch = pitch;
	}
	
	public void setData1(long data1){
		this.data1 = data1;
	}
	
	public long getData2(){
		return data2;
	}
	
	public void setData2(long data2){
		this.data2 = data2;
	}

}



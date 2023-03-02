package eu.davidgamez.mas.event;



public interface BufferListener {
	void playBufferAdvanceOccurred(long bufferCount);
	
	void startLoadBufferAdvanceOccurred(long bufferCount);
	
	void endLoadBufferAdvanceOccurred(long bufferCount);
	
	void trackAdvanceOccurred(long beatCount);  
}

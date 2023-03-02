package eu.davidgamez.mas.event;

import java.util.ArrayList;

public class EventRouter {
	/** Stores all of the classes that receive reset events from this class. */
	private static ArrayList<ResetListener> resetListenerArray = new ArrayList<ResetListener>();

	/** Stores all of the classes that receive reset events from this class. */
	private static ArrayList<TransportListener> transportListenerArray = new ArrayList<TransportListener>();

	/** Stores all of the classes that receive tempo events from this class  */
	private static ArrayList<TempoListener> tempoListenerArray = new ArrayList<TempoListener>();
	
	/** Stores all of the classes that receive tempo events from this class  */
	private static ArrayList<BufferListener> bufferListenerArray = new ArrayList<BufferListener>();
	
	/** Stores all of the classes that receive display events from this class  */
	private static ArrayList<DisplayListener> displayListenerArray = new ArrayList<DisplayListener>();
	
	public static void addBufferListener(BufferListener bufferListener){
		bufferListenerArray.add(bufferListener);
	}
	
	public static void removeBufferListener(BufferListener bufferListener){
		resetListenerArray.remove(bufferListener);
	}	
	
	public static void playBufferAdvancePerformed(long bufferCount){
		for(BufferListener bufferListener : bufferListenerArray){
			bufferListener.playBufferAdvanceOccurred(bufferCount);
		}
	}
	public static void startLoadBufferAdvancePerformed(long bufferCount){
		for(BufferListener bufferListener : bufferListenerArray){
			bufferListener.startLoadBufferAdvanceOccurred(bufferCount);
		}
	}
	public static void endLoadBufferAdvancePerformed(long bufferCount){
		for(BufferListener bufferListener : bufferListenerArray){
			bufferListener.endLoadBufferAdvanceOccurred(bufferCount);
		}
	}
	public static void trackAdvancePerformed(long bufferCount){
		for(BufferListener bufferListener : bufferListenerArray){
			bufferListener.trackAdvanceOccurred(bufferCount);
		}
	}
	
	public static void addResetListener(ResetListener resetListener){
		resetListenerArray.add(resetListener);
	}
	
	public static void removeResetListener(ResetListener resetListener){
		resetListenerArray.remove(resetListener);
	}
	
	
	public static void resetActionPerformed(){
		for(ResetListener resetListener : resetListenerArray){
			resetListener.resetActionPerformed();
		}
	}
	
	public static void addTempoListener(TempoListener tempoListener){
		tempoListenerArray.add(tempoListener);
	}
	
	public static void removeTempoListener(TempoListener tempoListener){
		tempoListenerArray.remove(tempoListener);
	}
	
	
	public static void tempoActionPerformed(TempoEvent tempoEvent){
		for(TempoListener tempoListener : tempoListenerArray){
			tempoListener.tempoActionPerformed(tempoEvent);
		}
	}
	
	public static void addTransportListener(TransportListener transportListener){
		transportListenerArray.add(transportListener);
	}
	
	public static void removeTransportListener(TransportListener transportListener){
		transportListenerArray.remove(transportListener);
	}
	
	
	public static void killNotesActionPerformed(){
		for(TransportListener transportListener : transportListenerArray){
			transportListener.killNotesActionPerformed();
		}
	}
	
	public static void playActionPerformed(){
		for(TransportListener transportListener : transportListenerArray){
			transportListener.playActionPerformed();
		}
	}
	
	public static void stopActionPerformed(){
		for(TransportListener transportListener : transportListenerArray){
			transportListener.stopActionPerformed();
		}
	}
	
	
	public static void addDisplayListener(DisplayListener displayListener){
		displayListenerArray.add(displayListener);
	}
	
	public static void removeDisplayListener(DisplayListener displayListener){
		displayListenerArray.remove(displayListener);
	}
	
	public static void zoomInActionPerformed(){
		for(DisplayListener displayListener : displayListenerArray){
			displayListener.zoomInActionPerformed();
		}
	}
	
	public static void zoomOutActionPerformed(){
		for(DisplayListener displayListener : displayListenerArray){
			displayListener.zoomOutActionPerformed();
		}
	}
	
}

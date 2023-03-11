package eu.davidgamez.mas.event;


public interface TransportListener {

  /* Called when play is started in the application. */
  void playActionPerformed();

  /* Called when play is stopped */
  void stopActionPerformed();

  /** Called when kill notes is requested */
  void killNotesActionPerformed();

}

package eu.davidgamez.mas.agents.midifragmentsequencer.gui;

//Java imports
import java.util.Vector;
import java.util.Iterator;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.sound.midi.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

//MAS imports
import eu.davidgamez.mas.exception.MASXmlException;
import eu.davidgamez.mas.gui.AgentPropertiesPanel;


public class MidiFragmentSequencer extends AgentPropertiesPanel implements ActionListener, DragGestureListener, DropTargetListener, DragSourceListener, ListSelectionListener{
  private JButton loadMidiFiles = new JButton("Load Midi Files");
  private JList midiFragmentList = new JList();
  private Vector midiFragmentNames = new Vector();
  private Vector midiFragments;

  //Drag and drop stuff
  private boolean isDragging = false;
  private int selectedIndex = -1;
  private String selectedFragmentDescription;
  private MidiEvent [] selectedMidiFragment;

  //Variables for loading midi fragments
  private JFileChooser midiFileChooser;
  private Track [] trackArray;

  public MidiFragmentSequencer() {
	  super("MIDIFragmentSequencer");
    setUpPanel();
//    midiFileChooser = new JFileChooser(MainGUIFrame.midiFileDirectory);
  }

  private void setUpPanel(){
    BorderLayout borderLayout = new BorderLayout();
    borderLayout.setVgap(10);
    borderLayout.setHgap(10);
    this.setLayout(borderLayout);

    //Set up load midi files button
    loadMidiFiles.addActionListener(this);
    Box loadFilesBox = Box.createHorizontalBox();
    loadFilesBox.add(loadMidiFiles);
    loadFilesBox.add(Box.createHorizontalGlue());
    this.add(loadFilesBox, BorderLayout.NORTH);

    //Set up agent list
    midiFragmentList.setSelectionBackground(Color.yellow);
    midiFragmentList.setListData(midiFragmentNames);
    midiFragmentList.addListSelectionListener(this);

    //Set up drag and drop for source
    DragSource dragSource = DragSource.getDefaultDragSource();
    // creating the recognizer is all that's necessary - it
    // does not need to be manipulated after creation
    dragSource.createDefaultDragGestureRecognizer(
        midiFragmentList, // component where drag originates
        DnDConstants.ACTION_COPY_OR_MOVE, // actions
        this); // drag gesture listener


    //Set up drag and drop for target
    DropTarget dropTarget = new DropTarget(midiFragmentList, // component
                                           DnDConstants.ACTION_COPY_OR_MOVE, // actions
                                           this); // DropTargetListener

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.getViewport().setView(midiFragmentList);
    this.add(scrollPane, BorderLayout.CENTER);
  }

  public void actionPerformed(ActionEvent e){
    if(e.getSource() == loadMidiFiles){
      loadMidiFragments();
    }
  }

  public void loadPanelState(String agentPropertiesString) throws Exception{}

  public void loadAgentProperties(){

  }

  public boolean okButtonPressed(){
  /*  try{
      ( (testagents.MIDI.MidiFragmentSequencer)this.midiAgent).connectFragments();
      return true;
    }
    catch(NoNoteOnFoundException e){
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Fragments do not contain any note on messages ",  "Midi Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }*/
  return true;
  }

  public boolean applyButtonPressed(){
    return true;
  }

  public boolean cancelButtonPressed(){
    return true;
  }

  //Drag methods for source
  public void dragGestureRecognized(DragGestureEvent e) {
    selectedFragmentDescription = (String) midiFragmentList.getSelectedValue();
    selectedIndex = midiFragmentList.getSelectedIndex();
    selectedMidiFragment = (MidiEvent[]) midiFragments.get(selectedIndex);
    isDragging = true;
    midiFragmentList.setSelectionForeground(Color.lightGray);
    midiFragmentList.setSelectionBackground(Color.white);
    e.startDrag(DragSource.DefaultCopyDrop, // cursor
                new StringSelection(selectedFragmentDescription), // transferable
                this); // drag source listener
  }

   public void dragDropEnd(DragSourceDropEvent e) {}
   public void dragEnter(DragSourceDragEvent e) {}
   public void dragExit(DragSourceEvent e) {}
   public void dragOver(DragSourceDragEvent e) {}
   public void dropActionChanged(DragSourceDragEvent e) {}

   //Drag methods for target
   public void drop(DropTargetDropEvent e) {
     isDragging = false;
     midiFragmentList.setSelectionForeground(Color.black);
     midiFragmentList.setSelectionBackground(Color.lightGray);
     //First get parameters of the drop point
     Point dropPoint = e.getLocation();
     int dropIndex = midiFragmentList.locationToIndex(dropPoint);
     Rectangle dropCell = midiFragmentList.getCellBounds(dropIndex, dropIndex);

     //Now remove selected agent from list and agent vector
     midiFragmentNames.remove(selectedIndex);
     midiFragments.remove(selectedIndex);

     //Now insert agent into new position in JList and in agent vector
     //Now work out if it is in top half or bottom half of cell
     if (dropPoint.y - dropCell.y < dropCell.height / 2) { //Dropped in top half of cell. Need to insert at an earlier location
       midiFragmentNames.add(dropIndex, selectedFragmentDescription);
       midiFragments.add(dropIndex, selectedMidiFragment); //-1 because have removed one item from list
       System.out.println("Dropping " + selectedFragmentDescription +
                          " at position " + (dropIndex));
     }
     else { //Insert after this cell
       if (dropIndex == midiFragmentNames.size() + 1){//+1 because have removed one item from list
         midiFragmentNames.add(selectedFragmentDescription);//Add to end of list
         midiFragments.add(selectedMidiFragment);
         System.out.println("Dropping " + selectedFragmentDescription +
                            " at position " + (midiFragmentNames.size()-1));
       }
       else {
         midiFragmentNames.add(dropIndex, selectedFragmentDescription);
         midiFragments.add(dropIndex, selectedMidiFragment);
         System.out.println("Dropping " + selectedFragmentDescription +
                            " at position " + dropIndex);
       }
     }
     midiFragmentList.clearSelection();
     e.dropComplete(true);
    }
    public void dragEnter(DropTargetDragEvent e) { }
    public void dragExit(DropTargetEvent e) { }
    public void dragOver(DropTargetDragEvent e) {
      int index = midiFragmentList.locationToIndex(e.getLocation());
      if(index %2 ==0 ){//Empty cells are on the even numbers of the list
        midiFragmentList.setSelectedIndex(index);
      }
    }
    public void dropActionChanged(DropTargetDragEvent e) { }

    public void valueChanged(ListSelectionEvent e){
      if(isDragging){
        midiFragmentList.setSelectedIndex(selectedIndex);
      }
    }

    public void loadMidiFragments(){
   //   midiFragments = ((testagents.MIDI.MidiFragmentSequencer)this.midiAgent).getMidiFragmentsVector();
      midiFileChooser.rescanCurrentDirectory();
      if (midiFileChooser.showOpenDialog(this) != JFileChooser.CANCEL_OPTION) {
        File file = midiFileChooser.getSelectedFile();
        if (file != null) {
          String s = file.getName();
          if (s.endsWith(".mid")) {
            try{
              //MidiFileFormat fileFormat = MidiSystem.getMidiFileFormat(file);
              //System.out.println("File format is: " + fileFormat.getType());
              Sequence sequence = MidiSystem.getSequence(file);
              if (sequence == null)
                System.out.println("NULL SEQUENCE"); //SHOW ERROR DIALOG HERE
              else {
                trackArray = sequence.getTracks();
                midiFragments.clear();
                midiFragmentNames.clear();
                for(int i=0; i<trackArray.length; i++){
                  Track tempTrack = trackArray[i];
                  if(containsShortMessages(tempTrack)){//Don't want to add a track without Short Messages
                    MidiEvent[] tempMidiEvents = new MidiEvent[tempTrack.size()];
                    for (int j = 0; j < tempTrack.size(); j++) {
                      tempMidiEvents[j] = tempTrack.get(j);
                    }
                    midiFragments.add(tempMidiEvents);
                    midiFragmentNames.add(new String("[" + i + "] " + file.getName()));
                  }
                }
              }
              //#FIXME# Sonar seems to save files using zero velocity instead of note off
              //For the moment change this manually, but there may be a better way around this.
              changeMidiFormat();
              midiFragmentList.setListData(midiFragmentNames);
            }
            catch(Exception e){
              e.printStackTrace();
              //SHOW ERROR DIALOG HERE
            }
          }
        }
      }
    }

    private void changeMidiFormat(){
      Iterator iterator = midiFragments.iterator();
      while(iterator.hasNext()){
        MidiEvent[] tempMidiEvents = (MidiEvent[])iterator.next();
        for(int i=0; i<tempMidiEvents.length; i++){
          if(tempMidiEvents[i].getMessage() instanceof ShortMessage){
            ShortMessage message = (ShortMessage)tempMidiEvents[i].getMessage();
            if(message.getCommand() == ShortMessage.NOTE_ON && message.getData2() == 0){//Event is note on with zero velocity
              try{
                message.setMessage(ShortMessage.NOTE_OFF, message.getChannel(), message.getData1(), 0);
              }
              catch (Exception  e){
                e.printStackTrace();
              }
            }
          }
        }
      }
    }

    //Checks to see if a track contains short messages. Tracks without short messages are discarded
    private boolean containsShortMessages(Track track){
      for(int i=0; i<track.size(); i++){
        if(track.get(i).getMessage() instanceof ShortMessage)
          return true;
      }
      return false;
    }

    public void printMidiEvents(MidiEvent[] midiEventArray) {
      System.out.println("========================================MIDI EVENTS=======================================");
      System.out.println("Midi event array length = " + midiEventArray.length);
      for (int j = 0; j < midiEventArray.length; j++) {
        if (midiEventArray[j].getMessage() instanceof ShortMessage) {
          System.out.print("Tick: " + midiEventArray[j].getTick());
          ShortMessage sm = (ShortMessage) midiEventArray[j].getMessage();
          switch (sm.getCommand()) {
            case (ShortMessage.NOTE_ON):
              System.out.print("; Note On");
              break;
            case (ShortMessage.NOTE_OFF):
              System.out.print("; Note Off");
              break;
            default:
              System.out.print("; Unrecognised");
          }
          System.out.println("; Channel: " + sm.getChannel() + "; Note: " + sm.getData1() + "; Velocity: " + sm.getData2());
        }
      }
      System.out.println();
    }

    
	/** Returns an XML string with the parameters of the panel */
	public String getXML(String indent) {
		String panelStr = indent + "<agent_panel>";
		panelStr += super.getXML(indent + "\t");
		panelStr += indent + "</agent_panel>";
		return panelStr;
	}
	

	@Override
	public void loadFromXML(String arg0) throws MASXmlException {

	}
  }

package eu.davidgamez.mas.exception;

import java.io.IOException;

public class MASFileException extends  IOException{
	  //Constructor
	  public MASFileException() {
	  }

	  //Constructor
	  public MASFileException(String s){
	    super(s);
	  }
}

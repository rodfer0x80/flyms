package eu.davidgamez.mas.file;

/* ----------------------------- MAS File Filter -------------------------------
   File filter for the application.
   -----------------------------------------------------------------------------
*/

//Java imports
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class MASFileFilter extends FileFilter{
  private String extension;
  private String description;


  public MASFileFilter(String ext, String desc) {
    extension = ext;
    description = desc;
  }

  public boolean accept(File file){
    return (file.isDirectory() || file.getName().toLowerCase().endsWith(extension));
  }

  public String getDescription(){
    return description;
  }

  public String getExtension(){
    return extension;
  }
}

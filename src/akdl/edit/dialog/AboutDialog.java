package akdl.edit.dialog;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import akdl.edit.AKDLEditor;

public class AboutDialog extends ADialog {

  private static final long serialVersionUID = 4210569792394752952L;

  public AboutDialog(Frame parent) {
    super(parent, null, null, "About Visual AKDL Editor", null);
    setAlwaysOnTop(true);
  }

  @Override
  public int createOwnContent(GridBagConstraints c) {
    ImageIcon icon = createImage("akdl.png", "logo");
    getContentPane().add(new JLabel(icon), c);
    return 1;
  }

  //
  private ImageIcon createImage(String path, String description) {
    URL imgURL = AKDLEditor.class.getClassLoader().getResource(path);
    if (imgURL != null) {
      return new ImageIcon(imgURL, description);
    }
    System.err.println("Couldn't find file: " + path);
    return null;
  }

  @Override
  public void performAction(ActionEvent e) {
  }

}

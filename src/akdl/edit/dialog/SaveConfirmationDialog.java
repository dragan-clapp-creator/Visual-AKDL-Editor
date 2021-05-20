package akdl.edit.dialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTextArea;

public class SaveConfirmationDialog extends ADialog {

  private static final long serialVersionUID = -372334904630531008L;

  private boolean isOk;

  public SaveConfirmationDialog(Frame parent) {
    super(parent, null, null, "Leave Current Work", new Dimension(400, 200));
    isOk = false;
  }

  @Override
  public int createOwnContent(GridBagConstraints c) {
    JButton cancelButton = new JButton("No");
    getOkButton().setText("Yes");
    cancelButton.addActionListener(this);
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 3;
    getContentPane().add(new JTextArea("Do you really want to leave this project\n"
                                      + " without saving your changes"), c);
    c.gridwidth = 1;
    c.gridy = 1;
    getContentPane().add(Box.createVerticalStrut(5), c);
    c.gridy = 2;
    getContentPane().add(cancelButton, c);
    c.gridx = 1;
    getContentPane().add(Box.createHorizontalStrut(70), c);
    c.gridx = 2;
    return 1;
  }


  @Override
  public void actionPerformed(ActionEvent e) {
    performAction(e);
    setVisible(false);
  }
  @Override
  public void performAction(ActionEvent e) {
    String name = ((JButton)e.getSource()).getText();
    if ("Yes".equals(name)) {
      isOk = true;
    }
  }

  public boolean isOk() {
    return isOk;
  }
}

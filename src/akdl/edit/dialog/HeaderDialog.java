package akdl.edit.dialog;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import akdl.sheet.HeaderInfo;

public class HeaderDialog extends ADialog {

  private static final long serialVersionUID = 477110516207013829L;

  private HeaderInfo headerInfo;

  private JTextField destination;
  private JCheckBox debug;
  private JCheckBox verbose;

  public HeaderDialog(Frame parent, HeaderInfo info) {
    super(parent, null, null, "Definition Sheet Header", null);
    headerInfo = info;
    redefineContent();
  }

  @Override
  public int createOwnContent(GridBagConstraints c) {
    if (headerInfo == null) {
      return 0;
    }
    destination = drawFieldLine("Destination", headerInfo.getDestination(), c, 0);
    debug = drawCheckBoxLine("Debug", headerInfo.isDebug(), c, 1);
    verbose = drawCheckBoxLine("Verbose", headerInfo.isVerbose(), c, 2);
    c.gridx = 2;
    return 3;
  }

  //
  private JTextField drawFieldLine(String label, String text, GridBagConstraints c, int row) {
    JTextField field = new JTextField(40);
    field.setText(text);
    field.addActionListener(this);
    c.gridy = row;
    c.gridx = 0;
    getContentPane().add(Box.createHorizontalStrut(5), c);
    c.gridx = 1;
    getContentPane().add(new JLabel(label), c);
    c.gridx = 2;
    getContentPane().add(Box.createVerticalStrut(5), c);
    c.gridx = 3;
    getContentPane().add(field, c);
    return field;
  }

  //
  private JCheckBox drawCheckBoxLine(String text, boolean isSelected, GridBagConstraints c, int row) {
    JCheckBox box = new JCheckBox(text);
    box.setSelected(isSelected);
    box.addActionListener(this);
    c.gridy = row;
    c.gridx = 0;
    getContentPane().add(Box.createHorizontalStrut(5), c);
    c.gridx = 1;
    getContentPane().add(Box.createVerticalStrut(5), c);
    c.gridx = 3;
    getContentPane().add(box, c);
    return box;
  }

  @Override
  public void performAction(ActionEvent e) {
    headerInfo.setDestination(destination.getText().isEmpty() ? null : destination.getText());
    headerInfo.setDebug(debug.isSelected());
    headerInfo.setVerbose(verbose.isSelected());
  }

}

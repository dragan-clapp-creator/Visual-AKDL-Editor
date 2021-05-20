package akdl.edit.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;

import akdl.edit.util.ColorSet;
import akdl.edit.util.ColorsHandler;

public class ColorsDialog extends JDialog implements ActionListener {

  private static final long serialVersionUID = 4052746551040636534L;

  private JButton okButton;
  private JButton cancelButton;
  private JButton resetButton;

  private ColorsHandler chandler;

  public ColorsDialog(Frame parent, ColorsHandler chandler) {
    super(parent, "Customize Color Preferences", true);
    this.chandler = chandler;
    Point p = parent.getLocation(); 
    setLocation(p.x + 300, p.y + 180);
    setLayout(new BorderLayout());
    defineContent();
    setAlwaysOnTop(false);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack(); 
  }

  public void defineContent() {
    getContentPane().removeAll();

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
    attributes.put(TextAttribute.FAMILY, Font.MONOSPACED);
    Font font2 = new JLabel().getFont().deriveFont(attributes);
    c.gridy = 0;
    c.gridwidth = 3;
    for (ColorSet cs : ColorSet.values()) {
      c.gridx = 0;
      getContentPane().add(Box.createVerticalStrut(5), c);
      c.gridx = 1;
      JButton button = new JButton();
      JLabel label = new JLabel(cs.getLabel());
      label.setBackground(cs.getColor());
      label.setFont(font2);
      label.setOpaque(true);
      button.add(label);
      getContentPane().add(button, c);
      button.addActionListener(this);
      c.gridy++;
    }
    c.gridwidth = 1;
    c.gridx = 0;
    cancelButton = new JButton("cancel");
    cancelButton.addActionListener(this);
    getContentPane().add(cancelButton, c);
    c.gridx = 1;
    okButton = new JButton("ok");
    okButton.addActionListener(this);
    getContentPane().add(okButton, c);
    c.gridx = 2;
    resetButton = new JButton("reset");
    resetButton.addActionListener(this);
    getContentPane().add(resetButton, c);
  }

  //
  private void performAction(ActionEvent e) {
    JButton btn = (JButton)e.getSource();
    JLabel label = (JLabel) btn.getComponent(0);
    for (ColorSet cs : ColorSet.values()) {
      if (label.getText().equals(cs.getLabel())) {
        Color color = JColorChooser.showDialog(this, "choose a color", cs.getColor());
        if (color != null) {
          cs.setColor(color);
          label.setBackground(color);
        }
        break;
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == resetButton) {
      chandler.removePreferredColors();
      dispose();
    }
    else if (e.getSource() == cancelButton) {
      dispose();
    }
    else if (e.getSource() == okButton) {
      chandler.savePreferredColors();
      dispose();
    }
    else {
      performAction(e);
    }
  }
}

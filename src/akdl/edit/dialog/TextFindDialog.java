package akdl.edit.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import akdl.edit.dialog.find.TextFinder;
import akdl.edit.handler.GeneralContext;

public class TextFindDialog extends JDialog implements ActionListener {

  private static final long serialVersionUID = 4052746551040636534L;

  private static JTextField namefield = new JTextField(15);
  private static JCheckBox checkWhole = new JCheckBox("whole word");
  private static JCheckBox checkCase = new JCheckBox("case sensitive");

  private static TextFinder finder;

  private GeneralContext context;

  private JTextComponent srcfield;

  private String rawSource;


  public TextFindDialog(JTextComponent srcfield, String rawSource, GeneralContext context) {
    super(context.getFrame(), "Find Text", true);
    this.srcfield = srcfield;
    this.rawSource = rawSource;
    this.context = context;
    finder = null;
    Point p = context.getFrame().getLocation(); 
    setLocation(p.x + 350, p.y + 200);
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    setPreferredSize(new Dimension(400, 200));

    JButton okButton = new JButton("ok");
    okButton.addActionListener(this);

    defineContent(c, okButton);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack(); 
  }

  //
  private void defineContent(GridBagConstraints c, JButton okButton) {
    getContentPane().removeAll();

    int row = createOwnContent(c);

    c.gridy = row+1;
    getContentPane().add(okButton, c);
  }

  public int createOwnContent(GridBagConstraints c) {
    ActionListener[] listeners = namefield.getActionListeners();
    for (int i=0; i<listeners.length; i++) {
      namefield.removeActionListener(listeners[i]);
    }
    namefield.addActionListener(this);

    c.gridy = 0;
    c.gridx = 0;
    getContentPane().add(new JLabel("Node name"), c);
    c.gridx = 1;
    getContentPane().add(Box.createVerticalStrut(5), c);
    c.gridx = 2;
    getContentPane().add(namefield, c);
    c.gridy = 1;
    c.gridx = 1;
    getContentPane().add(Box.createVerticalStrut(5), c);
    c.gridx = 2;
    getContentPane().add(checkCase, c);
    c.gridy = 2;
    c.gridx = 1;
    getContentPane().add(Box.createVerticalStrut(5), c);
    c.gridx = 2;
    getContentPane().add(checkWhole, c);
    c.gridx = 1;
    return 3;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JButton) {
      setVisible(false);
    }
    String text = namefield.getText();
    if (finder == null
        || !text.equals(finder.getText())
        || checkCase.isSelected() != finder.isCaseSensistive()
        || checkWhole.isSelected() != finder.isWholeWord()) {
      finder = new TextFinder(text, checkCase.isSelected(), checkWhole.isSelected(), srcfield, rawSource);
    }
    if (finder.findNext()) {
      context.refreshAll();
    }
  }

  public void findNext() {
    if (finder.findNext()) {
      context.refreshAll();
    }
  }
}

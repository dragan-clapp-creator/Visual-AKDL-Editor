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

import akdl.edit.dialog.find.NodeFinder;
import akdl.edit.handler.GeneralContext;
import akdl.edit.tree.node.ATreeNode;

public class NodeFindDialog extends JDialog implements ActionListener {

  private static final long serialVersionUID = 4052746551040636534L;

  private static JTextField namefield = new JTextField(15);
  private static JCheckBox checkWhole = new JCheckBox("whole word");
  private static JCheckBox checkCase = new JCheckBox("case sensitive");

  private static NodeFinder finder;


  private GeneralContext context;

  private ATreeNode tnode;

  public NodeFindDialog(ATreeNode tnode, GeneralContext context) {
    super(context.getFrame(), "Find Node Item", true);
    this.tnode = tnode;
    this.context = context;
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
    if (namefield.getActionListeners().length == 0) {
      namefield.addActionListener(this);
    }
    c.gridy = 0;
    c.gridx = 0;
    getContentPane().add(new JLabel("Node name"), c);
    c.gridx = 1;
    getContentPane().add(Box.createVerticalStrut(5), c);
    c.gridx = 2;
    getContentPane().add(namefield, c);
    c.gridy = 1;
    c.gridx = 0;
    getContentPane().add(new JLabel(""), c);
    c.gridx = 1;
    getContentPane().add(Box.createVerticalStrut(5), c);
    c.gridx = 2;
    getContentPane().add(checkCase, c);
    c.gridy = 2;
    c.gridx = 0;
    getContentPane().add(new JLabel(""), c);
    c.gridx = 1;
    getContentPane().add(Box.createVerticalStrut(5), c);
    c.gridx = 2;
    getContentPane().add(checkWhole, c);
    c.gridx = 1;
    return 3;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String text = namefield.getText();
    if (finder == null
        || !text.equals(finder.getText())
        || checkCase.isSelected() != finder.isCaseSensistive()
        || checkWhole.isSelected() != finder.isWholeWord()) {
      finder = new NodeFinder(text, checkCase.isSelected(), checkWhole.isSelected(),
                                 tnode, context.getRootNode());
    }
    ATreeNode node = finder.findNext();
    if (node != null) {
      context.refreshAll(node);
    }
    setVisible(false);
  }
}

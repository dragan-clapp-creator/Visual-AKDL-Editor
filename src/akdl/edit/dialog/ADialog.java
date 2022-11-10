package akdl.edit.dialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTree;

import akdl.edit.tree.node.ATreeNode;

abstract public class ADialog extends JDialog implements ActionListener {

  private static final long serialVersionUID = -7212179950213188669L;

  private Frame owner;
  private ATreeNode tnode;
  private JTree tree;

  private JButton okButton;

  private boolean isPerformOnOk;

  public ADialog(Frame parent, ATreeNode tnode, JTree tree, String title, Dimension dim) {
    super(parent, title, true);
    this.owner = parent;
    this.tnode = tnode;
    this.tree = tree;
    isPerformOnOk = true;
    if (parent != null) {
      Point p = parent.getLocation(); 
      setLocation(p.x + 350, p.y + 200);
    }
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    if (dim != null) {
      setPreferredSize(dim);
    }

    okButton = new JButton("ok");
    okButton.addActionListener(this);

    defineContent(c, okButton);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack(); 
  }

  //
  public void redefineContent() {
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    okButton = new JButton("ok");
    okButton.addActionListener(this);

    defineContent(c, okButton);
    pack(); 
  }

  //
  private void defineContent(GridBagConstraints c, JButton okButton) {
    getContentPane().removeAll();

    int row = createOwnContent(c);

    c.gridy = row+1;
    getContentPane().add(okButton, c);
  }

  abstract public int createOwnContent(GridBagConstraints c);

  abstract public void performAction(ActionEvent e);

  @Override
  public void actionPerformed(ActionEvent e) {
    if (isPerformOnOk || e.getSource() != okButton) {
      performAction(e);
    }
    if (e.getSource() == okButton) {
      dispose();
    }
  }

  public ATreeNode getTnode() {
    return tnode;
  }

  public JTree getTree() {
    return tree;
  }

  public Frame getOwner() {
    return owner;
  }

  /**
   * @param isPerformOnOk the isPerformOnOk to set
   */
  public void setPerformOnOk(boolean isPerformOnOk) {
    this.isPerformOnOk = isPerformOnOk;
  }

  /**
   * @return the okButton
   */
  public JButton getOkButton() {
    return okButton;
  }
}

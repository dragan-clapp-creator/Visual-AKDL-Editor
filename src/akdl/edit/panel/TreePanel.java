package akdl.edit.panel;

import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;

import akdl.edit.handler.GeneralContext;

public class TreePanel extends JPanel {

  private static final long serialVersionUID = 1823178409335678586L;

  public TreePanel() {
    setLayout(new  GridBagLayout());
    setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(EtchedBorder.RAISED),
            "Syntax Graph Area"));
  }

  public void addTree(GeneralContext ctx, JTree tree) {
    add(tree);
    tree.addSelectionRow(0);
    ctx.refreshAll();
    validate();
  }
}

package akdl.edit.tree.node.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import akdl.edit.tree.node.ATreeNode;
import akdl.edit.util.ColorSet;

public class TreeCellRenderer extends DefaultTreeCellRenderer {

  private static final long serialVersionUID = -578719241699499176L;

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    ATreeNode obj = (ATreeNode)value;
    setBackgroundNonSelectionColor(obj.getBackground());
    setFont(obj.getFont());
    setToolTipText(obj.getToolTipText());

    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    setVisible(true);

    setClosedIcon(null);
    setOpenIcon(null);
    setLeafIcon(null);
    setBackgroundSelectionColor(ColorSet.selectedBackground.getColor());
    setTextSelectionColor(Color.white);
    
    return this;
  }
}

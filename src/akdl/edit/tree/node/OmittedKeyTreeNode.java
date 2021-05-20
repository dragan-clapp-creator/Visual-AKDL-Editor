package akdl.edit.tree.node;

import java.awt.Color;

import javax.swing.JLabel;

import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.util.ColorSet;
import akdl.graph.nodes.VoidKeySymbol;

public class OmittedKeyTreeNode extends ATreeNode {

  private static final long serialVersionUID = 8653676225573453900L;

  public OmittedKeyTreeNode(String name, VoidKeySymbol item) {
    super(name, item);
  }

  @Override
  public Color getBackground() {
    return ColorSet.forOmitted.getColor();
  }

  @Override
  public String getToolTipText() {
    return "pseudo-terminal node used as 'ignored' to get temporarly a code generation";
  }

  public String toString() {
    return "IGNORED  " + getName();
  }

  @Override
  public void populate(PropertiesPanel propertiesPanel, ControlPanel controlPanel) {
    propertiesPanel.setupEvaluation(this);
    propertiesPanel.removeComponents();
    propertiesPanel.addComponent(new JLabel("Node name"), 0, 0);
    createTextField(propertiesPanel, getName(), Field.NAME, 0);

    displayPath(propertiesPanel, 7);
    propertiesPanel.redraw();
  }

  public String getSource(boolean isRaw) {
    return (isRaw ? "" : "<span style=\"background-color:" + ColorSet.forOmitted.getHexColor() + "\">")
        + "~" + getName() + (isRaw ? "" : "</span>");
  }
}

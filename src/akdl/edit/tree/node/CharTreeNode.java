package akdl.edit.tree.node;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.util.ColorSet;
import akdl.graph.nodes.CharSymbol;

public class CharTreeNode extends ATreeNode {

  private static final long serialVersionUID = -5388984927823016605L;

  public CharTreeNode(String name, CharSymbol item) {
    super(name, item);
  }

  @Override
  public Font getFont() {
    return new Font("Monospaced", Font.ITALIC, 14);
  }

  @Override
  public Color getBackground() {
    return ColorSet.forCharNode.getColor();
  }

  @Override
  public String getToolTipText() {
    return "terminal character node";
  }

  @Override
  public void populate(PropertiesPanel propertiesPanel, ControlPanel controlPanel) {
    propertiesPanel.setupEvaluation(this);
    propertiesPanel.removeComponents();
    propertiesPanel.addComponent(new JLabel("CHAR Constant"), 0, 0);
    createTextField(propertiesPanel, getName(), Field.NAME, 0);
    displayPath(propertiesPanel, 2);
    if (controlPanel != null) {
      controlPanel.update(this);
    }
    propertiesPanel.redraw();
  }

  public String toString() {
    return "CHAR '" + getName() + "'";
  }

  public String getSource(boolean isRaw) {
    return (isRaw ? "" : "<span style=\"background-color:" + ColorSet.forCharNode.getHexColor() + "\">")
        + "'" + getName() + "'" + (isRaw ? "" : "</span>");
  }
}

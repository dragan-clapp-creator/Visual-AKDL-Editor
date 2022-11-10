package akdl.edit.tree.node;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.util.ColorSet;
import akdl.graph.nodes.StringSymbol;

public class StringTreeNode extends ATreeNode {

  private static final long serialVersionUID = -2987283663426194311L;

  public StringTreeNode(String name, StringSymbol item) {
    super(name, item);
  }

  @Override
  public void populate(PropertiesPanel propertiesPanel, ControlPanel controlPanel) {
    propertiesPanel.setupEvaluation(this);
    propertiesPanel.removeComponents();
    propertiesPanel.addComponent(new JLabel("STRING Constant"), 0, 0);
    createTextField(propertiesPanel, getName(), Field.NAME, 0);
    displayPath(propertiesPanel, 2);
    if (controlPanel != null) {
      controlPanel.update(this);
    }
    propertiesPanel.redraw();
  }

  @Override
  public Color getBackground() {
    return ColorSet.forStringNode.getColor();
  }

  @Override
  public String getToolTipText() {
    return "terminal string node";
  }

  @Override
  public Font getFont() {
    return new Font("Monospaced", Font.ITALIC, 14);
  }

  public String toString() {
    return "KEYWORD \"" + getName() + "\"";
  }

  public String getSource(boolean isRaw) {
    return (isRaw ? "" : "<span style=\"background-color:" + ColorSet.forStringNode.getHexColor() + "\">")
        + "\"" + getName() + "\"" + (isRaw ? "" : "</span>");
  }
}

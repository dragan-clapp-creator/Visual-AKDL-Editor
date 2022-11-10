package akdl.edit.tree.node;

import java.awt.Color;

import javax.swing.JLabel;

import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.util.ColorSet;
import akdl.graph.nodes.EnumSymbol;

public class EnumElementTreeNode extends ATreeNode {

  private static final long serialVersionUID = 3421501457309757519L;

  private String identifier;

  public EnumElementTreeNode(String name, EnumSymbol item, String id) {
    super(name, item);
    identifier = id == null ? "" : id;
  }

  @Override
  public Color getBackground() {
    return ColorSet.forEnumElementNode.getColor();
  }

  @Override
  public String getToolTipText() {
    return "terminal enum element node";
  }

  public void setIdentifier(String name) {
    identifier = name;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String toString() {
    String id = ":" + identifier;
    return "ENUM ELEMENT '" + getName() + (identifier == null ? "" : id) + "'";
  }

  public String getSource(boolean isRaw) {
    String src =
        (isRaw ? "" : "<span style=\"background-color:" + ColorSet.forEnumElementNode.getHexColor() + "\">")
        + getName();
    if (identifier.length() > 0 && !identifier.equals(getName())) {
      if (identifier.length() == 1) {
        return src + "['" + identifier + "']" + (isRaw ? "" : "</span>");
      }
      return src + "[\"" + identifier + "\"]" + (isRaw ? "" : "</span>");
    }
    return src + "[]" + (isRaw ? "" : "</span>");
  }

  @Override
  public void populate(PropertiesPanel propertiesPanel, ControlPanel controlPanel) {
    propertiesPanel.setupEvaluation(this);
    propertiesPanel.removeComponents();
    propertiesPanel.addComponent(new JLabel("Enum Element name"), 0, 0);
    createTextField(propertiesPanel, getName(), Field.NAME, 0);

    propertiesPanel.addComponent(new JLabel("value"), 0, 1);
    createTextField(propertiesPanel, identifier, Field.IDENT, 1);
    displayPath(propertiesPanel, 3);
    if (controlPanel != null) {
      controlPanel.update(this);
    }
    propertiesPanel.redraw();
  }
}

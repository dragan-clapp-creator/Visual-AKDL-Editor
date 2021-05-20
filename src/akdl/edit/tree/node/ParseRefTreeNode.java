package akdl.edit.tree.node;

import java.awt.Color;

import javax.swing.JLabel;

import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.graph.nodes.KeySymbol;

public class ParseRefTreeNode extends ATreeNode {

  private static final long serialVersionUID = 7973519094130399406L;

  public ParseRefTreeNode(String name, String refName, KeySymbol item) {
    super(name, item);
    setIdentifier(refName);
  }

  public String toString() {
    return "PARSE REF '" + getName() + "' (" + getIdentifier() + ")";
  }

  @Override
  public Color getBackground() {
    return null;
  }

  @Override
  public void populate(PropertiesPanel propertiesPanel, ControlPanel controlPanel) {
    propertiesPanel.setupEvaluation(this);
    propertiesPanel.removeComponents();
    propertiesPanel.addComponent(new JLabel("Node name"), 0, 0);
    createTextField(propertiesPanel, getName(), Field.NAME, 0);

    if (getIdentifier() != null) {
      propertiesPanel.addComponent(new JLabel("identifier"), 0, 1);
      createComboBox(propertiesPanel, getIdentifier(), Field.IDENT, 1);
    }
    displayPath(propertiesPanel, 3);
    if (controlPanel != null) {
      controlPanel.update(this);
    }
    propertiesPanel.redraw();
  }

  public String getSource(boolean isRaw) {
    if (getIdentifier() == null) {
      return (isRaw ? "" : "<span style=\"background-color:#f2cc33" + "\">")
          + getName() + (isRaw ? "" : "</span>");
    }
    return (isRaw ? "" : "<span style=\"background-color:#f2cc33" + "\">")
        + getName() + ":" + getIdentifier() + ":ParseRef" + (isRaw ? "" : "</span>");
  }

  @Override
  public String getToolTipText() {
    return "pseudo- terminal node used to declare a reference to the parser";
  }
}

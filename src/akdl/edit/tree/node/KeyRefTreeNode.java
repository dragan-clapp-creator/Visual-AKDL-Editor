package akdl.edit.tree.node;

import java.awt.Color;

import javax.swing.JLabel;

import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.util.ColorSet;
import akdl.graph.nodes.KeySymbol;

public class KeyRefTreeNode extends ATreeNode {

  private static final long serialVersionUID = 8653676225573453900L;

  public KeyRefTreeNode(String name, KeySymbol item) {
    super(name, item);
  }

  @Override
  public Color getBackground() {
    return ColorSet.forRefNode.getColor();
  }

  @Override
  public String getToolTipText() {
    return "pseudo-terminal node referencing a keyword";
  }

  public String toString() {
    return getName() + (((KeySymbol)getItem()).isReference() ? ": RT REF" : "(tree REF)");
  }

  @Override
  public void populate(PropertiesPanel propertiesPanel, ControlPanel controlPanel) {
    propertiesPanel.setupEvaluation(this);
    propertiesPanel.removeComponents();
    propertiesPanel.addComponent(new JLabel("Node name"), 0, 0);
    createDisplayField(propertiesPanel, getName(), 0);

    if (getIdentifier() != null) {
      propertiesPanel.addComponent(new JLabel("identifier"), 0, 1);
      createTextField(propertiesPanel, getIdentifier(), Field.IDENT, 1);
    }

    displayPath(propertiesPanel, 7);
    propertiesPanel.redraw();
  }

  public String getSource(boolean isRaw) {
    return (isRaw ? "" : "<span style=\"background-color:" + ColorSet.forKeywordNode.getHexColor() + "\">")
          + (((KeySymbol)getItem()).isReference() ? "REFERENCE:" : "") + getName()
          + (isRaw ? "" : "</span>");
  }
}

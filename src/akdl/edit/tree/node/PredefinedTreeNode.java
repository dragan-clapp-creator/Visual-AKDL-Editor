package akdl.edit.tree.node;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import akdl.edit.handler.GeneralContext;
import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.util.ColorSet;
import akdl.graph.nodes.PredefinedSymbol;
import akdl.sheet.keys.LeafType;

public class PredefinedTreeNode extends ATreeNode {

  private static final long serialVersionUID = 6222968510248594425L;

  private PredefinedSymbol item;

  public PredefinedTreeNode(String name, String varname, PredefinedSymbol item) {
    super(name, item);
    setIdentifier(varname);
    this.item = item;
  }

  @Override
  public Color getBackground() {
    return ColorSet.forJavaNode.getColor();
  }

  @Override
  public String getToolTipText() {
    return "terminal predefined node";
  }

  public boolean isIdentifier() {
    return item.getType() == LeafType.IDENTIFIER;
  }

  public boolean isReference() {
    return item.isReference();
  }

  public String toString() {
    String id = "(" + getIdentifier() + (item.isReference() ? ": RT REF" : "") + ")";
    return "<" + getName() + "> " + (getIdentifier() == null ? "PREDEFINED": id);
  }

  public String getSource(boolean isRaw) {
    String src = (isRaw ? "" : "<span style=\"background-color:" + ColorSet.forJavaNode.getHexColor() + "\">") 
                + (item.isReference() ? "REFERENCE:" : "")
                + (getIdentifier() == null ? "" : getIdentifier() + ":") + getName();
    return src + (isRaw ? "" : "</span>");
  }

  public void populate(PropertiesPanel propertiesPanel, ControlPanel controlPanel) {
    propertiesPanel.setupEvaluation(this);
    propertiesPanel.removeComponents();
    propertiesPanel.addComponent(new JLabel("Node name"), 0, 0);
    createTextField(propertiesPanel, getName(), Field.NAME, 0);

    if (getIdentifier() != null) {
      propertiesPanel.addComponent(new JLabel("identifier"), 0, 1);
      if (getItem().isSerialized()) {
        createDisplayField(propertiesPanel, getIdentifier(), 1);
      }
      else {
        createPredefinedDropDown(propertiesPanel, getIdentifier(), Field.IDENT);
      }
    }
    JCheckBox checkRef = new JCheckBox("runtime reference");
    checkRef.setSelected(item.isReference());
    checkRef.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        item.setReference(checkRef.isSelected());
        GeneralContext.getInstance().refreshAll(PredefinedTreeNode.this);
        GeneralContext.getInstance().setDirty(true);
      }
    });
    propertiesPanel.addComponent(checkRef, 2, 2);
    displayPath(propertiesPanel, 3);
    if (controlPanel != null) {
      controlPanel.update(this);
    }
    propertiesPanel.redraw();
  }

  public void createPredefinedDropDown(PropertiesPanel propertiesPanel, String name, Field field) {
    propertiesPanel.addComponent(Box.createVerticalStrut(5), 1, 1);
    String[] values = new String[LeafType.values().length];
    for (int i=0; i<values.length; i++) {
      LeafType tp = LeafType.values()[i];
      values[i] = tp.name();
    }
    JComboBox<String> jt = new JComboBox<>(values);
    jt.setSelectedItem(name);
    jt.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        field.update((String) jt.getSelectedItem(), PredefinedTreeNode.this);
        GeneralContext.getInstance().refreshAll(PredefinedTreeNode.this);
        GeneralContext.getInstance().setDirty(true);
      }
    });
    propertiesPanel.addComponent(jt, 2, 1);
  }
}

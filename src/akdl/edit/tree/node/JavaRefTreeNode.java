package akdl.edit.tree.node;

import java.awt.Color;

import akdl.graph.nodes.elts.ASymbol;

public class JavaRefTreeNode extends ATreeNode {

  private static final long serialVersionUID = 2130480428661273253L;

  private String refName;
  private String refType;

  public JavaRefTreeNode(String name, String refName, String refType, ASymbol item) {
    super(name, item);
    this.refName = refName;
    this.refType = refType;
  }

  @Override
  public Color getBackground() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getToolTipText() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getRefName() {
    return refName;
  }

  public String getRefType() {
    return refType;
  }

  public String toString() {
    String action = "ADD REFERENCE TO JAVA -> ";
    if (refType != null) {
      return action + refName + ":" + refType;
    }
    return action + refName;
  }

  public String getSource(boolean isRaw) {
    if (refName != null) {
      if (refType != null) {
        return getName() + ":" + refName + ":" + refType;
      }
      return getName() + ":" + refName;
    }
    return getName();
  }
}

package akdl.edit.tree.node;

import java.awt.Color;

import javax.swing.JLabel;

import akdl.edit.handler.GeneralContext;
import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.util.ColorSet;
import akdl.graph.nodes.elts.ASymbol;

public class OperatorTreeNode extends ATreeNode {

  private static final long serialVersionUID = 4434093143351413818L;

  static public enum Operator {
    ALTERNATIVE('+', ColorSet.forAlternative.getColor()),
    OPTIONAL('^', ColorSet.forOptional.getColor()),
    OMITTED('~', ColorSet.forOmitted.getColor()),
    ZERO_OR_MANY('*', ColorSet.forZeroOrMany.getColor()),
    ANY_ORDER('&', ColorSet.forAnyOrder.getColor()),
    ENUM('+', ColorSet.forEnum.getColor()),
    DETACHED('-', ColorSet.forDetached.getColor());

    private char c;
    private Color clr;

    private Operator(char c, Color clr) {
      this.c = c;
      this.clr = clr;
    }

    public Color getColor() {
      return clr;
    }

    public char getChar() {
      return c;
    }

    public String getCharAsString() {
      return ""+c;
    }
    
  }

  private Operator operator;
  private ASymbol defaultItem;

  public OperatorTreeNode(Operator op, ASymbol item) {
    super(op.name(), item);
    operator = op;
  }

  public boolean isAlternative() {
    return operator == Operator.ALTERNATIVE;
  }

  public boolean isEnum() {
    return operator == Operator.ENUM;
  }

  public boolean isDetached() {
    return operator == Operator.DETACHED;
  }

  @Override
  public void populate(PropertiesPanel propertiesPanel, ControlPanel controlPanel) {
    propertiesPanel.setupEvaluation(this);
    propertiesPanel.removeComponents();
    propertiesPanel.addComponent(new JLabel("Node name"), 0, 0);
    createDisplayField(propertiesPanel, getName(), 0);

    propertiesPanel.addComponent(new JLabel("operator"), 0, 1);
    createDisplayField(propertiesPanel, operator.getCharAsString(), 1);

    displayPath(propertiesPanel, 7);
    if (controlPanel != null) {
      controlPanel.update(this);
    }
    propertiesPanel.redraw();
  }

  @Override
  public Color getBackground() {
    return operator.getColor();
  }

  @Override
  public String getToolTipText() {
    return "non-terminal operator node";
  }

  @Override
  public String toString() {
    return operator.name() + "('" + operator.getChar() + "')";
  }

  public void setTypeToEnum() {
    operator = Operator.ENUM;
  }

  public String getSource(boolean isRaw) {
    switch (getChildCount()) {
      case 0:
        return "";
      case 1:
        if (isDetached()) {
          return "";
        }
        return operator.getChar() + getChildren(isRaw);
      default:
        return operator.getChar() + "(" + getChildren(isRaw) + ")";
    }
  }

  //
  private String getChildren(boolean isRaw) {
    String src = " ";
    for (int i=0; i<getChildCount(); i++) {
      ATreeNode c = (ATreeNode) getChildAt(i);
      src += c.getSource(isRaw) + " ";
    }
    return src;
  }

  public void removeChildren() {
    for (int i=0; i<getChildCount(); i++) {
      ATreeNode c = (ATreeNode) getChildAt(i);
      if (c instanceof KeywordTreeNode) {
        GeneralContext.getInstance().getTreeHandler().removeNode(c.getName());
      }
      c.removeChildren();
    }
  }

  public boolean isOptional() {
    return operator == Operator.OPTIONAL;
  }

  public boolean isMany() {
    return operator == Operator.ZERO_OR_MANY;
  }

  public boolean isOption() {
    return operator == Operator.OPTIONAL || operator == Operator.ZERO_OR_MANY;
  }

  public boolean isOmitted() {
    return operator == Operator.OMITTED;
  }

  public boolean isAnyOrder() {
    return operator == Operator.ANY_ORDER;
  }

  public void setDefault(ASymbol item) {
    defaultItem = item;
  }

  public ASymbol getDefaultItem() {
    return defaultItem;
  }
}

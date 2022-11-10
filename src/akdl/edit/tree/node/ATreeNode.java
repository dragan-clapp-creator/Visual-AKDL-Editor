package akdl.edit.tree.node;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import akdl.edit.handler.GeneralContext;
import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.util.ColorSet;
import akdl.eval.EvaluationInfo.EvalClass;
import akdl.graph.Graph;
import akdl.graph.nodes.DefinitionNode;
import akdl.graph.nodes.KeySymbol;
import akdl.graph.nodes.elts.ASymbol;

public abstract class ATreeNode extends DefaultMutableTreeNode {

  private static final long serialVersionUID = -924053623271845397L;

  public static String DEFAULT_PARSER_PACKAGE = Graph.DEFAULT_PARSER_PACKAGE;
  public static String DEFAULT_RUNTIME_PACKAGE = Graph.DEFAULT_RUNTIME_PACKAGE;

  private static String OLD_DEFAULT_PARSER_PACKAGE = Graph.DEFAULT_PARSER_PACKAGE;
  private static String OLD_DEFAULT_RUNTIME_PACKAGE = Graph.DEFAULT_RUNTIME_PACKAGE;


  abstract public Color getBackground();
  abstract public String getToolTipText();

  public static enum Field {
    NAME, IDENT, PRS_PACK, PRS_NAME, RT_PACK, RT_NAME, JAVA;

    private String val;

    public String getVal() {
      return val;
    }

    public void setVal(String val) {
      this.val = val;
    }

    public void update(String text, ATreeNode aTreeNode) {
      KeywordTreeNode kw;
      switch (this) {
        case NAME:
          if (!text.equals(aTreeNode.getName())) {
            aTreeNode.setName(text);
            GeneralContext.getInstance().setDirty(true);
          }
          break;
        case IDENT:
          if (!text.equals(aTreeNode.getIdentifier())) {
            aTreeNode.setIdentifier(text);
            GeneralContext.getInstance().setDirty(true);
          }
          break;
        case PRS_PACK:
          kw = (KeywordTreeNode)aTreeNode;
          if (!text.equals(kw.getPrs_pack())) {
            kw.setPrs_pack(text);
            GeneralContext.getInstance().setDirty(true);
          }
          break;
        case PRS_NAME:
          kw = (KeywordTreeNode)aTreeNode;
          if (!text.equals(kw.getPrs_name())) {
            kw.setPrs_name(text);
            GeneralContext.getInstance().setDirty(true);
          }
          break;
        case RT_PACK:
          kw = (KeywordTreeNode)aTreeNode;
          if (!text.equals(kw.getRt_pack())) {
            kw.setRt_pack(text);
            GeneralContext.getInstance().setDirty(true);
          }
          break;
        case RT_NAME:
          kw = (KeywordTreeNode)aTreeNode;
          if (!text.equals(kw.getRt_name())) {
            kw.setRt_name(text);
            GeneralContext.getInstance().setDirty(true);
          }
          break;
        case JAVA:
          kw = (KeywordTreeNode)aTreeNode;
          if (text.isEmpty() && kw.getType() != null) {
            kw.setJavaType(null);
            GeneralContext.getInstance().setDirty(true);
          }
          else if (kw.getType() == null || !text.equals(kw.getType().name())) {
            kw.setJavaType(text);
            GeneralContext.getInstance().setDirty(true);
          }
          break;

        default:
          break;
      }
      GeneralContext.getInstance().refresh(true);
    }
  }
  private String name;
  private String identifier;
  private ASymbol item;
  private String position;

  private boolean isInvalide;


  abstract public String getSource(boolean isRaw);


  public ATreeNode(String name, ASymbol item) {
    this.name = name;
    this.item = item;
    if (item instanceof KeySymbol) {
      identifier = ((KeySymbol)item).getVarName();
    }
  }

  public void setupPositions() {
    definePosition();
    for (int i=0; i<getChildCount(); i++) {
      ((ATreeNode)getChildAt(i)).setupPositions();
    }
  }

  //
  private void definePosition() {
    position = "0";
    ATreeNode c = this;
    ATreeNode p = (ATreeNode) getParent();
    while (p != null) {
      position = p.getIndex(c)+";"+position;
      c = (ATreeNode) c.getParent();
      p = (ATreeNode) c.getParent();
    }
  }

  //
  public String findParentOutOfParents(Set<String> parents) {
    if (parents != null) {
      for (Iterator<String> it=parents.iterator(); it.hasNext();) {
        String pname = it.next();
        ATreeNode p = this;
        while (p != null && !p.getName().equals(pname)) {
          p = (ATreeNode) p.getParent();
        }
        if (p != null) {
          return pname;
        }
      }
    }
    return null;
  }

  public String toString() {
    return getName();
  }

  public String toString(String indent) {
    String str = indent + getName() + "\n";
    indent += "\t";
    for (int i=0; i<getChildCount(); i++) {
      ATreeNode child = (ATreeNode) getChildAt(i);
      str += child.toString(indent);
    }
    return str;
  }

  public void populate(PropertiesPanel propertiesPanel, ControlPanel controlPanel) {
    propertiesPanel.setupEvaluation(this);
    propertiesPanel.removeComponents();
    propertiesPanel.addComponent(new JLabel("Node name"), 0, 0);
    createTextField(propertiesPanel, name, Field.NAME, 0);

    if (identifier != null) {
      propertiesPanel.addComponent(new JLabel("identifier"), 0, 1);
      createTextField(propertiesPanel, identifier, Field.IDENT, 1);
    }
    displayPath(propertiesPanel, 3);
    if (controlPanel != null) {
      controlPanel.update(this);
    }
    propertiesPanel.redraw();
  }

  public void createComboBox(PropertiesPanel propertiesPanel, String name, Field field, int y) {
    propertiesPanel.addComponent(Box.createVerticalStrut(5), 1, y);
    String[] list = findElements(getName());
    JComboBox<String> jc = new JComboBox<>(list);
    jc.setSelectedItem(name);
    if (getItem().isSerialized()) {
      jc.setEditable(false);
      jc.setBackground(ColorSet.notEditableProperty.getColor());
    }
    else {
      jc.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          field.update((String) jc.getSelectedItem(), ATreeNode.this);
          GeneralContext.getInstance().refreshAll(ATreeNode.this);
        }
      });
      jc.addFocusListener(new FocusListener() {
        @Override
        public void focusLost(FocusEvent e) {
          field.update((String) jc.getSelectedItem(), ATreeNode.this);
          GeneralContext.getInstance().refreshAll(ATreeNode.this);
        }
        @Override
        public void focusGained(FocusEvent e) {
        }
      });
    }
    propertiesPanel.addComponent(jc, 2, y);
  }

  //
  private String[] findElements(String name) {
    TreeNode p = getParent();
    while (p.getParent() != null) {
      p = p.getParent();
    }
    for (int i=0; i<p.getChildCount(); i++) {
      TreeNode tn = p.getChildAt(i);
      if (tn instanceof OperatorTreeNode) {
        OperatorTreeNode op = (OperatorTreeNode) tn;
        if (op.isDetached()) {
          KeywordTreeNode kn = (KeywordTreeNode) op.getChildAt(0);
          if (kn.getName().equals(name)) {
            int n = kn.getChildAt(0).getChildCount();
            String[] list = new String[n];
            for (int j=0; j<n; j++) {
              list[j] = ((EnumElementTreeNode)kn.getChildAt(0).getChildAt(j)).getName();
            }
            return list;
          }
        }
      }
    }
    return null;
  }
  public void createTextField(PropertiesPanel propertiesPanel, String name, Field field, int y) {
    propertiesPanel.addComponent(Box.createVerticalStrut(5), 1, y);
    JTextField jt = new JTextField((name.length()+5)/2);
    jt.setText(name);
    if (getItem().isSerialized()) {
      jt.setEditable(false);
      jt.setBackground(ColorSet.notEditableProperty.getColor());
    }
    else {
      jt.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          field.update(jt.getText(), ATreeNode.this);
          GeneralContext.getInstance().refreshAll(ATreeNode.this);
        }
      });
      jt.addFocusListener(new FocusListener() {
        @Override
        public void focusLost(FocusEvent e) {
          field.update(jt.getText(), ATreeNode.this);
          GeneralContext.getInstance().refreshAll(ATreeNode.this);
        }
        @Override
        public void focusGained(FocusEvent e) {
        }
      });
    }
    propertiesPanel.addComponent(jt, 2, y);
  }

  public void createDisplayField(PropertiesPanel propertiesPanel, String name, int y) {
    propertiesPanel.addComponent(Box.createVerticalStrut(5), 1, y);
    JTextField jt = new JTextField((name.length()+5)/2);
    jt.setText(name);
    jt.setEnabled(false);
    jt.setBackground(ColorSet.notEditableArea.getColor());
    propertiesPanel.addComponent(jt, 2, y);
  }

  public void displayPath(PropertiesPanel propertiesPanel, int y) {
    if (ControlPanel.isPathDisplay) {
      propertiesPanel.addDisplay(getPaths(), y);
    }
  }

  private String getPaths() {
    String str = getName();
    ATreeNode parent = (ATreeNode) getParent();
    while (parent != null) {
      str = parent.getName() + "\n->" + str;
      parent = (ATreeNode) parent.getParent();
    }
    return str;
  }

  public Font getFont() {
    return new Font("Monospaced", Font.BOLD, 14);
  }

  public String getName() {
    if (name.length() == 1 && name.charAt(0) == '\\') {
      return "\\\\";
    }
    return name;
  }
  public void setName(String n) {
    item.setName(n);
    GeneralContext.getInstance().getTreeHandler().renameNode(name, n, this);
    name = n;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public ATreeNode cloneWithSuffix() {
    ATreeNode copy = (ATreeNode) super.clone();
    copy.addNameSuffix("Copy");
    if (copy instanceof KeywordTreeNode) {
      GeneralContext.getInstance().getTreeHandler().addKeyNode((KeywordTreeNode) copy, copy.getName());
    }
    for (int i=0; i<getChildCount(); i++) {
      ATreeNode child = (ATreeNode) getChildAt(i);
      copy.add(child.cloneWithSuffix());
    }
    return (ATreeNode) copy;
  }

  public void addNameSuffix(String suf) {
  }

  public void addSuffix(String suf) {
    name = name + suf;
  }

  public void removeChildren() {
  }


  public ASymbol getItem() {
    return item;
  }

  public void setItem(ASymbol def) {
    item = def;
  }

  public void removeChildSymbol(ASymbol sym) {
    if (item instanceof DefinitionNode) {
      ((DefinitionNode) item).getSyntax().remove(sym);
    }
    else if (item instanceof KeySymbol) {
      ((KeySymbol) item).getDefinition().getSyntax().remove(sym);
    }
  }

  public void removeChildSymbols() {
    if (item instanceof DefinitionNode) {
      ((DefinitionNode) item).getSyntax().clear();
    }
    else if (item instanceof KeySymbol) {
      ((KeySymbol) item).getDefinition().getSyntax().clear();
    }
  }

  public void addChildSymbol(ASymbol sym) {
    if (item instanceof DefinitionNode) {
      ((DefinitionNode) item).addToSyntax(sym);
    }
    else if (item instanceof KeySymbol) {
      DefinitionNode def = ((KeySymbol) item).getDefinition();
      if (def == null) {
        def = new DefinitionNode(item.getName());
        ((KeySymbol) item).setDefinition(def);
      }
      def.addToSyntax(sym);
    }
  }

  public void addChildSymbol(ASymbol sym, int index) {
    if (item instanceof DefinitionNode) {
      ((DefinitionNode) item).getSyntax().add(index, sym);
    }
    else if (item instanceof KeySymbol) {
      ((KeySymbol) item).getDefinition().getSyntax().add(index, sym);
    }
  }

  /**
   * @return the position
   */
  public String getPosition() {
    return position;
  }

  public void propagateNewDefaultParsePackage() {
    for (int i=0; i<getChildCount(); i++) {
      ATreeNode child = (ATreeNode) getChildAt(i);
      if (child instanceof KeywordTreeNode) {
        KeywordTreeNode ktn = (KeywordTreeNode) child;
        if (ktn.getPrs_pack().equals(OLD_DEFAULT_PARSER_PACKAGE)) {
          ktn.setPrs_pack(DEFAULT_PARSER_PACKAGE);
        }
      }
      child.propagateNewDefaultParsePackage();
    }
  }

  public void propagateNewDefaultRunPackage() {
    for (int i=0; i<getChildCount(); i++) {
      ATreeNode child = (ATreeNode) getChildAt(i);
      if (child instanceof KeywordTreeNode) {
        KeywordTreeNode ktn = (KeywordTreeNode) child;
        if (ktn.getRt_pack().equals(OLD_DEFAULT_RUNTIME_PACKAGE)) {
          ktn.setRt_pack(DEFAULT_RUNTIME_PACKAGE);
        }
      }
      child.propagateNewDefaultRunPackage();
    }
  }

  public void invalidate() {
    isInvalide = true;
  }
  /**
   * @return the isInvalide
   */
  public boolean isInvalide() {
    return isInvalide;
  }

  public void propagate(EvalClass eclass) {
    for (int i=0; i<getChildCount(); i++) {
      ((ATreeNode)getChildAt(i)).propagate(eclass);
    }
  }

  public boolean hasEval(TreeNode p) {
    if (p instanceof KeywordTreeNode) {
      return ((KeywordTreeNode) p).getRegisteredEvaluation() != null;
    }
    if (p != null) {
      return hasEval(p.getParent());
    }
    return false;
  }
}

package akdl.edit.tree.node;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import akdl.edit.handler.GeneralContext;
import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.util.ColorSet;
import akdl.eval.EvaluationInfo;
import akdl.eval.EvaluationInfo.EvalClass;
import akdl.eval.EvaluationInfo.EvalType;
import akdl.graph.nodes.DefinitionNode;
import akdl.graph.nodes.GroupOfSymbols;
import akdl.graph.nodes.KeySymbol;
import akdl.graph.nodes.elts.ASymbol;
import akdl.sheet.keys.AttKeys;

public class KeywordTreeNode extends ATreeNode {

  private static final long serialVersionUID = -8878088743758885138L;

  private AttKeys originalType;
  private AttKeys type;
  private String prs_pack;
  private String prs_name;
  private String rt_pack;
  private String rt_name;

  public KeywordTreeNode(String name, String varname, KeySymbol ks, DefinitionNode item) {
    super(name, ks);
    if (ks == null) {
      // ROOT case
      setItem(item);
    }
    else if (ks.getDefinition() == null) {
      ks.setDefinition(new DefinitionNode(name));
      setType(ks.getDefinition().getType());
    }
    setIdentifier(varname);
    setPrs_name(prs_name);
    setPrs_pack(prs_pack);
    setRt_name(rt_name);
    setRt_pack(rt_pack);
  }

  public AttKeys getType() {
    return type;
  }
  public void setType(AttKeys type) {
    this.type = type;
    this.originalType = type;
  }
  public String getPrs_pack() {
    return prs_pack;
  }
  public void setPrs_pack(String prs_pack) {
    if (prs_pack != null) {
      if (prs_pack.charAt(prs_pack.length()-1) == '.') {
        this.prs_pack = prs_pack;
      }
      else {
        this.prs_pack = prs_pack + ".";
      }
      if (GeneralContext.getInstance().getRootNode() == this) {
        DEFAULT_PARSER_PACKAGE = this.prs_pack;
        propagateNewDefaultParsePackage();
      }
    }
    else {
      this.prs_pack = DEFAULT_PARSER_PACKAGE;
      if (GeneralContext.getInstance().getRootNode() == this) {
        propagateNewDefaultParsePackage();
      }
    }
  }
  public String getPrs_name() {
    return prs_name;
  }
  public void setPrs_name(String prs_name) {
    if (prs_name != null && !prs_name.isEmpty()) {
      this.prs_name = getItem().upper(prs_name);
    }
    else {
      this.prs_name = getItem().upper(getName());
    }
  }
  public String getRt_pack() {
    return rt_pack;
  }
  public void setRt_pack(String rt_pack) {
    if (rt_pack != null) {
      if (rt_pack.charAt(rt_pack.length()-1) == '.') {
        this.rt_pack = rt_pack;
      }
      else {
        this.rt_pack = rt_pack + ".";
      }
      if (GeneralContext.getInstance().getRootNode() == this) {
        DEFAULT_RUNTIME_PACKAGE = this.rt_pack;
        propagateNewDefaultRunPackage();
      }
    }
    else {
      this.rt_pack = DEFAULT_RUNTIME_PACKAGE;
      if (GeneralContext.getInstance().getRootNode() == this) {
        propagateNewDefaultRunPackage();
      }
    }
  }
  public String getRt_name() {
    return rt_name;
  }
  public void setRt_name(String rt_name) {
    if (rt_name != null && !rt_name.isEmpty()) {
      this.rt_name = getItem().upper(rt_name);
    }
    else {
      this.rt_name = getItem().upper(getName());
    }
  }

  @Override
  public Color getBackground() {
    return ColorSet.forKeywordNode.getColor();
  }

  @Override
  public String getToolTipText() {
    return "non-terminal keyword node";
  }

  public void add(ATreeNode newChild) {
    super.add(newChild);
    if (type == null && newChild instanceof OperatorTreeNode) {
      if (((OperatorTreeNode)newChild).isEnum()) {
        type = AttKeys.ATT_ENUM;
      }
      else {
        type = AttKeys.ATT_INTERFACE;
      }
    }
    if (getChildCount() > 1) {
      type = originalType;
    }
  }

  public String toString() {
    ASymbol sym = getItem();
    if (sym instanceof KeySymbol) {
      return getName() + (((KeySymbol)sym).isReference() ? ": RT REF" : "");
    }
    return getName();
  }

  public boolean isRef() {
    ASymbol sym = getItem();
    if (sym instanceof KeySymbol) {
      return ((KeySymbol)sym).isReference();
    }
    return false;
  }

  @Override
  public void addNameSuffix(String suf) {
    super.addSuffix(suf);
  }

  @Override
  public void populate(PropertiesPanel propertiesPanel, ControlPanel controlPanel) {
    propertiesPanel.setupEvaluation(this);
    propertiesPanel.removeComponents();
    propertiesPanel.addComponent(new JLabel("Node name"), 0, 0);
    createTextField(propertiesPanel, getName(), Field.NAME, 0);

    if (getIdentifier() != null) {
      propertiesPanel.addComponent(new JLabel("identifier"), 0, 1);
      createTextField(propertiesPanel, getIdentifier(), Field.IDENT, 1);
    }

    propertiesPanel.addComponent(new JLabel("Parser package"), 0, 3);
    createTextField(propertiesPanel, prs_pack == null ? DEFAULT_PARSER_PACKAGE : prs_pack, Field.PRS_PACK, 3);

    propertiesPanel.addComponent(new JLabel("Parser name"), 0, 4);
    createTextField(propertiesPanel, prs_name == null ? getItem().upper(getName()) : prs_name, Field.PRS_NAME, 4);

    propertiesPanel.addComponent(new JLabel("RT package"), 0, 5);
    createTextField(propertiesPanel, rt_pack == null ? DEFAULT_RUNTIME_PACKAGE : rt_pack, Field.RT_PACK, 5);

    propertiesPanel.addComponent(new JLabel("RT name"), 0, 6);
    createTextField(propertiesPanel, rt_name == null ? getItem().upper(getName()) : rt_name, Field.RT_NAME, 6);

    if (getItem() instanceof KeySymbol && getChildCount() == 0) {
      KeySymbol sym = (KeySymbol) getItem();
      JCheckBox checkRef = new JCheckBox("runtime reference");
      checkRef.setSelected(sym.isReference());
      if (sym.isSerialized()) {
        checkRef.setEnabled(false);
      }
      else {
        checkRef.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            sym.setReference(checkRef.isSelected());
            GeneralContext.getInstance().reassign(KeywordTreeNode.this, true);
            GeneralContext.getInstance().setDirty(true);
          }
        });
      }
      propertiesPanel.addComponent(checkRef, 2, 7);

      JCheckBox checkPRef = new JCheckBox("parser reference");
      checkPRef.setSelected(sym.isParseRef());
      checkPRef.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          sym.setParseRef(checkPRef.isSelected());
          GeneralContext.getInstance().reassign(KeywordTreeNode.this, false);
          GeneralContext.getInstance().setDirty(true);
        }
      });
      propertiesPanel.addComponent(checkPRef, 2, 8);
    }

    displayPath(propertiesPanel, 9);
    if (controlPanel != null) {
      controlPanel.update(this);
    }
    propertiesPanel.redraw();
  }

  public String getSourceDefinition(boolean isRaw) {
    String indent = getIndent(isRaw);
    String src = (isRaw ? "\n" : "<div class=name>") + indent
                  + getName() + getProperties(isRaw) + (isRaw ? "\n" : "</div>")
                  + indent + (isRaw ? "    = " : "&emsp;= ");
    for (int i=0; i<getChildCount(); i++) {
      ATreeNode c = (ATreeNode) getChildAt(i);
      src += c.getSource(isRaw) + " ";
    }
    return src + (isRaw ? "\n" + indent + ";" : "<br>" + indent + " ;");
  }

  //
  private String getIndent(boolean isRaw) {
    String s = "";
    for (int i=0; i<getPosition().split(";").length; i++) {
      if (isRaw) {
        s += "    ";
      }
      else {
        s += "&emsp;";
      }
    }
    return s;
  }

  public String getDelta(boolean isRaw) {
    String src = (isRaw ? "\t" : "<div class=name>&emsp;")
                  + getName() + getProperties(isRaw) + (isRaw ? "\n" : "</div>")
                  + (isRaw ? "\t\t= " : "&emsp;&emsp;= ");
    for (int i=0; i<getChildCount(); i++) {
      ATreeNode c = (ATreeNode) getChildAt(i);
      if (c instanceof OperatorTreeNode) {
        for (int j=0; j<c.getChildCount(); j++) {
          ATreeNode child = (ATreeNode) c.getChildAt(j);
          if (!child.getItem().isSerialized()) {
            src += child.getSource(isRaw) + " ";
          }
        }
      }
    }
    return src + (isRaw ? "\n\t;" : "<br>&emsp; ;");
  }

  @Override
  public String getSource(boolean isRaw) {
    return (isRaw ? "" : "<span style=\"background-color:" + ColorSet.forKeywordNode.getHexColor() +  "\">")
        + (((KeySymbol)getItem()).isReference() ? ": REFERENCE" : "") + getName()
        + (isRaw ? "" : "</span>");
  }

  //
  private String getProperties(boolean isRaw) {
    String str = "";
    EvaluationInfo info = getRegisteredEvaluation();
    if (!isAllInitial() || GeneralContext.getInstance().getRootNode() == this) {
      str = (isRaw ? "   [" : "<div class=props>&emsp;[") + (info == null ? "" : getEval(info)+",");
      if (!DEFAULT_PARSER_PACKAGE.equals(prs_pack)) {
        str += prs_pack;
        if (!getName().equals(prs_name)) {
          str += prs_name;
        }
      }
      else if (!getName().equals(prs_name)) {
        str += prs_pack;
        str += prs_name;
      }
      str += ",";
      if (!DEFAULT_RUNTIME_PACKAGE.equals(rt_pack)) {
        str += rt_pack;
        if (!getName().equals(rt_name)) {
          str += rt_name;
        }
      }
      else if (!getName().equals(rt_name)) {
        str += rt_pack;
        str += rt_name;
      }
      str += isRaw ? "]" : "]</div>";
      return str;
    }
    return info == null ? "" : "["+getEval(info)+"]";
  }

  //
  private String getEval(EvaluationInfo info) {
    String core = info.getEval().name();
    if (info.isFirst()) {
      return core + "_ORG";
    }
    if (info.isOperator()) {
      return core + "_OP";
    }
    if (info.isInit()) {
      return core + "_INIT";
    }
    return core;
  }

  //
  private boolean isAllInitial() {
    return getName().equalsIgnoreCase(prs_name)
        && getName().equalsIgnoreCase(rt_name)
        && (prs_pack == null || DEFAULT_PARSER_PACKAGE.equals(prs_pack))
        && (rt_pack == null || DEFAULT_RUNTIME_PACKAGE.equals(rt_pack));
  }

  //
  public void removeChildren() {
    for (int i=0; i<getChildCount(); i++) {
      ATreeNode c = (ATreeNode) getChildAt(i);
      if (c instanceof KeywordTreeNode) {
        GeneralContext.getInstance().getTreeHandler().removeNode(c.getName());
      }
      c.removeChildren();
    }
    removeAllChildren();
  }

  public void setJavaType(String text) {
    if (text == null) {
      type = null;
    }
    else {
      for (AttKeys tp : AttKeys.values()) {
        if (tp.getName().equals(text)) {
          type = tp;
          break;
        }
      }
    }
  }

  public boolean hasNewElements() {
    for (int i=0; i<getChildCount(); i++) {
      ATreeNode c = (ATreeNode) getChildAt(i);
      if (c.getItem() instanceof DefinitionNode) {
        DefinitionNode def = (DefinitionNode) c.getItem();
        for (ASymbol sym : def.getSyntax()) {
          if (!sym.isSerialized()) {
            return true;
          }
          if (sym instanceof GroupOfSymbols) {
            for (ASymbol s : ((GroupOfSymbols)sym).getSyntax()) {
              if (!s.isSerialized()) {
                return true;
              }
            }
            
          }
        }
      }
    }
    return false;
  }

  private EvalType getEval(EvalClass eclass, boolean isFirst) {
    EvalType eval = null;
    switch (eclass) {
      case LOGIC:
        if (isFirst) {
          eval = EvalType.LOGIC_ORG;
        }
        else {
          eval = EvalType.LOGIC;
        }
       break;
      case NUMERIC:
        if (isFirst) {
          eval = EvalType.NUMERIC_ORG;
        }
        else {
          eval = EvalType.NUMERIC;
        }
        break;
      case CUSTOM:
        if (isFirst) {
          eval = EvalType.CUSTOM_ORG;
        }
        else {
          eval = EvalType.CUSTOM;
        }
        break;

      default:
        break;
    }
    return eval;
  }

  @Override
  public void propagate(EvalClass eclass) {
    DefinitionNode def;
    if (getItem() instanceof DefinitionNode) {
      def = (DefinitionNode)getItem();
    }
    else {
      def = ((KeySymbol)getItem()).getDefinition();
    }
    if (eclass == null) {
      def.setEval(null);
      def.setEvaluationInfo(null);
    }
    else {
      def.setEval(getEval(eclass, !hasEval(getParent())));
      def.registerEvaluation();
    }
    super.propagate(eclass);
  }

  public EvaluationInfo getRegisteredEvaluation() {
    if (getItem() instanceof DefinitionNode) {
      return ((DefinitionNode)getItem()).getEvaluationInfo();
    }
    return ((KeySymbol)getItem()).getDefinition().getEvaluationInfo();
  }
}

package akdl.edit.util;

import java.util.ArrayList;
import java.util.List;

import akdl.edit.handler.GeneralContext;
import akdl.edit.tree.node.ATreeNode;
import akdl.edit.tree.node.CharTreeNode;
import akdl.edit.tree.node.EnumElementTreeNode;
import akdl.edit.tree.node.KeyRefTreeNode;
import akdl.edit.tree.node.KeywordTreeNode;
import akdl.edit.tree.node.OmittedKeyTreeNode;
import akdl.edit.tree.node.OperatorTreeNode;
import akdl.edit.tree.node.ParseRefTreeNode;
import akdl.edit.tree.node.StringTreeNode;
import akdl.edit.tree.node.OperatorTreeNode.Operator;
import akdl.edit.tree.node.util.Helper;
import akdl.graph.nodes.CharSymbol;
import akdl.graph.nodes.DefinitionNode;
import akdl.graph.nodes.EnumSymbol;
import akdl.graph.nodes.GroupOfSymbols;
import akdl.graph.nodes.KeySymbol;
import akdl.graph.nodes.StringSymbol;
import akdl.graph.nodes.VoidKeySymbol;
import akdl.graph.nodes.elts.ASymbol;

public class TreeNodeProcessor {


  /**
   * toggle {@link KeywordTreeNode} <-> {@link OmittedKeyTreeNode}
   * 
   * @param tnode
   */
  public void performReplace(ATreeNode tnode, GeneralContext context) {
    ATreeNode parent = (ATreeNode) tnode.getParent();
    if (parent != null) {
      ATreeNode node;
      if (tnode instanceof KeywordTreeNode) {
        context .getTreeHandler().removeNode(tnode.getName());
        KeySymbol key = (KeySymbol) tnode.getItem();
        VoidKeySymbol vkey = new VoidKeySymbol(key.getName(), key.getOperator());
        node = new OmittedKeyTreeNode(tnode.getName(), vkey);
      }
      else {
        VoidKeySymbol vkey = (VoidKeySymbol)tnode.getItem();
        KeySymbol key = new KeySymbol(vkey.getName(), vkey.getOperator(), false);
        node = context.getTreeHandler().createKeyNode(key, new DefinitionNode(key.getName()));
        if (node == null) {
          node = new KeyRefTreeNode(key.getName(), key);
        }
      }
      int i = parent.getIndex(tnode);
      parent.remove(i);
      parent.removeChildSymbol(tnode.getItem());
      parent.insert(node, i);
      parent.addChildSymbol(node.getItem());
      context.refreshAll(node);
    }
  }

  /**
   * wrap selected objet(s) with a {@link KeywordTreeNode}
   * @param selection
   * @param context
   */
  public void performWrapWithKey(List<ATreeNode> selection, GeneralContext context) {
    ATreeNode tnode = selection.get(0);
    ATreeNode parent = (ATreeNode) tnode.getParent();
    if (parent != null) {
      KeySymbol key = new KeySymbol("?", akdl.graph.nodes.elts.Operator.NONE, false);
      DefinitionNode def = new DefinitionNode("?");
      KeywordTreeNode knode = context.getTreeHandler().createKeyNode(key, def);
      ArrayList<ATreeNode> children = new ArrayList<>();
      for (int i=0; i<parent.getChildCount(); i++) {
        ATreeNode child = (ATreeNode) parent.getChildAt(i);
        if (child == tnode) {
          children.add(knode);
        }
        else if (!selection.contains(child)) {
          children.add(child);
        }
      }
      parent.removeAllChildren();
      parent.removeChildSymbols();
      for (ATreeNode child : children) {
        parent.add(child);
        parent.addChildSymbol(child.getItem());
      }
      for (ATreeNode tn : selection) {
        knode.add(tn);
        knode.addChildSymbol(tn.getItem());
      }
      context.refreshAll(knode);
    }
  }

  /**
   * wrap selected object(s) with a {@link OperatorTreeNode}
   * @param op
   * @param selection
   * @param context
   */
  public void performWrapWithOperator(Operator op, List<ATreeNode> selection, GeneralContext context) {
    ATreeNode tnode = selection.get(0);
    ATreeNode opnode;
    switch (op) {
      case ANY_ORDER:
      case ENUM:
      case OPTIONAL:
      case ZERO_OR_MANY:
      case ALTERNATIVE:
        ASymbol item = createSymbol(op, tnode, selection);
        opnode = new OperatorTreeNode(op, item);
        ATreeNode parent = (ATreeNode) tnode.getParent();
        ArrayList<ATreeNode> children = new ArrayList<>();
        for (int i=0; i<parent.getChildCount(); i++) {
          ATreeNode child = (ATreeNode) parent.getChildAt(i);
          if (child == tnode) {
            children.add(opnode);
          }
          else if (!selection.contains(child)) {
            children.add(child);
          }
        }
        parent.removeAllChildren();
        parent.removeChildSymbols();
        for (ATreeNode child : children) {
          parent.add(child);
          parent.addChildSymbol(child.getItem());
        }
        for (ATreeNode tn : selection) {
          opnode.add(tn);
          opnode.addChildSymbol(tn.getItem());
        }
        context.refreshAll(opnode);
        break;

      default:
        break;
    }
  }

  //
  private ASymbol createSymbol(Operator op, ATreeNode tnode, List<ATreeNode> selection) {
    akdl.graph.nodes.elts.Operator akdlOp = Helper.getInstance().toAKDLOperator(op);
    if (selection.size() > 1) {
      return new GroupOfSymbols(op.name(), akdlOp);
    }
    if (tnode instanceof KeywordTreeNode) {
      return new KeySymbol(tnode.getName(), akdlOp, false);
    }
    if (tnode instanceof KeyRefTreeNode) {
      return new KeySymbol(tnode.getName(), akdlOp, true);
    }
    if (tnode instanceof ParseRefTreeNode) {
      KeySymbol sym = new KeySymbol(tnode.getName(), akdlOp, false);
      sym.setParseRef(true);
      return sym;
    }
    if (tnode instanceof OmittedKeyTreeNode) {
      return new VoidKeySymbol(tnode.getName(), akdlOp);
    }
    if (tnode instanceof CharTreeNode) {
      return new CharSymbol(tnode.getName().charAt(0), akdlOp);
    }
    if (tnode instanceof StringTreeNode) {
      return new StringSymbol(tnode.getName(), akdlOp);
    }
    if (tnode instanceof EnumElementTreeNode) {
      return new EnumSymbol(tnode.getName(), akdlOp);
    }
    return null;
  }
}

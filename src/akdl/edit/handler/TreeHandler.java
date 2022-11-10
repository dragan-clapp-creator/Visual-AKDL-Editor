package akdl.edit.handler;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import akdl.edit.tree.node.ATreeNode;
import akdl.edit.tree.node.CharTreeNode;
import akdl.edit.tree.node.EnumElementTreeNode;
import akdl.edit.tree.node.KeyRefTreeNode;
import akdl.edit.tree.node.KeywordTreeNode;
import akdl.edit.tree.node.OmittedKeyTreeNode;
import akdl.edit.tree.node.OperatorTreeNode;
import akdl.edit.tree.node.OperatorTreeNode.Operator;
import akdl.edit.tree.node.ParseRefTreeNode;
import akdl.edit.tree.node.PredefinedTreeNode;
import akdl.edit.tree.node.StringTreeNode;
import akdl.edit.tree.node.util.Helper;
import akdl.graph.nodes.CharSymbol;
import akdl.graph.nodes.DefinitionNode;
import akdl.graph.nodes.EnumSymbol;
import akdl.graph.nodes.GroupOfSymbols;
import akdl.graph.nodes.KeySymbol;
import akdl.graph.nodes.PredefinedSymbol;
import akdl.graph.nodes.StringSymbol;
import akdl.graph.nodes.VoidKeySymbol;
import akdl.graph.nodes.elts.ASymbol;
import akdl.sheet.HeaderInfo;
import akdl.sheet.keys.AttKeys;

public class TreeHandler {

  private Hashtable<String, KeywordTreeNode> keywords;
  private HeaderInfo headerInfo;

  public TreeHandler(HeaderInfo info) {
    keywords = new Hashtable<>();
    headerInfo = info;
  }

  public KeywordTreeNode createKeyNode(KeySymbol ks, DefinitionNode item) {
    if (item == null || keywords.containsKey(item.getName())) {
      return null;
    }
    KeywordTreeNode node = new KeywordTreeNode(item.getName(), ks == null ? null : ks.getVarName(), ks, item);
    keywords.put(item.getName(), node);
    return node;
  }

  public void addKeyNode(KeywordTreeNode node, String name) {
    keywords.put(name, node);
  }

  public void removeNode(String name) {
    keywords.remove(name);
  }

  public KeywordTreeNode getNode(String name) {
    return keywords.get(name);
  }

  public Hashtable<String, KeywordTreeNode> getKeywords() {
    return keywords;
  }

  public void renameNode(String oldname, String newname, ATreeNode node) {
    KeywordTreeNode def = keywords.remove(oldname);
    if (def != null) {
      GeneralContext.getInstance().renameInTreeMouseListener(oldname, newname);
      KeywordTreeNode key = keywords.get(newname);
      if (key == null) {
        keywords.put(newname, def);
        if (!newname.equals(oldname)) {
          if (oldname.equalsIgnoreCase(def.getPrs_name()) || def.getPrs_name() == null) {
            def.setPrs_name(newname);
          }
          if (oldname.equalsIgnoreCase(def.getRt_name()) || def.getRt_name() == null) {
            def.setRt_name(newname);
          }
          if (def.getPrs_pack() == null) {
            def.setPrs_pack(null); // will be initialized
          }
          if (def.getRt_pack() == null) {
            def.setRt_pack(null); // will be initialized
          }
        }
      }
      else {
        GeneralContext.getInstance().reassign(node, true);
      }
    }
  }

  public void createChildren(DefinitionNode item, ATreeNode tnode) {
    if (item != null) {
      TreeMouseListener.check(item.getName(), tnode);
      if (tnode instanceof KeywordTreeNode) {
        setupProperties(item, (KeywordTreeNode) tnode);
      }
      ATreeNode next = tnode;
      for (ASymbol sym : item.getSyntax()) {
        if (sym instanceof KeySymbol) {
          next = handleKeySymbol((KeySymbol) sym, tnode, item);
        }
        else if (sym instanceof GroupOfSymbols) {
          next = handleGroup((GroupOfSymbols) sym, tnode, item);
        }
        else if (sym instanceof PredefinedSymbol) {
          next = handlePredefined((PredefinedSymbol) sym, tnode, item);
        }
        else if (sym instanceof StringSymbol) {
          next = handleString((StringSymbol) sym, tnode, item);
        }
        else if (sym instanceof CharSymbol) {
          next = handleChar((CharSymbol) sym, tnode, item);
        }
        else if (sym instanceof VoidKeySymbol) {
          next = new OmittedKeyTreeNode(sym.getName(), (VoidKeySymbol) sym);
        }
        else if (sym instanceof EnumSymbol) {
          EnumSymbol es = (EnumSymbol) sym;
          next = new EnumElementTreeNode(sym.getName(), es, es.getVarName());
        }
        if (next != null) {
          tnode.add(next);
        }
      }
    }
  }

  //
  private void setupProperties(DefinitionNode item, KeywordTreeNode node) {
    node.setName(item.getName());
    node.setPrs_pack(item.getParserPath());
    node.setPrs_name(item.getParserClassName());
    node.setRt_pack(item.getRuntimePath());
    node.setRt_name(item.getRuntimeClassName());
  }

  //
  private ATreeNode handleKeySymbol(KeySymbol ks, ATreeNode tnode, DefinitionNode item) {
    DefinitionNode def = ks.getDefinition();
    ATreeNode next;
    boolean hasChildren = true;
    if (ks.isParseRef()) {
      next = new ParseRefTreeNode(ks.getName(), ks.getVarName(), ks);
      hasChildren = false;
    }
    else {
      next = createKeyNode(ks, def);
      if (next == null) {
        next = new KeyRefTreeNode(ks.getName(), ks);
        hasChildren = false;
      }
    }
    if (hasChildren) {
      createChildren(def, next);
    }
    Operator op = Helper.getInstance().toTreeNodeOperator(ks.getOperator());
    if (op != null) {
      OperatorTreeNode opnode = new OperatorTreeNode(op, item);
      opnode.add(next);
      next = opnode;
    }
    return next;
  }

  //
  private ATreeNode handleGroup(GroupOfSymbols gs, ATreeNode tnode, DefinitionNode item) {
    Operator op = Helper.getInstance().toTreeNodeOperator(gs.getOperator());
    if (op != null) {
      if (op == Operator.ALTERNATIVE && item.getType() == AttKeys.ATT_ENUM) {
        op = Operator.ENUM;
      }
      OperatorTreeNode next = new OperatorTreeNode(op, item);
      createChildren(gs, next);
      return next;
    }
    // ERROR
    return null;
  }

  //
  private ATreeNode handlePredefined(PredefinedSymbol sym, ATreeNode tnode, DefinitionNode item) {
    Operator op = Helper.getInstance().toTreeNodeOperator(sym.getOperator());
    ATreeNode next = new PredefinedTreeNode(sym.getName(), sym.getType().name(), sym);
    if (op != null) {
      ATreeNode opnode = new OperatorTreeNode(op, item);
      opnode.add(next);
      next = opnode;
    }
    return next;
  }

  //
  private ATreeNode handleString(StringSymbol sym, ATreeNode tnode, DefinitionNode item) {
    Operator op = Helper.getInstance().toTreeNodeOperator(sym.getOperator());
    ATreeNode next = new StringTreeNode(sym.getName(), sym);
    if (op != null) {
      ATreeNode opnode = new OperatorTreeNode(op, item);
      opnode.add(next);
      next = opnode;
    }
    return next;
  }

  //
  private ATreeNode handleChar(CharSymbol sym, ATreeNode tnode, DefinitionNode item) {
    Operator op = Helper.getInstance().toTreeNodeOperator(sym.getOperator());
    ATreeNode next = new CharTreeNode(sym.getName(), sym);
    if (op != null) {
      ATreeNode opnode = new OperatorTreeNode(op, item);
      opnode.add(next);
      next = opnode;
    }
    return next;
  }

  public String getSource(boolean isRaw) {
    StringBuilder sb = new StringBuilder(getHeader(isRaw));
    sb.append(isRaw ? "\n{\n" : "<p>{<br>");
    Set<KeywordTreeNode> sortedKeys = getSortedKeys(keywords);
    for (KeywordTreeNode node : sortedKeys) {
      if (GeneralContext.getInstance().isShowDelta()) {
        if (!node.getItem().isSerialized()) {
          sb.append(isRaw ? "\t" : "&emsp;");
          sb.append(node.getSourceDefinition(isRaw));
          sb.append(isRaw ? "\n" : "<br>");
        }
        else {
          ASymbol sym = node.getItem();
          if (sym instanceof KeySymbol) {
            AttKeys type = ((KeySymbol)sym).getDefinition().getType();
            if ((type == AttKeys.ATT_INTERFACE || type == AttKeys.ATT_ENUM)
                && node.hasNewElements()) {
              sb.append(isRaw ? "\n\t+" : "<br>&emsp;+");
              sb.append(node.getDelta(isRaw));
              sb.append(isRaw ? "\n" : "<br>");
            }
          }
        }
      }
      else {
        sb.append(isRaw ? "\t" : "&emsp;");
        sb.append(node.getSourceDefinition(isRaw));
        sb.append(isRaw ? "\n" : "<br>");
      }
    }
    sb.append(isRaw ? "}\n" : "}</p>");
    return sb.toString();
  }

  //
  private Set<KeywordTreeNode> getSortedKeys(Hashtable<String, KeywordTreeNode> keywords) {
    TreeSet<KeywordTreeNode> sortedKeys = new TreeSet<KeywordTreeNode>(new Comparator<KeywordTreeNode>() {
      @Override
      public int compare(KeywordTreeNode kw1, KeywordTreeNode kw2) {
        String[] sp1 = kw1.getPosition().split(";");
        String[] sp2 = kw2.getPosition().split(";");
        if (sp1.length == sp2.length) {
          for (int i=0; i<sp1.length; i++) {
            int i1 = Integer.parseInt(sp1[i]);
            int i2 = Integer.parseInt(sp2[i]);
            if (i1 != i2) {
              return i1 - i2;
            }
          }
        }
        return sp1.length - sp2.length;
      }
    });
    sortedKeys.addAll(keywords.values());
    return sortedKeys;
  }

  //
  private StringBuilder getHeader(boolean isRaw) {
    StringBuilder sb = new StringBuilder();
    sb.append("destination" + (isRaw ? "    \"" : "&emsp;\"") + headerInfo.getDestination() + (isRaw ? "\"\n" : "\"<br>"));
    if (headerInfo.isDebug()) {
      sb.append("debug" + (isRaw ? "\n" : "<br>"));
    }
    if (headerInfo.isVerbose()) {
      sb.append("verbosity" + (isRaw ? "    1" : "&emsp;1")  + (isRaw ? "\n" : "<br>"));
    }
    return sb;
  }
}

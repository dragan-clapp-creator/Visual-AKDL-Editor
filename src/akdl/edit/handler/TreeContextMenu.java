package akdl.edit.handler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import akdl.edit.dialog.NodeFindDialog;
import akdl.edit.dialog.SourceDialog;
import akdl.edit.tree.node.ATreeNode;
import akdl.edit.tree.node.CharTreeNode;
import akdl.edit.tree.node.EnumElementTreeNode;
import akdl.edit.tree.node.KeywordTreeNode;
import akdl.edit.tree.node.OmittedKeyTreeNode;
import akdl.edit.tree.node.OperatorTreeNode;
import akdl.edit.tree.node.OperatorTreeNode.Operator;
import akdl.edit.tree.node.PredefinedTreeNode;
import akdl.edit.tree.node.StringTreeNode;
import akdl.edit.tree.node.util.Helper;
import akdl.edit.util.TreeNodeProcessor;
import akdl.eval.EvaluationInfo.EvalClass;
import akdl.graph.nodes.CharSymbol;
import akdl.graph.nodes.DefinitionNode;
import akdl.graph.nodes.EnumSymbol;
import akdl.graph.nodes.KeySymbol;
import akdl.graph.nodes.PredefinedSymbol;
import akdl.graph.nodes.StringSymbol;
import akdl.sheet.keys.LeafType;

public class TreeContextMenu extends JPopupMenu {

  private static final long serialVersionUID = 4380597140182866078L;

  private static List<ATreeNode> cutOrCopy = new ArrayList<>();
  private static boolean isCut;

  enum Action {
    MOVE_UP, MOVE_DOWN,
    MOVE_DOWN_IN, MOVE_UP_OUT, MOVE_DOWN_OUT,
    INSERT, REMOVE, WRAP, REPLACE,
    EVAL,
    COPY, CUT, PASTE,
    FIND, FIND_IGNORED, SOURCE;
  }

  enum SubAction {
    CHAR, STRING, ENUM_ELEMENT, OPERATION, KEY, PREDEFINED;
  }

  private ATreeNode tnode;
  private List<ATreeNode> selection;

  private JTree tree;

  private GeneralContext context;

  private TreeNodeProcessor processor;

  private NodeFindDialog findDialog;

  /**
   * constructor used for single selection
   * 
   * @param path
   * @param node
   * @param context
   * @param t
   */
  public TreeContextMenu(TreePath path, ATreeNode tnode, GeneralContext context, JTree t) {
    this.tnode = tnode;
    this.selection = new ArrayList<>();
    this.context = context;
    this.tree = t;
    selection.add(tnode);
    processor = new TreeNodeProcessor();
    findDialog = new NodeFindDialog(tnode, context);
    boolean isSerialized = tnode.getItem().isSerialized();

    boolean isParentOperator = false;
    int n = path.getPathCount();
    if (n > 1) {
      ATreeNode tparent = (ATreeNode) path.getPathComponent(n - 2);
      isParentOperator = (tparent instanceof OperatorTreeNode);
    }
    JMenu menu;
    if (!isSerialized && tnode instanceof KeywordTreeNode) {
      if (tnode instanceof KeywordTreeNode && tnode.getChildCount() == 0) {
        createAndAddItem("turn to ignored", Action.REPLACE, true);
        add(new Separator());
      }
      menu = addChild(null);

      if (tnode.getParent() != null) {
        menu = (JMenu) createAndAddItem("wrap with", Action.WRAP, false);
        menu.add(createAndAddSubItem("keyword", Action.WRAP, SubAction.KEY));
        if (!isParentOperator) {
          JMenu submenu = (JMenu) createAndAddSubItem("operator", Action.WRAP, SubAction.OPERATION);
          menu.add(submenu);
          addOperators(submenu, Action.WRAP, null, true);
        }
      }
    }
    else if (tnode instanceof OperatorTreeNode) {
      if (((OperatorTreeNode) tnode).isEnum()) {
        menu = (JMenu) createAndAddItem("add child", Action.INSERT, false);
        menu.add(createAndAddSubItem("enum element", Action.INSERT, SubAction.ENUM_ELEMENT));
      }
      else {
        menu = addChild((OperatorTreeNode) tnode);
      }
      menu = (JMenu) createAndAddItem("wrap with", Action.WRAP, false);
      menu.add(createAndAddSubItem("keyword", Action.WRAP, SubAction.KEY));
    }
    else if (tnode instanceof EnumElementTreeNode) {
      menu = (JMenu) createAndAddItem("wrap with", Action.WRAP, false);
      menu.add(createAndAddOperation("enum", Action.WRAP, Operator.ENUM));
    }
    else if (!isSerialized && tnode instanceof OmittedKeyTreeNode) {
      createAndAddItem("turn to keyword", Action.REPLACE, true);
    }
    else if (!isSerialized) {
      menu = (JMenu) createAndAddItem("wrap with", Action.WRAP, false);
      menu.add(createAndAddSubItem("keyword", Action.WRAP, SubAction.KEY));
      JMenu submenu = (JMenu) createAndAddSubItem("operator", Action.WRAP, SubAction.OPERATION);
      menu.add(submenu);
      addOperators(submenu, Action.WRAP, null, true);
    }
    if (!isSerialized) {
      addMoveAndRemove();
    }
    else {
      add(new Separator());
    }
    if (!isSerialized && tnode instanceof KeywordTreeNode) {
      if (((KeywordTreeNode)tnode).getRegisteredEvaluation() == null) {
        menu = (JMenu) createAndAddItem("assign evaluator...", Action.EVAL, false);
        for (EvalClass eclass : EvalClass.values()) {
          menu.add(createAndAddSubItem(eclass.name(), Action.EVAL, SubAction.KEY));
        }
        add(new Separator());
      }
    }
    createAndAddItemForCutPaste("copy", Action.COPY, cutOrCopy.isEmpty());
    createAndAddItemForCutPaste("cut", Action.CUT, cutOrCopy.isEmpty());
    if (!isSerialized) {
      createAndAddItemForCutPaste("paste", Action.PASTE, !cutOrCopy.isEmpty());
    }
    add(new Separator());
    createAndAddItem("find ...", Action.FIND, true);
    createAndAddItem("find next ignored...", Action.FIND_IGNORED, true);
    if (tnode instanceof KeywordTreeNode) {
      add(new Separator());
      createAndAddItem("source ...", Action.SOURCE, true);
    }
    pack();
  }

  //
  private void addMoveAndRemove() {
    boolean hasNextkeyOrOp = hasNextKeywordOrOperator(tnode);
    boolean hasGrandParent = hasParentHavingParent(tnode);
    if (hasNextkeyOrOp || hasGrandParent) {
      add(new Separator());
      if (hasNextkeyOrOp) {
        createAndAddItem("move within next", Action.MOVE_DOWN_IN, true);
      }
      if (hasGrandParent) {
        createAndAddItem("move up out", Action.MOVE_UP_OUT, true);
        createAndAddItem("move down out", Action.MOVE_DOWN_OUT, true);
      }
    }
    boolean hasNext = hasNext(tnode);
    boolean hasPrevious = hasPrevious(tnode);
    if (hasNext || hasPrevious) {
      add(new Separator());
      if (hasNext) {
        createAndAddItem("move down", Action.MOVE_DOWN, true);
      }
      if (hasPrevious) {
        createAndAddItem("move up", Action.MOVE_UP, true);
      }
    }
    add(new Separator());
    if (tnode.getChildCount() == 0) {
      createAndAddItem("remove", Action.REMOVE, true);
    }
  }

  //
  private JMenu addChild(OperatorTreeNode node) {
    JMenu menu = (JMenu) createAndAddItem("add child", Action.INSERT, false);
    menu.add(createAndAddSubItem("keyword", Action.INSERT, SubAction.KEY));
    JMenu submenu = (JMenu) createAndAddSubItem("predefined", Action.INSERT, SubAction.PREDEFINED);
      menu.add(submenu);
      addPredefineds(submenu);
    submenu = (JMenu) createAndAddSubItem("operator", Action.INSERT, SubAction.OPERATION);
      menu.add(submenu);
      addOperators(submenu, Action.INSERT, node, false);
    menu.add(createAndAddSubItem("constant word", Action.INSERT, SubAction.STRING));
    menu.add(createAndAddSubItem("char symbol", Action.INSERT, SubAction.CHAR));
    return menu;
  }

  //
  private void addPredefineds(JMenu submenu) {
    for (LeafType t : LeafType.values()) {
      submenu.add(createAndAddPredefined(t));
    }
  }

  //
  private JMenuItem createAndAddPredefined(LeafType t) {
    JMenuItem item = new JMenuItem(t.name());
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        performInsertPredefined(t);
      }
    });
    return item;
  }

  //
  private void performInsertPredefined(LeafType t) {
    PredefinedSymbol ps = new PredefinedSymbol(t, t.name().toLowerCase(), false, akdl.graph.nodes.elts.Operator.NONE);
    PredefinedTreeNode node = new PredefinedTreeNode(ps.getName(), ps.getType().name(), ps);
    tnode.add(node);
    tnode.addChildSymbol(ps);
    context.refreshAll(node);
  }

  //
  private void addOperators(JMenu submenu, Action act, OperatorTreeNode node, boolean isWrap) {
    if (isWrap) {
      if (node == null && areAllChildNodesSelected()) {
        submenu.add(createAndAddOperation("alternative", act, Operator.ALTERNATIVE));
      }
    }
    else {
      if (node == null && tnode.getChildCount() == 0) {
        submenu.add(createAndAddOperation("enum", act, Operator.ENUM));
        submenu.add(createAndAddOperation("alternative", act, Operator.ALTERNATIVE));
      }
    }
    if (node == null || !node.isAnyOrder()) {
      submenu.add(createAndAddOperation("any order", act, Operator.ANY_ORDER));
    }
    if (node == null || !node.isOptional()) {
      submenu.add(createAndAddOperation("option", act, Operator.OPTIONAL));
    }
    if (node == null || !node.isMany()) {
      submenu.add(createAndAddOperation("zero or many", act, Operator.ZERO_OR_MANY));
    }
  }

  //
  private boolean areAllChildNodesSelected() {
    ATreeNode parent = (ATreeNode) tnode.getParent();
    if (parent != null && parent.getChildCount() == selection.size()) {
      for (ATreeNode tn : selection) {
        if (parent.getIndex(tn) < 0) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  //
  private boolean hasNextKeywordOrOperator(ATreeNode tnode) {
    if (tnode.getParent() != null) {
      TreeNode parent = tnode.getParent();
      int i = parent.getIndex(tnode);
      if (i < parent.getChildCount() - 1) {
        ATreeNode next = (ATreeNode) parent.getChildAt(i + 1);
        return (next instanceof KeywordTreeNode || next instanceof OperatorTreeNode);
      }
    }
    return false;
  }

  private boolean hasParentHavingParent(ATreeNode tnode) {
    if (tnode.getParent() != null) {
      TreeNode parent = tnode.getParent();
      return (parent.getParent() != null);
    }
    return false;
  }

  private boolean hasNext(ATreeNode tnode) {
    if (tnode.getParent() != null) {
      TreeNode parent = tnode.getParent();
      return (parent.getIndex(tnode) < parent.getChildCount() - 1);
    }
    return false;
  }

  private boolean hasPrevious(ATreeNode tnode) {
    if (tnode.getParent() != null) {
      TreeNode parent = tnode.getParent();
      return (parent.getIndex(tnode) > 0);
    }
    return false;
  }

  /**
   * constructor used for multiple selection
   * 
   * @param paths
   * @param context
   */
  public TreeContextMenu(TreePath[] paths, GeneralContext context) {
    boolean isParentOperator = false;
    this.context = context;
    TreePath path = paths[0];
    ATreeNode tnode = (ATreeNode) path.getLastPathComponent();
    int n = path.getPathCount();
    if (n > 1) {
      ATreeNode tparent = (ATreeNode) path.getPathComponent(n - 2);
      isParentOperator = (tparent instanceof OperatorTreeNode);
      if (tparent instanceof OperatorTreeNode && (tparent.getChildCount() == paths.length)) {
        return;
      }
    }

    processor = new TreeNodeProcessor();
    this.tnode = tnode;
    this.selection = new ArrayList<>();
    for (int i = 0; i < paths.length; i++) {
      path = paths[i];
      ATreeNode node = (ATreeNode) path.getLastPathComponent();
      selection.add(node);
    }
    if (!isSerialized(selection)) {
      addMoveAndRemove();
      add(new Separator());
    }
    createAndAddItemForCutPaste("copy", Action.COPY, cutOrCopy.isEmpty());
    createAndAddItemForCutPaste("cut", Action.CUT, cutOrCopy.isEmpty());
    add(new Separator());

    if (isWrapAllowed(selection)) {
      JMenu menu = (JMenu) createAndAddItem("wrap with", Action.WRAP, false);
      menu.add(createAndAddSubItem("keyword", Action.WRAP, SubAction.KEY));
      if (!isParentOperator) {
        JMenu submenu = (JMenu) createAndAddSubItem("operator", Action.WRAP, SubAction.OPERATION);
        menu.add(submenu);
        addOperators(submenu, Action.WRAP, null, true);
      }
    }
  }

  //
  private boolean isWrapAllowed(List<ATreeNode> sel) {
    for (ATreeNode node : sel) {
      if (node.getParent() == null) {
        return false;
      }
    }
    return true;
  }

  //
  private boolean isSerialized(List<ATreeNode> sel) {
    for (ATreeNode node : sel) {
      if (node.getItem().isSerialized()) {
        return true;
      }
    }
    return false;
  }

  //
  private JComponent createAndAddItem(String name, Action act, boolean isItem) {
    if (isItem) {
      JMenuItem item = new JMenuItem(name);
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (performAction(act, null, null, e.getActionCommand())) {
            context.setDirty(true);
          }
        }
      });
      add(item);
      return item;
    }
    JMenu menu = new JMenu(name);
    menu.setName(act.name());
    add(menu);
    return menu;
  }

  //
  private JComponent createAndAddItemForCutPaste(String name, Action act, boolean isEnabled) {
    JMenuItem item = new JMenuItem(name);
    item.setEnabled(isEnabled);
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (performAction(act, null, null, e.getActionCommand())) {
          context.setDirty(true);
        }
      }
    });
    add(item);
    return item;
  }

  //
  private JComponent createAndAddSubItem(String name, Action act, SubAction sub) {
    if (sub != SubAction.OPERATION && sub != SubAction.PREDEFINED) {
      JMenuItem item = new JMenuItem(name);
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (performAction(act, sub, null, e.getActionCommand())) {
            context.setDirty(true);
          }
        }
      });
      return item;
    }
    JMenu submenu = new JMenu(name);
    return submenu;
  }

  //
  private JMenuItem createAndAddOperation(String name, Action act, Operator op) {
    JMenuItem item = new JMenuItem(name);
    item.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (performAction(act, SubAction.OPERATION, op, e.getActionCommand())) {
          context.setDirty(true);
        }
      }
    });
    return item;
  }

  //
  private boolean performAction(Action action, SubAction sub, Operator op, String cmd) {
    switch (action) {
      case INSERT:
        performInsert(sub, op);
        break;
      case REMOVE:
        handleRemove();
        break;
      case REPLACE:
        processor.performReplace(tnode, context);
        break;
      case WRAP:
        if (op == null) {
          processor.performWrapWithKey(selection, context);
        } else {
          processor.performWrapWithOperator(op, selection, context);
        }
        break;
      case MOVE_DOWN:
        handleMoveDown();
        break;
      case MOVE_DOWN_IN:
        handleMoveDownIn();
        break;
      case MOVE_DOWN_OUT:
        handleMoveDownOut();
        break;
      case MOVE_UP:
        handleMoveUp();
        break;
      case MOVE_UP_OUT:
        handleMoveUpOut();
        break;
      case CUT:
        cutOrCopy.addAll(selection);
        isCut = true;
        break;
      case COPY:
        cutOrCopy.addAll(selection);
        isCut = false;
        break;
      case PASTE:
        handleCutOrCopy();
        cutOrCopy.clear();
        break;
      case FIND_IGNORED:
        findNextIgnored();
        return false;
      case FIND:
        findDialog.setVisible(true);
        return false;
      case SOURCE:
        SourceDialog sd = new SourceDialog((KeywordTreeNode) tnode, tree, context);
        sd.setVisible(true);
        return false;
      case EVAL:
        EvalClass eclass = EvalClass.valueOf(cmd);
        tnode.propagate(eclass);
        context.refreshAll(tnode);
        break;

      default:
        break;
    }
    return true;
  }

  //
  private void findNextIgnored() {
    if (tnode instanceof KeywordTreeNode) {
      OmittedKeyTreeNode ignored = findIgnored((KeywordTreeNode) tnode);
      if (ignored != null) {
        context.refreshAll(ignored);
        return;
      }
    }
    ATreeNode parent = (ATreeNode) tnode.getParent();
    if (parent != null) {
      boolean isStart = false;
      for (int i=0; i<parent.getChildCount(); i++) {
        ATreeNode tn = (ATreeNode) parent.getChildAt(i);
        if (!isStart) {
          if (tn == tnode) {
            isStart = true;
          }
          continue;
        }
        if (isStart) {
          if (tn instanceof KeywordTreeNode) {
            OmittedKeyTreeNode ignored = findIgnored((KeywordTreeNode) tn);
            if (ignored != null) {
              context.refreshAll(ignored);
              return;
            }
          }
          else if (tn instanceof OmittedKeyTreeNode) {
            context.refreshAll(tn);
            return;
          }
        }
      }
      tnode = parent;
      findNextIgnored();
    }
  }

  //
  private OmittedKeyTreeNode findIgnored(ATreeNode node) {
    for (int i=0; i<node.getChildCount(); i++) {
      ATreeNode tn = (ATreeNode) node.getChildAt(i);
      if (tn instanceof OmittedKeyTreeNode) {
        return (OmittedKeyTreeNode) tn;
      }
      if (tn instanceof KeywordTreeNode) {
        OmittedKeyTreeNode ignored = findIgnored((KeywordTreeNode) tn);
        if (ignored != null) {
          return ignored;
        }
      }
      if (tn instanceof OperatorTreeNode) {
        OmittedKeyTreeNode ignored = findIgnored(tn);
        if (ignored != null) {
          return ignored;
        }
      }
    }
    return null;
  }

  //
  private void handleMoveUp() {
    ATreeNode parent = (ATreeNode) tnode.getParent();
    if (parent != null) {
      int i = parent.getIndex(tnode);
      parent.remove(i);
      parent.removeChildSymbol(tnode.getItem());
      parent.insert(tnode, i - 1);
      parent.addChildSymbol(tnode.getItem(), i-1);
      context.refreshAll(tnode);
    }
  }

  //
  private void handleMoveUpOut() {
    ATreeNode parent = (ATreeNode) tnode.getParent();
    if (parent != null) {
      ATreeNode granpa = (ATreeNode) parent.getParent();
      if (granpa != null) {
        int i = granpa.getIndex(parent);
        parent.remove(tnode);
        parent.removeChildSymbol(tnode.getItem());
        granpa.insert(tnode, i);
        granpa.addChildSymbol(tnode.getItem(), i);
      }
      context.refreshAll(tnode);
    }
  }

  //
  private void handleMoveDown() {
    ATreeNode parent = (ATreeNode) tnode.getParent();
    if (parent != null) {
      int i = parent.getIndex(tnode);
      parent.remove(i);
      parent.removeChildSymbol(tnode.getItem());
      parent.insert(tnode, i + 1);
      parent.addChildSymbol(tnode.getItem(), i+1);
      context.refreshAll(tnode);
    }
  }

  //
  private void handleMoveDownIn() {
    ATreeNode parent = (ATreeNode) tnode.getParent();
    if (parent != null) {
      int i = parent.getIndex(tnode);
      ATreeNode newParent = (ATreeNode) parent.getChildAt(i + 1);
      parent.remove(i);
      parent.removeChildSymbol(tnode.getItem());
      newParent.insert(tnode, 0);
      newParent.addChildSymbol(tnode.getItem(), 0);
      context.refreshAll(tnode);
    }
  }

  //
  private void handleMoveDownOut() {
    ATreeNode parent = (ATreeNode) tnode.getParent();
    if (parent != null) {
      ATreeNode granpa = (ATreeNode) parent.getParent();
      if (granpa != null) {
        int i = granpa.getIndex(parent);
        parent.remove(tnode);
        parent.removeChildSymbol(tnode.getItem());
        granpa.insert(tnode, i + 1);
        granpa.addChildSymbol(tnode.getItem(), i+1);
      }
      context.refreshAll(tnode);
    }
  }

  //
  private void handleRemove() {
    ATreeNode parent = (ATreeNode) selection.get(0).getParent();
    if (parent != null) {
      for (int i = selection.size() - 1; i >= 0; i--) {
        ATreeNode node = selection.get(i);
        parent.remove(node);
        parent.removeChildSymbol(tnode.getItem());
      }
      if (tnode instanceof KeywordTreeNode) {
        context.getTreeHandler().removeNode(tnode.getName());
        context.removeInTreeMouseListener(tnode.getName());
      }
      context.refreshAll(parent);
    }
  }

  //
  private void handleCutOrCopy() {
    ATreeNode currentParent = tnode;
    ATreeNode previousParent = isCut ? (ATreeNode) cutOrCopy.get(0).getParent() : null;
    if (!isCut || previousParent != null && currentParent != previousParent) {
      for (ATreeNode node : cutOrCopy) {
        if (isCut) {
          previousParent.remove(node);
          previousParent.removeChildSymbol(node.getItem());
          currentParent.add(node);
          currentParent.addChildSymbol(node.getItem());
        }
        else {
          ATreeNode copy = node.cloneWithSuffix();
          currentParent.add(copy);
          currentParent.addChildSymbol(copy.getItem());
        }
        node.setParent(currentParent);
      }
      context.refreshAll(currentParent);
    }
  }

  //
  private void performInsert(SubAction sub, Operator op) {
    ATreeNode node = null;
    akdl.graph.nodes.elts.Operator akdlOp = Helper.getInstance().toAKDLOperator(op);
    switch (sub) {
      case CHAR:
        node = new CharTreeNode("?", new CharSymbol('?', akdlOp));
        break;
      case ENUM_ELEMENT:
        node = new EnumElementTreeNode("?",  new EnumSymbol("?", akdlOp), "?");
        break;
      case KEY:
        KeySymbol ks = new KeySymbol("?", akdlOp, false);
        DefinitionNode def = new DefinitionNode("?");
        node = context.getTreeHandler().createKeyNode(ks, def);
        break;
      case OPERATION:
        node = performOperation(op);
        break;
      case STRING:
        node = new StringTreeNode("?", new StringSymbol("?", akdlOp));
        break;

      default:
        break;
    }
    if (node != null) {
      tnode.add(node);
      tnode.addChildSymbol(node.getItem());
      context.refreshAll(node);
    }
  }

  //
  private ATreeNode performOperation(Operator op) {
    ATreeNode node = null;
    switch (op) {
      case ANY_ORDER:
      case ENUM:
      case OPTIONAL:
      case ZERO_OR_MANY:
      case ALTERNATIVE:
        StringSymbol item = new StringSymbol(op.name(), akdl.graph.nodes.elts.Operator.NONE);
        node = new OperatorTreeNode(op, item);
        break;

      default:
        break;
    }
    return node;
  }
}

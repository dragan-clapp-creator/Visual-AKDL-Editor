package akdl.edit.handler;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.tree.node.ATreeNode;
import akdl.edit.tree.node.EnumElementTreeNode;
import akdl.edit.tree.node.KeyRefTreeNode;
import akdl.edit.tree.node.KeywordTreeNode;
import akdl.edit.tree.node.OperatorTreeNode;
import akdl.edit.tree.node.ParseRefTreeNode;

public class TreeMouseListener extends MouseAdapter {

  private static Hashtable<String, TreeNode> nodes = new Hashtable<>();

  private JTree jtree;

  private TreeContextMenu cmenu;

  private GeneralContext context;


  public TreeMouseListener(JTree tree, GeneralContext context) {
    this.jtree = tree;
    this.context = context;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    Point p = e.getPoint();
    TreePath path = jtree.getClosestPathForLocation(p.x, p.y);
    ATreeNode tnode = (ATreeNode) path.getLastPathComponent();
    if (jtree.getSelectionCount() == 1) {
      if (tnode != null && path.equals(jtree.getSelectionPath())) {
        PropertiesPanel propertiesPanel = context.getPropertiesPanel();
        ControlPanel controlPanel = context.getControlPanel();
        if (SwingUtilities.isRightMouseButton(e)) {
          cmenu = new TreeContextMenu(path, tnode, context, jtree);
          cmenu.show(e.getComponent(), e.getX()+30, e.getY());
       }
        else if (e.getClickCount() == 1){
          tnode.populate(propertiesPanel, controlPanel);
        }
        else if (e.getClickCount() == 2 && (tnode instanceof KeyRefTreeNode || tnode instanceof ParseRefTreeNode)) {
          String name = tnode.getName();
          TreePath paths;
          if (tnode instanceof KeyRefTreeNode) {
            paths = getPath(getNodePath(name));
          }
          else {
            paths = findPath(name, (ParseRefTreeNode) tnode);
          }
          jtree.setSelectionPath(paths);
          path = jtree.getSelectionPath();
          if (path != null) {
            tnode = (ATreeNode) path.getLastPathComponent();
            if (tnode != null) {
              tnode.populate(propertiesPanel, controlPanel);
            }
          }
        }
        context.refresh(true);
      }
    }
    else if (jtree.getSelectionCount() > 1 && SwingUtilities.isRightMouseButton(e)) {
      TreePath[] paths = jtree.getSelectionPaths();
      cmenu = new TreeContextMenu(paths, context);
      cmenu.show(e.getComponent(), e.getX()+30, e.getY());
    }
  }

  //
  private TreePath findPath(String name, ParseRefTreeNode tnode) {
    for (int i=0; i<context.getRootNode().getChildCount(); i++) {
      TreeNode tn = context.getRootNode().getChildAt(i);
      if (tn instanceof OperatorTreeNode) {
        OperatorTreeNode op = (OperatorTreeNode) tn;
        if (op.isDetached()) {
          KeywordTreeNode kn = (KeywordTreeNode) op.getChildAt(0);
          if (kn.getName().equals(name)) {
            int n = kn.getChildAt(0).getChildCount();
            for (int j=0; j<n; j++) {
              EnumElementTreeNode en = (EnumElementTreeNode)kn.getChildAt(0).getChildAt(j);
              if (tnode.getIdentifier().equals(en.getName())) {
                TreeNode[] tnodes = en.getPath();
                TreePath paths = new TreePath(tnodes);
                return paths;
              }
            }
          }
        }
      }
    }
    return null;
  }

  public static void check(String name, ATreeNode node) {
    if (!nodes.containsKey(name) || node instanceof KeywordTreeNode) {
      TreeNode[] tnodes = node.getPath();
      nodes.put(name, tnodes[tnodes.length-1]);
    }
  }

  public TreeNode getNodePath(String n) {
    return nodes.get(n);
  }
 
  public static TreePath getPath(TreeNode treeNode) {
    List<Object> nodes = new ArrayList<Object>();
    if (treeNode != null) {
      nodes.add(treeNode);
      treeNode = treeNode.getParent();
      while (treeNode != null) {
        nodes.add(0, treeNode);
        treeNode = treeNode.getParent();
      }
    }

    return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
  }

  /**
   * 
   * @param oldname
   * @param newname
   */
  public void renameNode(String oldname, String newname) {
    TreeNode node = nodes.remove(oldname);
    if (node != null) {
      nodes.put(newname, node);
    }
  }

  public void removeNode(String name) {
    nodes.remove(name);
  }
}

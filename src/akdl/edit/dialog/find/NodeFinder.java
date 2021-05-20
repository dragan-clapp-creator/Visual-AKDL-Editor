package akdl.edit.dialog.find;

import java.util.ArrayList;

import akdl.edit.tree.node.ATreeNode;
import akdl.edit.tree.node.KeywordTreeNode;

public class NodeFinder {

  private String text;
  private boolean isCaseSensistive;
  private boolean isWholeWord;
  private ATreeNode selectedNode;
  private KeywordTreeNode rootNode;

  private ArrayList<ATreeNode> list;
  private int index;

  /**
   * constructor
   * 
   * @param text
   * @param isCaseSensistive
   * @param isWholeWord
   * @param selectedNode 
   * @param rootNode 
   */
  public NodeFinder(String text, boolean isCaseSensistive, boolean isWholeWord,
      ATreeNode selectedNode, KeywordTreeNode rootNode) {
    this.text = isCaseSensistive ? text : text.toLowerCase();
    this.isCaseSensistive = isCaseSensistive;
    this.isWholeWord = isWholeWord;
    this.selectedNode = selectedNode;
    this.rootNode = rootNode;
    findAll();
  }

  //
  private void findAll() {
    list = new ArrayList<>();
    index = -1;
    fillList(rootNode);
    index = 0;
  }

  //
  private void fillList(ATreeNode node) {
    if (node == selectedNode) {
      index = 0;
    }
    else if (match(node.getName())) {
      if (index < 0) {
        list.add(node);
      }
      else {
        list.add(index, node);
        index++;
      }
    }
    for (int i=0; i<node.getChildCount(); i++) {
      fillList((ATreeNode) node.getChildAt(i));
    }
  }

  //
  private boolean match(String name) {
    if (!isCaseSensistive) {
      name = name.toLowerCase();
    }
    if (isWholeWord) {
      return name.equals(text);
    }
    return name.contains(text);
  }

  public ATreeNode findNext() {
    if (!list.isEmpty()) {
      if (index >= list.size()) {
        index = 0;
      }
      return list.get(index++);
    }
    return null;
  }

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @return the isCaseSensistive
   */
  public boolean isCaseSensistive() {
    return isCaseSensistive;
  }

  /**
   * @return the isWholeWord
   */
  public boolean isWholeWord() {
    return isWholeWord;
  }
}

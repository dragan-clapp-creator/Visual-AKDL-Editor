package akdl.edit.handler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import akdl.edit.dialog.TextFindDialog;
import akdl.edit.dialog.help.Help;
import akdl.edit.panel.ControlPanel;
import akdl.edit.panel.PropertiesPanel;
import akdl.edit.panel.TreePanel;
import akdl.edit.tree.node.ATreeNode;
import akdl.edit.tree.node.CharTreeNode;
import akdl.edit.tree.node.KeyRefTreeNode;
import akdl.edit.tree.node.KeywordTreeNode;
import akdl.edit.tree.node.OmittedKeyTreeNode;
import akdl.edit.tree.node.OperatorTreeNode;
import akdl.edit.tree.node.OperatorTreeNode.Operator;
import akdl.edit.tree.node.ParseRefTreeNode;
import akdl.edit.tree.node.StringTreeNode;
import akdl.edit.tree.node.util.TreeCellRenderer;
import akdl.edit.util.ColorSet;
import akdl.edit.util.CommonScrollPanel;
import akdl.edit.util.TreeNodeProcessor;
import akdl.graph.nodes.DefinitionNode;
import akdl.graph.nodes.KeySymbol;
import akdl.sheet.DefinitionSheet;
import akdl.sheet.DefinitionSheetMaker;
import akdl.sheet.keys.AttKeys;
import akdl.sheet.parse.ParseResult;

public class GeneralContext {

  static private GeneralContext context;

  static public GeneralContext getInstance() {
    return context;
  }

  private PropertiesPanel propertiesPanel;
  private ControlPanel controlPanel;
  private TreePanel treePanel;

  private JTree mainTree;
  private KeywordTreeNode rootNode;
  public TreeHandler treeHandler;

  public DefinitionNode defRoot;
  private JTabbedPane tabbedPane;

  private JFrame frame;
  private JScrollPane pane;

  private DefinitionSheet definitionSheet;

  private List<KeywordTreeNode> undefined;
  private List<OmittedKeyTreeNode> omitted;

  private boolean isDirty;
  private TreeMouseListener treeMouseListener;

  private TextFindDialog findDialog;

  private boolean isShowDelta;
  private boolean isShowRawSource;


  /**
   * constructor
   * 
   * @param frame
   * @param pane
   */
  public GeneralContext(JFrame frame, JScrollPane pane) {
    this.frame = frame;
    this.pane = pane;
    undefined = new ArrayList<>();
    omitted = new ArrayList<>();
    context = this;
  }

  /**
   * create initial panels
   * 
   * @param rect
   * @param frame
   * @param pane 
   */
  public void createPanels() {
    pane.removeAll();
    tabbedPane = new JTabbedPane();
    tabbedPane.setSize(pane.getWidth(), pane.getHeight());
    UIManager.put("TabbedPane.highlight", Color.BLUE);

    Rectangle rect = tabbedPane.getBounds();
    int controllerHeight = 400;
    String ld = definitionSheet.getHeaderInfo().getLoadDefinitionsFrom();
    if (ld != null && !ld.isEmpty()) {
      controllerHeight += 100;
    }
    propertiesPanel = new PropertiesPanel(rect.height-controllerHeight);
    treePanel = new TreePanel();
    controlPanel = new ControlPanel(controllerHeight, this);
    JSplitPane akdlPanel = createSplitPane();
    tabbedPane.add("AKDL Structure Area", akdlPanel);

    pane.add(tabbedPane);
    JScrollPane sourceScrollPane = new CommonScrollPanel(tabbedPane.getWidth(), tabbedPane.getHeight());
    sourceScrollPane.getViewport().add(new JPanel());
    tabbedPane.add("Source", sourceScrollPane);
    tabbedPane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        JTabbedPane tp = (JTabbedPane)e.getSource();
        if (tp.getSelectedIndex() == 1) {
          JScrollPane sp = (JScrollPane) tp.getComponentAt(1);
          for (Component cmp : sp.getComponents()) {
            if (cmp instanceof JViewport) {
              JViewport vport = (JViewport)cmp;
              for (Component vcmp : vport.getComponents()) {
                if (vcmp instanceof JPanel) {
                  vport.remove(vcmp);
                  JPanel panel = new JPanel();
                  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                  JTextComponent srcfield;
                  if (isShowRawSource) {
                    srcfield = new JTextArea();
                    srcfield.setEditable(false);
                    srcfield.setText(createRawSourceFromNodes());
                    srcfield.setCaretPosition(0);
                    Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
                    srcfield.setFont(font);
                  }
                  else {
                    srcfield = new JEditorPane();
                    ((JEditorPane)srcfield).setContentType("text/html");
                    srcfield.setEditable(false);
                    srcfield.setText(createSourceFromNodes());
                    srcfield.setCaretPosition(0);
                  }
                  SwingUtilities.invokeLater( new Runnable() { 
                    public void run() { 
                      srcfield.requestFocus(); 
                    } 
                  } );
                  srcfield.addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                      if (e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK
                          && ((int)e.getKeyChar() + KeyEvent.VK_A - 1) == KeyEvent.VK_F) {
                        if (findDialog != null) {
                          findDialog.dispose();
                        }
                        findDialog = new TextFindDialog(srcfield, createRawSourceFromNodes(), getInstance());
                        findDialog.setVisible(true);
                      }
                    }
                    @Override
                    public void keyReleased(KeyEvent e) {
                    }
                    @Override
                    public void keyPressed(KeyEvent e) {
                      if (KeyEvent.getKeyText(e.getKeyCode()).equals("F3")) {
                        if (findDialog != null) {
                          findDialog.findNext();
                        }
                      }
                    }
                  });
                  panel.add(srcfield);
                  vport.add(panel); 
                  refresh(true);
                  break;
                }
              }
            }
          }
        }
      }
    });
  }

  public void setup(File selectedFile) throws IOException {
    if (selectedFile == null) {
      definitionSheet = new DefinitionSheet();
      defRoot = new DefinitionNode("ROOT");
      defRoot.setType(AttKeys.ATT_CLASS);
    }
    else {
      DefinitionSheetMaker sheetMaker = createSheetMaker(selectedFile);
      definitionSheet = sheetMaker.getDefinitionSheet();
      defRoot = sheetMaker.getGraph().getRoot();
    }
    treeHandler = new TreeHandler(definitionSheet.getHeaderInfo());
    createPanels();

    rootNode = treeHandler.createKeyNode(null, defRoot);
    mainTree = new JTree(new DefaultTreeModel(rootNode));
    generateGraph(mainTree, defRoot, rootNode);
    treePanel.addTree(this, mainTree);
  }

  /**
   * @return the defRoot
   */
  public DefinitionNode generateDefRoot() {
    String source = createRawSourceFromNodes();
    DefinitionSheetMaker sheetMaker;
    try {
      sheetMaker = createSheetMaker(source);
      definitionSheet = sheetMaker.getDefinitionSheet();
      defRoot = sheetMaker.getGraph().getRoot();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return defRoot;
  }

  //
  private DefinitionSheetMaker createSheetMaker(String source) throws IOException {
    DefinitionSheetMaker sheetMaker = new DefinitionSheetMaker(new StringReader(source), true);
    ParseResult result = ParseResult.getInstance();
    result.initialize();
    sheetMaker.createDefinitionSheet(result);
    return sheetMaker;
  }

  //
  private DefinitionSheetMaker createSheetMaker(File selectedFile) throws IOException {
    DefinitionSheetMaker sheetMaker = new DefinitionSheetMaker(new FileReader(selectedFile), true);
    ParseResult result = ParseResult.getInstance();
    result.initialize();
    sheetMaker.createDefinitionSheet(result);
    return sheetMaker;
  }

  //
  private JSplitPane createSplitPane() {
    JScrollPane scroll = new JScrollPane();
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.getViewport().add(treePanel, BorderLayout.LINE_START);
    scroll.getViewport().addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        refresh(true);
      }
    });

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, controlPanel, propertiesPanel);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, scroll);
    return splitPane;
  }

  public void reassign(ATreeNode node, boolean isKeyRef) {
//    SwingUtilities.invokeLater(new Runnable() {
//      public void run() {
//      }
//    });
    ATreeNode parent = (ATreeNode) node.getParent();
    if (parent != null) {
      int index = parent.getIndex(node);
      parent.remove(node);
      KeySymbol ks = (KeySymbol) node.getItem();
      String id = ks.getVarName() == null ? "?" : ks.getVarName();
      ATreeNode next = isKeyRef ?
                        new KeyRefTreeNode(ks.getName(), ks) :
                        new ParseRefTreeNode(ks.getName(), id, ks) ;
      parent.insert(next, index);
      if (!isKeyRef) {
        treeHandler.removeNode(ks.getName());
      }
      node.invalidate();
      refreshAll(next);
    }
  }

  public void refresh(boolean isControlRefresh) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (isControlRefresh) {
          controlPanel.redraw();
        }
        tabbedPane.updateUI();
      }
    });
  }

  public void refreshAll(ATreeNode node) {
    if (!node.isInvalide()) {
      DefaultTreeModel model = (DefaultTreeModel) mainTree.getModel();
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
      model.reload(root);
      if (node != null) {
        TreePath path =  TreeMouseListener.getPath(node);
        mainTree.setSelectionPath(path);
        mainTree.scrollPathToVisible(path);
        node.populate(propertiesPanel, controlPanel);
        if (!"?".equals(node.getName())) {
          TreeMouseListener.check(node.getName(), node);
        }
      }
      refresh(false);
    }
  }

  public void refreshAll() {
    refreshAll(rootNode);
  }

  public void createChildren(DefinitionNode item, KeywordTreeNode tnode) {
    treeHandler.createChildren(item, tnode);
  }

  public String createRawSourceFromNodes() {
    rootNode.setupPositions();
    return treeHandler.getSource(true);
  }

  public String createSourceFromNodes() {
    try {
      rootNode.setupPositions();
      StringBuilder sb = new StringBuilder( Help.WHOLE_SOURCE.getContent() );
      String src = treeHandler.getSource(false);
      int index = sb.indexOf("%SOURCE%");
      sb = sb.replace(index, index+8, src);
      return sb.toString();
    }
    catch(StringIndexOutOfBoundsException e) {
      return "SORRY: YOUR SOURCE IS TOO LONG TO BE DISPLAYED";
    }
  }

  public List<String> getErrors() {
    List<String> list = getUndefinedKeywordDefinitions();
    for (String key : treeHandler.getKeywords().keySet()) {
      KeywordTreeNode knode = treeHandler.getKeywords().get(key);
      String err = checkOperatorError(knode);
      if (err != null) {
        list.add(key+": "+err);
      }
      err = checkSameStartError(knode);
      if (err != null) {
        list.add(key+": "+err);
      }
      err = checkSameNameError(knode);
      if (err != null) {
        list.add(key+": "+err);
      }
    }
    return list;
  }

  //
  private String checkOperatorError(KeywordTreeNode knode) {
    for (int i=0; i<knode.getChildCount(); i++) {
      ATreeNode child = (ATreeNode) knode.getChildAt(i);
      if (child instanceof OperatorTreeNode && knode.getChildCount() > 1) {
        OperatorTreeNode op = (OperatorTreeNode) child;
        if (op.isEnum()) {
          return Operator.ENUM.name()+" not allowed here";
        }
        if (op.isAlternative() && !isException(knode, op)) {
          return Operator.ALTERNATIVE.name()+" not allowed here";
        }
        if (op.isOption()) {
          for (int j=0; j<op.getChildCount(); j++) {
            ATreeNode c = (ATreeNode) op.getChildAt(j);
            if (c instanceof OperatorTreeNode) {
              return "NO embeded operator allowed here";
            }
          }
        }
      }
    }
    return null;
  }

  //
  private boolean isException(KeywordTreeNode knode, OperatorTreeNode op) {
    if (knode.getChildCount() == 2 && knode.getIndex(op) == 1) {
      ATreeNode child = (ATreeNode) knode.getChildAt(0);
      if (child instanceof OperatorTreeNode) {
        OperatorTreeNode op2 = (OperatorTreeNode) child;
        if (op2.isOptional() && op2.getChildCount() == 1) {
          ATreeNode ch = (ATreeNode) op2.getChildAt(0);
          return ch instanceof CharTreeNode || ch instanceof StringTreeNode;
        }
      }
    }
    return false;
  }

  //
  private String checkSameStartError(KeywordTreeNode knode) {
    for (int i=0; i<knode.getChildCount()-1; i++) {
      ATreeNode child1 = (ATreeNode) knode.getChildAt(i);
      if (child1.getChildCount() > 0 && !(child1 instanceof ParseRefTreeNode)) {
        if (child1 instanceof KeywordTreeNode && ((KeywordTreeNode)child1).isRef()) {
          continue;
        }
        ATreeNode ch1 = (ATreeNode) child1.getChildAt(0);
        for (int j=i+1; j<knode.getChildCount(); j++) {
          ATreeNode child2 = (ATreeNode) knode.getChildAt(j);
          if (child2.getChildCount() > 0) {
            ATreeNode ch2 = (ATreeNode) child2.getChildAt(0);
            if (ch1.getClass().getName().equals(ch2.getClass().getName()) &&
                ch1.getName().equals(ch2.getName())) {
              if (ch1 instanceof OperatorTreeNode) {
                OperatorTreeNode op = (OperatorTreeNode) ch1;
                if (op.isEnum() && !child1.getName().equals(child2.getName())) {
                  continue;
                }
              }
              return "same start (" + ch1.getName() + ") found";
            }
          } 
        }
      }
    }
    return null;
  }

  //
  private String checkSameNameError(KeywordTreeNode knode) {
    for (int i=0; i<knode.getChildCount()-1; i++) {
      ATreeNode child1 = (ATreeNode) knode.getChildAt(i);
      if (!(child1 instanceof OperatorTreeNode)) {
        for (int j=i+1; j<knode.getChildCount(); j++) {
          ATreeNode child2 = (ATreeNode) knode.getChildAt(j);
          if (child1.getClass().getName().equals(child2.getClass().getName()) &&
              child1.getName().equals(child2.getName()) &&
              child1.getIdentifier() != null && child1.getIdentifier().equals(child2.getIdentifier())) {
            return "same name (" + child1.getName() + ") found";
          }
        }
      }
    }
    return null;
  }

  //
  public List<String> getUndefinedKeywordDefinitions() {
    List<String> list = new ArrayList<>();
    undefined.clear();
    KeywordTreeNode node = rootNode;
    gatherUndefined(node);
    if (!undefined.isEmpty()) {
      for (KeywordTreeNode key : undefined) {
        list.add(key.getName()+": UNDEFINED");
      }
    }
    return list;
  }

  //
  private void gatherUndefined(ATreeNode node) {
    if (node.getChildCount() == 0 && node instanceof KeywordTreeNode) {
      undefined.add((KeywordTreeNode) node);
    }
    else {
      for (int i=0; i<node.getChildCount(); i++) {
        ATreeNode tn = (ATreeNode) node.getChildAt(i);
        gatherUndefined(tn);
      }
    }
  }

  public List<String> getIgnoredKeywordDefinitions() {
    omitted.clear();
    KeywordTreeNode node = rootNode;
    gatherIgnored(node);
    if (!omitted.isEmpty()) {
      List<String> list = new ArrayList<>();
      for (OmittedKeyTreeNode key : omitted) {
        list.add(key.getName());
      }
      return list;
    }
    return null;
  }

  //
  private void gatherIgnored(ATreeNode node) {
    for (int i=0; i<node.getChildCount(); i++) {
      ATreeNode tn = (ATreeNode) node.getChildAt(i);
      if (tn instanceof OmittedKeyTreeNode) {
        omitted.add((OmittedKeyTreeNode) tn);
      }
      else {
        gatherIgnored(tn);
      }
    }
  }

  public void turnToIgnored() {
    TreeNodeProcessor processor = new TreeNodeProcessor();

    for (KeywordTreeNode key : undefined) {
      processor.performReplace(key, this);
    }
  }

  public JTree getMainTree() {
    return mainTree;
  }

  public void generateGraph(JTree tree, DefinitionNode dr, KeywordTreeNode rn) {
    ToolTipManager.sharedInstance().registerComponent(tree);
    TreeCellRenderer renderer = new TreeCellRenderer();
    renderer.setBackgroundSelectionColor(ColorSet.selectedBackground.getColor());
    renderer.setTextSelectionColor(Color.white);
    tree.setCellRenderer(renderer);
    treeHandler.createChildren(dr, rn);
    treeMouseListener = new TreeMouseListener(tree, this);
    tree.addMouseListener(treeMouseListener);
  }

  /**
   * @return the propertiesPanel
   */
  public PropertiesPanel getPropertiesPanel() {
    return propertiesPanel;
  }

  /**
   * @return the controlPanel
   */
  public ControlPanel getControlPanel() {
    return controlPanel;
  }

  /**
   * @return the treePanel
   */
  public TreePanel getTreePanel() {
    return treePanel;
  }

  /**
   * @return the frame
   */
  public JFrame getFrame() {
    return frame;
  }

  public void updateSize(JScrollPane pane) {
    if (tabbedPane != null) {
      tabbedPane.setSize(pane.getWidth(), pane.getHeight());
      tabbedPane.paintImmediately(pane.getVisibleRect());
    }
  }

  /**
   * @return the definitionSheet
   */
  public DefinitionSheet getDefinitionSheet() {
    return definitionSheet;
  }

  /**
   * @return the rootNode
   */
  public KeywordTreeNode getRootNode() {
    return rootNode;
  }

  /**
   * @return the treeHandler
   */
  public TreeHandler getTreeHandler() {
    return treeHandler;
  }

  /**
   * @return the isDirty
   */
  public boolean isDirty() {
    return isDirty;
  }

  /**
   * @param isDirty the isDirty to set
   */
  public void setDirty(boolean isDirty) {
    this.isDirty = isDirty;
  }

  public void renameInTreeMouseListener(String oldname, String newname) {
    if (treeMouseListener != null) {
      treeMouseListener.renameNode(oldname, newname);
    }
  }

  public void removeInTreeMouseListener(String name) {
    if (treeMouseListener != null) {
      treeMouseListener.removeNode(name);
    }
  }

  /**
   * @return the isShowDelta
   */
  public boolean isShowDelta() {
    return isShowDelta;
  }

  /**
   * @param isShowDelta the isShowDelta to set
   */
  public void setShowDelta(boolean isShowDelta) {
    this.isShowDelta = isShowDelta;
  }

  /**
   * @return the isShowRawSource
   */
  public boolean isShowRawSource() {
    return isShowRawSource;
  }

  /**
   * @param isShowRawSource the isShowRawSource to set
   */
  public void setShowRawSource(boolean isShowRawSource) {
    this.isShowRawSource = isShowRawSource;
  }
}

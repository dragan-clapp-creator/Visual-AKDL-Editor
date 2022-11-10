package akdl.edit.panel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.TreeNode;

import akdl.edit.tree.node.ATreeNode;
import akdl.edit.tree.node.KeywordTreeNode;
import akdl.edit.tree.node.OperatorTreeNode;
import akdl.edit.tree.node.PredefinedTreeNode;
import akdl.edit.util.ColorSet;
import akdl.eval.EvaluationInfo;
import akdl.eval.EvaluationInfo.EvalClass;

public class PropertiesPanel extends JPanel {

  private static final long serialVersionUID = 6629890201203633718L;

  private GridBagConstraints c;

  private JPanel jpDisplay;
  private JPanel jpComp;
  private JPanel jpEval;

  private boolean isEvaluationDisplay;
  private String[] evalString = new String[] {
    "", "NUMERIC", "LOGIC", "CUSTOM"
  };


  public PropertiesPanel(int height) {
    setLayout(new GridBagLayout());
    setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(EtchedBorder.RAISED),
            "Properties Area"));
    setPreferredSize(new Dimension(300, height));
    setSize(new Dimension(300, height));
    c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    jpDisplay = new JPanel();
    jpDisplay.setLayout(new GridBagLayout());
    jpDisplay.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(EtchedBorder.LOWERED),
            "Display Info"));

    jpComp = new JPanel();
    jpComp.setLayout(new GridBagLayout());
    jpComp.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(EtchedBorder.LOWERED),
            "Data Info"));

    jpEval = new JPanel();
    jpEval.setLayout(new BoxLayout(jpEval, BoxLayout.Y_AXIS));
    jpEval.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(EtchedBorder.LOWERED),
            "Evaluation Info"));
    addElements();
  }

  //
  private void addElements() {
    c.gridx = 0;
    if (ControlPanel.isPathDisplay) {
      c.gridy = 0;
      add(jpDisplay, c);
    }
    c.gridy = 1;
    add(jpComp, c);
    if (isEvaluationDisplay) {
      c.gridy = 2;
      add(jpEval, c);
    }
  }

  public void addDisplay(String paths, int y) {
    if (ControlPanel.isPathDisplay) {
      jpDisplay.removeAll();
      c.gridy = y;
      c.gridx = 0;
      jpDisplay.add(new JLabel("path to node"), c);
      c.gridx = 1;
      jpDisplay.add(Box.createHorizontalStrut(5), c);
      JTextArea area = new JTextArea(paths,1,1);
      area.setEditable(false);
      area.setBackground(ColorSet.notEditableProperty.getColor());
      c.gridx = 2;
      jpDisplay.add(area, c);
    }
  }

  public void removeComponents() {
    jpComp.removeAll();
  }

  public void addComponent(Component comp, int x, int y) {
    c.gridx = x;
    c.gridy = y;
    jpComp.add(comp, c);
  }

  public void redraw() {
    removeAll();
    addElements();
    updateUI();
  }

  public void setupEvaluation(ATreeNode node) {
    if (node instanceof KeywordTreeNode) {
      KeywordTreeNode knode = (KeywordTreeNode) node;
      isEvaluationDisplay = knode.getRegisteredEvaluation() != null;
      if (isEvaluationDisplay) {
        jpEval.removeAll();
        TreeNode p = node.getParent();
        boolean isFirst = !node.hasEval(p);
        EvaluationInfo info = knode.getRegisteredEvaluation();
        c.gridx = 0;
        c.gridy = 0;
        if (isFirst) {
          JComboBox<String> jc = new JComboBox<>(evalString);
          jc.setSelectedItem(info.getEval().name());
          jpEval.add(jc, c);
          jc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              String sel = (String) jc.getSelectedItem();
              if (sel.isEmpty()) {
                if (info != null) {
                  node.propagate(null);
                }
              }
              else {
                EvalClass eval = EvalClass.valueOf(sel);
                if (info == null || info.getEval() != eval) {
                  node.propagate(eval);
                }
              }
            }
          });
        }
        else {
          JTextField tf = new JTextField(info.getEval().name());
          tf.setEnabled(false);
          tf.setBackground(ColorSet.notEditableArea.getColor());
          jpEval.add(tf, c);
        }
        if (doesAcceptInitialization(node)) {
          c.gridx = 1;
          JCheckBox cb1 = new JCheckBox("Mark for Init");
          jpEval.add(cb1, c);
          cb1.setSelected(info.isInit());
          cb1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              info.setInit(cb1.isSelected());
            }
          });
        }
        if (doesAcceptOperator(node)) {
          c.gridx = 2;
          JCheckBox cb2 = new JCheckBox("Mark as Operator");
          jpEval.add(cb2, c);
          cb2.setSelected(info.isOperator());
          cb2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              info.setOperator(cb2.isSelected());
            }
          });
        }
      }
    }
    else {
      isEvaluationDisplay = false;
    }
  }

  //
  private boolean doesAcceptInitialization(ATreeNode node) {
    boolean hasReference = false;
    boolean hasIdentifier = false;
    for (int i=0; i<node.getChildCount(); i++) {
      TreeNode child = node.getChildAt(i);
      if (child instanceof PredefinedTreeNode) {
        if (((PredefinedTreeNode)child).isReference()) {
          hasReference = true;
        }
        else if (((PredefinedTreeNode)child).isIdentifier()) {
          hasIdentifier = true;
        }
      }
    }
    return hasReference && hasIdentifier;
  }

  //
  private boolean doesAcceptOperator(ATreeNode node) {
    if (node.getChildCount() == 1) {
      if (node.getChildAt(0) instanceof OperatorTreeNode) {
        OperatorTreeNode op = (OperatorTreeNode)node.getChildAt(0);
        return op.getName().equals("ENUM");
      }
    }
    return false;
  }
}

package akdl.edit.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import akdl.edit.dialog.CodeGenerationDialog;
import akdl.edit.dialog.HeaderDialog;
import akdl.edit.dialog.KeysDialog;
import akdl.edit.handler.GeneralContext;
import akdl.edit.tree.node.ATreeNode;
import akdl.sheet.DefinitionSheet;
import akdl.sheet.HeaderInfo;

public class ControlPanel extends JPanel {

  private static final long serialVersionUID = 6629890201203633718L;

  public static boolean isPathDisplay;

  private GridBagConstraints c;

  private ATreeNode node;

  private JCheckBox checkDisplay;
  private JButton header;
  private JButton keys;
  private JButton gen;

  private DefinitionSheet definitionSheet;
  private HeaderInfo headerInfo;

  private GeneralContext context;

  private JRadioButton radio1;
  private JRadioButton radio2;
  private JRadioButton radio3;
  private JRadioButton radio4;

  private boolean isShowVisible;


  public ControlPanel(int height, GeneralContext context) {
    this.context = context;
    this.definitionSheet = context.getDefinitionSheet();
    this.headerInfo = definitionSheet.getHeaderInfo();
    this.isShowVisible = height > 400;

    setLayout(new GridBagLayout());
    setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(EtchedBorder.RAISED),
            "Control Area"));
    setPreferredSize(new Dimension(300, height));
    setSize(new Dimension(300, height));
    c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;

    addElements();
  }

  //
  private void addElements() {
    addButtonsBlock();
    addChoicesBlock();
  }

  //
  private void addButtonsBlock() {
    JPanel jp = new JPanel();
    jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
    jp.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(EtchedBorder.LOWERED),
            "Buttons"));

    keys = new JButton("check keywords consistency");
    keys.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new KeysDialog(context).setVisible(true);
      }
    });
    jp.add(keys);

    header = new JButton("setup header info");
    header.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new HeaderDialog(context.getFrame(), headerInfo).setVisible(true);
        gen.setEnabled(headerInfo.getDestination() != null);
      }
    });
    jp.add(header);

    gen = new JButton("generate code");
    gen.setForeground(Color.red);
    gen.setEnabled(isEnabled(headerInfo));
    gen.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new CodeGenerationDialog(context.getFrame(), context, definitionSheet).setVisible(true);
      }
    });
    jp.add(gen);

    addComp(jp, 0, 1);
  }

  //
  private boolean isEnabled(HeaderInfo hi) {
    if (hi == null || hi.getDestination() == null) {
      return false;
    }
    if (hi.isBottomUpApproach()) {
      return hi.getPathToGrammar() != null;
    }
    return hi.getDestination() != null || hi.getPathToSaveDefinitions() != null;
  }

  //
  private void addChoicesBlock() {
    JPanel jp = new JPanel();
    jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
    jp.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(EtchedBorder.LOWERED),
            "Choices"));

    checkDisplay = new JCheckBox("display path");
    checkDisplay.setSelected(isPathDisplay);
    checkDisplay.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        isPathDisplay = checkDisplay.isSelected();
        context.refreshAll(node);
      }
    });
    jp.add(checkDisplay);

    ItemListener radioListener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        JRadioButton rb = (JRadioButton) e.getSource();
        if (rb == radio1) {
          context.setShowRawSource(true);
        }
        else if (rb == radio2) {
          context.setShowRawSource(false);
        }
        if (rb == radio3) {
          context.setShowDelta(true);
        }
        else if (rb == radio4) {
          context.setShowDelta(false);
        }
      }
    };

    JPanel jp_1 = new JPanel();
    jp_1.setLayout(new BoxLayout(jp_1, BoxLayout.Y_AXIS));
    jp_1.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(EtchedBorder.RAISED),
            "source type"));
    jp.add(jp_1);
    ButtonGroup bg1 = new ButtonGroup();
    radio1 = (JRadioButton) jp_1.add( new JRadioButton("raw source") );
    radio1.addItemListener(radioListener);
    radio1.setSelected(true);
    bg1.add(radio1);
    radio2 = (JRadioButton) jp_1.add( new JRadioButton("colored source") );
    radio2.addItemListener(radioListener);
    bg1.add(radio2);

    if (isShowVisible) {
      JPanel jp_2 = new JPanel();
      jp_2.setLayout(new BoxLayout(jp_2, BoxLayout.Y_AXIS));
      jp_2.setBorder(
          BorderFactory.createTitledBorder(
              BorderFactory.createBevelBorder(EtchedBorder.RAISED),
              "source quantity"));
      jp.add(jp_2);
      ButtonGroup bg2 = new ButtonGroup();
      radio3 = (JRadioButton) jp_2.add( new JRadioButton("show source delta") );
      radio3.addItemListener(radioListener);
      radio3.setSelected(true);
      bg2.add(radio3);
      radio4 = (JRadioButton) jp_2.add( new JRadioButton("show all") );
      radio4.addItemListener(radioListener);
      bg2.add(radio4);
    }

    addComp(jp, 0, 2);
  }

  public void redraw() {
    removeAll();
    addElements();
    updateUI();
  }

  public void update(ATreeNode node) {
    this.node = node;
    context.refresh(true);
  }

  public void addComp(Component comp, int x, int y) {
    c.gridx = x;
    c.gridy = y;
    add(comp, c);
  }
}

package akdl.edit.dialog;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.PrintStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import akdl.edit.handler.GeneralContext;
import akdl.edit.util.ColorSet;
import akdl.edit.util.CustomOutputStream;
import akdl.sheet.DefinitionSheet;

public class CodeGenerationDialog extends ADialog {

  private static final long serialVersionUID = 477110516207013829L;

  private GeneralContext context;

  private DefinitionSheet definitionSheet;

  private JScrollPane scrollpane;


  public CodeGenerationDialog(Frame parent, GeneralContext context, DefinitionSheet ds) {
    super(parent, null, null, "Code Generation Result Dialog", null);
    setPerformOnOk(false);
    this.context = context;
    definitionSheet = ds;
    redefineContent();
  }

  @Override
  public int createOwnContent(GridBagConstraints c) {
    if (context == null) {
      return 0;
    }
    c.gridx = 0;
    c.gridy = 0;
    JButton go = new JButton("go");
    go.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        PrintStream out = System.out;
        PrintStream err = System.err;
        PrintStream printStream = new PrintStream(new CustomOutputStream(scrollpane));
        System.setOut(printStream);
        System.setErr(printStream);
        JButton btn = (JButton) e.getSource();
        if ("go".equals(btn.getText())) {
          List<String> list = context.getErrors();
          if (list.isEmpty()) {
            definitionSheet.generateCodeFromEditor(context.generateDefRoot());
            list = context.getIgnoredKeywordDefinitions();
            if (list != null) {
              System.out.println("\n[WARNING] The following elements were ignored:\n"+list);
            }
          }
          else {
            btn.setText("Ignore and Retry");
            System.err.println("Could not process code generation because of following errors:\n");
            for (String key : list) {
              System.err.println("\t" + key);
            }
          }
        }
        else {
          context.turnToIgnored();
          definitionSheet.generateCodeFromEditor(context.generateDefRoot());
        }
        System.setOut(out);
        System.setErr(err);
      }
    });
    getContentPane().add(go, c);
    c.gridy = 1;
    JTextArea ta = new JTextArea(30, 50);
    ta.setEditable(false);
    ta.setBackground(ColorSet.notEditableArea.getColor());
    scrollpane = new JScrollPane(ta);
    scrollpane.setBackground(Color.yellow);
    scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollpane.addContainerListener(new ContainerListener() {
      @Override
      public void componentRemoved(ContainerEvent e) {
      }
      @Override
      public void componentAdded(ContainerEvent e) {
        refresh();
      }
    });
    getContentPane().add(scrollpane, c);

    return 2;
  }

  public void refresh() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        getContentPane().repaint();
      }
    });
  }

  @Override
  public void performAction(ActionEvent e) {
  }

}

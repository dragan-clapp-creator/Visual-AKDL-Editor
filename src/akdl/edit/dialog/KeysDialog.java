package akdl.edit.dialog;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.PrintStream;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import akdl.edit.handler.GeneralContext;
import akdl.edit.util.ColorSet;
import akdl.edit.util.CustomOutputStream;

public class KeysDialog extends ADialog {

  private static final long serialVersionUID = 477110516207013829L;

  private GeneralContext context;

  private JScrollPane scrollpane;


  public KeysDialog(GeneralContext context) {
    super(context.getFrame(), null, null, "List of Keywords", null);
    setPerformOnOk(false);
    this.context = context;
    redefineContent();
  }

  @Override
  public int createOwnContent(GridBagConstraints c) {
    if (context == null) {
      return 0;
    }
    c.gridx = 0;
    c.gridy = 0;
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
    setup();
    getContentPane().add(scrollpane, c);

    return 2;
  }

  //
  private void setup() {
    PrintStream out = System.out;
    PrintStream err = System.err;
    PrintStream printStream = new PrintStream(new CustomOutputStream(scrollpane));
    System.setOut(printStream);
    System.setErr(printStream);

    List<String> list = context.getErrors();
    if (list.isEmpty()) {
      System.out.println("\nALL KEYWORDS WELL DEFINED\n");
    }
    else {
      System.out.println("\nERRORS:\n");
      for (String key : list) {
        System.out.println("\t" + key);
      }
    }
    list = context.getIgnoredKeywordDefinitions();
    if (list != null) {
      System.out.println("\nWARNING, IGNORED KEYWORDS:\n");
      for (String key : list) {
        System.out.println("\t" + key);
      }
    }
    System.setOut(out);
    System.setErr(err);
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

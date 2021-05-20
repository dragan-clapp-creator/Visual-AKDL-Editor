package akdl.edit.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import akdl.edit.dialog.help.Help;
import akdl.edit.util.CommonScrollPanel;

public class HelpDialog extends JDialog {

  private static final long serialVersionUID = 477110516207013829L;

  private CommonScrollPanel scrollPane;
  private JPanel panel;


  public HelpDialog(Frame parent) {
    super(parent, "Help to Visual AKDL Editor", false);
    Point p = parent.getLocation(); 
    setLocation(p.x + 700, p.y + 80);
    setLayout(new BorderLayout());
    defineContent();
    setAlwaysOnTop(false);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack(); 
  }

  public void defineContent() {
    scrollPane = new CommonScrollPanel(550, 700);

    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    scrollPane.getViewport().add(panel);

    add(scrollPane, BorderLayout.CENTER);

    createSection("<h1>Help on Visual AKDL Editor</h1>", 50, new Color(0xaed6f1));

    createSection("<h2>Get started</h2>", 30, new Color(0xfdf2e9));
    createBlock(new JButton("What is it about?"), Help.INIT, 80);
    createBlock(new JButton("How do I begin?"), Help.START, 250);

    createSection("<h2>Control Area</h2>", 30, new Color(0xfdf2e9));
    createBlock(new JButton("check keywords list"), Help.KEYS, 80);
    createBlock(new JButton("setup header info"), Help.HEADER, 110);
    createBlock(new JButton("generate code"), Help.CODE, 260);
    createBlock(new JButton("display path"), Help.DISPLAY, 40);
    createBlock(new JButton("source options"), Help.SOURCE_OPTION, 330);

    createSection("<h2>Properties Area</h2>", 30, new Color(0xfdf2e9));
    createBlock(new JButton("setup node's properties"), Help.PROPS, 100);
    createBlock(new JButton("node types"), Help.NODES, 530);

    createSection("<h2>Context Menu</h2>", 30, new Color(0xfdf2e9));
    createBlock(new JButton("add child nodes"), Help.ADD, 290);
    createBlock(new JButton("wrap a node or a group of nodes"), Help.WRAP, 200);
    createBlock(new JButton("copy/cut - paste & move"), Help.COPY, 200);
    createBlock(new JButton("find a node"), Help.FIND, 70);
    createBlock(new JButton("source of a node"), Help.SOURCE, 25);
    createBlock(new JButton("handling ignored node"), Help.IGNORED, 100);

    createSection("<h2>Learn More...</h2>", 30, new Color(0xfdf2e9));
    createBlock(new JButton("about EBNF metalanguage"), Help.BNF, 400);
    createBlock(new JButton("about AKDL syntax"), Help.AKDL, 640);
    createBlock(new JButton("some rules you should know"), Help.RULES, 1200);
  }

  //
  private void createSection(String html, int height, Color color) {
    final JEditorPane jep = new JEditorPane();
    jep.setPreferredSize(new Dimension(450, height));
    jep.setEditable(false);
    jep.setContentType("text/html");
    jep.setText(html);
    jep.setBackground(color);
    JSeparator sep = new JSeparator();
    panel.add(sep);
    panel.add(jep);
  }

  //
  private void createBlock(JButton jButton, Help html, int height) {
    final JEditorPane jep = new JEditorPane();
    jep.setPreferredSize(new Dimension(450, height));
    jep.setEditable(false);
    jep.setContentType("text/html");
    jep.setText(html.getContent().toString());
    jep.setVisible(false);

    jButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        jep.setVisible(!jep.isVisible());
        if (jep.isVisible()) {
          scrollPane.addHeight(height);
          Point p = jep.getLocation();
          scrollPane.getViewport().setViewPosition(new Point(p.x, p.y-30));
        }
        else {
          scrollPane.subHeight(height);
        }
      }
    });
    panel.add(jButton);
    panel.add(jep);
  }
}

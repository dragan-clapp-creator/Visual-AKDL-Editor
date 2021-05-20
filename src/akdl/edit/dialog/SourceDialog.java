package akdl.edit.dialog;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;

import akdl.edit.dialog.help.Help;
import akdl.edit.handler.GeneralContext;
import akdl.edit.tree.node.KeywordTreeNode;

public class SourceDialog extends JDialog implements ActionListener {

  private static final long serialVersionUID = 4052746551040636534L;

  private JEditorPane srcfield;

  private String source;

  private JButton okButton;

  private KeywordTreeNode tnode;

  public SourceDialog(KeywordTreeNode tnode, JTree tree, GeneralContext context) {
    super(context.getFrame(), "Node Item's Source", true);

    this.tnode = tnode;
    Dimension dim = new Dimension(550, 200);
    Point p = context.getFrame().getLocation(); 
    setLocation(p.x + 350, p.y + 200);
    setLayout(new GridLayout(2, 1));
    if (dim != null) {
      setPreferredSize(dim);
    }

    okButton = new JButton("ok");
    okButton.addActionListener(this);

    getContentPane().removeAll();

    getContentPane().add(createOwnContent());

    getContentPane().add(line(okButton));

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    pack(); 
  }

  private JPanel line(JButton btn) {
    JPanel jp = new JPanel();
    jp.add(Box.createHorizontalStrut(50));
    jp.add(btn);
    jp.add(Box.createHorizontalStrut(50));
    return jp;
  }

  //
  private String generateSource(KeywordTreeNode tnode) {
    StringBuilder sb = new StringBuilder( Help.WHOLE_SOURCE.getContent() );
    String src = tnode.getSourceDefinition(false);
    int index = sb.indexOf("%SOURCE%");
    sb = sb.replace(index, index+8, src);
    return sb.toString();
  }

  public JPanel createOwnContent() {
    srcfield = new JEditorPane();
    source = generateSource(tnode);
    srcfield.setEditable(false);
    srcfield.setContentType("text/html");
    srcfield.setText(source);
    JPanel jp = new JPanel();
    jp.add(new JLabel("Source"));
    jp.add(Box.createVerticalStrut(5));
    jp.add(srcfield);
    return jp;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okButton) {
      dispose();
    }
  }
}

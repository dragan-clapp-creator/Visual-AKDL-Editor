package akdl.edit.handler;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import akdl.edit.AKDLEditor;

public class ActionHandler {

  /**
   * create MenuItem With Action
   * 
   * @param title
   * @param key
   * @param editor
   * @return
   */
  public JMenuItem createItemWithAction(String title, int key, AKDLEditor editor) {
    JMenuItem item = new JMenuItem(title);
    item.addActionListener(editor);
    Action a = createAction(title, key);
    item.setAction(a);
    return item;
  }

  //
  private Action createAction(String title, int key) {
    @SuppressWarnings("serial")
    Action a = new AbstractAction(title) {
      
      @Override
      public void actionPerformed(ActionEvent e) {
      }
    };
    a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key, KeyEvent.CTRL_DOWN_MASK));
    return a;
  }
}

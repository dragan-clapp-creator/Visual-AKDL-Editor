package akdl.edit.util;

import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CommonScrollPanel extends JScrollPane {

  private static final long serialVersionUID = 1163378046873682877L;

  private int width;
  private int height;

  /**
   * constructor
   */
  public CommonScrollPanel(int w, int h) {
    super(null, 
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    width = w;
    height = h;
    getViewport().addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            updateUI();
          }
        });
      }
    });
  }

  @Override
  public Dimension getMaximumSize(){
      return getCustomDimensions();
  }

  @Override
  public Dimension getMinimumSize(){
      return getCustomDimensions();
  }

  @Override
  public Dimension getPreferredSize(){
      return getCustomDimensions();
  }

  //
  private Dimension getCustomDimensions() {
    return new Dimension(width, height);
  }

  public void addHeight(int h) {
    height += h;
  }

  public void subHeight(int h) {
    height -= h;
  }
}

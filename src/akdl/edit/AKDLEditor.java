package akdl.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import akdl.edit.handler.GeneralContext;
import akdl.edit.handler.MainMenuHandler;

public class AKDLEditor extends ComponentAdapter implements ActionListener {

  private File selectedDirectory = new File(".");
  private File selectedFile;

  private MainMenuHandler  menuhandler;

  private JScrollPane pane;

  private JFrame application;

  private GeneralContext context;

  public AKDLEditor() {
    initialize();
  }

  /**
   * MAIN
   * @param args
   */
  public static void main(String[] args) {
    new AKDLEditor();
  }

  //
  private void initialize() {
    application = new JFrame("Visual AKDL Editor");
    application.addComponentListener(this);
    application.setSize(1200, 800);
    application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    pane = new JScrollPane(null, 
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JPanel jp = new JPanel();
    jp.setLayout(new BorderLayout());
    ImageIcon icon = createImage("akdl.jpg", "logo");
    jp.add(new JLabel(icon));
    pane.getViewport().add(jp);
    jp.setBackground(Color.white);

    context = new GeneralContext(application, pane);

    menuhandler = new MainMenuHandler();
    menuhandler.initialize(this);

    GraphicsConfiguration conf = application.getGraphicsConfiguration();
    Rectangle screenRect = conf.getBounds();
    Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(conf);
    Dimension size = application.getSize();
    int centerWidth = screenRect.width < size.width ?
        screenRect.x : screenRect.x + screenRect.width / 2 - size.width / 2;
    int centerHeight = screenRect.height < size.height ?
        screenRect.y : screenRect.y + screenRect.height / 2 - size.height / 2;
    centerHeight = centerHeight < screenInsets.top ?
        screenInsets.top : centerHeight;
    application.setLocation(centerWidth, centerHeight);

    application.add(menuhandler, BorderLayout.NORTH);
    application.add(pane, BorderLayout.CENTER);

    application.setVisible(true);
  }

  //
  private ImageIcon createImage(String path, String description) {
    URL imgURL = AKDLEditor.class.getClassLoader().getResource(path);
    if (imgURL != null) {
      return new ImageIcon(imgURL, description);
    }
    System.err.println("Couldn't find file: " + path);
    return null;
  }

  @Override
  public void actionPerformed(ActionEvent object) {
    try {
      menuhandler.perform(object.getSource(), pane);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void componentResized(ComponentEvent e) {
    if (context != null) {
      context.updateSize(pane);
    }
  }

  public File chooseFile(Component pane, boolean isSave, boolean isSaveAs) {
    JFileChooser chooser = new JFileChooser(selectedDirectory);
    int result;
    if (!isSave) {
      chooser.setFileFilter(new FileNameExtensionFilter("Choose a .def file", "def"));
      result = chooser.showOpenDialog(pane);
    }
    else {
      if (!isSaveAs) {
        chooser.setSelectedFile(selectedFile);
      }
      result = chooser.showSaveDialog(pane);
    }
    if (result == JFileChooser.APPROVE_OPTION) {
      selectedFile = chooser.getSelectedFile();
      selectedDirectory = selectedFile.getParentFile();
      return selectedFile;
    }
    return null;
  }

  /**
   * @return the context
   */
  public GeneralContext getContext() {
    return context;
  }

  /**
   * @return the selectedFile
   */
  public File getSelectedFile() {
    return selectedFile;
  }

  /**
   * @param selectedFile the selectedFile to set
   */
  public void setSelectedFile(File selectedFile) {
    this.selectedFile = selectedFile;
    this.selectedDirectory = selectedFile.getParentFile();
  }

  public void saveContent(boolean isSaveAs) {
    if (context.isDirty() || isSaveAs) {
      File file = chooseFile(pane, true, isSaveAs);
      if (file != null) {
        List<String> list = context.getUndefinedKeywordDefinitions();
        if (list != null && !list.isEmpty()) {
          System.out.println("\n[WARNING] The following elements were ignored:\n"+list);
          context.turnToIgnored();
        }
        try {
          FileOutputStream out = new FileOutputStream(file);
          String source = context.createRawSourceFromNodes();
          out.write(source.getBytes());
          out.close();
          context.setDirty(false);
        }
        catch(IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
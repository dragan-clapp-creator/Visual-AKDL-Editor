package akdl.edit.handler;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import akdl.edit.AKDLEditor;
import akdl.edit.dialog.AboutDialog;
import akdl.edit.dialog.ColorsDialog;
import akdl.edit.dialog.HelpDialog;
import akdl.edit.dialog.RecentlyOpenedDialog;
import akdl.edit.dialog.SaveConfirmationDialog;
import akdl.edit.util.ColorsHandler;
import akdl.edit.util.RecentHandler;

public class MainMenuHandler extends JMenuBar {

  private static final long serialVersionUID = -7655572996174119569L;

  enum ChosenProject {
    CLAPP, GRAFCET, RDP, ACTIGRAM;
  }

  private JMenuItem create;
  private JMenuItem open;
  private JMenuItem save;
  private JMenuItem saveAs;
  private JMenuItem recent;

  private JMenuItem about;
  private JMenuItem cpreferences;
  private JMenuItem rpreferences;
  private JMenuItem help;
  private JMenuItem exit;

  private RecentHandler rhandler;
  private ColorsHandler chandler;

  private GeneralContext context;
  private AKDLEditor editor;

  // Menu elements
  public void initialize(AKDLEditor ed) {

    editor = ed;
    context = editor.getContext();

    rhandler = new RecentHandler();
    rhandler.populateRecentFiles();

    chandler = new ColorsHandler();
    chandler.retreivePreferredColors();

    ActionHandler ahandler = new ActionHandler();

    JMenu source = new JMenu("Source ");
      create = ahandler.createItemWithAction("New...", KeyEvent.VK_N, editor);
      open = ahandler.createItemWithAction("Open", KeyEvent.VK_O, editor);
      save = ahandler.createItemWithAction("Save", KeyEvent.VK_S, editor);
        save.setEnabled(false);
      saveAs = ahandler.createItemWithAction("Save As...", KeyEvent.VK_W, editor);
        saveAs.setEnabled(false);
    add(source);
      source.add(create);
      source.add(open);
      source.add(save);
      source.add(saveAs);
      source.addSeparator();
    recent = new JMenu("Recently opened...");
      source.add(recent);
      ArrayList<File> files = rhandler.getFiles();
      if (files != null && !files.isEmpty()) {
        fillRecent(files);
      }
      else {
        recent.setEnabled(false);
      }

    JMenu akdl = new JMenu("AKDL");
      cpreferences = ahandler.createItemWithAction("Color Preferences...", KeyEvent.VK_C, editor);
      rpreferences = ahandler.createItemWithAction("Recently Opened Preferences...", KeyEvent.VK_R, editor);
      exit = new JMenuItem("Exit");
        exit.addActionListener(editor);
      help = ahandler.createItemWithAction("Help", KeyEvent.VK_H, editor);
      about = ahandler.createItemWithAction("About", KeyEvent.VK_A, editor);
    add(akdl);
      akdl.add(about);
      akdl.addSeparator();
      akdl.add(cpreferences);
      akdl.addSeparator();
      akdl.add(rpreferences);
      akdl.addSeparator();
      akdl.add(help);
      akdl.addSeparator();
      akdl.add(exit);
  }

  //
  private void fillRecent(ArrayList<File> files) {
    recent.setEnabled(true);
    recent.removeAll();
    for (File file : files) {
      String text = file.getName();
      try {
        text += " ("+ file.getParentFile().getCanonicalPath() + ")";
      } catch (IOException e) {
        e.printStackTrace();
      }
      JMenuItem item = new JMenuItem(text);
      recent.add(item);
      item.addActionListener(editor);
    }
  }

  public void perform(Object source, JScrollPane pane)
      throws InterruptedException, IOException {
    if (source == create) {
      if (!context.isDirty() || askForConfirmation()) {
        updateContext(null);
      }
    }
    else if (source == open) {
      if (!context.isDirty() || askForConfirmation()) {
        File selectedFile = editor.chooseFile(pane, false, false);
        if (selectedFile != null) {
          if (rhandler.add(selectedFile)) {
            fillRecent(rhandler.getFiles());
          }
          updateContext(selectedFile);
        }
      }
    }
    else if (source == exit) {
      if (!context.isDirty() || askForConfirmation()) {
        System.exit(0);
      }
    }
    else if (source == about) {
      new AboutDialog(context.getFrame()).setVisible(true);
    }
    else if (source == help) {
      new HelpDialog(context.getFrame()).setVisible(true);
    }
    else if (source == cpreferences) {
      new ColorsDialog(context.getFrame(), chandler).setVisible(true);
    }
    else if (source == rpreferences) {
      RecentlyOpenedDialog rd = new RecentlyOpenedDialog(context.getFrame(), rhandler);
      rd.setVisible(true);
      if (rd.isRefreshNeeded()) {
        fillRecent(rhandler.getFiles());
      }
    }
    else if (source == save) {
      editor.saveContent(false);
    }
    else if (source == saveAs) {
      editor.saveContent(true);
    }
    else {
      if (!context.isDirty() || askForConfirmation()) {
        File file = rhandler.getFile(((JMenuItem)source).getText());
        editor.setSelectedFile(file);
        updateContext(file);
      }
    }
  }

  //
  private void updateContext(File selectedFile) throws IOException {
    context.setup(selectedFile);
    save.setEnabled(true);
    saveAs.setEnabled(true);
    context.setDirty(false);
  }

  //
  private boolean askForConfirmation() {
    SaveConfirmationDialog conf = new SaveConfirmationDialog(context.getFrame());
    conf.setVisible(true);
    boolean ok = conf.isOk();
    conf.dispose();
    if (ok) {
      context.setDirty(false);
    }
    return ok;
  }
}

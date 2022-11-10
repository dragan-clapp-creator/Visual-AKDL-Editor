package akdl.edit.dialog.find;

import java.awt.Color;

import javax.swing.text.JTextComponent;

public class TextFinder {

  private String text;
  private boolean isCaseSensistive;
  private boolean isWholeWord;
  private JTextComponent srcfield;
  private String rawSource;

  private int index;
  private int lastIndex;

  /**
   * constructor
   * 
   * @param text
   * @param isCaseSensistive
   * @param isWholeWord
   * @param rawSource 
   * @param source 
   */
  public TextFinder(String text, boolean isCaseSensistive, boolean isWholeWord, JTextComponent srcfield, String rawSource) {
    this.text = isCaseSensistive ? text : text.toLowerCase();
    this.isCaseSensistive = isCaseSensistive;
    this.isWholeWord = isWholeWord;
    this.srcfield = srcfield;
    this.rawSource = isCaseSensistive ? rawSource : rawSource.toLowerCase();
  }

  public boolean findNext() {
    int i = -1;
    boolean finished = false;
    index = lastIndex;

    while (!finished) {
      i = rawSource.indexOf(text, index);
      if (i >= 0) {
        index = i + text.length();
        finished = !isWholeWord || isSeparator(rawSource.charAt(index));
      }
      else {
        finished = true;
      }
    }
    if (i >= 0) {
      lastIndex = index;
      selectText(i);
      return true;
    }
    if (lastIndex > 0) {
      index = 0;
      lastIndex = 0;
      return findNext();
    }
    return false;
  }

  //
  private boolean isSeparator(char c) {
    return c == ' ' || c == '\n' || c == '[' || c == ')' || c == ':';
  }

  private void selectText(int i) {
    srcfield.setCaretPosition(i);
    String txt; 
    do {
      srcfield.setSelectionStart(i--);
      srcfield.setSelectionEnd(index--);
      txt = srcfield.getSelectedText(); 
      if (txt == null) {
        break;
      }
    }
    while (!txt.equalsIgnoreCase(text));

    srcfield.setSelectedTextColor(Color.lightGray);
  }

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * @return the isCaseSensistive
   */
  public boolean isCaseSensistive() {
    return isCaseSensistive;
  }

  /**
   * @return the isWholeWord
   */
  public boolean isWholeWord() {
    return isWholeWord;
  }
}

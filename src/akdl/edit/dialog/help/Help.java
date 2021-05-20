package akdl.edit.dialog.help;

public enum Help {

  INIT,
  START,
  DISPLAY,
  KEYS,
  HEADER,
  CODE,
  PROPS,
  NODES,
  ADD,
  WRAP,
  COPY,
  FIND,
  SOURCE,
  SOURCE_OPTION,
  WHOLE_SOURCE,
  BNF,
  AKDL,
  IGNORED,
  RULES;

  private Help() {
  }

  /**
   * @return the content template
   */
  public StringBuilder getContent() {
    return HelpReader.getInstance().getContent(name());
  }

  @Override
  public String toString() {
    return getContent().toString();
  }
}

package akdl.edit.util;

import java.awt.Color;

public enum ColorSet {

  notEditableProperty(0xffffd0, "Not Editable Property Field    "),
  notEditableArea(0xccffff,     "Not Editable Text Area         "),

  selectedBackground(0xe14169,  "Background of Selected Item    "),

  forKeywordNode(0xa2d9ce,      "Keyword Node Color             "),
  forRefNode(0xaed6f1,          "Reference to Keyword Node Color"),
  forNumericNode(0xf4ecf7,      "Numeric Node Color             "),
  forJavaNode(0xfdf2e9,         "Predefined Node Color          "),
  forEnumElementNode(0xf9e79f,  "Enum Element Node Color        "),
  forStringNode(0xe1c2fe,       "String Nodes Color             "),
  forCharNode(0xfec8fc,         "Char Node Color                "),

  forAlternative(0xffffd0,      "Alternative Indicator Color    "),
  forOptional(0xfef9e7,         "Option Indicator Color         "),
  forZeroOrMany(0xfcfecd,       "Zero-Or-Many Indicator Color   "),
  forOmitted(0xfbe391,          "Omitted Indicator Color        "),
  forAnyOrder(0xfee9b1,         "Any-Order Indicator Color      "),
  forEnum(0xf2cc33,             "Enum Indicator Color           "),
  forDetached(0xf2cc33,         "Detached Enum Indicator Color  ");

  private Color color;
  private String label;

  private ColorSet(int rgb, String lbl) {
    color = new Color(rgb);
    label = lbl;
  }

  /**
   * @return the color
   */
  public Color getColor() {
    return color;
  }

  /**
   * @param color the color to set
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  public String getHexColor() {
    return Integer.toHexString(color.getRGB()).replace("ff", "#");
  }
}

package akdl.edit.tree.node.util;

import akdl.edit.tree.node.OperatorTreeNode.Operator;

public class Helper {

  private static Helper instance = new Helper();

  public static Helper getInstance() {
    return instance;
  }

  private Helper() {
  }

  /**
   * turn AKDL graph operator to TreeNode graph operator
   * @param op
   * @return
   */
  public Operator toTreeNodeOperator(akdl.graph.nodes.elts.Operator op) {
    switch (op) {
      case AMPERS:
        return Operator.ANY_ORDER;
      case HUT:
        return Operator.OPTIONAL;
      case PLUS:
        return Operator.ALTERNATIVE;
      case STAR:
        return Operator.ZERO_OR_MANY;
      case DETACHED:
        return Operator.DETACHED;

      default:
        break;
    }
    return null;
  }

  /**
   * turn AKDL graph operator to TreeNode graph operator
   * @param op
   * @return
   */
  public akdl.graph.nodes.elts.Operator toAKDLOperator(Operator op) {
    if (op == null) {
      return akdl.graph.nodes.elts.Operator.NONE;
    }
    switch (op) {
      case ANY_ORDER:
        return akdl.graph.nodes.elts.Operator.AMPERS;
      case OPTIONAL:
        return akdl.graph.nodes.elts.Operator.HUT;
      case ALTERNATIVE:
        return akdl.graph.nodes.elts.Operator.PLUS;
      case ZERO_OR_MANY:
        return akdl.graph.nodes.elts.Operator.STAR;
      case DETACHED:
        return akdl.graph.nodes.elts.Operator.DETACHED;

      default:
        break;
    }
    return null;
  }
}

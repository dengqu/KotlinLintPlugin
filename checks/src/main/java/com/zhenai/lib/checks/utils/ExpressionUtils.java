/*
 * SonarSource SLang
 * Copyright (C) 2018-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.zhenai.lib.checks.utils;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import com.zhenai.lib.core.slang.api.BinaryExpressionTree;
import com.zhenai.lib.core.slang.api.BlockTree;
import com.zhenai.lib.core.slang.api.ExceptionHandlingTree;
import com.zhenai.lib.core.slang.api.IfTree;
import com.zhenai.lib.core.slang.api.LiteralTree;
import com.zhenai.lib.core.slang.api.LoopTree;
import com.zhenai.lib.core.slang.api.MatchCaseTree;
import com.zhenai.lib.core.slang.api.ParenthesizedExpressionTree;
import com.zhenai.lib.core.slang.api.PlaceHolderTree;
import com.zhenai.lib.core.slang.api.TopLevelTree;
import com.zhenai.lib.core.slang.api.Tree;
import com.zhenai.lib.core.slang.api.UnaryExpressionTree;

import static com.zhenai.lib.core.slang.api.BinaryExpressionTree.Operator.CONDITIONAL_AND;
import static com.zhenai.lib.core.slang.api.BinaryExpressionTree.Operator.CONDITIONAL_OR;

public class ExpressionUtils {
  private static final String TRUE_LITERAL = "true";
  private static final String FALSE_LITERAL = "false";
  private static final List<String> BOOLEAN_LITERALS = Arrays.asList(TRUE_LITERAL, FALSE_LITERAL);

  private ExpressionUtils() {
  }

  public static boolean isBooleanLiteral(Tree tree) {
    return tree instanceof LiteralTree && BOOLEAN_LITERALS.contains(((LiteralTree) tree).value());
  }

  public static boolean isFalseValueLiteral(Tree originalTree) {
    Tree tree = skipParentheses(originalTree);
    return (tree instanceof LiteralTree && FALSE_LITERAL.equals(((LiteralTree) tree).value()))
      || (isNegation(tree) && isTrueValueLiteral(((UnaryExpressionTree) tree).operand()));
  }

  public static boolean isTrueValueLiteral(Tree originalTree) {
    Tree tree = skipParentheses(originalTree);
    return (tree instanceof LiteralTree && TRUE_LITERAL.equals(((LiteralTree) tree).value()))
      || (isNegation(tree) && isFalseValueLiteral(((UnaryExpressionTree) tree).operand()));
  }

  public static boolean isNegation(Tree tree) {
    return tree instanceof UnaryExpressionTree && ((UnaryExpressionTree) tree).operator() == UnaryExpressionTree.Operator.NEGATE;
  }

  public static boolean isBinaryOperation(Tree tree, BinaryExpressionTree.Operator operator) {
    return tree instanceof BinaryExpressionTree && ((BinaryExpressionTree) tree).operator() == operator;
  }

  public static boolean isLogicalBinaryExpression(Tree tree) {
    return isBinaryOperation(tree, CONDITIONAL_AND) || isBinaryOperation(tree, CONDITIONAL_OR);
  }

  public static Tree skipParentheses(Tree tree) {
    Tree result = tree;
    while (result instanceof ParenthesizedExpressionTree) {
      result = ((ParenthesizedExpressionTree) result).expression();
    }
    return result;
  }

  public static boolean containsPlaceHolder(Tree tree) {
    return tree.descendants().anyMatch(t -> t instanceof PlaceHolderTree);
  }

  public static boolean isTernaryOperator(Deque<Tree> ancestors, Tree tree) {
    if (!isIfWithElse(tree)) {
      return false;
    }
    Tree child = tree;
    for (Tree ancestor : ancestors) {
      if (ancestor instanceof BlockTree || ancestor instanceof ExceptionHandlingTree || ancestor instanceof TopLevelTree ||
        isBranchOfLoopOrCaseOrIfWithoutElse(ancestor, child)) {
        break;
      }
      if (!isBranchOfIf(ancestor, child)) {
        return tree.descendants().noneMatch(BlockTree.class::isInstance);
      }
      child = ancestor;
    }
    return false;
  }

  private static boolean isIfWithElse(Tree tree) {
    return tree instanceof IfTree && ((IfTree) tree).elseBranch() != null;
  }

  private static boolean isBranchOfLoopOrCaseOrIfWithoutElse(Tree parent, Tree child) {
    return (parent instanceof LoopTree && child == ((LoopTree) parent).body()) ||
      (parent instanceof MatchCaseTree && child == ((MatchCaseTree) parent).body()) ||
      (isBranchOfIf(parent, child) && ((IfTree) parent).elseBranch() == null);
  }

  private static boolean isBranchOfIf(Tree parent, Tree child) {
    if (parent instanceof IfTree) {
      IfTree ifTree = (IfTree) parent;
      return child == ifTree.thenBranch() || child == ifTree.elseBranch();
    }
    return false;
  }

}

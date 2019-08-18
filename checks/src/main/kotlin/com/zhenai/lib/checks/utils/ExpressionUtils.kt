package com.zhenai.lib.checks.utils

import com.zhenai.lib.core.slang.api.*
import java.util.*

import com.zhenai.lib.core.slang.api.BinaryExpressionTree.Operator.CONDITIONAL_AND
import com.zhenai.lib.core.slang.api.BinaryExpressionTree.Operator.CONDITIONAL_OR

class ExpressionUtils {

    companion object {
        private val TRUE_LITERAL = "true"
        private val FALSE_LITERAL = "false"
        private val BOOLEAN_LITERALS = Arrays.asList(TRUE_LITERAL, FALSE_LITERAL)

        fun isBooleanLiteral(tree: Tree): Boolean {
            return tree is LiteralTree && BOOLEAN_LITERALS.contains(tree.value())
        }

        fun isFalseValueLiteral(originalTree: Tree): Boolean {
            val tree = skipParentheses(originalTree)
            return tree is LiteralTree && FALSE_LITERAL == tree.value() || isNegation(tree) && isTrueValueLiteral((tree as UnaryExpressionTree).operand())
        }

        fun isTrueValueLiteral(originalTree: Tree): Boolean {
            val tree = skipParentheses(originalTree)
            return tree is LiteralTree && TRUE_LITERAL == tree.value() || isNegation(tree) && isFalseValueLiteral((tree as UnaryExpressionTree).operand())
        }

        fun isNegation(tree: Tree): Boolean {
            return tree is UnaryExpressionTree && tree.operator() == UnaryExpressionTree.Operator.NEGATE
        }

        fun isBinaryOperation(tree: Tree, operator: BinaryExpressionTree.Operator): Boolean {
            return tree is BinaryExpressionTree && tree.operator() == operator
        }

        fun isLogicalBinaryExpression(tree: Tree): Boolean {
            return isBinaryOperation(tree, CONDITIONAL_AND) || isBinaryOperation(tree, CONDITIONAL_OR)
        }

        fun skipParentheses(tree: Tree): Tree {
            var result = tree
            while (result is ParenthesizedExpressionTree) {
                result = result.expression()
            }
            return result
        }

        fun containsPlaceHolder(tree: Tree): Boolean {
            return tree.descendants().anyMatch { t -> t is PlaceHolderTree }
        }

        fun isTernaryOperator(ancestors: Deque<Tree>, tree: Tree): Boolean {
            if (!isIfWithElse(tree)) {
                return false
            }
            var child = tree
            for (ancestor in ancestors) {
                if (ancestor is BlockTree || ancestor is ExceptionHandlingTree || ancestor is TopLevelTree ||
                    isBranchOfLoopOrCaseOrIfWithoutElse(ancestor, child)
                ) {
                    break
                }
                if (!isBranchOfIf(ancestor, child)) {
                    return tree.descendants().noneMatch { BlockTree::class.java.isInstance(it) }
                }
                child = ancestor
            }
            return false
        }

        private fun isIfWithElse(tree: Tree): Boolean {
            return tree is IfTree && tree.elseBranch() != null
        }

        private fun isBranchOfLoopOrCaseOrIfWithoutElse(parent: Tree, child: Tree): Boolean {
            return parent is LoopTree && child === parent.body() ||
                    parent is MatchCaseTree && child === parent.body() ||
                    isBranchOfIf(parent, child) && (parent as IfTree).elseBranch() == null
        }

        private fun isBranchOfIf(parent: Tree, child: Tree): Boolean {
            return if (parent is IfTree) {
                child === parent.thenBranch() || child === parent.elseBranch()
            } else false
        }
    }

}
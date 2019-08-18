package com.zhenai.lib.checks.complexity

import com.zhenai.lib.checks.api.CheckContext
import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.checks.utils.ExpressionUtils
import com.zhenai.lib.checks.utils.ExpressionUtils.Companion.isLogicalBinaryExpression
import com.zhenai.lib.core.slang.api.*
import java.util.function.BiConsumer

class TooComplexExpressionCheck:ICheck{
    private val DEFAULT_MAX_COMPLEXITY = 3
    private val sIssue = SIssue.SIssueBuilder().issueId("TooComplexExpressionCheck")
        .name("表达式不能过于复杂，默认最大值是$DEFAULT_MAX_COMPLEXITY")
        .des("表达式不能过于复杂，默认最大值是$DEFAULT_MAX_COMPLEXITY").build()

    override fun initialize(init: InitContext) {
        init.register(BinaryExpressionTree::class.java, BiConsumer{ ctx, tree ->
            if (isParentExpression(ctx)) {
                val complexity = computeExpressionComplexity(tree)
                if (complexity > DEFAULT_MAX_COMPLEXITY) {
                    val message = String.format(
                        "Reduce the number of conditional operators (%s) used in the expression (maximum allowed %s).",
                        complexity,
                        DEFAULT_MAX_COMPLEXITY
                    )
                    val gap = complexity.toDouble() - DEFAULT_MAX_COMPLEXITY
                    ctx.reportIssue(tree, sIssue)
                }
            }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun isParentExpression(ctx: CheckContext): Boolean {
        val iterator = ctx.ancestors().iterator()
        while (iterator.hasNext()) {
            val parentExpression = iterator.next()
            if (parentExpression is BinaryExpressionTree) {
                return false
            } else if (parentExpression !is UnaryExpressionTree || parentExpression !is ParenthesizedExpressionTree) {
                return true
            }
        }
        return true
    }

    private fun computeExpressionComplexity(originalTree: Tree): Int {
        val tree = ExpressionUtils.skipParentheses(originalTree)
        if (tree is BinaryExpressionTree) {
            val complexity = if (isLogicalBinaryExpression(tree)) 1 else 0
            val binary = tree as BinaryExpressionTree
            return (complexity
                    + computeExpressionComplexity(binary.leftOperand())
                    + computeExpressionComplexity(binary.rightOperand()))
        } else return if (tree is UnaryExpressionTree) {
            computeExpressionComplexity((tree as UnaryExpressionTree).operand())
        } else {
            0
        }
    }
}
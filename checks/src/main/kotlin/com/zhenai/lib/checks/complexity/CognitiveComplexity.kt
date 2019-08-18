package com.zhenai.lib.checks.complexity


import com.zhenai.lib.checks.utils.ExpressionUtils
import com.zhenai.lib.checks.utils.ExpressionUtils.Companion.isLogicalBinaryExpression
import java.util.ArrayList
import java.util.HashSet
import com.zhenai.lib.core.slang.api.BinaryExpressionTree
import com.zhenai.lib.core.slang.api.CatchTree
import com.zhenai.lib.core.slang.api.ClassDeclarationTree
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree
import com.zhenai.lib.core.slang.api.IfTree
import com.zhenai.lib.core.slang.api.LoopTree
import com.zhenai.lib.core.slang.api.MatchTree
import com.zhenai.lib.core.slang.api.Token
import com.zhenai.lib.core.slang.api.Tree
import com.zhenai.lib.core.slang.impl.JumpTreeImpl
import com.zhenai.lib.core.slang.visitors.TreeContext
import com.zhenai.lib.core.slang.visitors.TreeVisitor

class CognitiveComplexity(root: Tree) {

    private val increments = ArrayList<Increment>()

    init {
        val visitor = CognitiveComplexityVisitor()
        visitor.scan(TreeContext(), root)
    }

    fun value(): Int {
        var total = 0
        for (increment in increments) {
            total += increment.nestingLevel + 1
        }
        return total
    }

    fun increments(): List<Increment> {
        return increments
    }

    class Increment constructor(val token: Token, val nestingLevel: Int) {

        fun token(): Token {
            return token
        }

        fun nestingLevel(): Int {
            return nestingLevel
        }
    }

    private inner class CognitiveComplexityVisitor : TreeVisitor<TreeContext>() {

        private val alreadyConsideredOperators = HashSet<Token>()

        init {

            // TODO ternary operator
            // TODO "break" or "continue" with label

            register(LoopTree::class.java) { ctx, tree -> incrementWithNesting(tree.keyword(), ctx) }
            register(MatchTree::class.java) { ctx, tree -> incrementWithNesting(tree.keyword(), ctx) }
            register(CatchTree::class.java) { ctx, tree -> incrementWithNesting(tree.keyword(), ctx) }
            register(JumpTreeImpl::class.java) { ctx, tree ->
                if (tree.label() != null) {
                    incrementWithoutNesting(tree.keyword())
                }
            }

            register(IfTree::class.java) { ctx, tree ->
                val parent = ctx.ancestors().peek()
                val isElseIf = parent is IfTree && tree === parent.elseBranch()
                val isTernary = ExpressionUtils.isTernaryOperator(ctx.ancestors(), tree)
                if (!isElseIf || isTernary) {
                    incrementWithNesting(tree.ifKeyword(), ctx)
                }
                val elseKeyword = tree.elseKeyword()
                if (elseKeyword != null && !isTernary) {
                    incrementWithoutNesting(elseKeyword)
                }
            }

            register(BinaryExpressionTree::class.java) { ctx, tree -> handleBinaryExpressions(tree) }
        }

        private fun handleBinaryExpressions(tree: BinaryExpressionTree) {
            if (!isLogicalBinaryExpression(tree) || alreadyConsideredOperators.contains(tree.operatorToken())) {
                return
            }

            val operators = ArrayList<Token>()
            flattenOperators(tree, operators)

            var previous: Token? = null
            for (operator in operators) {
                if (previous == null || previous.text() != operator.text()) {
                    incrementWithoutNesting(operator)
                }
                previous = operator
                alreadyConsideredOperators.add(operator)
            }
        }

        // TODO parentheses should probably be skipped
        private fun flattenOperators(tree: BinaryExpressionTree, operators: MutableList<Token>) {
            if (isLogicalBinaryExpression(tree.leftOperand())) {
                flattenOperators(tree.leftOperand() as BinaryExpressionTree, operators)
            }

            operators.add(tree.operatorToken())

            if (isLogicalBinaryExpression(tree.rightOperand())) {
                flattenOperators(tree.rightOperand() as BinaryExpressionTree, operators)
            }
        }

        private fun incrementWithNesting(token: Token, ctx: TreeContext) {
            increment(token, nestingLevel(ctx))
        }

        private fun incrementWithoutNesting(token: Token) {
            increment(token, 0)
        }

        private fun increment(token: Token, nestingLevel: Int) {
            increments.add(Increment(token, nestingLevel))
        }

        private fun nestingLevel(ctx: TreeContext): Int {
            var nestingLevel = 0
            var isInsideFunction = false
            val ancestors = ctx.ancestors().descendingIterator()
            var parent: Tree? = null
            while (ancestors.hasNext()) {
                val t = ancestors.next()
                if (t is FunctionDeclarationTree) {
                    if (isInsideFunction || nestingLevel > 0) {
                        nestingLevel++
                    }
                    isInsideFunction = true
                } else if (t is IfTree && !isElseIfBranch(
                        parent,
                        t
                    ) || t is MatchTree || t is LoopTree || t is CatchTree
                ) {
                    nestingLevel++
                } else if (t is ClassDeclarationTree) {
                    nestingLevel = 0
                    isInsideFunction = false
                }
                parent = t
            }
            return nestingLevel
        }

        private fun isElseIfBranch(parent: Tree?, t: Tree): Boolean {
            return parent is IfTree && parent.elseBranch() === t
        }

    }

}
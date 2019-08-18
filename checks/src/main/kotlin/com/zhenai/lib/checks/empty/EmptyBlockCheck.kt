package com.zhenai.lib.checks.empty

import com.zhenai.lib.checks.api.CheckContext
import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.*
import java.util.function.BiConsumer

class EmptyBlockCheck : ICheck {
    private val sIssue = SIssue.SIssueBuilder()
        .issueId("EmptyBlockCheck")
        .name("实现或者移除空代码块")
        .des("实现或者移除空代码块").build()

    override fun initialize(init: InitContext) {
        init.register(BlockTree::class.java, BiConsumer { ctx, blockTree ->
            val parent = ctx.parent()
            if (isValidBlock(parent) && blockTree.statementOrExpressions().isEmpty()) {
                checkComments(ctx, blockTree)
            }
        })

        init.register(MatchTree::class.java, BiConsumer { ctx, matchTree ->
            if (matchTree.cases().isEmpty()) {
                checkComments(ctx, matchTree)
            }
        })

    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun isValidBlock(parent: Tree?): Boolean {
        return (parent !is FunctionDeclarationTree
                && parent !is NativeTree
                && !isWhileLoop(parent))
    }

    private fun isWhileLoop(parent: Tree?): Boolean {
        return parent is LoopTree && parent.kind() == LoopTree.LoopKind.WHILE
    }

    private fun checkComments(ctx: CheckContext, tree: Tree) {
        if (tree.metaData().commentsInside().isEmpty()) {
            ctx.reportIssue(tree, sIssue)
        }
    }
}
package com.zhenai.lib.checks.empty

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.checks.utils.FunctionUtils
import com.zhenai.lib.core.slang.api.BlockTree
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.api.TreeMetaData
import java.util.function.BiConsumer

class EmptyFunctionCheck:ICheck{
    private val sIssue = SIssue.SIssueBuilder()
        .name("空方法检查")
        .des("警告：方法为空而且没有任何注释")
        .issueId("EmptyFunctionCheck").build()

    override fun initialize(init: InitContext) {
        init.register(FunctionDeclarationTree::class.java, BiConsumer{ ctx, tree ->
            val body = tree.body()
            if (!FunctionUtils.isOverrideMethod(tree) && !tree.isConstructor() && body != null && body!!.statementOrExpressions().isEmpty() && !hasComment(
                    body!!,
                    ctx.parent()!!.metaData()
                )
            ) {
                ctx.reportIssue(body!!, sIssue)
            }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun hasComment(body: BlockTree, parentMetaData: TreeMetaData): Boolean {
        if (!body.metaData().commentsInside().isEmpty()) {
            return true
        }

        val emptyBodyEndLine = body.textRange().end().line()
        return parentMetaData.commentsInside().stream()
            .anyMatch { comment -> comment.contentRange().start().line() == emptyBodyEndLine }
    }
}
package com.zhenai.lib.checks.comments

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.impl.FunctionDeclarationTreeImpl
import java.util.function.BiConsumer

class InterfaceMethodCommentCheck : ICheck {
    private val sIssue = SIssue.SIssueBuilder()
        .issueId("InterfaceMethodCommentCheck")
        .name("接口或者抽象方法需要添加说明")
        .des("接口或者抽象方法需要添加comment").build()

    override fun initialize(init: InitContext) {
        init.register(FunctionDeclarationTree::class.java, BiConsumer { ctx, tree ->
            if (!tree.isConstructor() && tree.body() == null && tree.metaData().commentsInside().isEmpty()) {
                if (tree is FunctionDeclarationTreeImpl && tree.isLiteral) {
                    return@BiConsumer
                }
                ctx.reportIssue(tree, sIssue)
            }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }
}
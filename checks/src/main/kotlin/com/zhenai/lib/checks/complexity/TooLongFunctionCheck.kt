package com.zhenai.lib.checks.complexity

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree
import com.zhenai.lib.core.slang.api.SIssue
import java.util.function.BiConsumer

class TooLongFunctionCheck : ICheck {
    private val DEFAULT_MAX = 100
    private val DEFAULT_MAX_VALUE = "" + DEFAULT_MAX

    private val sIssue = SIssue.SIssueBuilder().issueId("TooLongFunctionCheck")
        .name("方法不应过长，最大行数默认是$DEFAULT_MAX_VALUE")
        .des("方法不应过长，最大行数默认是$DEFAULT_MAX_VALUE").build()
    var max = DEFAULT_MAX

    override fun initialize(init: InitContext) {
        init.register(FunctionDeclarationTree::class.java, BiConsumer { ctx, tree ->
            val numberOfLinesOfCode = tree.metaData().linesOfCode().size
            if (numberOfLinesOfCode > max) {
                ctx.reportIssue(tree.rangeToHighlight(), sIssue)
            }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }
}
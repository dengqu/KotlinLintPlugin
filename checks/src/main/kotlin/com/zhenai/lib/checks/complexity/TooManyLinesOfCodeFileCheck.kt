package com.zhenai.lib.checks.complexity

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.api.TopLevelTree
import java.util.function.BiConsumer

class TooManyLinesOfCodeFileCheck : ICheck {
    private val DEFAULT_MAX = 1000
    private val DEFAULT_MAX_VALUE = "" + DEFAULT_MAX
    private val sIssue = SIssue.SIssueBuilder().issueId("TooManyLinesOfCodeFileCheck")
        .name("类不应过大，最大行数默认是$DEFAULT_MAX_VALUE")
        .des("类不应过大，最大行数默认是$DEFAULT_MAX_VALUE").build()
    var max = DEFAULT_MAX

    override fun initialize(init: InitContext) {
        init.register(TopLevelTree::class.java, BiConsumer { ctx, tree ->
            val numberOfLinesOfCode = tree.metaData().linesOfCode().size
            if (numberOfLinesOfCode > max) {
                ctx.reportIssue(tree.firstCpdToken()!!, sIssue)
            }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }
}
package com.zhenai.lib.checks.empty

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.api.TopLevelTree
import java.util.function.BiConsumer
import java.util.regex.Pattern

class EmptyCommentCheck : ICheck {
    private val EMPTY_CONTENT_PATTERN = Pattern.compile("[/*\\n\\r\\s]+(@.*)?", Pattern.DOTALL)

    private val sIssue = SIssue.SIssueBuilder()
        .issueId("EmptyCommentCheck")
        .name("空注释或者缺少说明检查")
        .des("请移除空的注释或者添加为注释添加说明").build()

    override fun initialize(init: InitContext) {
        init.register(TopLevelTree::class.java, BiConsumer { ctx, tree ->
            tree.allComments().stream()
                .filter { comment ->
                    EMPTY_CONTENT_PATTERN.matcher(comment.contentText().trim()).matches() || comment.contentText().trim().isEmpty()
                }
                .forEach { comment -> ctx.reportIssue(comment, sIssue) }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

}
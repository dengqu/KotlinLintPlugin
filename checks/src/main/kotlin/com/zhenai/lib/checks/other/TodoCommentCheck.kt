package com.zhenai.lib.checks.other

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.api.TopLevelTree
import java.util.function.BiConsumer
import java.util.regex.Pattern

class TodoCommentCheck:ICheck{
    private val sIssue = SIssue.SIssueBuilder().issueId("TodoCommentCheck")
        .name("todo tag 检查")
        .des("完成 TODO comment.").build()
    private val todoPattern = Pattern.compile("(?i)(^|[[^\\p{L}]&&\\D])(todo)($|[[^\\p{L}]&&\\D])")

    override fun initialize(init: InitContext) {
        init.register(TopLevelTree::class.java, BiConsumer{ ctx, tree ->
            tree.allComments().forEach { comment ->
                val matcher = todoPattern.matcher(comment.text())
                if (matcher.find()) {
                    ctx.reportIssue(comment.textRange(), sIssue)
                }
            }
        }
        )
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }
}
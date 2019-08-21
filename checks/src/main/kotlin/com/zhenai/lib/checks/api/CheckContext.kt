package com.zhenai.lib.checks.api

import com.zhenai.lib.core.slang.api.HasTextRange
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.api.TextRange
import com.zhenai.lib.core.slang.api.Tree
import java.util.*

/**
 * 规则检查上下文相关类
 */
interface CheckContext {
    fun ancestors(): Deque<Tree>

    fun parent(): Tree? {
        return if (this.ancestors().isEmpty()) {
            null
        } else {
            this.ancestors().peek()
        }
    }

    fun filename(): String

    fun fileContent(): String

    fun reportIssue(textRange: TextRange, issue: SIssue)

    fun reportIssue(textRange: HasTextRange, issue: SIssue)
}
package com.zhenai.lib.checks.comments

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.*
import com.zhenai.lib.core.slang.impl.TextRanges
import java.util.ArrayList
import java.util.function.BiConsumer
import java.util.stream.Collectors

class CommentedCodeCheck : ICheck {
    private val sIssue = SIssue.SIssueBuilder()
        .issueId("CommentedCodeCheck")
        .name("请移除注释的代码")
        .des("请移除注释的代码").build()
    private var codeVerifier: CodeVerifier? = null

    fun setCodeVerifier(codeVerifier: CodeVerifier) {
        this.codeVerifier = codeVerifier
    }

    override fun initialize(init: InitContext) {
        init.register(TopLevelTree::class.java, BiConsumer { ctx, tree ->
            val groupedComments = groupComments(tree.allComments())
            groupedComments.forEach { comments ->
                val content = comments.stream()
                    .map {
                        it.contentText()
                    }
                    .collect(Collectors.joining("\n"))
                if (codeVerifier != null && codeVerifier!!.containsCode(content)) {
                    val textRanges = comments.stream()
                        .map { it.textRange() }
                        .collect(Collectors.toList())
                    ctx.reportIssue(TextRanges.merge(textRanges), sIssue)
                }
            }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun groupComments(comments: List<Comment>): List<List<Comment>> {
        val groups = ArrayList<List<Comment>>()
        var currentGroup: MutableList<Comment>? = null
        for (comment in comments) {
            if (currentGroup == null) {
                currentGroup = initNewGroup(comment)
            } else if (areAdjacent(currentGroup[currentGroup.size - 1], comment)) {
                currentGroup.add(comment)
            } else {
                groups.add(currentGroup)
                currentGroup = initNewGroup(comment)
            }
        }
        if (currentGroup != null) {
            groups.add(currentGroup)
        }
        return groups
    }

    private fun initNewGroup(comment: Comment): MutableList<Comment> {
        val group = ArrayList<Comment>()
        group.add(comment)
        return group
    }

    private fun areAdjacent(commentA: Comment, commentB: Comment): Boolean {
        return commentA.textRange().start().line() + 1 == commentB.textRange().start().line()
    }
}
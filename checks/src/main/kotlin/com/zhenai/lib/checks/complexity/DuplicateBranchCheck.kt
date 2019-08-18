package com.zhenai.lib.checks.complexity

import com.zhenai.lib.checks.api.CheckContext
import com.zhenai.lib.core.slang.api.BlockTree
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.api.Tree
import com.zhenai.lib.core.slang.utils.SyntacticEquivalence

class DuplicateBranchCheck : AbstractBranchDuplicationCheck() {
    private var sIssue = SIssue.SIssueBuilder()
        .issueId("DuplicateBranchCheck")
        .name("分支块与块相同代码")
        .des("该分支块与块相同代码，请抽离").build()

    override fun checkDuplicatedBranches(ctx: CheckContext, branches: List<Tree>) {
        for (group in SyntacticEquivalence.findDuplicatedGroups(branches)) {
            val original = group[0]
            group.stream().skip(1)
                .filter { spansMultipleLines(it) }
                .forEach { duplicated ->
                    val originalRange = original.metaData().textRange()
                    sIssue = SIssue.SIssueBuilder()
                        .issueId("DuplicateBranchCheck")
                        .name("分支块与块相同代码")
                        .des("T该分支的代码块与" + originalRange.start().line() + "行代码块相同 ").build()
                    ctx.reportIssue(
                        duplicated,
                        sIssue
                    )
                }
        }

    }

    override fun onAllIdenticalBranches(ctx: CheckContext, tree: Tree) {
        // handled by S3923
    }

    private fun spansMultipleLines(tree: Tree?): Boolean {
        if (tree == null) {
            return false
        }
        if (tree is BlockTree) {
            val block = tree as BlockTree?
            val statements = block!!.statementOrExpressions()
            if (statements.isEmpty()) {
                return false
            }
            val firstStatement = statements[0]
            val lastStatement = statements[statements.size - 1]
            return firstStatement.metaData().textRange().start().line() != lastStatement.metaData().textRange().end().line()
        }
        val range = tree.metaData().textRange()
        return range.start().line() < range.end().line()
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }
}
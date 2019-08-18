package com.zhenai.lib.checks.complexity

import com.zhenai.lib.checks.api.CheckContext
import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.IfTree
import com.zhenai.lib.core.slang.api.MatchTree
import com.zhenai.lib.core.slang.api.Tree
import com.zhenai.lib.core.slang.utils.SyntacticEquivalence.areEquivalent
import java.util.ArrayList
import java.util.function.BiConsumer

abstract class AbstractBranchDuplicationCheck : ICheck {
    protected abstract fun checkDuplicatedBranches(ctx: CheckContext, branches: List<Tree>)

    protected abstract fun onAllIdenticalBranches(ctx: CheckContext, tree: Tree)

    override fun initialize(init: InitContext) {
        init.register(IfTree::class.java, BiConsumer { ctx, tree ->
            val parent = ctx.parent()
            if (parent !is IfTree || tree === (parent as IfTree).thenBranch()) {
                checkConditionalStructure(ctx, tree, ConditionalStructure(tree))
            }
        })
        init.register(
            MatchTree::class.java,
            BiConsumer { ctx, tree -> checkConditionalStructure(ctx, tree, ConditionalStructure(tree)) }
        )
    }

    private fun checkConditionalStructure(ctx: CheckContext, tree: Tree, conditional: ConditionalStructure) {
        if (conditional.allBranchesArePresent && conditional.allBranchesAreIdentical()) {
            onAllIdenticalBranches(ctx, tree)
        } else {
            checkDuplicatedBranches(ctx, conditional.branches)
        }
    }

    protected class ConditionalStructure {

        var allBranchesArePresent = false

        val branches = ArrayList<Tree>()

        constructor(ifTree: IfTree) {
            branches.add(ifTree.thenBranch())
            var elseBranch = ifTree.elseBranch()
            while (elseBranch != null) {
                if (elseBranch is IfTree) {
                    val elseIf = elseBranch as IfTree?
                    branches.add(elseIf!!.thenBranch())
                    elseBranch = elseIf.elseBranch()
                } else {
                    branches.add(elseBranch)
                    allBranchesArePresent = true
                    elseBranch = null
                }
            }
        }

        constructor(tree: MatchTree) {
            for (caseTree in tree.cases()) {
                branches.add(caseTree.body()!!)
                if (caseTree.expression() == null) {
                    allBranchesArePresent = true
                }
            }
        }

        fun allBranchesAreIdentical(): Boolean {
            return branches.stream()
                .skip(1)
                .allMatch { branch -> areEquivalent(branches[0], branch) }
        }
    }
}
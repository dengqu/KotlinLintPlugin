package com.zhenai.lib.checks

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.*
import com.zhenai.lib.core.slang.utils.SyntacticEquivalence
import java.util.function.BiConsumer
import java.util.stream.Collectors

class UnusedLocalVariableCheck : ICheck {
    private val sIssue = SIssue.SIssueBuilder()
        .issueId("UnusedLocalVariableCheck")
        .name("无用变量检查")
        .des("请移除无用变量检查").build()

    override fun initialize(init: InitContext) {
        init.register(FunctionDeclarationTree::class.java, BiConsumer { ctx, functionDeclarationTree ->

            if (ctx.ancestors().stream().anyMatch { tree -> tree is FunctionDeclarationTree }) {
                return@BiConsumer
            }

            val variableIdentifiers = functionDeclarationTree.descendants()
                .filter { tree -> tree is VariableDeclarationTree }
                .map { VariableDeclarationTree::class.java.cast(it) }
                .map { it.identifier() }
                .collect(Collectors.toSet<IdentifierTree>())

            val identifierTrees = functionDeclarationTree.descendants()
                .filter { tree -> !variableIdentifiers.contains(tree) }
                .collect(Collectors.toSet<Tree>())

            variableIdentifiers.stream()
                .filter { `var` ->
                    identifierTrees.stream()
                        .noneMatch { identifier -> SyntacticEquivalence.areEquivalent(`var`, identifier) }
                }
                .forEach { identifier -> ctx.reportIssue(identifier, sIssue) }

        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }
}
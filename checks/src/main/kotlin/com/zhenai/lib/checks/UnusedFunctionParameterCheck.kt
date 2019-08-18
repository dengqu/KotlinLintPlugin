package com.zhenai.lib.checks

import com.zhenai.lib.checks.api.CheckContext
import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.checks.utils.FunctionUtils.Companion.isOverrideMethod
import com.zhenai.lib.checks.utils.FunctionUtils.Companion.isPrivateMethod
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree
import com.zhenai.lib.core.slang.api.ParameterTree
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.impl.TopLevelTreeImpl
import com.zhenai.lib.core.slang.utils.SyntacticEquivalence.areEquivalent
import java.util.function.BiConsumer
import java.util.regex.Pattern
import java.util.stream.Collectors

class UnusedFunctionParameterCheck : ICheck {
    private val sIssue = SIssue.SIssueBuilder()
        .name("无用方法参数检查")
        .issueId("UnusedFunctionParameterCheck")
        .des("请移除无用的方法参数").build()
    private val IGNORED_PATTERN = Pattern.compile("main", Pattern.CASE_INSENSITIVE)

    override fun initialize(init: InitContext) {
        init.register(FunctionDeclarationTree::class.java!!, BiConsumer { ctx, functionDeclarationTree ->
            if (functionDeclarationTree.isConstructor() || shouldBeIgnored(ctx, functionDeclarationTree)) {
                return@BiConsumer
            }
            val unusedParameters = functionDeclarationTree.formalParameters().stream()
                .filter { ParameterTree::class.java.isInstance(it) }
                .map { ParameterTree::class.java.cast(it) }
                .filter { parameterTree ->
                    parameterTree.modifiers().isEmpty() && functionDeclarationTree.descendants()
                        .noneMatch { tree ->
                            tree != parameterTree.identifier() && areEquivalent(
                                tree,
                                parameterTree.identifier()
                            )
                        }
                }
                .collect(Collectors.toList<ParameterTree>())

            if (unusedParameters.isEmpty()) {
                return@BiConsumer
            }
            val firstUnused = unusedParameters.get(0).identifier()
            ctx.reportIssue(firstUnused, sIssue)
        })

    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun shouldBeIgnored(ctx: CheckContext, tree: FunctionDeclarationTree): Boolean {
        val name = tree.name()
        val validFunctionForRule =
            ctx.parent() is TopLevelTreeImpl || (isPrivateMethod(tree) && !isOverrideMethod(tree))
        return (!validFunctionForRule
                || tree.body() == null
                || (name != null && IGNORED_PATTERN.matcher(name!!.name()).matches()))
    }
}
package com.zhenai.lib.checks.other

import com.zhenai.lib.checks.api.CheckContext
import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.*
import java.util.function.BiConsumer
import java.util.regex.Pattern

class VariableAndParameterNameCheck:ICheck{
    private val DEFAULT_FORMAT = "^[_a-z][a-zA-Z0-9]*$"

    private val sIssue = SIssue.SIssueBuilder()
        .issueId("VariableAndParameterNameCheck")
        .name("局部变量和函数参数名称应符合命名约定")
        .des("局部变量和函数参数名称应符合命名约定").build()

    var format = DEFAULT_FORMAT

    override fun initialize(init: InitContext) {
        val pattern = Pattern.compile(format)

        init.register(VariableDeclarationTree::class.java, BiConsumer{ ctx, tree ->
            if (ctx.ancestors().stream().anyMatch { FunctionDeclarationTree::class.java.isInstance(it) }) {
                check(pattern, ctx, tree.identifier(), "local variable")
            }
        })

        init.register(FunctionDeclarationTree::class.java, BiConsumer{ ctx, tree ->
            tree.formalParameters().stream()
                .filter { ParameterTree::class.java.isInstance(it) }
                .map { ParameterTree::class.java.cast(it) }
                .forEach { param -> check(pattern, ctx, param.identifier(), "parameter") }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun check(pattern: Pattern, ctx: CheckContext, identifier: IdentifierTree, variableKind: String) {
        if (!pattern.matcher(identifier.name()).matches()) {
            val message = String.format("请重新按照这个规则 %s 去重命名%s", this.format, variableKind)
            sIssue.des = message
            ctx.reportIssue(identifier, sIssue)
        }
    }
}
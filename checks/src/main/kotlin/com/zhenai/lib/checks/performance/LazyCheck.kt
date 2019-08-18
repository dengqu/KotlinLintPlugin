package com.zhenai.lib.checks.performance

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.converter.KotlinNativeKind
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.impl.NativeTreeImpl
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtValueArgumentList
import java.util.function.BiConsumer

class LazyCheck : ICheck {
    private val sIssue = SIssue.SIssueBuilder()
        .issueId("LazyCheck")
        .name("Lazy规范使用")
        .des("请为Lazy指定LazyThreadSafetyMode").build()

    override fun initialize(init: InitContext) {
        init.register(NativeTreeImpl::class.java, BiConsumer { ctx, tree ->
            if (tree != null && tree!!.nativeKind() != null && tree!!.nativeKind() is KotlinNativeKind) {
                val kotlinNativeKind = tree!!.nativeKind() as KotlinNativeKind
                if (kotlinNativeKind != null && kotlinNativeKind.originalObject is KtProperty) {
                    val ktProperty = kotlinNativeKind.originalObject as KtProperty
                    if (isValid(ktProperty.delegateExpression)) {
                        ctx.reportIssue(tree!!, sIssue)
                    }
                }
            }

        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun isValid(expression: KtExpression?): Boolean {
        if (expression == null) {
            return false
        }
        var isLazy = false
        var isSpeifyMode = false
        if (expression != null) {
            val psiElements = expression.children
            for (psiElement in psiElements) {
                if (psiElement is KtNameReferenceExpression) {
                    if ("lazy" == psiElement.getReferencedName()) {
                        isLazy = true
                    }
                } else if (psiElement is KtValueArgumentList) {
                    val valueArguments = psiElement.arguments
                    for (valueArgument in valueArguments) {
                        val argumentValue = valueArgument.getArgumentExpression()
                        if (argumentValue != null) {
                            if (argumentValue.text.contains("SYNCHRONIZED") ||
                                argumentValue.text.contains("PUBLICATION") ||
                                argumentValue.text.contains("NONE")
                            ) {
                                isSpeifyMode = true
                            }
                        }
                    }
                }
            }
            if (isLazy && !isSpeifyMode) {
                return true
            }

        }
        return false
    }
}
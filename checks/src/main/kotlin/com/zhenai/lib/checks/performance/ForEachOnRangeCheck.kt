package com.zhenai.lib.checks.performance

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.converter.KotlinNativeKind
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.impl.NativeTreeImpl
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getCallNameExpression
import org.jetbrains.kotlin.psi.psiUtil.getReceiverExpression
import java.util.function.BiConsumer

class ForEachOnRangeCheck : ICheck {
    private val sIssue = SIssue.SIssueBuilder()
        .issueId("ForEachOnRangeCheck")
        .name("在范围上使用forEach方法会产生很高的性能成本。更喜欢使用简单的for循环。")
        .des("基准测试表明，与简单的for循环相比，在范围上使用forEach可能会产生巨大的性能成本。因此，在大多数情况下，应该使用简单的for循环。在此处查看更多详细信息：https：//sites.google.com/a/athaydes.com/renato-athaydes/posts/kotlinshiddencosts-benchmarks要解决此CodeSmell，forEach用法应替换为for循环。").build()

    private val minimumRangeSize = 3
    private val rangeOperators = setOf("..", "downTo", "until", "step")

    override fun initialize(init: InitContext) {
        init.register(
            NativeTreeImpl::class.java,
            BiConsumer { checkContext, nativeTreeImpl ->
                if (nativeTreeImpl.nativeKind() is KotlinNativeKind) {
                    var kotlinNativeKind: KotlinNativeKind = nativeTreeImpl.nativeKind() as KotlinNativeKind
                    kotlinNativeKind.originalObject?.let { it ->
                        if (it is KtCallExpression) {
                            it.getCallNameExpression()?.let { it1 ->
                                if (!it1.textMatches("forEach")) {
                                    return@BiConsumer
                                }
                                val parenthesizedExpression =
                                    it1.getReceiverExpression() as? KtParenthesizedExpression
                                val binaryExpression = parenthesizedExpression?.expression as? KtBinaryExpression
                                if (binaryExpression != null && isRangeOperator(binaryExpression)) {
                                    checkContext.reportIssue(nativeTreeImpl, sIssue)
                                }
                            }

                        }
                    }

                }
            })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun isRangeOperator(binaryExpression: KtBinaryExpression): Boolean {
        val range = binaryExpression.children
        if (range.size >= minimumRangeSize) {
            val hasCorrectLowerValue = hasCorrectLowerValue(range[0])
            val hasCorrectUpperValue = getIntValueForPsiElement(range[2]) != null
            return hasCorrectLowerValue && hasCorrectUpperValue && rangeOperators.contains(range[1].text)
        }
        return false
    }

    private fun hasCorrectLowerValue(element: PsiElement): Boolean {
        var lowerValue = getIntValueForPsiElement(element) != null
        if (!lowerValue) {
            val expression = element as? KtBinaryExpression
            if (expression != null) {
                lowerValue = isRangeOperator(expression)
            }
        }
        return lowerValue
    }

    fun getIntValueForPsiElement(element: PsiElement): Int? {
        return (element as? KtConstantExpression)?.text?.toIntOrNull()
    }

}
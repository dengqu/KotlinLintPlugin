package com.zhenai.lib.checks.performance

import com.zhenai.lib.checks.api.CheckContext
import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.converter.KotlinNativeKind
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.api.Tree
import com.zhenai.lib.core.slang.impl.NativeTreeImpl
import com.zhenai.lib.core.slang.impl.ObjectDeclarationImpl
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import java.util.function.BiConsumer

class ObjectDeclarationCheck : ICheck {
    private val sIssue = SIssue.SIssueBuilder()
        .issueId("ObjectDeclarationCheck")
        .name("伴生对象 规范使用，val 常亮需要添加const或者@JvmStatic或者@JvmField来修饰，否则影响性能")
        .des("伴生对象 规范使用，val 常亮需要添加const或者@JvmStatic或者@JvmField来修饰，否则影响性能").build()

    override fun initialize(init: InitContext) {
        init.register(ObjectDeclarationImpl::class.java, BiConsumer { ctx, tree -> visit(tree, ctx) }
        )
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }


    private fun visit(tree: Tree?, checkContext: CheckContext) {
        if (tree == null) {
            return
        }

        if (tree is NativeTreeImpl) {
            if (tree.nativeKind() is KotlinNativeKind) {
                val kotlinNativeKind = tree.nativeKind() as KotlinNativeKind
                if (kotlinNativeKind != null && kotlinNativeKind.originalObject is KtProperty) {
                    val ktProperty = kotlinNativeKind.originalObject as KtProperty
                    val textRange = kotlinNativeKind.textRange
                    if (ktProperty != null) {
                        if (ktProperty.modifierList != null) {
                            val psiElements = ktProperty.modifierList!!.children
                            var isConst = false
                            var isJvmStatic = false
                            var isJvmField = false
                            if (psiElements != null && psiElements.size > 0) {
                                for (element in psiElements) {
                                    if (element.text == "const") {
                                        isConst = true
                                    }
                                    if (element.text == "@JvmStatic") {
                                        isJvmStatic = true
                                    }
                                    if (element.text == "@JvmField") {
                                        isJvmField = true
                                    }

                                }

                                if (isConst || isJvmStatic || isJvmField) {
                                    return
                                }
                                checkContext.reportIssue(textRange, sIssue)
                            }
                        } else {
                            checkContext.reportIssue(textRange, sIssue)
                        }
                    }
                }
            }
        }
        tree.children().forEach { child -> visit(child, checkContext) }
    }

    private fun isValidExpression(psiElement: PsiElement?): Boolean {
        if (psiElement == null) {
            return false
        }

        return if (psiElement is KtStringTemplateExpression || psiElement is KtConstantExpression) {
            true
        } else false

    }
}
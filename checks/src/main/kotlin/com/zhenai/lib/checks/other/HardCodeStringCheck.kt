package com.zhenai.lib.checks.other

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.converter.KotlinNativeKind
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.api.StringLiteralTree
import com.zhenai.lib.core.slang.impl.NativeTreeImpl
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import java.util.function.BiConsumer
import java.util.regex.Pattern

class HardCodeStringCheck:ICheck{
    private val sIssue = SIssue.SIssueBuilder()
        .name("不能在kotlin文件使用中文字符串硬编码")
        .issueId("HardCodeStringCheck")
        .des("不能在kotlin文件使用中文字符串硬编码").build()
    private val todoPattern = Pattern.compile("[\\u4e00-\\u9fa5]")

    override fun initialize(init: InitContext) {
        init.register(StringLiteralTree::class.java, BiConsumer{ ctx, tree ->
            val content = tree.content()
            if (todoPattern.matcher(content).find()) {
                ctx.reportIssue(tree, sIssue)
            }
        })


        init.register(NativeTreeImpl::class.java, BiConsumer{ ctx, tree ->
            if (tree.nativeKind() != null && tree.nativeKind() is KotlinNativeKind) {
                if ((tree.nativeKind() as KotlinNativeKind).psiElement.isAssignableFrom(KtLiteralStringTemplateEntry::class.java)) {
                    for (`object` in (tree.nativeKind() as KotlinNativeKind).differentiators) {
                        if (`object` is String) {
                            if (todoPattern.matcher(`object`.toString()).find()) {
                                ctx.reportIssue(tree, sIssue)
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
}
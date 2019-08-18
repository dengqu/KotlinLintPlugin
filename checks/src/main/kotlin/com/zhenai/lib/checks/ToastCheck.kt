package com.zhenai.lib.checks

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.NativeKind
import com.zhenai.lib.core.slang.api.NativeTree
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.impl.IdentifierTreeImpl
import com.zhenai.lib.core.slang.impl.NativeTreeImpl
import java.util.function.BiConsumer

class ToastCheck:ICheck{
    private val sIssue = SIssue.SIssueBuilder()
        .issueId("ToastCheck")
        .name("Toast规范使用")
        .des("请使用我们团队的Toast工具类").build()

    override fun initialize(init: InitContext) {
        init.register(NativeTree::class.java, BiConsumer{ ctx, tree ->
            if (isSystemToast(tree)) {
                ctx.reportIssue(tree, sIssue)
            }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun isSystemToast(tree: NativeTree?): Boolean {
        if (tree == null) {
            return false
        }

        if (tree.nativeKind() == null || tree.nativeKind() !is NativeKind) {
            return false
        }

        if (tree.children() == null || tree.children().size < 2) {
            return false
        }

        var nativeLog = false

        if (tree.children()[0] is IdentifierTreeImpl) {
            if ((tree.children()[0] as IdentifierTreeImpl).name() == "Toast") {
                nativeLog = true
            }
        }

        var nativeMethod = false

        if (tree.children()[1] is NativeTreeImpl) {
            if (tree.children()[1].children() != null && tree.children()[1].children().size >= 2 && tree.children()[1].children()[0] is IdentifierTreeImpl) {
                nativeMethod = (tree.children()[1].children()[0] as IdentifierTreeImpl).name() == "makeText"
            }
        }
        return nativeLog && nativeMethod
    }
}
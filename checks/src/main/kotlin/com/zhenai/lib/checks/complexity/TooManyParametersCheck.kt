package com.zhenai.lib.checks.complexity

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree
import com.zhenai.lib.core.slang.api.ModifierTree
import com.zhenai.lib.core.slang.api.SIssue
import java.util.function.BiConsumer

class TooManyParametersCheck : ICheck {
    /**
     * 最大参数个数
     */
    private val DEFAULT_MAX = 7
    var max = DEFAULT_MAX
    private val sIssue = SIssue.SIssueBuilder()
        .name("方法参数过多检查")
        .issueId("TooManyParametersCheck")
        .des("方法参数过多，超过" + DEFAULT_MAX + "个").build()

    override fun initialize(init: InitContext) {
        //注册监听的FunctionDeclarationTree树
        init.register(FunctionDeclarationTree::class.java, BiConsumer { ctx, tree ->
            if (!tree.isConstructor && !isOverrideMethod(tree) && tree.formalParameters().size > max) {
                //匹配到进行错误结果的上报
                if (tree.name() == null) {
                    ctx.reportIssue(tree, sIssue)
                } else {
                    ctx.reportIssue(tree.name()!!, sIssue)
                }
            }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    /**
     * 是否是重载方法
     *
     * @param tree
     * @return
     */
    private fun isOverrideMethod(tree: FunctionDeclarationTree): Boolean {
        return tree.modifiers().stream().anyMatch { mod ->
            if (mod !is ModifierTree) {
                return@anyMatch false
            }
            mod.kind() == ModifierTree.Kind.OVERRIDE
        }
    }
}
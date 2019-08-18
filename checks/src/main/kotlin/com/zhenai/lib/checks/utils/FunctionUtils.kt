package com.zhenai.lib.checks.utils

import com.zhenai.lib.core.slang.api.FunctionDeclarationTree
import com.zhenai.lib.core.slang.api.ModifierTree

import com.zhenai.lib.core.slang.api.ModifierTree.Kind.OVERRIDE
import com.zhenai.lib.core.slang.api.ModifierTree.Kind.PRIVATE

class FunctionUtils {
    companion object {
        fun isPrivateMethod(method: FunctionDeclarationTree): Boolean {
            return hasModifierMethod(method, PRIVATE)
        }

        fun isOverrideMethod(method: FunctionDeclarationTree): Boolean {
            return hasModifierMethod(method, OVERRIDE)
        }

        fun hasModifierMethod(method: FunctionDeclarationTree, kind: ModifierTree.Kind): Boolean {
            return method.modifiers().stream()
                .filter { ModifierTree::class.java.isInstance(it) }
                .map {
                    ModifierTree::class.java.cast(it)
                }
                .anyMatch { modifier -> modifier.kind() == kind }
        }
    }
}
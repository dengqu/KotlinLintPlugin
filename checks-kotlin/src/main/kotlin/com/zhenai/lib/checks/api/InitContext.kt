package com.zhenai.lib.checks.api

import com.zhenai.lib.core.slang.api.Tree
import java.util.function.BiConsumer

interface InitContext {
    fun <T : Tree> register(cls: Class<T>, visitor: BiConsumer<CheckContext, T>)
}
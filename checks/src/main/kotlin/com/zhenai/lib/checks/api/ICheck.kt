package com.zhenai.lib.checks.api

import com.zhenai.lib.core.slang.api.SIssue

/**
 * 规则统一接口
 */
interface ICheck {
    /**
     * 初始化
     *
     * @param init
     */
    fun initialize(init: InitContext)

    /**
     * 获取错误规则描述
     *
     * @return
     */
    fun getSIssue(): SIssue


}
package com.zhenai.lib.checks.api;

import com.zhenai.lib.core.slang.api.SIssue;

/**
 * 规则的统一接口
 */
public interface ICheck {

    /**
     * 初始化
     *
     * @param init
     */
    void initialize(InitContext init);

    /**
     * 获取错误规则描述
     *
     * @return
     */
    SIssue getSIssue();

}

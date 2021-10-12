/*
 * Copyright 1999-2017 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhenai.lib.idea.component

import com.zhenai.lib.idea.config.ZhenaiConfig
import com.zhenai.lib.idea.i18n.P3cBundle
import com.zhenai.lib.idea.util.HighlightInfoTypes
import com.zhenai.lib.idea.util.HighlightSeverities
import com.zhenai.lib.common.component.AliBaseApplicationComponent
import com.intellij.codeInsight.daemon.impl.SeverityRegistrar
import com.intellij.openapi.actionSystem.ActionManager

/**
 *
 *
 * @author caikang
 * @date 2017/06/19
 */
class CommonSettingsApplicationComponent(private val p3cConfig: ZhenaiConfig) : AliBaseApplicationComponent {
    override fun initComponent() {
        SeverityRegistrar.registerStandard(HighlightInfoTypes.BLOCKER, HighlightSeverities.BLOCKER)
        SeverityRegistrar.registerStandard(HighlightInfoTypes.CRITICAL, HighlightSeverities.CRITICAL)
        SeverityRegistrar.registerStandard(HighlightInfoTypes.MAJOR, HighlightSeverities.MAJOR)

        //val analyticsGroup = ActionManager.getInstance().getAction(analyticsGroupId)
    }

    companion object {
        val analyticsGroupId = "com.alibaba.p3c.analytics.action_group"
        val analyticsGroupText = "$analyticsGroupId.text"
    }
}

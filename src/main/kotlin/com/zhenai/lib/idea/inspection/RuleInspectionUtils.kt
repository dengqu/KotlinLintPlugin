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
package com.zhenai.lib.idea.inspection

import com.google.common.collect.ImmutableMap
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.openapi.diagnostic.Logger
import freemarker.template.Configuration
import java.util.regex.Pattern

/**
 * @author caikang
 * @date 2016/12/16
 */
object RuleInspectionUtils {

    private val logger = Logger.getInstance(RuleInspectionUtils::class.java)
    private val ruleSetFilePattern = Pattern.compile("(java|vm)/ali-.*?\\.xml")
    private val staticDescriptionTemplate = run {
        val cfg = Configuration(Configuration.VERSION_2_3_25)
        cfg.setClassForTemplateLoading(RuleInspectionUtils::class.java, "/tpl")
        cfg.defaultEncoding = "UTF-8"
        cfg.getTemplate("StaticDescriptionTemplate.ftl")
    }

    private val ruleSetsPrefix = "rulesets/"

    private val ruleStaticDescriptions: Map<String, String>
    private val ruleMessages: Map<String, String>
    private val displayLevelMap: Map<String, HighlightDisplayLevel>

    init {
        val builder = ImmutableMap.builder<String, String>()
        val messageBuilder = ImmutableMap.builder<String, String>()
        val displayLevelBuilder = ImmutableMap.builder<String, HighlightDisplayLevel>()
        ruleStaticDescriptions = builder.build()
        ruleMessages = messageBuilder.build()
        displayLevelMap = displayLevelBuilder.build()
    }

    fun getRuleStaticDescription(ruleName: String): String {
        return ruleStaticDescriptions[ruleName]!!
    }

    fun getHighlightDisplayLevel(ruleName: String): HighlightDisplayLevel {
        val level = displayLevelMap[ruleName]

        return level ?: HighlightDisplayLevel.WEAK_WARNING
    }

}

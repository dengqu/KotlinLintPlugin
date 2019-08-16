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

import com.zhenai.lib.idea.util.HighlightDisplayLevels
import com.zhenai.lib.idea.util.NumberConstants
import com.zhenai.lib.idea.util.QuickFixes
import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.Nls
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.checks.CheckList

/**
 * @author dengqu
 * @date 2016/12/16
 */
class ZhenaiKotlinInspection(private val ruleName: String) : LocalInspectionTool(),
    ZhenaiBaseInspection {
    override fun manualBuildFix(psiElement: PsiElement, isOnTheFly: Boolean): LocalQuickFix? {
        return QuickFixes.getQuickFix(ruleName, isOnTheFly)
    }

    private val staticDescription: String

    private val displayName: String


    private val defaultLevel: HighlightDisplayLevel

    private val rule: SIssue

    init {
        rule = CheckList.getSlangCheck(ruleName).sIssue
        displayName = rule.name
        staticDescription = rule.des
        defaultLevel = HighlightDisplayLevels.MAJOR
    }

    override fun runForWholeFile(): Boolean {
        return true
    }

    override fun checkFile(
        file: PsiFile, manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        if (file == null || !file.virtualFile.canonicalPath!!.endsWith(".kt")) {
            return null
        }
        return ZhenaiKotlinInspectionInvoker.invokeInspection(file, manager, rule, isOnTheFly)
    }

    override fun getStaticDescription(): String? {
        return staticDescription
    }

    override fun ruleName(): String {
        return ruleName
    }

    @Nls
    override fun getDisplayName(): String {
        return displayName
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return defaultLevel
    }

    @Nls
    override fun getGroupDisplayName(): String {
        return ZhenaiBaseInspection.GROUP_NAME1
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun isSuppressedFor(element: PsiElement): Boolean {
        return false
    }

    override fun getShortName(): String {

        var shortName = "Alibaba" + rule.issue_id
        val index = shortName.lastIndexOf("Rule")
        if (index > NumberConstants.INDEX_0) {
            shortName = shortName.substring(NumberConstants.INDEX_0, index)
        }
        return shortName
    }
}

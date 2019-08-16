package com.zhenai.lib.idea.inspection

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.Nls

/**
 *
 * @author dengqu
 * @date 2017/07/19
 */
class DelegateLocalInspectionTool : LocalInspectionTool(), ZhenaiBaseInspection {

    private val forJavassist: LocalInspectionTool? = null

    private val localInspectionTool: LocalInspectionTool

    init {
        localInspectionTool = forJavassist ?: throw IllegalStateException()
    }

    override fun runForWholeFile(): Boolean {
        return localInspectionTool.runForWholeFile()
    }

    override fun checkFile(
        file: PsiFile, manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        System.out.println("checkFile =" + file.virtualFile)
        return localInspectionTool.checkFile(file, manager, isOnTheFly)
    }

    override fun getStaticDescription(): String? {
        return localInspectionTool.staticDescription
    }

    override fun ruleName(): String {
        return (localInspectionTool as ZhenaiBaseInspection).ruleName()
    }

    @Nls
    override fun getDisplayName(): String {
        return localInspectionTool.displayName
    }

    override fun getDefaultLevel(): HighlightDisplayLevel {
        return localInspectionTool.defaultLevel
    }

    @Nls
    override fun getGroupDisplayName(): String {
        return ZhenaiBaseInspection.GROUP_NAME
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    override fun getShortName(): String {
        return localInspectionTool.shortName
    }

    override fun isSuppressedFor(element: PsiElement): Boolean {
        return false
    }

    override fun buildVisitor(
        holder: ProblemsHolder, isOnTheFly: Boolean,
        session: LocalInspectionToolSession
    ): PsiElementVisitor {
        if (!ZhenaiLocalInspectionToolProvider.javaShouldInspectChecker.shouldInspect(holder.file)) {
            return PsiElementVisitor.EMPTY_VISITOR
        }
        return localInspectionTool.buildVisitor(holder, isOnTheFly, session)
    }
}
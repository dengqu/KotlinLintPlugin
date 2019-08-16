package com.zhenai.lib.idea.inspection

import com.zhenai.lib.idea.config.ZhenaiConfig
import com.zhenai.lib.idea.util.DocumentUtils.calculateRealOffset
import com.zhenai.lib.idea.util.ProblemsUtils
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.checks.CheckList
import com.zhenai.lib.client.ChecksVisitor
import com.zhenai.lib.client.InputFileContext
import org.sonar.api.internal.google.common.collect.Lists
import java.util.concurrent.TimeUnit
import com.zhenai.lib.core.converter.KotlinConverter
/**
 * @author dengqu
 * @date 2016/12/13
 */
class ZhenaiKotlinInspectionInvoker(
    private val psiFile: PsiFile,
    private val manager: InspectionManager,
    private val rule: SIssue
) {
    val logger = Logger.getInstance(javaClass)
    private var problems: List<ChecksVisitor.Problem> = emptyList()

    fun doInvoke() {
        Thread.currentThread().contextClassLoader = javaClass.classLoader
        val start = System.currentTimeMillis()
        var tree = KotlinConverter().parse(psiFile?.text!!)
        var checksVisitor = ChecksVisitor(listOf(CheckList.getSlangCheck(rule.name)), null)
        checksVisitor.scan(InputFileContext(null, null), tree)
        problems = checksVisitor.problems
        logger.debug(
            "elapsed ${System.currentTimeMillis() - start}ms to" +
                    " to apply rule ${rule.name} for file ${psiFile.virtualFile.canonicalPath}"
        )
    }

    fun getRuleProblems(isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if (problems.isEmpty()) {
            return null
        }
        val problemDescriptors = Lists.newArrayList<ProblemDescriptor>()
        for (problem in problems) {
            val virtualFile = psiFile.virtualFile ?: continue
            val psiFile = PsiManager.getInstance(manager.project).findFile(virtualFile) ?: continue
            val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: continue
            val offsets = Offsets(
                calculateRealOffset(document, problem.textRange.start().line(), problem.textRange.start().lineOffset()),
                calculateRealOffset(document, problem.textRange.end().line(), problem.textRange.end().lineOffset())
            )
            val errorMessage = problem.issue.des
            val problemDescriptor = ProblemsUtils.createProblemDescriptorForPmdRule(
                psiFile,
                manager,
                isOnTheFly,
                problem.issue.des,
                errorMessage,
                offsets.start,
                offsets.end,
                problem.textRange.start().line()
            ) ?: continue
            problemDescriptors.add(problemDescriptor)
        }

        return problemDescriptors.toTypedArray()
    }

    companion object {
        private lateinit var invokers: Cache<FileRule, ZhenaiKotlinInspectionInvoker>

        val smartFoxConfig = ServiceManager.getService(ZhenaiConfig::class.java)!!

        init {
            reInitInvokers(smartFoxConfig.ruleCacheTime)
        }

        fun invokeInspection(
            psiFile: PsiFile?, manager: InspectionManager, rule: SIssue,
            isOnTheFly: Boolean
        ): Array<ProblemDescriptor>? {
            if (psiFile == null) {
                return null
            }
            val virtualFile = psiFile.virtualFile ?: return null
            val invoker = ZhenaiKotlinInspectionInvoker(psiFile, manager, rule)
            invoker.doInvoke()
            return invoker.getRuleProblems(isOnTheFly)
        }

        private fun doInvokeIfPresent(filePath: String, rule: String) {
            invokers.getIfPresent(FileRule(filePath, rule))?.doInvoke()
        }

        fun refreshFileViolationsCache(file: VirtualFile) {
            ZhenaiLocalInspectionToolProvider.ruleNames.forEach {
                doInvokeIfPresent(file.canonicalPath!!, it)
            }
        }

        fun reInitInvokers(expireTime: Long) {
            invokers = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(
                expireTime,
                TimeUnit.MILLISECONDS
            ).build<FileRule, ZhenaiKotlinInspectionInvoker>()!!
        }
    }

    data class FileRule(val filePath: String, val ruleName: String)
    data class Offsets(val start: Int, val end: Int)
}


package com.zhenai.lib.idea.inspection

import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiCompiledFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiImportList
import com.intellij.psi.PsiJavaFile
import javassist.CannotCompileException
import javassist.ClassClassPath
import javassist.ClassPool
import javassist.CtField
import javassist.NotFoundException
import com.zhenai.lib.checks.CheckList
import org.sonar.api.internal.google.common.collect.Lists
import javax.annotation.Generated

/**
 * @author dengqu
 * @date 2016/12/16
 */
class ZhenaiLocalInspectionToolProvider : InspectionToolProvider {

    override fun getInspectionClasses(): Array<Class<*>> {
        return CLASS_LIST.toTypedArray()
    }

    interface ShouldInspectChecker {
        /**
         * check inspect whether or not

         * @param file file to inspect
         * @return true or false
         */
        fun shouldInspect(file: PsiFile): Boolean
    }

    companion object {
        private val LOGGER = Logger.getInstance(ZhenaiLocalInspectionToolProvider::class.java)
        val ruleNames: MutableList<String> = Lists.newArrayList<String>()!!
        private val CLASS_LIST = Lists.newArrayList<Class<*>>()
        val javaShouldInspectChecker = object : ShouldInspectChecker {
            override fun shouldInspect(file: PsiFile): Boolean {
                val basicInspect = file is PsiJavaFile && file !is PsiCompiledFile
                if (!basicInspect) {
                    return false
                }

                if (!validScope(file)) {
                    return false
                }

                val importList = file.children.firstOrNull {
                    it is PsiImportList
                } as? PsiImportList ?: return true

                return !importList.allImportStatements.any {
                    it.text.contains(Generated::class.java.name)
                }
            }

            private fun validScope(file: PsiFile): Boolean {
                val virtualFile = file.virtualFile
                val index = ProjectRootManager.getInstance(file.project).fileIndex
                return index.isInSource(virtualFile)
                        && !index.isInTestSourceContent(virtualFile)
                        && !index.isInLibraryClasses(virtualFile)
                        && !index.isInLibrarySource(virtualFile)
            }
        }

        init {
            Thread.currentThread().contextClassLoader = ZhenaiLocalInspectionToolProvider::class.java.classLoader
            initKotlinInspection()
        }

        private fun initKotlinInspection() {
            val pool = ClassPool.getDefault()
            pool.insertClassPath(ClassClassPath(DelegateKotlinInspection::class.java))
            try {
                for (ruleInfo in CheckList.allChecks()) {
                    val cc = pool.get(DelegateKotlinInspection::class.java.name)
                    cc.name = ruleInfo.sIssue.issue_id + "Inspection"
                    val ctField = cc.getField("ruleName")
                    cc.removeField(ctField)
                    val value = "\"" + ruleInfo.sIssue.name + "\""
                    val newField = CtField.make("private String ruleName = $value;", cc)
                    cc.addField(newField, value)
                    CLASS_LIST.add(cc.toClass())
                }
            } catch (e: NotFoundException) {
                LOGGER.error(e)
            } catch (e: CannotCompileException) {
                LOGGER.error(e)
            }
        }
    }
}

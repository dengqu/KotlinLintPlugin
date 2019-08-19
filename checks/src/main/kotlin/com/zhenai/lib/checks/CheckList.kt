package com.zhenai.lib.checks

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.comments.CommentedCodeCheck
import com.zhenai.lib.checks.comments.EmptyCommentCheck
import com.zhenai.lib.checks.comments.InterfaceMethodCommentCheck
import com.zhenai.lib.checks.complexity.*
import com.zhenai.lib.checks.empty.EmptyBlockCheck
import com.zhenai.lib.checks.empty.EmptyFunctionCheck
import com.zhenai.lib.checks.other.*
import com.zhenai.lib.checks.performance.ArrayPrimitiveCheck
import com.zhenai.lib.checks.performance.LazyCheck
import com.zhenai.lib.checks.performance.ObjectDeclarationCheck
import com.zhenai.lib.checks.unuse.UnusedFunctionParameterCheck
import com.zhenai.lib.checks.unuse.UnusedLocalVariableCheck
import com.zhenai.lib.checks.unuse.UnusedPrivateMethodCheck
import java.util.*

class CheckList {

    private constructor()

    companion object {

        fun allChecks(): List<ICheck> {
            return Arrays.asList(
                TodoCommentCheck(),
                TooManyParametersCheck(),
                UnusedFunctionParameterCheck(),
                HardCodeStringCheck(),
                UseLogCheck(),
                ToastCheck(),
                EmptyCommentCheck(),
                InterfaceMethodCommentCheck(),
                UnusedPrivateMethodCheck(),
                UnusedLocalVariableCheck(),
                ObjectDeclarationCheck(),
                TooComplexExpressionCheck(),
                TooLongFunctionCheck(),
                TooManyLinesOfCodeFileCheck(),
                CommentedCodeCheck(),
                EmptyBlockCheck(),
                DuplicateBranchCheck(),
                LazyCheck(),
                VariableAndParameterNameCheck(),
                ArrayPrimitiveCheck(),
                EmptyFunctionCheck()
            )
        }

        fun getSlangCheck(name: String): ICheck? {
            for (check in allChecks()) {
                if (check.getSIssue().name == name) {
                    return check
                }
            }
            return null
        }
    }

}
package com.zhenai.lib.checks

import com.zhenai.lib.checks.api.CheckContext
import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.core.converter.KotlinNativeKind
import com.zhenai.lib.core.slang.api.*
import com.zhenai.lib.core.slang.impl.IdentifierTreeImpl
import com.zhenai.lib.core.slang.impl.NativeTreeImpl
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import java.util.function.BiConsumer

class ArrayPrimitiveCheck : ICheck {
    private val sIssue = SIssue.SIssueBuilder()
        .issueId("ArrayPrimitiveCheck")
        .name("ArrayPrimitiveCheck")
        .des("ArrayPrimitiveCheck").build()

    private val primitiveTypes = hashSetOf(
        "Int",
        "Double",
        "Float",
        "Short",
        "Byte",
        "Long",
        "Char"
    )

    override fun initialize(init: InitContext) {
        init.register(FunctionDeclarationTree::class.java,
            BiConsumer { checkContext, functionDeclarationTree ->
                if (functionDeclarationTree.returnType() != null && functionDeclarationTree.returnType() is IdentifierTreeImpl) {
                    val returnTree: IdentifierTreeImpl = functionDeclarationTree.returnType() as IdentifierTreeImpl
                    returnTree.typeReference?.let {
                        if (it is KtTypeReference) {
                            reportArrayPrimitives(it, returnTree, checkContext)

                        }
                    }
                }
            })
        init.register(
            ParameterTree::class.java,
            BiConsumer { checkContext, parameterTree ->
                if (parameterTree.type() != null && parameterTree.type() is NativeTreeImpl) {
                    val nativeTreeImpl: NativeTreeImpl = (parameterTree.type() as NativeTreeImpl)
                    var nativeKind = nativeTreeImpl.nativeKind()
                    if (nativeKind is KotlinNativeKind) {
                        if (nativeKind.originalObject is KtTypeReference) {
                            var ktTypeReference: KtTypeReference = nativeKind.originalObject as KtTypeReference
                            reportArrayPrimitives(ktTypeReference, parameterTree, checkContext)
                        }
                    }
                }
            })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun reportArrayPrimitives(element: KtElement, parameterTree: Tree, checkContext: CheckContext) {
        return element
            .collectDescendantsOfType<KtTypeReference>()
            .filter { isArrayPrimitive(it) }
            .forEach {
                checkContext.reportIssue(parameterTree, sIssue)
            }
    }

    private fun isArrayPrimitive(it: KtTypeReference): Boolean {
        if (it.text?.startsWith("Array<") == true) {
            val genericTypeArguments = it.typeElement?.typeArgumentsAsTypes
            return genericTypeArguments?.size == 1 && primitiveTypes.contains(genericTypeArguments[0].text)
        }
        return false
    }

//    private fun isArrayPrimitiveReturnType(tree: IdentifierTree){
//        if (tree.name()?.startsWith("Array<") == true) {
//            val genericTypeArguments = it.typeElement?.typeArgumentsAsTypes
//            return genericTypeArguments?.size == 1 && primitiveTypes.contains(genericTypeArguments[0].text)
//        }
//        return false
//    }

}
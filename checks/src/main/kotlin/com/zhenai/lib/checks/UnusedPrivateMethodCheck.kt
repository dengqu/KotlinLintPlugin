package com.zhenai.lib.checks

import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.checks.utils.FunctionUtils
import com.zhenai.lib.core.slang.api.ClassDeclarationTree
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree
import com.zhenai.lib.core.slang.api.IdentifierTree
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.utils.SyntacticEquivalence
import com.zhenai.lib.core.slang.utils.SyntacticEquivalence.getUniqueIdentifier
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Collectors

class UnusedPrivateMethodCheck : ICheck {
    // Serializable method should not raise any issue in Kotlin. Either change it as parameter when adding new language,
    // or add all exceptions here
    private val sIssue = SIssue.SIssueBuilder()
        .name("无用的私有方法检查")
        .issueId("UnusedPrivateMethodCheck")
        .des("请及时清理无用的私用方法").build()
    private val IGNORED_METHODS = HashSet(
        Arrays.asList(
            "writeObject",
            "readObject",
            "writeReplace",
            "readResolve",
            "readObjectNoData"
        )
    )

    override fun initialize(init: InitContext) {
        init.register(ClassDeclarationTree::class.java, BiConsumer { ctx, classDeclarationTree ->
            // return if this is not the outermost class
            if (ctx.ancestors().stream().anyMatch { ClassDeclarationTree::class.java.isInstance(it) }) {
                return@BiConsumer
            }

            val methods = HashSet<FunctionDeclarationTree>()
            val usedIdentifiers = HashSet<IdentifierTree>()

            classDeclarationTree.descendants().forEach { tree ->
                if (tree is FunctionDeclarationTree && !tree.isConstructor) {
                    methods.add(tree)
                } else if (tree is IdentifierTree) {
                    usedIdentifiers.add(tree)
                }
            }

            usedIdentifiers.removeAll(
                methods.stream()
                    .map<IdentifierTree> { it.name() }
                    .collect(Collectors.toSet())
            )

            val usedUniqueIdentifiers = usedIdentifiers.stream()
                .filter { Objects.nonNull(it) }
                .map<String> { SyntacticEquivalence.getUniqueIdentifier(it) }
                .collect(Collectors.toCollection(Supplier<HashSet<String>> { HashSet() }))

            methods.stream()
                .filter { isValidPrivateMethod(it) }
                .forEach { tree ->
                    val identifier = tree.name()
                    if (isUnusedMethod(
                            identifier,
                            usedUniqueIdentifiers
                        ) && !IGNORED_METHODS.contains(identifier!!.name())
                    ) {
                        val message = String.format("Remove this unused private \"%s\" method.", identifier.name())
                        ctx.reportIssue(tree.rangeToHighlight(), sIssue)
                    }
                }
        })
    }

    override fun getSIssue(): SIssue {
        return sIssue
    }

    private fun isValidPrivateMethod(method: FunctionDeclarationTree): Boolean {
        return FunctionUtils.isPrivateMethod(method) && !FunctionUtils.isOverrideMethod(method)
    }

    private fun isUnusedMethod(identifier: IdentifierTree?, usedIdentifierNames: Set<String>): Boolean {
        return identifier != null && !usedIdentifierNames.contains(getUniqueIdentifier(identifier))
    }
}
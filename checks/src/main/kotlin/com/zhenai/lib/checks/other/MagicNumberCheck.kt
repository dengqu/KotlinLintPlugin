package com.zhenai.lib.checks.other

import com.zhenai.lib.checks.api.CheckContext
import com.zhenai.lib.checks.api.ICheck
import com.zhenai.lib.checks.api.InitContext
import com.zhenai.lib.checks.ext.isPartOf
import com.zhenai.lib.core.slang.api.SIssue
import com.zhenai.lib.core.slang.api.Tree
import com.zhenai.lib.core.slang.impl.BaseTreeImpl
import com.zhenai.lib.core.slang.impl.LiteralTreeImpl
import io.gitlab.arturbosch.detekt.rules.isConstant
import io.gitlab.arturbosch.detekt.rules.isHashCodeFunction
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import java.util.*
import java.util.function.BiConsumer
import kotlin.reflect.KClass

/**
 * 魔法值规则检查
 */
class MagicNumberCheck : ICheck {
    private val sIssue = SIssue.SIssueBuilder()
        .name("代码不建议直接使用魔法值")
        .issueId("MagicNumberCheck")
        .des("直接使用魔法值会让人不知道该值包含得意义").build()

    val PRIMITIVES: Set<KClass<out Any>> = setOf(
        Int::class,
        Boolean::class,
        Float::class,
        Double::class,
        String::class,
        Short::class,
        Char::class,
        Long::class
    )

    private val ignoredNumbers = valueOrDefault(IGNORE_NUMBERS, "-1,0,1,2")
        .splitToSequence(",")
        .filterNot { it.isEmpty() }
        .map { parseAsDouble(it) }
        .sorted()
        .toList()

    private val ignoreAnnotation = valueOrDefault(IGNORE_ANNOTATION, false)
    private val ignoreHashCodeFunction = valueOrDefault(IGNORE_HASH_CODE, true)
    private val ignorePropertyDeclaration = valueOrDefault(IGNORE_PROPERTY_DECLARATION, false)
    private val ignoreLocalVariables = valueOrDefault(IGNORE_LOCAL_VARIABLES, false)
    private val ignoreNamedArgument = valueOrDefault(IGNORE_NAMED_ARGUMENT, false)
    private val ignoreEnums = valueOrDefault(IGNORE_ENUMS, false)
    private val ignoreConstantDeclaration = valueOrDefault(IGNORE_CONSTANT_DECLARATION, true)
    private val ignoreCompanionObjectPropertyDeclaration =
        valueOrDefault(IGNORE_COMPANION_OBJECT_PROPERTY_DECLARATION, true)
    private val ignoreRanges = valueOrDefault(IGNORE_RANGES, false)


    override fun initialize(init: InitContext) {
        init.register(BaseTreeImpl::class.java, BiConsumer { ctx, tree ->
            if (tree is LiteralTreeImpl) {
                tree.originalObject?.let {
                    if (it is KtConstantExpression) {
                        visitConstantExpression(ctx, tree, it)
                    }
                }
            }
        })

    }


    fun visitConstantExpression(ctx: CheckContext, tree: Tree, expression: KtConstantExpression) {
        if (isIgnoredByConfig(expression) || expression.isPartOfFunctionReturnConstant() ||
            expression.isPartOfConstructorOrFunctionConstant()
        ) {
            return
        }

        val parent = expression.parent
        val rawNumber = if (parent.hasUnaryMinusPrefix()) {
            parent.text
        } else {
            expression.text
        }

        val number = parseAsDoubleOrNull(rawNumber) ?: return
        if (!ignoredNumbers.contains(number)) {
            sIssue.des = expression.text +"直接使用魔法值会让人不知道该值包含得意义"
            ctx.reportIssue(tree, sIssue)
        }
    }


    override fun getSIssue(): SIssue {
        return sIssue
    }


    fun <T : Any> valueOrDefault(key: String, default: T): T {
        return valueOrDefaultInternal(key, null, default) as T
    }

    protected open fun valueOrDefaultInternal(key: String, result: Any?, default: Any): Any {
        return try {
            if (result != null) {
                when {
                    result is String -> tryParseBasedOnDefault(result, default)
                    default::class in PRIMITIVES &&
                            result::class != default::class -> throw ClassCastException()
                    else -> result
                }
            } else {
                default
            }
        } catch (_: Exception) {
            error(
                "Value \"$result\" set for config parameter \"$key\" is not of" +
                        " required type ${default::class.simpleName}."
            )
        }
    }


    protected open fun tryParseBasedOnDefault(result: String, defaultResult: Any): Any = when (defaultResult) {
        is Int -> result.toInt()
        is Boolean -> result.toBoolean()
        is Double -> result.toDouble()
        is String -> result
        else -> throw ClassCastException()
    }

    private fun isIgnoredByConfig(expression: KtConstantExpression) = when {
        ignorePropertyDeclaration && expression.isProperty() -> true
        ignoreLocalVariables && (expression.isLocalProperty()) -> true
        ignoreConstantDeclaration && expression.isConstantProperty() -> true
        ignoreCompanionObjectPropertyDeclaration && expression.isCompanionObjectProperty() -> true
        ignoreAnnotation && expression.isPartOf(KtAnnotationEntry::class) -> true
        ignoreHashCodeFunction && expression.isPartOfHashCode() -> true
        ignoreEnums && expression.isPartOf(KtEnumEntry::class) -> true
        ignoreNamedArgument && expression.isNamedArgument() -> true
        ignoreRanges && expression.isPartOfRange() -> true
        else -> false
    }

    private fun parseAsDoubleOrNull(rawToken: String?): Double? = try {
        rawToken?.let { parseAsDouble(it) }
    } catch (e: NumberFormatException) {
        null
    }

    private fun parseAsDouble(rawNumber: String): Double {
        val normalizedText = normalizeForParsingAsDouble(rawNumber)
        return when {
            normalizedText.startsWith("0x") || normalizedText.startsWith("0X") ->
                normalizedText.substring(2).toLong(HEX_RADIX).toDouble()
            normalizedText.startsWith("0b") || normalizedText.startsWith("0B") ->
                normalizedText.substring(2).toLong(BINARY_RADIX).toDouble()
            else -> normalizedText.toDouble()
        }
    }

    private fun normalizeForParsingAsDouble(text: String): String {
        return text.trim()
            .toLowerCase(Locale.US)
            .replace("_", "")
            .removeSuffix("l")
            .removeSuffix("d")
            .removeSuffix("f")
    }

    private fun KtConstantExpression.isNamedArgument() =
        parent is KtValueArgument && (parent as? KtValueArgument)?.isNamed() == true && isPartOf(KtCallElement::class)

    private fun KtConstantExpression.isPartOfFunctionReturnConstant() =
        parent is KtNamedFunction || parent is KtReturnExpression && parent.parent.children.size == 1

    private fun KtConstantExpression.isPartOfConstructorOrFunctionConstant(): Boolean {
        return parent is KtParameter &&
                when (parent.parent.parent) {
                    is KtNamedFunction, is KtPrimaryConstructor, is KtSecondaryConstructor -> true
                    else -> false
                }
    }

    private fun KtConstantExpression.isPartOfRange(): Boolean {
        val theParent = parent
        val rangeOperators = setOf("downTo", "until", "step")
        return when (theParent is KtBinaryExpression) {
            true -> theParent.operationToken == KtTokens.RANGE ||
                    theParent.operationReference.getReferencedName() in rangeOperators
            else -> false
        }
    }

    private fun KtConstantExpression.isPartOfHashCode(): Boolean {
        val containingFunction = getNonStrictParentOfType<KtNamedFunction>()
        return containingFunction?.isHashCodeFunction() == true
    }

    private fun KtConstantExpression.isLocalProperty() =
        getNonStrictParentOfType<KtProperty>()?.isLocal ?: false

    private fun KtConstantExpression.isProperty() =
        getNonStrictParentOfType<KtProperty>()?.let { !it.isLocal } ?: false

    private fun KtConstantExpression.isCompanionObjectProperty() = isProperty() && isInCompanionObject()

    private fun KtConstantExpression.isInCompanionObject() =
        getNonStrictParentOfType<KtObjectDeclaration>()?.isCompanion() ?: false

    private fun KtConstantExpression.isConstantProperty(): Boolean =
        isProperty() && getNonStrictParentOfType<KtProperty>()?.isConstant() ?: false

    private fun PsiElement.hasUnaryMinusPrefix(): Boolean = this is KtPrefixExpression &&
            (firstChild as? KtOperationReferenceExpression)?.operationSignTokenType == KtTokens.MINUS

    companion object {
        const val IGNORE_NUMBERS = "ignoreNumbers"
        const val IGNORE_HASH_CODE = "ignoreHashCodeFunction"
        const val IGNORE_PROPERTY_DECLARATION = "ignorePropertyDeclaration"
        const val IGNORE_LOCAL_VARIABLES = "ignoreLocalVariableDeclaration"
        const val IGNORE_CONSTANT_DECLARATION = "ignoreConstantDeclaration"
        const val IGNORE_COMPANION_OBJECT_PROPERTY_DECLARATION = "ignoreCompanionObjectPropertyDeclaration"
        const val IGNORE_ANNOTATION = "ignoreAnnotation"
        const val IGNORE_NAMED_ARGUMENT = "ignoreNamedArgument"
        const val IGNORE_ENUMS = "ignoreEnums"
        const val IGNORE_RANGES = "ignoreRanges"

        private const val HEX_RADIX = 16
        private const val BINARY_RADIX = 2
    }

}
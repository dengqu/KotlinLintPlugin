package com.zhenai.lib.checks;

import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.core.converter.KotlinNativeKind;
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.impl.NativeTreeImpl;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.*;

import java.util.List;

public class LazyCheck implements ICheck {
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .issueId("LazyCheck")
            .name("Lazy规范使用")
            .des("请为Lazy指定LazyThreadSafetyMode").build();

    @Override
    public void initialize(InitContext init) {
        init.register(NativeTreeImpl.class, (ctx, tree) -> {
            if (tree != null && tree.nativeKind() != null && tree.nativeKind() instanceof KotlinNativeKind) {
                KotlinNativeKind kotlinNativeKind = (KotlinNativeKind) tree.nativeKind();
                if (kotlinNativeKind != null && kotlinNativeKind.getOriginalObject() instanceof KtProperty) {
                    KtProperty ktProperty = (KtProperty) kotlinNativeKind.getOriginalObject();
                    if (isValid(ktProperty.getDelegateExpression())) {
                        ctx.reportIssue(tree, sIssue);
                    }
                }
            }

        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    private boolean isValid(KtExpression expression) {
        if (expression == null) {
            return false;
        }
        boolean isLazy = false;
        boolean isSpeifyMode = false;
        if (expression != null) {
            PsiElement[] psiElements = expression.getChildren();
            for (PsiElement psiElement : psiElements) {
                if (psiElement instanceof KtNameReferenceExpression) {
                    if ("lazy".equals(((KtNameReferenceExpression) psiElement).getReferencedName())) {
                        isLazy = true;
                    }
                } else if (psiElement instanceof KtValueArgumentList) {
                    List<KtValueArgument> valueArguments = ((KtValueArgumentList) psiElement).getArguments();
                    for (KtValueArgument valueArgument : valueArguments) {
                        KtExpression argumentValue = valueArgument.getArgumentExpression();
                        if (argumentValue != null) {
                            if (argumentValue.getText().contains("SYNCHRONIZED") ||
                                    argumentValue.getText().contains("PUBLICATION") ||
                                    argumentValue.getText().contains("NONE")) {
                                isSpeifyMode = true;
                            }
                        }
                    }
                }
            }
            if (isLazy && !isSpeifyMode) {
                return true;
            }

        }
        return false;
    }
}

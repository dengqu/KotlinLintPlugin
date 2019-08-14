/*
 * SonarSource SLang
 * Copyright (C) 2018-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.zhenai.lib.checks;

import com.zhenai.lib.checks.api.CheckContext;
import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.core.converter.KotlinNativeKind;
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.api.TextRange;
import com.zhenai.lib.core.slang.api.Tree;
import com.zhenai.lib.core.slang.impl.NativeTreeImpl;
import com.zhenai.lib.core.slang.impl.ObjectDeclarationImpl;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtConstantExpression;
import org.jetbrains.kotlin.psi.KtProperty;
import org.jetbrains.kotlin.psi.KtStringTemplateExpression;


public class ObjectDeclarationCheck implements ICheck {
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .issueId("ObjectDeclarationCheck")
            .name("伴生对象 规范使用，val 常亮需要添加const或者@JvmStatic或者@JvmField来修饰，否则影响性能")
            .des("伴生对象 规范使用，val 常亮需要添加const或者@JvmStatic或者@JvmField来修饰，否则影响性能").build();

    @Override
    public void initialize(InitContext init) {
        init.register(ObjectDeclarationImpl.class, (ctx, tree) ->
                visit(tree, ctx)
        );
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }


    private void visit(Tree tree, CheckContext checkContext) {
        if (tree == null) {
            return;
        }

        if (tree instanceof NativeTreeImpl) {
            if (((NativeTreeImpl) tree).nativeKind() instanceof KotlinNativeKind) {
                KotlinNativeKind kotlinNativeKind = (KotlinNativeKind) ((NativeTreeImpl) tree).nativeKind();
                if (kotlinNativeKind != null && kotlinNativeKind.getOriginalObject() instanceof KtProperty) {
                    KtProperty ktProperty = (KtProperty) kotlinNativeKind.getOriginalObject();
                    TextRange textRange = kotlinNativeKind.getTextRange();
                    if (ktProperty != null) {
                        if (ktProperty.getModifierList() != null) {
                            PsiElement[] psiElements = ktProperty.getModifierList().getChildren();
                            boolean isConst = false;
                            boolean isJvmStatic = false;
                            boolean isJvmField = false;
                            if (psiElements != null && psiElements.length > 0) {
                                for (PsiElement element : psiElements) {
                                    if (element.getText().equals("const")) {
                                        isConst = true;
                                    }
                                    if (element.getText().equals("@JvmStatic")) {
                                        isJvmStatic = true;
                                    }
                                    if (element.getText().equals("@JvmField")) {
                                        isJvmField = true;
                                    }

                                }

                                if (isConst || isJvmStatic || isJvmField) {
                                    return;
                                }
                                checkContext.reportIssue(textRange, sIssue);
                            }
                        } else {
                            checkContext.reportIssue(textRange, sIssue);
                        }
                    }
                }
            }
        }
        tree.children().forEach(child -> {
            visit(child, checkContext);
        });
    }

    private boolean isValidExpression(PsiElement psiElement) {
        if (psiElement == null) {
            return false;
        }

        if (psiElement instanceof KtStringTemplateExpression || psiElement instanceof KtConstantExpression) {
            return true;
        }
        return false;

    }

}

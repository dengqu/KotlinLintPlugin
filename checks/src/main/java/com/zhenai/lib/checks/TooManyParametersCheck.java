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

import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree;
import com.zhenai.lib.core.slang.api.ModifierTree;
import com.zhenai.lib.core.slang.api.SIssue;

/**
 * 方法参数过多规则
 */
public class TooManyParametersCheck implements ICheck {
    /**
     * 最大参数个数
     */
    private static final int DEFAULT_MAX = 7;
    public int max = DEFAULT_MAX;
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .name("方法参数过多检查")
            .issueId("TooManyParametersCheck")
            .des("方法参数过多，超过" + DEFAULT_MAX + "个").build();

    @Override
    public void initialize(InitContext init) {
        //注册监听的FunctionDeclarationTree树
        init.register(FunctionDeclarationTree.class, (ctx, tree) -> {
            if (!tree.isConstructor() && !isOverrideMethod(tree) && tree.formalParameters().size() > max) {
                //匹配到进行错误结果的上报
                if (tree.name() == null) {
                    ctx.reportIssue(tree, sIssue);
                } else {
                    ctx.reportIssue(tree.name(), sIssue);
                }
            }
        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    /**
     * 是否是重载方法
     *
     * @param tree
     * @return
     */
    private static boolean isOverrideMethod(FunctionDeclarationTree tree) {
        return tree.modifiers().stream().anyMatch(mod -> {
            if (!(mod instanceof ModifierTree)) {
                return false;
            }
            return ((ModifierTree) mod).kind() == ModifierTree.Kind.OVERRIDE;
        });
    }

}

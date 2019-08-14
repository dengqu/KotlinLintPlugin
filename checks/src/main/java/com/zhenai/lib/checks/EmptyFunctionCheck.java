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
import com.zhenai.lib.checks.utils.FunctionUtils;
import com.zhenai.lib.core.slang.api.BlockTree;
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree;
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.api.TreeMetaData;
public class EmptyFunctionCheck implements ICheck {
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .name("空方法检查")
            .des("警告：方法为空而且没有任何注释")
            .issueId("EmptyFunctionCheck").build();

    @Override
    public void initialize(InitContext init) {
        init.register(FunctionDeclarationTree.class, (ctx, tree) -> {
            BlockTree body = tree.body();
            if (!FunctionUtils.isOverrideMethod(tree) && !tree.isConstructor() && body != null && body.statementOrExpressions().isEmpty() && !hasComment(body, ctx.parent().metaData())) {
                ctx.reportIssue(body, sIssue);
            }
        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    private static boolean hasComment(BlockTree body, TreeMetaData parentMetaData) {
        if (!body.metaData().commentsInside().isEmpty()) {
            return true;
        }

        int emptyBodyEndLine = body.textRange().end().line();
        return parentMetaData.commentsInside().stream()
                .anyMatch(comment -> comment.contentRange().start().line() == emptyBodyEndLine);
    }

}

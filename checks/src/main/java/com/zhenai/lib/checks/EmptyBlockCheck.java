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
import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.core.slang.api.*;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class EmptyBlockCheck implements ICheck {
    private static SIssue sIssue = new SIssue.SIssueBuilder()
            .issueId("EmptyBlockCheck")
            .name("实现或者移除空代码块")
            .des("实现或者移除空代码块").build();

    @Override
    public void initialize(InitContext init) {
        init.register(BlockTree.class, (ctx, blockTree) -> {
            Tree parent = ctx.parent();
            if (isValidBlock(parent) && blockTree.statementOrExpressions().isEmpty()) {
                checkComments(ctx, blockTree);
            }
        });

        init.register(MatchTree.class, (ctx, matchTree) -> {
            if (matchTree.cases().isEmpty()) {
                checkComments(ctx, matchTree);
            }
        });

    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    private static boolean isValidBlock(@Nullable Tree parent) {
        return !(parent instanceof FunctionDeclarationTree)
                && !(parent instanceof NativeTree)
                && !isWhileLoop(parent);
    }

    private static boolean isWhileLoop(@Nullable Tree parent) {
        return parent instanceof LoopTree && ((LoopTree) parent).kind() == LoopTree.LoopKind.WHILE;
    }

    private static void checkComments(CheckContext ctx, Tree tree) {
        if (tree.metaData().commentsInside().isEmpty()) {
            ctx.reportIssue(tree, sIssue);
        }
    }

}

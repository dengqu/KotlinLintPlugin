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
import com.zhenai.lib.core.slang.api.BlockTree;
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.api.TextRange;
import com.zhenai.lib.core.slang.api.Tree;
import com.zhenai.lib.core.slang.utils.SyntacticEquivalence;

import javax.annotation.Nullable;
import java.util.List;

public class DuplicateBranchCheck extends AbstractBranchDuplicationCheck {
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .issueId("DuplicateBranchCheck")
            .name("分支块与块相同代码")
            .des("该分支块与块相同代码，请抽离").build();

    @Override
    protected void checkDuplicatedBranches(CheckContext ctx, List<Tree> branches) {
        for (List<Tree> group : SyntacticEquivalence.findDuplicatedGroups(branches)) {
            Tree original = group.get(0);
            group.stream().skip(1)
                    .filter(DuplicateBranchCheck::spansMultipleLines)
                    .forEach(duplicated -> {
                        TextRange originalRange = original.metaData().textRange();
                        sIssue = new SIssue.SIssueBuilder()
                                .issueId("DuplicateBranchCheck")
                                .name("分支块与块相同代码")
                                .des("T该分支的代码块与" + originalRange.start().line() + "行代码块相同 ").build();
                        ctx.reportIssue(
                                duplicated,
                                sIssue);
                    });
        }

    }

    @Override
    protected void onAllIdenticalBranches(CheckContext ctx, Tree tree) {
        // handled by S3923
    }

    private static boolean spansMultipleLines(@Nullable Tree tree) {
        if (tree == null) {
            return false;
        }
        if (tree instanceof BlockTree) {
            BlockTree block = (BlockTree) tree;
            List<Tree> statements = block.statementOrExpressions();
            if (statements.isEmpty()) {
                return false;
            }
            Tree firstStatement = statements.get(0);
            Tree lastStatement = statements.get(statements.size() - 1);
            return firstStatement.metaData().textRange().start().line() != lastStatement.metaData().textRange().end().line();
        }
        TextRange range = tree.metaData().textRange();
        return range.start().line() < range.end().line();
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }
}

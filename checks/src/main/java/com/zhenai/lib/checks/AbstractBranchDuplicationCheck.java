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
import com.zhenai.lib.core.slang.api.IfTree;
import com.zhenai.lib.core.slang.api.MatchCaseTree;
import com.zhenai.lib.core.slang.api.MatchTree;
import com.zhenai.lib.core.slang.api.Tree;

import java.util.ArrayList;
import java.util.List;

import static com.zhenai.lib.core.slang.utils.SyntacticEquivalence.areEquivalent;


public abstract class AbstractBranchDuplicationCheck implements ICheck {

    protected abstract void checkDuplicatedBranches(CheckContext ctx, List<Tree> branches);

    protected abstract void onAllIdenticalBranches(CheckContext ctx, Tree tree);

    @Override
    public void initialize(InitContext init) {
        init.register(IfTree.class, (ctx, tree) -> {
            Tree parent = ctx.parent();
            if (!(parent instanceof IfTree) || tree == ((IfTree) parent).thenBranch()) {
                checkConditionalStructure(ctx, tree, new ConditionalStructure(tree));
            }
        });
        init.register(MatchTree.class, (ctx, tree) ->
                checkConditionalStructure(ctx, tree, new ConditionalStructure(tree))
        );
    }

    private void checkConditionalStructure(CheckContext ctx, Tree tree, ConditionalStructure conditional) {
        if (conditional.allBranchesArePresent && conditional.allBranchesAreIdentical()) {
            onAllIdenticalBranches(ctx, tree);
        } else {
            checkDuplicatedBranches(ctx, conditional.branches);
        }
    }

    protected static class ConditionalStructure {

        private boolean allBranchesArePresent = false;

        private final List<Tree> branches = new ArrayList<>();

        private ConditionalStructure(IfTree ifTree) {
            branches.add(ifTree.thenBranch());
            Tree elseBranch = ifTree.elseBranch();
            while (elseBranch != null) {
                if (elseBranch instanceof IfTree) {
                    IfTree elseIf = (IfTree) elseBranch;
                    branches.add(elseIf.thenBranch());
                    elseBranch = elseIf.elseBranch();
                } else {
                    branches.add(elseBranch);
                    allBranchesArePresent = true;
                    elseBranch = null;
                }
            }
        }

        private ConditionalStructure(MatchTree tree) {
            for (MatchCaseTree caseTree : tree.cases()) {
                branches.add(caseTree.body());
                if (caseTree.expression() == null) {
                    allBranchesArePresent = true;
                }
            }
        }

        private boolean allBranchesAreIdentical() {
            return branches.stream()
                    .skip(1)
                    .allMatch(branch -> areEquivalent(branches.get(0), branch));
        }
    }

}

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

import java.util.Iterator;

import static com.zhenai.lib.checks.utils.ExpressionUtils.isLogicalBinaryExpression;
import static com.zhenai.lib.checks.utils.ExpressionUtils.skipParentheses;

public class TooComplexExpressionCheck implements ICheck {

    private static final int DEFAULT_MAX_COMPLEXITY = 3;
    private static final SIssue sIssue = new SIssue.SIssueBuilder().issueId("TooComplexExpressionCheck")
            .name("表达式不能过于复杂，默认最大值是" + DEFAULT_MAX_COMPLEXITY)
            .des("表达式不能过于复杂，默认最大值是" + DEFAULT_MAX_COMPLEXITY).build();

    @Override
    public void initialize(InitContext init) {
        init.register(BinaryExpressionTree.class, (ctx, tree) -> {
            if (isParentExpression(ctx)) {
                int complexity = computeExpressionComplexity(tree);
                if (complexity > DEFAULT_MAX_COMPLEXITY) {
                    String message = String.format(
                            "Reduce the number of conditional operators (%s) used in the expression (maximum allowed %s).",
                            complexity,
                            DEFAULT_MAX_COMPLEXITY);
                    double gap = (double) complexity - DEFAULT_MAX_COMPLEXITY;
                    ctx.reportIssue(tree, sIssue);
                }
            }
        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    private static boolean isParentExpression(CheckContext ctx) {
        Iterator<Tree> iterator = ctx.ancestors().iterator();
        while (iterator.hasNext()) {
            Tree parentExpression = iterator.next();
            if (parentExpression instanceof BinaryExpressionTree) {
                return false;
            } else if (!(parentExpression instanceof UnaryExpressionTree) || !(parentExpression instanceof ParenthesizedExpressionTree)) {
                return true;
            }
        }
        return true;
    }

    private static int computeExpressionComplexity(Tree originalTree) {
        Tree tree = skipParentheses(originalTree);
        if (tree instanceof BinaryExpressionTree) {
            int complexity = isLogicalBinaryExpression(tree) ? 1 : 0;
            BinaryExpressionTree binary = (BinaryExpressionTree) tree;
            return complexity
                    + computeExpressionComplexity(binary.leftOperand())
                    + computeExpressionComplexity(binary.rightOperand());
        } else if (tree instanceof UnaryExpressionTree) {
            return computeExpressionComplexity(((UnaryExpressionTree) tree).operand());
        } else {
            return 0;
        }
    }

}

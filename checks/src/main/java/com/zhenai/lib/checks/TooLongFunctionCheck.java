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

import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree;
import com.zhenai.lib.core.slang.api.SIssue;

public class TooLongFunctionCheck implements ICheck {

    private static final int DEFAULT_MAX = 100;
    private static final String DEFAULT_MAX_VALUE = "" + DEFAULT_MAX;

    private static final SIssue sIssue = new SIssue.SIssueBuilder().issueId("TooLongFunctionCheck")
            .name("方法不应过长，最大行数默认是" + DEFAULT_MAX_VALUE)
            .des("方法不应过长，最大行数默认是" + DEFAULT_MAX_VALUE).build();
    public int max = DEFAULT_MAX;

    @Override
    public void initialize(InitContext init) {
        init.register(FunctionDeclarationTree.class, (ctx, tree) -> {
            int numberOfLinesOfCode = tree.metaData().linesOfCode().size();
            if (numberOfLinesOfCode > max) {
                ctx.reportIssue(tree.rangeToHighlight(), sIssue);
            }
        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

}

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
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.api.TopLevelTree;

import java.util.regex.Pattern;

public class EmptyCommentCheck implements ICheck {
    private static final Pattern EMPTY_CONTENT_PATTERN = Pattern.compile("[/*\\n\\r\\s]+(@.*)?", Pattern.DOTALL);

    private SIssue sIssue = new SIssue.SIssueBuilder()
            .issueId("EmptyCommentCheck")
            .name("空注释或者缺少说明检查")
            .des("请移除空的注释或者添加为注释添加说明").build();

    @Override
    public void initialize(InitContext init) {
        init.register(TopLevelTree.class, (ctx, tree) ->
                tree.allComments().stream()
                        .filter(comment -> EMPTY_CONTENT_PATTERN.matcher(comment.contentText().trim()).matches() || (comment.contentText().trim().isEmpty()))
                        .forEach(comment -> ctx.reportIssue(comment, sIssue)));
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }


}

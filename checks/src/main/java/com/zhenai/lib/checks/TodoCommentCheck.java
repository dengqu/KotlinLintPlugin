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

import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.api.TopLevelTree;
import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.checks.api.ICheck;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodoCommentCheck implements ICheck {
    private SIssue sIssue = new SIssue.SIssueBuilder().issueId("TodoCommentCheck")
            .name("todo tag 检查")
            .des("完成 TODO comment.").build();
    private final Pattern todoPattern = Pattern.compile("(?i)(^|[[^\\p{L}]&&\\D])(todo)($|[[^\\p{L}]&&\\D])");

    @Override
    public void initialize(InitContext init) {
        init.register(TopLevelTree.class, (ctx, tree) ->
                tree.allComments().forEach(comment -> {
                    Matcher matcher = todoPattern.matcher(comment.text());
                    if (matcher.find()) {
                        ctx.reportIssue(comment.textRange(), sIssue);
                    }
                })
        );
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

}

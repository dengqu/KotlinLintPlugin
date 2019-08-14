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
import com.zhenai.lib.core.slang.api.*;
import com.zhenai.lib.core.slang.impl.TextRanges;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommentedCodeCheck implements ICheck {
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .issueId("CommentedCodeCheck")
            .name("请移除注释的代码")
            .des("请移除注释的代码").build();
    private CodeVerifier codeVerifier;

    public void setCodeVerifier(CodeVerifier codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    @Override
    public void initialize(InitContext init) {
        init.register(TopLevelTree.class, (ctx, tree) -> {
            List<List<Comment>> groupedComments =
                    groupComments(tree.allComments());
            groupedComments.forEach(comments -> {
                String content = comments.stream()
                        .map(Comment::contentText)
                        .collect(Collectors.joining("\n"));
                if (codeVerifier != null && codeVerifier.containsCode(content)) {
                    List<TextRange> textRanges = comments.stream()
                            .map(HasTextRange::textRange)
                            .collect(Collectors.toList());
                    ctx.reportIssue(TextRanges.merge(textRanges), sIssue);
                }
            });
        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    private static List<List<Comment>> groupComments(List<Comment> comments) {
        List<List<Comment>> groups = new ArrayList<>();
        List<Comment> currentGroup = null;
        for (Comment comment : comments) {
            if (currentGroup == null) {
                currentGroup = initNewGroup(comment);
            } else if (areAdjacent(currentGroup.get(currentGroup.size() - 1), comment)) {
                currentGroup.add(comment);
            } else {
                groups.add(currentGroup);
                currentGroup = initNewGroup(comment);
            }
        }
        if (currentGroup != null) {
            groups.add(currentGroup);
        }
        return groups;
    }

    private static List<Comment> initNewGroup(Comment comment) {
        List<Comment> group = new ArrayList<>();
        group.add(comment);
        return group;
    }

    private static boolean areAdjacent(Comment commentA, Comment commentB) {
        return commentA.textRange().start().line() + 1 == commentB.textRange().start().line();
    }

}

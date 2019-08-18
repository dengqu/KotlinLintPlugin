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
package com.zhenai.lib.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

import com.zhenai.lib.checks.CommentedCodeCheck;
import com.zhenai.lib.core.slang.api.HasTextRange;
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.api.TextRange;
import com.zhenai.lib.core.slang.api.Tree;
import com.zhenai.lib.checks.api.CheckContext;
import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.core.slang.visitors.TreeVisitor;
import com.zhenai.lib.core.converter.KotlinCodeVerifier;

public class ChecksVisitor extends TreeVisitor<InputFileContext> {

    private List<Problem> problems;

    private final DurationStatistics statistics;

    public ChecksVisitor(List<ICheck> checks, DurationStatistics statistics) {
        this.statistics = statistics;
        problems = new ArrayList<>();
        for (ICheck check : checks) {
            if (check instanceof CommentedCodeCheck) {
                ((CommentedCodeCheck) check).setCodeVerifier(new KotlinCodeVerifier());
            }
            check.initialize(new ContextAdapter());
        }
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public class ContextAdapter implements InitContext, CheckContext {

        private InputFileContext currentCtx;


        @Override
        public <T extends Tree> void register(Class<T> cls, BiConsumer<CheckContext, T> visitor) {
            ChecksVisitor.this.register(cls, (ctx, tree) -> {
                currentCtx = ctx;
                visitor.accept(this, tree);
            });
        }

        @Override
        public Deque<Tree> ancestors() {
            return currentCtx.ancestors();
        }

        @Override
        public String filename() {
            return currentCtx.inputFile.filename();
        }

        @Override
        public String fileContent() {
            try {
                return currentCtx.inputFile.contents();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read content of " + currentCtx.inputFile, e);
            }
        }

        @Override
        public void reportIssue(TextRange textRange, SIssue issue) {
            doReportIssue(textRange, issue);
        }

        @Override
        public void reportIssue(HasTextRange textRange, SIssue issue) {
            doReportIssue(textRange.textRange(), issue);
        }

        private void doReportIssue(@Nullable TextRange textRange, SIssue issue) {
            Problem problem = new Problem();
            problem.issue = issue;
            problem.textRange = textRange;
            problems.add(problem);
        }

        @org.jetbrains.annotations.Nullable
        @Override
        public Tree parent() {
            if (this.ancestors().isEmpty()) {
                return null;
            } else {
                return this.ancestors().peek();
            }
        }
    }

    public class Problem {
        public SIssue issue;
        public TextRange textRange;
    }

}

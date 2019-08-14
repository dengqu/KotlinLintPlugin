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

import java.util.Arrays;
import java.util.List;

public class CheckList {
    private CheckList() {
    }

    public static List<ICheck> allChecks() {
        return Arrays.asList(
                new TodoCommentCheck(),
                new TooManyParametersCheck(),
                new UnusedFunctionParameterCheck(),
                new HardCodeStringCheck(),
                new UseLogCheck(),
                new ToastCheck(),
                new EmptyCommentCheck(),
                new InterfaceMethodCommentCheck(),
                new UnusedPrivateMethodCheck(),
                new UnusedLocalVariableCheck(),
                new ObjectDeclarationCheck(),
                new TooComplexExpressionCheck(),
                new TooLongFunctionCheck(),
                new TooManyLinesOfCodeFileCheck(),
                new CommentedCodeCheck(),
                new EmptyBlockCheck(),
                new DuplicateBranchCheck(),
                new EmptyFunctionCheck());
    }

    public static ICheck getSlangCheck(String name) {
        for (ICheck check : allChecks()) {
            if (check.getSIssue().getName().equals(name)) {
                return check;
            }
        }
        return null;
    }
}

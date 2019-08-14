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

import java.util.regex.Pattern;

public class VariableAndParameterNameCheck implements ICheck {

    private static final String DEFAULT_FORMAT = "^[_a-z][a-zA-Z0-9]*$";

    private SIssue sIssue = new SIssue.SIssueBuilder()
            .issueId("VariableAndParameterNameCheck")
            .name("局部变量和函数参数名称应符合命名约定")
            .des("局部变量和函数参数名称应符合命名约定").build();

    public String format = DEFAULT_FORMAT;

    @Override
    public void initialize(InitContext init) {
        Pattern pattern = Pattern.compile(format);

        init.register(VariableDeclarationTree.class, (ctx, tree) -> {
            if (ctx.ancestors().stream().anyMatch(FunctionDeclarationTree.class::isInstance)) {
                check(pattern, ctx, tree.identifier(), "local variable");
            }
        });

        init.register(FunctionDeclarationTree.class, (ctx, tree) ->
                tree.formalParameters().stream()
                        .filter(ParameterTree.class::isInstance)
                        .map(ParameterTree.class::cast)
                        .forEach(
                                param -> check(pattern, ctx, param.identifier(), "parameter")));
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    private void check(Pattern pattern, CheckContext ctx, IdentifierTree identifier, String variableKind) {
        if (!pattern.matcher(identifier.name()).matches()) {
            String message = String.format("请重新按照这个规则 %s 去重命名%s", this.format, variableKind);
            sIssue.setDes(message);
            ctx.reportIssue(identifier, sIssue);
        }
    }

}

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
import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.checks.api.SecondaryLocation;
import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree;
import com.zhenai.lib.core.slang.api.IdentifierTree;
import com.zhenai.lib.core.slang.api.ParameterTree;
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.impl.TopLevelTreeImpl;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.zhenai.lib.checks.utils.FunctionUtils.isOverrideMethod;
import static com.zhenai.lib.checks.utils.FunctionUtils.isPrivateMethod;
import static com.zhenai.lib.core.slang.utils.SyntacticEquivalence.areEquivalent;

public class UnusedFunctionParameterCheck implements ICheck {

    private SIssue sIssue = new SIssue.SIssueBuilder()
            .name("无用方法参数检查")
            .issueId("UnusedFunctionParameterCheck")
            .des("请移除无用的方法参数").build();
    // Currently we ignore all functions named "main", however this should be configurable based on the analyzed language in the future.
    private static final Pattern IGNORED_PATTERN = Pattern.compile("main", Pattern.CASE_INSENSITIVE);

    @Override
    public void initialize(InitContext init) {
        init.register(FunctionDeclarationTree.class, (ctx, functionDeclarationTree) -> {
            if (functionDeclarationTree.isConstructor() || shouldBeIgnored(ctx, functionDeclarationTree)) {
                return;
            }

            List<ParameterTree> unusedParameters =
                    functionDeclarationTree.formalParameters().stream()
                            .filter(ParameterTree.class::isInstance)
                            .map(ParameterTree.class::cast)
                            .filter(parameterTree -> parameterTree.modifiers().isEmpty() && functionDeclarationTree.descendants()
                                    .noneMatch(tree -> !tree.equals(parameterTree.identifier()) && areEquivalent(tree, parameterTree.identifier())))
                            .collect(Collectors.toList());

            if (unusedParameters.isEmpty()) {
                return;
            }

            List<SecondaryLocation> secondaryLocations = unusedParameters.stream()
                    .map(unusedParameter ->
                            new SecondaryLocation(unusedParameter.identifier(), "Remove this unused method parameter " + unusedParameter.identifier().name() + "\"."))
                    .collect(Collectors.toList());

            IdentifierTree firstUnused = unusedParameters.get(0).identifier();
            String msg;

//            if (unusedParameters.size() > 1) {
//                msg = "Remove these unused function parameters.";
//            } else {
//                msg = "Remove this unused function parameter \"" + firstUnused.name() + "\".";
//            }
            ctx.reportIssue(firstUnused, sIssue);
        });

    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    private static boolean shouldBeIgnored(CheckContext ctx, FunctionDeclarationTree tree) {
        IdentifierTree name = tree.name();
        boolean validFunctionForRule = ctx.parent() instanceof TopLevelTreeImpl || (isPrivateMethod(tree) && !isOverrideMethod(tree));
        return !validFunctionForRule
                || tree.body() == null
                || (name != null && IGNORED_PATTERN.matcher(name.name()).matches());
    }

}

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
import com.zhenai.lib.checks.utils.FunctionUtils;
import com.zhenai.lib.core.slang.api.ClassDeclarationTree;
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree;
import com.zhenai.lib.core.slang.api.IdentifierTree;
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.utils.SyntacticEquivalence;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import static com.zhenai.lib.core.slang.utils.SyntacticEquivalence.getUniqueIdentifier;

public class UnusedPrivateMethodCheck implements ICheck {
    // Serializable method should not raise any issue in Kotlin. Either change it as parameter when adding new language,
    // or add all exceptions here
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .name("无用的私有方法检查")
            .issueId("UnusedPrivateMethodCheck")
            .des("请及时清理无用的私用方法").build();
    private static final Set<String> IGNORED_METHODS = new HashSet<>(Arrays.asList(
            "writeObject",
            "readObject",
            "writeReplace",
            "readResolve",
            "readObjectNoData"));

    @Override
    public void initialize(InitContext init) {
        init.register(ClassDeclarationTree.class, (ctx, classDeclarationTree) -> {
            // return if this is not the outermost class
            if (ctx.ancestors().stream().anyMatch(ClassDeclarationTree.class::isInstance)) {
                return;
            }

            Set<FunctionDeclarationTree> methods = new HashSet<>();
            Set<IdentifierTree> usedIdentifiers = new HashSet<>();

            classDeclarationTree.descendants().forEach(tree -> {
                if (tree instanceof FunctionDeclarationTree && !((FunctionDeclarationTree) tree).isConstructor()) {
                    methods.add(((FunctionDeclarationTree) tree));
                } else if (tree instanceof IdentifierTree) {
                    usedIdentifiers.add((IdentifierTree) tree);
                }
            });

            usedIdentifiers.removeAll(methods.stream()
                    .map(FunctionDeclarationTree::name)
                    .collect(Collectors.toSet()));

            Set<String> usedUniqueIdentifiers = usedIdentifiers.stream()
                    .filter(Objects::nonNull)
                    .map(SyntacticEquivalence::getUniqueIdentifier)
                    .collect(Collectors.toCollection(HashSet::new));

            methods.stream()
                    .filter(UnusedPrivateMethodCheck::isValidPrivateMethod)
                    .forEach(tree -> {
                        IdentifierTree identifier = tree.name();
                        if (isUnusedMethod(identifier, usedUniqueIdentifiers) && !IGNORED_METHODS.contains(identifier.name())) {
                            String message = String.format("Remove this unused private \"%s\" method.", identifier.name());
                            ctx.reportIssue(tree.rangeToHighlight(), sIssue);
                        }
                    });
        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    private static boolean isValidPrivateMethod(FunctionDeclarationTree method) {
        return FunctionUtils.isPrivateMethod(method) && !FunctionUtils.isOverrideMethod(method);
    }

    private static boolean isUnusedMethod(@Nullable IdentifierTree identifier, Set<String> usedIdentifierNames) {
        return identifier != null && !usedIdentifierNames.contains(getUniqueIdentifier(identifier));
    }

}

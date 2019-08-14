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
package com.zhenai.lib.checks.utils;

import com.zhenai.lib.core.slang.api.FunctionDeclarationTree;
import com.zhenai.lib.core.slang.api.ModifierTree;

import static com.zhenai.lib.core.slang.api.ModifierTree.Kind.OVERRIDE;
import static com.zhenai.lib.core.slang.api.ModifierTree.Kind.PRIVATE;

public class FunctionUtils {

  private FunctionUtils() {
  }

  public static boolean isPrivateMethod(FunctionDeclarationTree method) {
    return hasModifierMethod(method, PRIVATE);

  }

  public static boolean isOverrideMethod(FunctionDeclarationTree method) {
    return hasModifierMethod(method, OVERRIDE);
  }

  public static boolean hasModifierMethod(FunctionDeclarationTree method, ModifierTree.Kind kind) {
    return method.modifiers().stream()
      .filter(ModifierTree.class::isInstance)
      .map(ModifierTree.class::cast)
      .anyMatch(modifier -> modifier.kind() == kind);
  }

}

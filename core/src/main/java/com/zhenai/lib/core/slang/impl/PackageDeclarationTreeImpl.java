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
package com.zhenai.lib.core.slang.impl;

import java.util.List;
import com.zhenai.lib.core.slang.api.PackageDeclarationTree;
import com.zhenai.lib.core.slang.api.Tree;
import com.zhenai.lib.core.slang.api.TreeMetaData;

public class PackageDeclarationTreeImpl extends BaseTreeImpl implements PackageDeclarationTree {

  private final List<Tree> children;

  public PackageDeclarationTreeImpl(TreeMetaData metaData, List<Tree> children) {
    super(metaData);
    this.children = children;
  }

  @Override
  public List<Tree> children() {
    return children;
  }

}

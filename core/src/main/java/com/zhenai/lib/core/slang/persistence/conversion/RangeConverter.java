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
package com.zhenai.lib.core.slang.persistence.conversion;

import java.util.NoSuchElementException;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import com.zhenai.lib.core.slang.api.TextPointer;
import com.zhenai.lib.core.slang.api.TextRange;
import com.zhenai.lib.core.slang.api.Token;
import com.zhenai.lib.core.slang.api.Tree;
import com.zhenai.lib.core.slang.api.TreeMetaData;
import com.zhenai.lib.core.slang.impl.TextRangeImpl;
import com.zhenai.lib.core.slang.impl.TreeMetaDataProvider;

public final class RangeConverter {

  private RangeConverter() {
  }

  @Nullable
  public static String format(@Nullable TextRange range) {
    if (range == null) {
      return null;
    }
    TextPointer start = range.start();
    TextPointer end = range.end();
    return start.line() + ":" + start.lineOffset() + ":" + end.line() + ":" + end.lineOffset();
  }

  @Nullable
  public static TextRange parse(@Nullable String value) {
    if (value == null) {
      return null;
    }
    String[] values = value.split(":", 4);
    if (values.length != 4) {
      throw new IllegalArgumentException("Invalid TextRange '" + value + "'");
    }
    int startLine = Integer.parseInt(values[0]);
    int startLineOffset = Integer.parseInt(values[1]);
    int endLine = Integer.parseInt(values[2]);
    int endLineOffset = Integer.parseInt(values[3]);
    return new TextRangeImpl(startLine, startLineOffset, endLine, endLineOffset);
  }

  @Nullable
  public static String tokenReference(@Nullable Token token) {
    if (token == null) {
      return null;
    }
    return format(token.textRange());
  }

  @Nullable
  public static Token resolveToken(TreeMetaDataProvider metaDataProvider, @Nullable String tokenReference) {
    TextRange range = parse(tokenReference);
    if (range == null) {
      return null;
    }
    return metaDataProvider.firstToken(range)
      .orElseThrow(() -> new NoSuchElementException("Token not found: " + tokenReference));
  }

  public static String metaDataReference(Tree tree) {
    return format(tree.metaData().textRange());
  }

  public static TreeMetaData resolveMetaData(TreeMetaDataProvider metaDataProvider, String metaDataReference) {
    return metaDataProvider.metaData(parse(metaDataReference));
  }

  @Nullable
  public static String treeReference(@Nullable Tree tree) {
    if (tree == null) {
      return null;
    }
    return format(tree.metaData().textRange());
  }

  @Nullable
  public static <T extends Tree> T resolveNullableTree(Tree parent, @Nullable String treeReference, Class<T> childClass) {
    if (treeReference == null) {
      return null;
    }
    TextRange range = parse(treeReference);
    return Stream.concat(Stream.of(parent), parent.descendants())
      .filter(child -> child.textRange().equals(range))
      .filter(childClass::isInstance)
      .map(childClass::cast)
      .findFirst()
      .orElse(null);
  }

}

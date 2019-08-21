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

import java.math.BigInteger;

import com.zhenai.lib.core.slang.api.IntegerLiteralTree;
import com.zhenai.lib.core.slang.api.TreeMetaData;

/**
 * Languages that can have integer literal with other bases, or who use a different syntax for binary/octal/decimal/hexadecimal
 * values, specific plugins should provide its own implementation of {@link IntegerLiteralTree}
 */
public class IntegerLiteralTreeImpl extends LiteralTreeImpl implements IntegerLiteralTree {

    private final Base base;
    private final String numericPart;

    public IntegerLiteralTreeImpl(Object object, TreeMetaData metaData, String stringValue) {
        super(object, metaData, stringValue);

        if (hasExplicitHexadecimalPrefix(stringValue)) {
            base = Base.HEXADECIMAL;
            numericPart = stringValue.substring(2);
        } else if (hasExplicitBinaryPrefix(stringValue)) {
            base = Base.BINARY;
            numericPart = stringValue.substring(2);
        } else if (hasExplicitDecimalPrefix(stringValue)) {
            base = Base.DECIMAL;
            numericPart = stringValue.substring(2);
        } else if (hasExplicitOctalPrefix(stringValue)) {
            base = Base.OCTAL;
            numericPart = stringValue.substring(2);
        } else if (!stringValue.equals("0") && stringValue.startsWith("0")) {
            base = Base.OCTAL;
            numericPart = stringValue.substring(1);
        } else {
            base = Base.DECIMAL;
            numericPart = stringValue;
        }
    }

    @Override
    public Base getBase() {
        return base;
    }

    @Override
    public BigInteger getIntegerValue() {
        return new BigInteger(numericPart, base.getRadix());
    }

    @Override
    public String getNumericPart() {
        return numericPart;
    }

    private static boolean hasExplicitOctalPrefix(String value) {
        return value.startsWith("0o") || value.startsWith("0O");
    }

    private static boolean hasExplicitHexadecimalPrefix(String value) {
        return value.startsWith("0x") || value.startsWith("0X");
    }

    private static boolean hasExplicitBinaryPrefix(String value) {
        return value.startsWith("0b") || value.startsWith("0B");
    }

    private static boolean hasExplicitDecimalPrefix(String value) {
        return value.startsWith("0d") || value.startsWith("0D");
    }

}

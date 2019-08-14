package com.zhenai.lib.core.slang.impl;

import com.zhenai.lib.core.slang.api.Tree;
import com.zhenai.lib.core.slang.api.TreeMetaData;

import java.util.Collections;
import java.util.List;

public class ObjectDeclarationImpl extends BaseTreeImpl {
    private final List<Tree> children;

    public ObjectDeclarationImpl(TreeMetaData metaData, List<Tree> children) {
        super(metaData);
        this.children = children;
    }

    @Override
    public List<Tree> children() {
        return children;
    }
}

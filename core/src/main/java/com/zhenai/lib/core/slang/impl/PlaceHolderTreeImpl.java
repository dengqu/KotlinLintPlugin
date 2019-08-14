package com.zhenai.lib.core.slang.impl;

import java.util.Collections;
import java.util.List;
import com.zhenai.lib.core.slang.api.PlaceHolderTree;
import com.zhenai.lib.core.slang.api.Token;
import com.zhenai.lib.core.slang.api.Tree;
import com.zhenai.lib.core.slang.api.TreeMetaData;

public class PlaceHolderTreeImpl extends BaseTreeImpl implements PlaceHolderTree {
  private final Token placeHolderToken;

  public PlaceHolderTreeImpl(TreeMetaData metaData, Token placeHolderToken) {
    super(metaData);
    this.placeHolderToken = placeHolderToken;
  }

  @Override
  public Token placeHolderToken() {
    return placeHolderToken;
  }

  @Override
  public List<Tree> children() {
    return Collections.emptyList();
  }
}

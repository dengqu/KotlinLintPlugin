package com.zhenai.lib.checks.api

import com.zhenai.lib.core.slang.api.TextRange
import com.zhenai.lib.core.slang.api.Tree

class SecondaryLocation(textRange: TextRange, message: String?) {
    val textRange: TextRange

    val message: String?

    init {
        this.textRange = textRange
        this.message = message
    }

    constructor(tree: Tree) : this(tree, null) {

    }

    constructor(tree: Tree, message: String?) : this(tree.metaData().textRange(), message) {

    }

}
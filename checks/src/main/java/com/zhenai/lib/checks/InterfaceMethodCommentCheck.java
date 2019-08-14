package com.zhenai.lib.checks;

import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.core.slang.api.FunctionDeclarationTree;
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.impl.FunctionDeclarationTreeImpl;

public class InterfaceMethodCommentCheck implements ICheck {
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .issueId("InterfaceMethodCommentCheck")
            .name("接口或者抽象方法需要添加说明")
            .des("接口或者抽象方法需要添加comment").build();

    @Override
    public void initialize(InitContext init) {
        init.register(FunctionDeclarationTree.class, (ctx, tree) -> {
            if (!tree.isConstructor() && tree.body() == null && tree.metaData().commentsInside().isEmpty()) {
                if (tree instanceof FunctionDeclarationTreeImpl && ((FunctionDeclarationTreeImpl) tree).isLiteral) {
                    return;
                }
                ctx.reportIssue(tree, sIssue);
            }
        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }
}

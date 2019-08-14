package com.zhenai.lib.checks;

import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.core.slang.api.*;
import com.zhenai.lib.core.slang.impl.IdentifierTreeImpl;
import com.zhenai.lib.core.slang.impl.NativeTreeImpl;


public class UseLogCheck implements ICheck {
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .issueId("UseLogCheck")
            .name("Log 规范使用")
            .des("请使用我们团队统一的LogUtils").build();

    @Override
    public void initialize(InitContext init) {
        init.register(NativeTree.class, (ctx, tree) -> {
            if (isSystemLog(tree)) {
                ctx.reportIssue(tree, sIssue);
            }
        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    private boolean isSystemLog(NativeTree tree) {
        if (tree == null) {
            return false;
        }

        if (tree.nativeKind() == null || !(tree.nativeKind() instanceof NativeKind)) {
            return false;
        }

        if (tree.children() == null || tree.children().size() < 2) {
            return false;
        }

        boolean nativeLog = false;

        if (tree.children().get(0) instanceof IdentifierTreeImpl) {
            if (((IdentifierTreeImpl) tree.children().get(0)).name().equals("Log")) {
                nativeLog = true;
            }
        }

        boolean nativeMethod = false;

        if (tree.children().get(1) instanceof NativeTreeImpl) {
            if (tree.children().get(1).children() != null && tree.children().get(1).children().size() >= 2 && tree.children().get(1).children().get(0) instanceof IdentifierTreeImpl) {
                nativeMethod = ((IdentifierTreeImpl) tree.children().get(1).children().get(0)).name().equals("i") ||
                        ((IdentifierTreeImpl) tree.children().get(1).children().get(0)).name().equals("d") ||
                        ((IdentifierTreeImpl) tree.children().get(1).children().get(0)).name().equals("e") ||
                        ((IdentifierTreeImpl) tree.children().get(1).children().get(0)).name().equals("v");
            }
        }
        return nativeLog && nativeMethod;
    }
}

package com.zhenai.lib.checks;

import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.core.slang.api.NativeKind;
import com.zhenai.lib.core.slang.api.NativeTree;
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.impl.IdentifierTreeImpl;
import com.zhenai.lib.core.slang.impl.NativeTreeImpl;

public class ToastCheck implements ICheck {
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .issueId("ToastCheck")
            .name("Toast规范使用")
            .des("请使用我们团队的Toast工具类").build();

    @Override
    public void initialize(InitContext init) {
        init.register(NativeTree.class, (ctx, tree) -> {
            if (isSystemToast(tree)) {
                ctx.reportIssue(tree, sIssue);
            }
        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }

    private boolean isSystemToast(NativeTree tree) {
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
            if (((IdentifierTreeImpl) tree.children().get(0)).name().equals("Toast")) {
                nativeLog = true;
            }
        }

        boolean nativeMethod = false;

        if (tree.children().get(1) instanceof NativeTreeImpl) {
            if (tree.children().get(1).children() != null && tree.children().get(1).children().size() >= 2 && tree.children().get(1).children().get(0) instanceof IdentifierTreeImpl) {
                nativeMethod = ((IdentifierTreeImpl) tree.children().get(1).children().get(0)).name().equals("makeText");
            }
        }
        return nativeLog && nativeMethod;
    }
}

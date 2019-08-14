package com.zhenai.lib.checks;

import com.zhenai.lib.checks.api.InitContext;
import com.zhenai.lib.checks.api.ICheck;
import com.zhenai.lib.core.converter.KotlinNativeKind;
import com.zhenai.lib.core.slang.api.SIssue;
import com.zhenai.lib.core.slang.api.StringLiteralTree;
import com.zhenai.lib.core.slang.impl.NativeTreeImpl;
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry;

import java.util.regex.Pattern;

public class HardCodeStringCheck implements ICheck {
    private SIssue sIssue = new SIssue.SIssueBuilder()
            .name("不能在kotlin文件使用中文字符串硬编码")
            .issueId("HardCodeStringCheck")
            .des("不能在kotlin文件使用中文字符串硬编码").build();
    private final Pattern todoPattern = Pattern.compile("[\\u4e00-\\u9fa5]");

    @Override
    public void initialize(InitContext init) {
        init.register(StringLiteralTree.class, (ctx, tree) -> {
            String content = tree.content();
            if (todoPattern.matcher(content).find()) {
                ctx.reportIssue(tree, sIssue);
            }
        });


        init.register(NativeTreeImpl.class, (ctx, tree) -> {
            if (tree.nativeKind() != null && tree.nativeKind() instanceof KotlinNativeKind) {
                if (((KotlinNativeKind) tree.nativeKind()).getPsiElement().isAssignableFrom(KtLiteralStringTemplateEntry.class)) {
                    for (Object object : ((KotlinNativeKind) tree.nativeKind()).getDifferentiators()) {
                        if (object instanceof String) {
                            if (todoPattern.matcher(object.toString()).find()) {
                                ctx.reportIssue(tree, sIssue);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public SIssue getSIssue() {
        return sIssue;
    }
}

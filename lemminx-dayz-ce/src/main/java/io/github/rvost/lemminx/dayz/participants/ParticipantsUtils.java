package io.github.rvost.lemminx.dayz.participants;

import org.eclipse.lsp4j.Diagnostic;

public class ParticipantsUtils {
    public static boolean match(Diagnostic diagnostic, String code) {
        if (diagnostic == null || diagnostic.getCode() == null || !diagnostic.getCode().isLeft()) {
            return false;
        }

        return code == null ? diagnostic.getCode().getLeft() == null :
                code.equals(diagnostic.getCode().getLeft());
    }
}

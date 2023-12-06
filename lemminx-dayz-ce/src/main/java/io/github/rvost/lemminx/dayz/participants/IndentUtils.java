package io.github.rvost.lemminx.dayz.participants;

public class IndentUtils {
    public static String whitespaceBuffer(String indent, int column) {
        StringBuilder sb = new StringBuilder();
        if ((indent != null) && (!indent.isEmpty())) {
            for (int i = 0; i < column / indent.length(); ++i) {
                sb.append(indent);
            }
        }
        return sb.toString();
    }

    /**
     * Will return a string where `\n` will be replaced with a proper line separator and match the
     * indentation level for the passed in column number. Adding `\t` will add an indent level.
     * @param text
     * @param indent
     * @param column
     * @return
     */
    public static String formatText(String text, String indent, int column) {
        return text.replace("\n", System.lineSeparator() + whitespaceBuffer(indent, column))
                .replace("\t", indent);
    }
}
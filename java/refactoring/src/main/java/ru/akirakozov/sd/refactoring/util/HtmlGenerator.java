package ru.akirakozov.sd.refactoring.util;

import java.io.PrintWriter;
import java.util.Stack;

public class HtmlGenerator implements AutoCloseable {
    public HtmlGenerator(PrintWriter writer) {
        this.writer = writer;
    }

    public HtmlGenerator newline() {
        writer.println();
        return this;
    }

    public HtmlGenerator appendBr() {
        return append("</br>");
    }

    public HtmlGenerator beginTag(String tag) {
        printTag(tag);
        openedTags.push(tag);
        return this;
    }

    public HtmlGenerator closeLastTag() {
        closeTag(openedTags.pop());
        return this;
    }

    public HtmlGenerator append(CharSequence str) {
        writer.append(str);
        return this;
    }

    public HtmlGenerator appendInTag(String tag, CharSequence str) {
        return beginTag(tag).append(str).closeLastTag();
    }

    public HtmlGenerator closeAllTags() {
        while (!openedTags.empty()) {
            closeLastTag();
        }
        return this;
    }

    @Override
    public void close() {
        closeAllTags().newline();
    }

    private void printTag(String tag) {
        writer.print("<" + tag + ">");
    }

    private void closeTag(String tag) {
        printTag("/" + tag);
    }

    private final PrintWriter writer;
    private final Stack<String> openedTags = new Stack<>();
}

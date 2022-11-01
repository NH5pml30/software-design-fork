package ru.akirakozov.sd.refactoring.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

public class HtmlGeneratorTest {
    private final static String THE_TAG_1 = "some-tag";
    private final static String THE_TAG_2 = "another-tag";
    private final static String STRING_TO_APPEND = "Some text in text form";
    private final static String STRING_TO_APPEND_2 = "Some more text in text form";
    private final static String STRING_TO_APPEND_3 = "Even more text in text form";

    void checkString(Consumer<HtmlGenerator> action, String expected) {
        var writer = new StringWriter();
        HtmlGenerator gen = new HtmlGenerator(new PrintWriter(writer));
        action.accept(gen);
        Assert.assertEquals(expected, writer.toString());
    }

    @Test
    public void newLineTest() {
        checkString(HtmlGenerator::newline, System.lineSeparator());
        checkString(gen -> gen.newline().newline(), System.lineSeparator() + System.lineSeparator());
    }

    @Test
    public void beginTagTest() {
        checkString(gen -> gen.beginTag(THE_TAG_1), String.format("<%s>", THE_TAG_1));
        checkString(gen -> gen.beginTag(THE_TAG_1).beginTag(THE_TAG_2), String.format("<%s><%s>", THE_TAG_1, THE_TAG_2));
    }

    @Test
    public void appendTest() {
        checkString(gen -> gen.append(STRING_TO_APPEND), STRING_TO_APPEND);
    }

    @Test
    public void closeLastTagTest() {
        checkString(gen -> gen.beginTag(THE_TAG_1).closeLastTag(), String.format("<%s></%s>", THE_TAG_1, THE_TAG_1));
    }

    @Test
    public void appendInTagTest() {
        checkString(gen -> gen.appendInTag(THE_TAG_1, STRING_TO_APPEND),
                String.format("<%s>%s</%s>", THE_TAG_1, STRING_TO_APPEND, THE_TAG_1));
    }

    @Test
    public void comboTest() {
        var sep = System.lineSeparator();
        checkString(gen ->
                        gen.beginTag(THE_TAG_1).newline()
                                .append(STRING_TO_APPEND).newline()
                                .appendInTag(THE_TAG_2, STRING_TO_APPEND_2).newline()
                                .closeLastTag().newline()
                                .append(STRING_TO_APPEND_3).newline(),
                String.format("<%s>%s%s%s<%s>%s</%s>%s</%s>%s%s%s",
                        THE_TAG_1, sep,
                        STRING_TO_APPEND, sep,
                        THE_TAG_2, STRING_TO_APPEND_2, THE_TAG_2, sep,
                        THE_TAG_1, sep,
                        STRING_TO_APPEND_3, sep));
    }
}

package org.dataflowanalysis.analysis.utils;

public class StringView {
    private final String string;
    private int index;

    public StringView(String string) {
        this.string = string;
        this.index = 0;
    }

    public boolean invalid() {
        return this.index > string.length() || this.index < 0;
    }

    public String getString() {
        if (this.invalid()) {
            throw new IllegalArgumentException();
        }
        return string.substring(this.index);
    }

    public void advance(int amount) {
        this.index += amount;
    }

    public void retreat(int amount) {
        this.index -= amount;
    }

    public boolean startsWith(String prefix) {
        if (this.invalid()) {
            throw new IllegalArgumentException();
        }
        return this.string.substring(index).startsWith(prefix);
    }

    public <T> ParseResult<T> expect(String prefix) {
        if (this.invalid()) {
            throw new IllegalArgumentException();
        }
        return ParseResult.error(this.string + System.lineSeparator() + " ".repeat(this.index) + "- Error: Expected " + prefix);
    }
    public boolean empty() {
        return this.string.length() < this.index;
    }
}

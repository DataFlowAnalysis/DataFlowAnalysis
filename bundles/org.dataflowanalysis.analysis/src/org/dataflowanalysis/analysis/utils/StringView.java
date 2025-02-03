package org.dataflowanalysis.analysis.utils;

/**
 * Represents a view on a given string at a starting index
 */
public class StringView {
    private final String string;
    private int index;

    /**
     * Constructs a new string view with the given string and initializes the view at the beginning
     * @param string String that is viewed though the {@link StringView}
     */
    public StringView(String string) {
        this.string = string;
        this.index = 0;
    }

    /**
     * Determines whether the view on the string is valid
     * @return Returns true, if the index remains in bounds of the string. Otherwise, the method returns false
     */
    public boolean invalid() {
        return this.index > string.length() || this.index < 0;
    }

    /**
     * Returns the stored string beginning at the stored index
     * @throws IllegalArgumentException Throws a {@link IllegalArgumentException} if the view on the string is {@link StringView#invalid()}
     * @return Returns the stored string beginning at the stored index
     */
    public String getString() {
        if (this.invalid()) {
            throw new IllegalArgumentException();
        }
        return string.substring(this.index);
    }

    /**
     * Moves the string view forward by the given amount
     * @param amount Amount to move the string view forward
     */
    public void advance(int amount) {
        this.index += amount;
    }

    /**
     * Moves the string view backward by the given amount
     * @param amount Amount to move the string view backward
     */
    public void retreat(int amount) {
        this.index -= amount;
    }

    /**
     * Determines whether the view of the stored string begins with the given prefix
     * @param prefix Prefix that the view of the stored string is compared against
     * @throws IllegalArgumentException Throws a {@link IllegalArgumentException} if the view on the string is {@link StringView#invalid()}
     * @return Returns true, if the view of the stored string begins with the given prefix.
     *          Otherwise, the method returns false
     */
    public boolean startsWith(String prefix) {
        if (this.invalid()) {
            throw new IllegalArgumentException();
        }
        return this.string.substring(index).startsWith(prefix);
    }

    /**
     * Returns an erroneous {@link ParseResult} that contains an error, specifying an expected given prefix
     * @param prefix Given prefix that was expected
     * @throws IllegalArgumentException Throws a {@link IllegalArgumentException} if the view on the string is {@link StringView#invalid()}
     * @return Returns a {@link ParseResult} with the given error
     * @param <T> Type of the {@link ParseResult} that is not relevant here
     */
    public <T> ParseResult<T> expect(String prefix) {
        if (this.invalid()) {
            throw new IllegalArgumentException();
        }
        return ParseResult.error(this.string + System.lineSeparator() + " ".repeat(this.index) + "- Error: Expected " + prefix);
    }

    /**
     * Determines whether the string view has overrun and is empty
     * @return Returns true, if the string view has overrun its bounds and is empty.
     *          Otherwise, the method returns false.
     */
    public boolean empty() {
        return this.string.length() < this.index;
    }
}

package org.dataflowanalysis.analysis.utils;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a result from parsing.
 * It may contain a result with type {@link T} or an error as a {@link String}
 * @param <T> Resulting type after parsing
 */
public class ParseResult<T> {
    private final Optional<T> result;
    private final Optional<String> error;

    /**
     * Creates a new {@link ParseResult} with the given successful result
     * @param result Successful result stored in the {@link ParseResult}
     */
    public ParseResult(T result) {
        this.result = Optional.of(result);
        this.error = Optional.empty();
    }

    /**
     * Creates a new {@link ParseResult} with the given error
     * @param error Error stored in the {@link ParseResult}
     */
    public ParseResult(String error) {
        this.result = Optional.empty();
        this.error = Optional.of(error);
    }

    /**
     * Indicates whether the parse result contains a successful result
     * @return Returns true, if the {@link ParseResult} contains a successful result. Otherwise, the method returns false
     */
    public boolean successful() {
        return result.isPresent();
    }

    /**
     * Indicates whether the parse result contains a erroneous result
     * @return Returns true, if the {@link ParseResult} contains a erroneous result. Otherwise, the method returns false
     */
    public boolean failed() { return error.isPresent(); };

    /**
     * Returns the contained result in the {@link ParseResult}, if present
     * @throws NoSuchElementException Throws an {@link NoSuchElementException} when the {@link ParseResult} is erroneous
     * @return Returns the contained result in the {@link ParseResult}
     */
    public T getResult() {
        if (this.result.isEmpty()) throw new NoSuchElementException();
        return result.get();
    }

    /**
     * Returns the contained error in the {@link ParseResult}, if present
     * @throws NoSuchElementException Throws an {@link NoSuchElementException} when the {@link ParseResult} is successful
     * @return Returns the contained error in the {@link ParseResult}
     */
    public String getError() {
        if (this.error.isEmpty()) throw new NoSuchElementException();
        return error.get();
    }

    /**
     * Maps the successful result of the {@link ParseResult}, if it is present using the given function
     * This is safe, because the value being cast (T) is null/not present
     * @param function Function that maps the result typed {@link T} to a result typed {@link N}
     * @return Returns a {@link ParseResult} containing the same error or the mapped value after applying the function
     * @param <N> Return type of the function and contained type in the returned {@link ParseResult}
     */
    @SuppressWarnings("unchecked")
    public <N> ParseResult<N> map(Function<T, N> function) {
        if (this.error.isPresent()) return (ParseResult<N>) this;
        return new ParseResult<>(function.apply(this.getResult()));
    }

    /**
     * Chains two {@link ParseResult}, if the {@link ParseResult} is erroneous.
     * If the {@link ParseResult} is present, this function equals the identity function.
     * @param other Other {@link ParseResult} that is used, when this {@link ParseResult} is not erroneous
     * @return Returns this, when this {@link ParseResult} is successful. Otherwise, it returns the provided {@link ParseResult}
     */
    public ParseResult<T> orElse(ParseResult<T> other) {
        if (this.result.isPresent()) return this;
        return other;
    }

    /**
     * Returns either this {@link ParseResult}, if it is successful, or the other provided value
     * @param other Other provided value of the same type as {@link T}
     * @return Returns the contained successful value in the {@link ParseResult}, if it is present.
     *          Otherwise, the method returns the given parameter of the same type.
     */
    public T or(T other) {
        if (this.result.isPresent()) return this.getResult();
        return other;
    }

    /**
     * Evaluates the given predicate, if a successful result is present
     * @param predicate Given predicate that is run if the {@link ParseResult} is successful
     */
    public void ifPresent(Predicate<T> predicate) {
        this.result.ifPresent(predicate::test);
    }

    /**
     * Evaluates the given predicate, if a successful result is present.
     * Otherwise, it runs the specified empty action
     * @param predicate Given predicate that is run if the {@link ParseResult} is successful
     * @param emptyAction Action that is run, if the {@link ParseResult} is erroneous
     */
    public void ifPresentOrElse(Predicate<T> predicate, Runnable emptyAction) {
        this.result.ifPresentOrElse(predicate::test, emptyAction);
    }

    /**
     * Creates a new {@link ParseResult} with the given error
     * @param error Error stored in the {@link ParseResult}
     * @return Returns a new {@link ParseResult} with the given error
     */
    public static <T> ParseResult<T> error(String error) {
        return new ParseResult<>(error);
    }

    /**
     * Creates a new {@link ParseResult} with the given successful type
     * @param result Result stored in the {@link ParseResult}
     * @return Returns a new {@link ParseResult} with the given successful type
     */
    public static <T> ParseResult<T> ok(T result) {
        return new ParseResult<>(result);
    }

    @Override
    public String toString() {
        if (this.successful()) return this.getResult().toString();
        else return this.getError();
    }
}

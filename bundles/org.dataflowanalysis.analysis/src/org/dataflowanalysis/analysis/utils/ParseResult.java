package org.dataflowanalysis.analysis.utils;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ParseResult<T> {
    private final Optional<T> result;
    private final Optional<String> error;

    public ParseResult(T result) {
        this.result = Optional.of(result);
        this.error = Optional.empty();
    }

    public ParseResult(String error) {
        this.result = Optional.empty();
        this.error = Optional.of(error);
    }

    public boolean successful() {
        return result.isPresent();
    }

    public boolean failed() { return error.isPresent(); };

    public T getResult() {
        if (this.result.isEmpty()) throw new NoSuchElementException();
        return result.get();
    }

    public String getError() {
        if (this.error.isEmpty()) throw new NoSuchElementException();
        return error.get();
    }

    /**
     * This is safe, because the value being cast (T) is null/not present
     * @param function
     * @return
     * @param <N>
     */
    @SuppressWarnings("unchecked")
    public <N> ParseResult<N> map(Function<T, N> function) {
        if (this.error.isPresent()) return (ParseResult<N>) this;
        return new ParseResult<>(function.apply(this.getResult()));
    }

    public ParseResult<T> orElse(ParseResult<T> other) {
        if (this.result.isPresent()) return this;
        return other;
    }

    public T or(T other) {
        if (this.result.isPresent()) return this.getResult();
        return other;
    }

    public void ifPresent(Predicate<T> predicate) {
        this.result.ifPresent(predicate::test);
    }

    public void ifPresentOrElse(Predicate<T> predicate, Runnable emptyAction) {
        this.result.ifPresentOrElse(predicate::test, emptyAction);
    }

    public static <T> ParseResult<T> error(String error) {
        return new ParseResult<>(error);
    }

    public static <T> ParseResult<T> ok(T result) {
        return new ParseResult<>(result);
    }

    @Override
    public String toString() {
        if (this.successful()) return this.getResult().toString();
        else return this.getError();
    }
}

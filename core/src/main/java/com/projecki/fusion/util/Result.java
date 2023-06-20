package com.projecki.fusion.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A result class similar to the one in the Rust programming language. Basically an extension off of an optional.
 *
 * @param <V> The type of the value to be used when this result is OK.
 * @param <E> The type of the error to be used when this result is ERROR.
 */
public class Result<V, E> {

    private final @Nullable V value;
    private final @Nullable E error;

    public Result(@Nullable V value, @Nullable E error) {
        this.value = value;
        this.error = error;
    }

    /**
     * Applies a function to this result's value if it is not an error. If it is an error then it returns result of the
     * mapped type with the error value already present in this result. You can be assured that the value you are
     * manipulating in the mapping function will always be non-null as it is only called upon / mapped if it is present
     * and not the error.
     *
     * @param mapper The mapping function for this result
     * @param <T> The type of value to map this value to if present
     * @return A mapped result if its value was present, otherwise a result of the mapped type containing the already
     * present error in this result.
     */
    public <T> Result<T, E> map(Function<V, Result<T, E>> mapper) {
        return this.ifOkayOrElse(mapper, Result::error);
    }

    /**
     * Forcefully unwraps the result with no checks for nullity and returns the current value for value.
     *
     * @return The current value for value (nullable)
     */
    public @Nullable V unwrap() {
        return this.value;
    }

    /**
     * Forcefully unwraps the result with no checks for nullity and returns the current value for error.
     *
     * @return The current value for error (nullable)
     */
    public @Nullable E unwrapError() {
        return this.error;
    }

    /**
     * Returns the current OK value of this result if it is OKAY, otherwise returns the value passed in
     *
     * @param otherwise The fallback value to return if this result is ERROR
     * @return The original OKAY value or the fallback value passed in if original was ERROR.
     */
    public V okayOrElse(V otherwise) {
        return this.value == null ? otherwise : this.value;
    }

    /**
     * Runs a consumer with the action handling if this result was okay or not. Similar to optional.
     *
     * @param okay The action to run, supplied with the non-null value, if this result was okay.
     * @param error The action to run, supplied with the non-null error, if this result was error
     */
    public void ifOkayOrElse(Consumer<V> okay, Consumer<E> error) {
        if (this.value != null) {
            okay.accept(this.value);
        } else {
            error.accept(this.error);
        }
    }

    /**
     * Runs a function for each of the possibilities of this result and returns the value of either function to this
     * function.
     *
     * @param okay The function to run if this result is okay.
     * @param error The function to run if this result is error.
     * @param <T> The return type of the functions of each result outcome and this function
     * @return The value returned from either of the two functions processed based on the value of this result.
     */
    public <T> T ifOkayOrElse(Function<V, T> okay, Function<E, T> error) {
        return this.value != null ? okay.apply(this.value) : error.apply(this.error);
    }

    /**
     * Runs an action with the supplied non-null value if this result is OK.
     *
     * @param action The action to run if this result is OK
     */
    public void ifOkay(Consumer<V> action) {
        if (value != null) {
            action.accept(this.value);
        }
    }

    /**
     * Runs an action with the supplied non-null error if this result is ERROR.
     *
     * @param action The action to run if this result is ERROR
     */
    public void ifError(Consumer<E> action) {
        if (value == null && error != null) {
            action.accept(this.error);
        }
    }

    /**
     * @return Whether this result is OK or not. Note this does NOT check if the result has a non-null error. Though
     * it most likely (%99.9999) is the case that if the result is OK, then it is not ERROR.
     */
    public boolean isOkay() {
        return this.value != null;
    }

    /**
     * @return Whether this result is error or not. Note this does NOT check if the result has a non-null value. Though
     * it most likely (%99.9999) is the case that if the result is ERROR, then it is not OK.
     */
    public boolean isError() {
        return this.error != null;
    }

    /**
     * Constructs a new result with the given OK value.
     *
     * @param value The value for this result to hold as OK.
     * @param <T> The type of the OK value
     * @param <U> The type of the ERROR value
     * @return A constructed, immutable, OK result with the given value.
     */
    public static <T, U> Result<T, U> ok(@NotNull T value) {
        Objects.requireNonNull(value, "Value must be non-null");
        return new Result<>(value, null);
    }

    /**
     * Constructs a new result with the given ERROR value.
     *
     * @param error The value for this result to hold as ERROR.
     * @param <T> The type of the OK value
     * @param <U> The type of the ERROR value
     * @return A constructed, immutable, ERROR result with the given value.
     */
    public static <T, U> Result<T, U> error(@NotNull U error) {
        Objects.requireNonNull(error, "Error must be non-null");
        return new Result<>(null, error);
    }
}

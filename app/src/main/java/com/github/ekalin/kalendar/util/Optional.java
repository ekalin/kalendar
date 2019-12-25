package com.github.ekalin.kalendar.util;

import androidx.arch.core.util.Function;
import androidx.core.util.Consumer;
import androidx.core.util.Supplier;

import java.util.NoSuchElementException;
import java.util.Objects;

public class Optional<T> {
    private static final Optional EMPTY = new Optional();

    private T value;

    private Optional() {
        this.value = null;
    }

    private Optional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> empty() {
        return EMPTY;
    }

    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    public static <T> Optional<T> ofNullable(T value) {
        if (value == null) {
            return Optional.empty();
        } else {
            return Optional.of(value);
        }
    }

    public boolean isPresent() {
        return this.value != null;
    }

    public T get() {
        if (this.value == null) {
            throw new NoSuchElementException("No value present");
        } else {
            return this.value;
        }
    }

    public T orElse(T elseValue) {
        if (isPresent()) {
            return this.value;
        } else {
            return elseValue;
        }
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return this.value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    public void ifPresent(Consumer<? super T> operation) {
        if (isPresent()) {
            operation.accept(this.value);
        }
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        if (!isPresent()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(mapper.apply(this.value));
        }
    }

    public <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        if (!isPresent()) {
            return Optional.empty();
        } else {
            return Objects.requireNonNull(mapper.apply(this.value));
        }
    }
}

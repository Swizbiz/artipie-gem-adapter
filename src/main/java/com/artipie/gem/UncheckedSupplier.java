/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */

package com.artipie.gem;

import com.artipie.ArtipieException;
import java.util.function.Supplier;

/**
 * Supplier to wrap checked supplier throwing checke exception
 * with unchecked one.
 * @param <T> Supplier type
 * @since 1.0
 * @todo #85:30min Move this class to artipie/asto repo
 *  This class was created due to lack of unchecked supplier
 *  implementation in asto project. Let's move it to asto repo.
 */
public final class UncheckedSupplier<T> implements Supplier<T> {

    /**
     * Supplier which throws checked exceptions.
     */
    private final CheckedSupplier<? extends T, ? extends Exception> checked;

    /**
     * Wrap checked supplier with unchecked.
     * @param checked Checked supplier
     */
    public UncheckedSupplier(final CheckedSupplier<T, ? extends Exception> checked) {
        this.checked = checked;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public T get() {
        try {
            return this.checked.get();
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Exception err) {
            throw new ArtipieException(err);
        }
    }

    /**
     * Checked supplier which throws exception.
     * @param <T> Supplier type
     * @param <E> Exception type
     * @since 1.0
     */
    @FunctionalInterface
    public interface CheckedSupplier<T, E extends Exception> {

        /**
         * Get value or throw exception.
         * @return Value
         * @throws Exception of type E
         */
        T get() throws E;
    }
}

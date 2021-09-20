/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem;

import java.nio.file.Path;

/**
 * Gem metadata parser.
 * @since 1.0
 */
public interface GemMeta {

    /**
     * Extract Gem info.
     * @param gem Path to gem
     * @return JSON object
     */
    MetaInfo info(Path gem);

    /**
     * Gem info metadata format.
     * @since 1.0
     */
    interface MetaFormat {

        /**
         * Print info string.
         * @param name Key
         * @param value String
         */
        void print(String name, String value);

        /**
         * Print info child.
         * @param name Key
         * @param value Node
         */
        void print(String name, MetaInfo value);
    }

    /**
     * Metadata info.
     * @since 1.0
     */
    interface MetaInfo {
        /**
         * Print meta info using format.
         * @param fmt Meta format
         */
        void print(MetaFormat fmt);
    }
}

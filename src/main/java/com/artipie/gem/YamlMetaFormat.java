/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem;

import com.amihaiemil.eoyaml.Yaml;
import com.amihaiemil.eoyaml.YamlMapping;
import com.amihaiemil.eoyaml.YamlMappingBuilder;
import com.amihaiemil.eoyaml.YamlSequenceBuilder;
import com.artipie.gem.GemMeta.MetaFormat;
import com.artipie.gem.GemMeta.MetaInfo;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * New JSON format for Gem meta info.
 *
 * @since 1.0
 * @todo #122:30min Add tests for this class and ApiGetSlice.
 *  Check that this class produces valid YAMLs as for JsonMetaFormat.
 *  Also, check that ApiGetSlice returns valid response for `.yaml` suffix
 *  in response path. Test for `ApiGetSlice` could be quite primitive just to check
 *  that slice is working fine with yaml. On the other hand `YamlMetaFormatTest`
 *  must cover all methods of YamlMetaFormat (same as JsonMetaFormatTest).
 */
public final class YamlMetaFormat implements MetaFormat {

    /**
     * Yaml transformations consumer.
     */
    private final Consumer<UnaryOperator<YamlMappingBuilder>> yamler;

    /**
     * New yaml format.
     * @param yamler Yaml transformation consumer
     */
    public YamlMetaFormat(final Consumer<UnaryOperator<YamlMappingBuilder>> yamler) {
        this.yamler = yamler;
    }

    @Override
    public void print(final String name, final String value) {
        this.yamler.accept(yaml -> yaml.add(name, value));
    }

    @Override
    public void print(final String name, final MetaInfo value) {
        final Yamler child = new Yamler();
        value.print(new YamlMetaFormat(child));
        this.yamler.accept(yaml -> yaml.add(name, child.build()));
    }

    @Override
    public void print(final String name, final String[] values) {
        final YamlSequenceBuilder seq = Yaml.createYamlSequenceBuilder();
        for (final String item : values) {
            seq.add(item);
        }
        this.yamler.accept(yaml -> yaml.add(name, seq.build()));
    }

    /**
     * Yaml tranformation consumer with volatile in-memory state.
     * @implNote This implementation is not thread safe
     * @since 1.3
     */
    public static final class Yamler implements Consumer<UnaryOperator<YamlMappingBuilder>> {

        /**
         * Memory for yaml builder.
         */
        private volatile YamlMappingBuilder yaml;

        /**
         * New yaml transformation consumer.
         */
        public Yamler() {
            this(Yaml.createYamlMappingBuilder());
        }

        /**
         * New Yaml tranfsormation consumer with initial state.
         * @param yaml Initial Yaml builder
         */
        public Yamler(final YamlMappingBuilder yaml) {
            this.yaml = yaml;
        }

        @Override
        public void accept(final UnaryOperator<YamlMappingBuilder> transform) {
            this.yaml = transform.apply(this.yaml);
        }

        /**
         * Build yaml from curren state.
         * @return Yaml mapping
         */
        public YamlMapping build() {
            return this.yaml.build();
        }
    }
}

/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem.ruby;

import com.artipie.gem.GemMeta;
import java.nio.file.Path;
import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.javasupport.JavaEmbedUtils;

/**
 * JRuby implementation of GemInfo metadata parser.
 * @since 1.0
 */
public final class RubyGemMeta implements GemMeta, SharedRuntime.RubyPlugin {

    /**
     * Ruby runtime.
     */
    private final Ruby ruby;

    /**
     * Ctor.
     * @param ruby Runtime
     */
    public RubyGemMeta(final Ruby ruby) {
        this.ruby = ruby;
    }

    @Override
    public GemMeta.MetaInfo info(final Path gem) {
        final RubyObject spec = (RubyObject) JavaEmbedUtils.newRuntimeAdapter().eval(
            this.ruby, String.format(
                "Gem::Package.new('%s').spec", gem.toString()
            )
        );
        return new RubyMetaInfo(spec);
    }

    @Override
    public String identifier() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public void initialize() {
        JavaEmbedUtils.newRuntimeAdapter()
            .eval(this.ruby, "require 'rubygems/package.rb'");
    }

    /**
     * Meta info implementation for Ruby spec object.
     * @since 1.0
     */
    private static final class RubyMetaInfo implements GemMeta.MetaInfo {

        /**
         * Ruby meta spec object.
         */
        private final RubyObject spec;

        /**
         * New meta info.
         * @param spec Spec object
         */
        RubyMetaInfo(final RubyObject spec) {
            this.spec = spec;
        }

        @Override
        public void print(final MetaFormat fmt) {
            this.spec.getVariableList().stream()
                .filter(item -> item.getValue() != null).forEach(
                    node -> fmt.print(
                        node.getName().substring(1),
                        node.getValue().toString()
                    )
            );
        }
    }
}

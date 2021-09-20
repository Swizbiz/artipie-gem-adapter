/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */

package com.artipie.gem.ruby;

import com.artipie.ArtipieException;
import com.artipie.gem.GemIndex;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.apache.commons.io.IOUtils;
import org.jruby.Ruby;
import org.jruby.RubyRuntimeAdapter;
import org.jruby.javasupport.JavaEmbedUtils;

/**
 * Ruby runtime gem index implementation.
 *
 * @since 1.0
 */
public final class RubyGemIndex implements GemIndex {

    /**
     * Ruby runtime.
     */
    private final Ruby ruby;

    /**
     * New gem indexer.
     * @param ruby Runtime
     */
    public RubyGemIndex(final Ruby ruby) {
        this.ruby = ruby;
    }

    @Override
    public void update(final Path path) {
        final RubyRuntimeAdapter adapter = JavaEmbedUtils.newRuntimeAdapter();
        try {
            final String script = IOUtils.toString(
                RubyGemIndex.class.getResourceAsStream(String.format("/metarunner.rb")),
                StandardCharsets.UTF_8
            );
            adapter.eval(this.ruby, script);
            JavaEmbedUtils.invokeMethod(
                this.ruby,
                adapter.eval(this.ruby, "MetaRunner"),
                "new",
                new Object[]{path.toString()},
                Object.class
            );
        } catch (final IOException err) {
            throw new ArtipieException(err);
        }
    }
}

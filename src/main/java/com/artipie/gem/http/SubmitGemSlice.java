/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem.http;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.gem.Gem;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.slice.ContentWithSize;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.UUID;
import org.reactivestreams.Publisher;

/**
 * A slice, which servers gem packages.
 * @since 1.0
 */
final class SubmitGemSlice implements Slice {

    /**
     * Repository storage.
     */
    private final Storage storage;

    /**
     * Gem SDK.
     */
    private final Gem gem;

    /**
     * Ctor.
     *
     * @param storage The storage.
     */
    SubmitGemSlice(final Storage storage) {
        this.storage = storage;
        this.gem = new Gem(storage);
    }

    @Override
    public Response response(final String line, final Iterable<Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        final Key key = new Key.From(
            "gems", UUID.randomUUID().toString().replace("-", "").concat(".gem")
        );
        return new AsyncResponse(
            this.storage.save(
                key, new ContentWithSize(body, headers)
            ).thenCompose(none -> this.gem.update(key))
            .thenCompose(none -> this.storage.delete(key))
            .thenApply(none -> new RsWithStatus(RsStatus.CREATED))
        );
    }
}

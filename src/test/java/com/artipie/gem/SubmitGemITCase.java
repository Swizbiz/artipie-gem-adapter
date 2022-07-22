/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem;

import com.artipie.asto.fs.FileStorage;
import com.artipie.gem.http.GemSlice;
import com.artipie.http.rs.RsStatus;
import com.artipie.vertx.VertxSliceServer;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.WebClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A test for gem submit operation.
 *
 * @since 0.2
 */
public class SubmitGemITCase {

    @Test
    public void submitResultsInOkResponse(@TempDir final Path temp) throws IOException {
        final Vertx vertx = Vertx.vertx();
        final VertxSliceServer server = new VertxSliceServer(
            vertx,
            new GemSlice(new FileStorage(temp))
        );
        final WebClient web = WebClient.create(vertx);
        final int port = server.start();
        final byte[] gem = Files.readAllBytes(
            Paths.get("./src/test/resources/builder-3.2.4.gem")
        );
        final int code = web.post(port, "localhost", "/api/v1/gems")
            .rxSendBuffer(Buffer.buffer(gem))
            .blockingGet()
            .statusCode();
        MatcherAssert.assertThat(
            code,
            new IsEqual<>(Integer.parseInt(RsStatus.CREATED.code()))
        );
        web.close();
        server.close();
        vertx.close();
    }
}

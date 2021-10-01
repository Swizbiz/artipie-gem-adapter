/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem.http;

import com.artipie.asto.fs.FileStorage;
import com.artipie.asto.test.TestResource;
import com.artipie.http.Headers;
import com.artipie.http.hm.IsJson;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.SliceHasResponse;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rq.RqMethod;
import java.io.IOException;
import java.nio.file.Path;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wtf.g4s8.hamcrest.json.JsonHas;

/**
 * A test for gem submit operation.
 *
 * @since 0.7
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class ApiGetSliceTest {
    @Test
    public void queryResultsInOkResponse(@TempDir final Path tmp) throws IOException {
        new TestResource("gviz-0.3.5.gem").saveTo(tmp.resolve("./gviz-0.3.5.gem"));
        MatcherAssert.assertThat(
            new ApiGetSlice(new FileStorage(tmp)),
            new SliceHasResponse(
                new RsHasBody(new IsJson(new JsonHas("name", "gviz"))),
                new RequestLine(RqMethod.GET, "/api/v1/gems/gviz.json"),
                Headers.EMPTY,
                com.artipie.asto.Content.EMPTY
            )
        );
    }
}


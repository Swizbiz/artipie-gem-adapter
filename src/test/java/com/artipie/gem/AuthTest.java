/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem;

import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.asto.test.TestResource;
import com.artipie.gem.http.GemSlice;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.Permissions;
import com.artipie.http.headers.Authorization;
import com.artipie.http.headers.Header;
import com.artipie.http.hm.RsHasBody;
import com.artipie.http.hm.RsHasHeaders;
import com.artipie.http.hm.RsHasStatus;
import com.artipie.http.rq.RequestLine;
import com.artipie.http.rs.RsStatus;
import io.reactivex.Flowable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.cactoos.text.Base64Encoded;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.Test;

/**
 * A test for api key endpoint.
 *
 * @since 0.3
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class AuthTest {

    @Test
    public void keyIsReturned() {
        final String token = "aGVsbG86d29ybGQ=";
        final Headers headers = new Headers.From(
            new Authorization(String.format("Basic %s", token))
        );
        MatcherAssert.assertThat(
            new GemSlice(new InMemoryStorage()).response(
                new RequestLine("GET", "/api/v1/api_key").toString(),
                headers,
                Flowable.empty()
            ), new RsHasBody(token.getBytes(StandardCharsets.UTF_8))
        );
    }

    @Test
    public void unauthorizedWhenNoIdentity() {
        MatcherAssert.assertThat(
            new GemSlice(
                new InMemoryStorage(),
                Permissions.FREE,
                Authentication.ANONYMOUS
            ).response(
                new RequestLine("GET", "/api/v1/api_key").toString(),
                new Headers.From(),
                Flowable.empty()
            ), new RsHasStatus(RsStatus.UNAUTHORIZED)
        );
    }

    @Test
    public void notAllowedToPushUsersAreRejected() throws IOException {
        final String lgn = "usr";
        final String pwd = "pwd";
        final String token = new Base64Encoded(String.format("%s:%s", lgn, pwd)).asString();
        MatcherAssert.assertThat(
            new GemSlice(
                new InMemoryStorage(),
                new Permissions.Single(lgn, "download"),
                new Authentication.Single(lgn, pwd)
            ).response(
                new RequestLine("POST", "/api/v1/gems").toString(),
                new Headers.From(new Authorization(token)),
                Flowable.empty()
            ), new RsHasStatus(RsStatus.FORBIDDEN)
        );
    }

    @Test
    public void notAllowedToInstallsUsersAreRejected() throws IOException {
        final String lgn = "usr";
        final String pwd = "pwd";
        final String token = new Base64Encoded(String.format("%s:%s", lgn, pwd)).asString();
        MatcherAssert.assertThat(
            new GemSlice(
                new InMemoryStorage(),
                new Permissions.Single(String.format("another %s", lgn), "download"),
                new Authentication.Single(lgn, pwd)
            ).response(
                new RequestLine("GET", "specs.4.8").toString(),
                new Headers.From(new Authorization(token)),
                Flowable.empty()
            ), new RsHasStatus(RsStatus.FORBIDDEN)
        );
    }

    @Test
    public void returnsUnauthorizedIfUnableToAuthenticate() throws IOException {
        MatcherAssert.assertThat(
            AuthTest.postWithBasicAuth(false),
            new AllOf<>(
                Arrays.asList(
                    new RsHasStatus(RsStatus.UNAUTHORIZED),
                    new RsHasHeaders(new Header("WWW-Authenticate", "Basic"))
                )
            )
        );
    }

    @Test
    public void returnsOkWhenBasicAuthTokenCorrect() throws IOException {
        MatcherAssert.assertThat(
            AuthTest.postWithBasicAuth(true),
            new RsHasStatus(RsStatus.CREATED)
        );
    }

    private static Response postWithBasicAuth(final boolean authorized) throws IOException {
        final String user = "alice";
        final String pswd = "123";
        final String token;
        if (authorized) {
            token = new Base64Encoded(String.format("%s:%s", user, pswd)).asString();
        } else {
            token = new Base64Encoded(String.format("%s:wrong%s", user, pswd)).asString();
        }
        return new GemSlice(
            new InMemoryStorage(),
            new Permissions.Single(user, "upload"),
            new Authentication.Single(user, pswd)
        ).response(
            new RequestLine("POST", "/api/v1/gems").toString(),
            new Headers.From(
                new Authorization(String.format("Basic %s", token))
            ),
            Flowable.just(
                ByteBuffer.wrap(new TestResource("rails-6.0.2.2.gem").asBytes())
            )
        );
    }
}

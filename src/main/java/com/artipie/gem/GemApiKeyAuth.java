/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem;

import com.artipie.http.auth.AuthScheme;
import com.artipie.http.auth.Authentication;
import com.artipie.http.auth.BasicAuthScheme;
import com.artipie.http.headers.Authorization;
import com.artipie.http.rq.RqHeaders;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.cactoos.text.Base64Decoded;

/**
 * {@link AuthScheme} implementation for gem api key decoding.
 * @since 0.6
 */
public final class GemApiKeyAuth implements AuthScheme {

    /**
     * Concrete implementation for User Identification.
     */
    private final Authentication auth;

    /**
     * Ctor.
     * @param auth Concrete implementation for User Identification.
     */
    public GemApiKeyAuth(final Authentication auth) {
        this.auth = auth;
    }

    @Override
    public CompletionStage<Result> authenticate(
        final Iterable<Map.Entry<String, String>> headers,
        final String header
    ) {
        return new RqHeaders(headers, Authorization.NAME).stream()
            .findFirst()
            .map(
                str -> {
                    final CompletionStage<Result> res;
                    if (str.startsWith(BasicAuthScheme.NAME)) {
                        res = new BasicAuthScheme(this.auth).authenticate(headers);
                    } else {
                        res = CompletableFuture.completedFuture(
                            Optional.of(str)
                                .map(Base64Decoded::new)
                                .map(dec -> dec.toString().split(":"))
                                .flatMap(
                                    cred -> this.auth.user(cred[0].trim(), cred[1].trim())
                                )
                                .<Result>map(Success::new)
                                .orElseGet(Failure::new)
                        );
                    }
                    return res;
                }
            )
            .get();
    }

    /**
     * Successful result with authenticated user.
     *
     * @since 0.5.4
     */
    private static class Success implements AuthScheme.Result {

        /**
         * Authenticated user.
         */
        private final Authentication.User usr;

        /**
         * Ctor.
         *
         * @param user Authenticated user.
         */
        Success(final Authentication.User user) {
            this.usr = user;
        }

        @Override
        public Optional<Authentication.User> user() {
            return Optional.of(this.usr);
        }

        @Override
        public String challenge() {
            return "";
        }
    }

    /**
     * Failed result without authenticated user.
     *
     * @since 0.5.4
     */
    private static class Failure implements AuthScheme.Result {

        @Override
        public Optional<Authentication.User> user() {
            return Optional.empty();
        }

        @Override
        public String challenge() {
            return "";
        }
    }
}

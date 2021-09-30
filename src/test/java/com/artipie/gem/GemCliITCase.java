/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem;

import com.artipie.asto.fs.FileStorage;
import com.artipie.asto.test.TestResource;
import com.artipie.gem.http.GemSlice;
import com.artipie.vertx.VertxSliceServer;
import io.vertx.reactivex.core.Vertx;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;

/**
 * A test which ensures {@code gem} console tool compatibility with the adapter.
 *
 * @since 0.2
 * @checkstyle StringLiteralsConcatenationCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 */
@SuppressWarnings("PMD.SystemPrintln")
@DisabledIfSystemProperty(named = "os.name", matches = "Windows.*")
final class GemCliITCase {

    /**
     * Ruby Docker container.
     */
    private RubyContainer container;

    /**
     * Vertx instance for Artipie server.
     */
    private Vertx vertx;

    /**
     * Artipie server.
     */
    private VertxSliceServer server;

    /**
     * Base URL.
     */
    private String base;

    @BeforeEach
    void setUp(@TempDir final Path temp) throws Exception {
        this.vertx = Vertx.vertx();
        this.server = new VertxSliceServer(
            this.vertx, new GemSlice(new FileStorage(temp))
        );
        final int port = this.server.start();
        this.base = String.format("http://host.testcontainers.internal:%d", port);
        System.out.printf("Started artipie gem server: %s\n", this.base);
        this.container = new RubyContainer()
            .withWorkingDirectory("/w")
            .withCommand("tail", "-f", "/dev/null");
        Testcontainers.exposeHostPorts(port);
        this.container.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (this.server != null) {
            this.server.close();
        }
        if (this.vertx != null) {
            this.vertx.close();
        }
        if (this.container != null) {
            this.container.close();
        }
    }

    @Test
    void gemPushAndInstallWorks()
        throws IOException, InterruptedException {
        final Set<String> gems = new HashSet<>(
            Arrays.asList(
                "builder-3.2.4.gem", "rails-6.0.2.2.gem",
                "file-tail-1.2.0.gem"
            )
        );
        gems.stream().forEach(
            name -> this.container.copyFileToContainer(
                Transferable.of(new TestResource(name).asBytes()),
                String.format("/w/%s", name)
            )
        );
        for (final String gem : gems) {
            MatcherAssert.assertThat(
                String.format("'gem `%s` push to %s failed with non-zero code", gem, this.base),
                bash(
                    this.container,
                    String.format(
                        "env GEM_HOST_API_KEY='dXNyOnB3ZA==' gem push %s --host %s",
                        gem, this.base
                    )
                ),
                Matchers.equalTo(0)
            );
        }
        for (final String gem : gems) {
            MatcherAssert.assertThat(
                String.format("'gem `%s` fetch from %s failed with non-zero code", gem, this.base),
                bash(
                    this.container,
                    String.format(
                        "GEM_HOST_API_KEY='dXNyOnB3ZA==' gem fetch -V %s --source %s",
                        gem.substring(0, gem.lastIndexOf('-')), this.base
                    )
                ),
                Matchers.equalTo(0)
            );
        }
    }

    /**
     * Executes a bash command in a ruby container.
     * @param ruby The ruby container.
     * @param command Bash command to execute.
     * @return Exit code.
     * @throws IOException If fails.
     * @throws InterruptedException If fails.
     */
    private static int bash(final RubyContainer ruby, final String command)
        throws IOException, InterruptedException {
        final Container.ExecResult exec = ruby.execInContainer(
            "/bin/bash",
            "-c",
            command
        );
        if (!exec.getStderr().equals("")) {
            throw new IllegalStateException(exec.getStderr());
        }
        return exec.getExitCode();
    }

    /**
     * Inner subclass to instantiate Ruby container.
     *
     * @since 0.1
     */
    private static class RubyContainer extends GenericContainer<RubyContainer> {
        RubyContainer() {
            super("ruby:2.7");
        }
    }
}

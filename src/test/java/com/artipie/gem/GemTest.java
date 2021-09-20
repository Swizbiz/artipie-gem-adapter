/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.gem;

import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.asto.blocking.BlockingStorage;
import com.artipie.asto.memory.InMemoryStorage;
import com.artipie.asto.test.TestResource;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Gem} SDK.
 *
 * @since 1.0
 */
public class GemTest {

    @Test
    public void updateRepoIndex() throws Exception {
        final Storage repo = new InMemoryStorage();
        final Key target = new Key.From("gems", UUID.randomUUID().toString());
        new TestResource("builder-3.2.4.gem").saveTo(repo, target);
        final Gem gem = new Gem(repo);
        gem.update(target).toCompletableFuture().join();
        MatcherAssert.assertThat(
            new BlockingStorage(repo).list(Key.ROOT)
                .stream().map(Key::string)
                .collect(Collectors.toSet()),
            Matchers.hasItems(
                "prerelease_specs.4.8",
                "prerelease_specs.4.8.gz",
                "specs.4.8",
                "specs.4.8.gz",
                "latest_specs.4.8",
                "latest_specs.4.8.gz",
                "quick/Marshal.4.8/builder-3.2.4.gemspec.rz",
                "gems/builder-3.2.4.gem"
            )
        );
    }
}

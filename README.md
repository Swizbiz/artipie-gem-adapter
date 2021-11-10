<img src="https://www.artipie.com/logo.svg" width="64px" height="64px"/>

`gem-adapter` is a SDK for managing Gem repositories with low-level operations and HTTP endpoint for Gem repository.

[![Maven Build](https://github.com/artipie/gem-adapter/actions/workflows/maven.yml/badge.svg)](https://github.com/artipie/gem-adapter/actions/workflows/maven.yml)
[![Javadoc](http://www.javadoc.io/badge/com.artipie/gem-adapter.svg)](http://www.javadoc.io/doc/com.artipie/gem-adapter)
[![Maven Central](https://img.shields.io/maven-central/v/com.artipie/gem-adapter.svg)](https://maven-badges.herokuapp.com/maven-central/com.artipie/gem-adapter)

`gem-adapter` is a slice in Artpie, aimed to support gem packages.

This is the dependency you need:

```xml
<dependency>
  <groupId>com.artipie</groupId>
  <artifactId>gem-adapter</artifactId>
  <version>[...]</version>
</dependency>
```

Read the [Javadoc](http://www.javadoc.io/doc/com.artipie/gem-adapter)
for more technical details.

## Usage

There are two public APIs for working with Gem repository:
 - low-level `Gem` SDK
 - high-level `GemSlice` HTTP endpoint

### Gem SDK

Create a new instance of `Gem` class with: `new Gem(storage)`,
where `storage` is a [asto](https://github.com/artipie/asto) `Storage` implementation
with Gem repository.

To **update** repository with a new `gem` package use `gem.update(key)`, where `key` is a package key in storage.

For retreiving package spec info use `gem.info(key)` method with, where `key` is a package key in storage.
It returns future with `MetaInfo` interface, which can be printed to one of standard formats with `meta.print(fmt)`:
 - `JsonMetaFormat` - for JSON meta spec format
 - `YamlMetaFormat` - for YAML meta spec format

To extract **dependencies** binary metadata of packages, use `gem.dependencies(names)` method, where
`names` is a set of gem names (`Set<String>`); this method returns future with binary dependencies metadata
merged for multiple packages.

### HTTP endpoint

To integrate Gem HTTP endpoint to server, use `GemSlice` class instance: `new GemSlice(storage)`, where
`storage` is a repository storage for gem packages. This `Slice` implementation exposes standard Gem repository
APIs and could be used by `gem` CLI.

## Useful links

* [RubyGem Index Internals](https://blog.packagecloud.io/eng/2015/12/15/rubygem-index-internals/) - File structure and gem format
* [Make Your Own Gem](https://guides.rubygems.org/make-your-own-gem/) - How to create and publish
a simple ruby gem into rubygems.org registry.
* [rubygems.org API](https://guides.rubygems.org/rubygems-org-api/) - A page with rubygems.org 
API specification 
* [Gugelines at rubygems.org](https://guides.rubygems.org/) - Guidelines around the `gem` package 
manager.

## Similar solutions

* [Artifactory RubyGems Repositories](https://www.jfrog.com/confluence/display/JFROG/RubyGems+Repositories)
* [Gem in a Box](https://github.com/geminabox/geminabox)
* [Gemfury](https://gemfury.com/l/gem-server)
* `gem server` [command](https://guides.rubygems.org/run-your-own-gem-server/)   
 
## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

To avoid build errors use Maven 3.2+.


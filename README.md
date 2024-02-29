Android bindings for [gofeed](https://github.com/mmcdole/gofeed)

# Usage

Add this to your gradle file

```
implementation("com.nononsenseapps.gofeed:gofeed-android:0.1.0")
```

And use in code like this

```
// parseBodyBytes is also available
val jsonByteArray = gofeedandroid.Gofeedandroid.parseBodyString(rssFeed)
```

# Building the project

## Requirements before building the first time

```
go install golang.org/x/mobile/cmd/gomobile@latest
gomobile init
```

## Building

```
./gradlew bundleReleaseAar
```

## Publishing

```
./gradlew publish
```

Then go to https://oss.sonatype.org/#stagingRepositories and close and release the repository.

## Updating gofeed

Exclude commit/version to get latest version.

```
go get -u github.com/mmcdole/gofeed@[COMMIT/VERSION]
```

# Tests in downstream project

If you want to include the bindings in unit tests, they must run as Android instrumentation tests (emulator tests).

The bindings won't work in JVM unit tests.

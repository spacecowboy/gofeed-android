plugins {
  `maven-publish`
  signing
}

val releaseAarFile = project.buildDir.resolve("outputs/aar/lib-release.aar")
val minSdk = 23

val goSources = fileTree(project.projectDir) {
  include("**/*.go", "go.mod", "go.sum")
  exclude("build/**")
}

tasks {
  register("bundleDebugAar", Exec::class.java) {
    group = "build"
    description = "Builds the debug AAR"

    val aar = project.buildDir.resolve("outputs/aar/lib-debug.aar")

    outputs.files(
      aar,
      project.buildDir.resolve("outputs/aar/lib-debug-sources.jar"),
    )

    inputs.files(goSources)

    // Important to align to 16KB page size for Android
    // Set the flags with environment variable because commandline argument does not support memory alignment flags to the linker
    environment["CGO_LDFLAGS"] = "-Wl,-z,max-page-size=16384"
    commandLine("gomobile", "bind", "-v", "-androidapi", "$minSdk", "-o", "$aar", "-target=android", "github.com/spacecowboy/gofeed-android")
  }

  named("clean") {
    doLast {
      file("build").deleteRecursively()
    }
  }

  register("test") {
    group = "verification"
    description = "Runs all tests"
  }

  named("check") {
    dependsOn("test")
  }

  named("build") {
    dependsOn("bundleDebugAar", "bundleReleaseAar", "check")
  }
}

val bundleReleaseAar = tasks.register("bundleReleaseAar", Exec::class.java) {
  group = "build"
  description = "Builds the release AAR"
  shouldRunAfter("bundleDebugAar")

  val aar = releaseAarFile

  outputs.files(
    aar,
    project.buildDir.resolve("outputs/aar/lib-release-sources.jar"),
  )

  inputs.files(goSources)

  // Important to align to 16KB page size for Android
  // Set the flags with environment variable because commandline argument does not support memory alignment flags to the linker
  environment["CGO_LDFLAGS"] = "-Wl,-z,max-page-size=16384"
  environment["CGO_CFLAGS"] = "-O2 -g -s -w"
  commandLine("gomobile", "bind", "-v", "-androidapi", "$minSdk", "-o", "$aar", "-target=android", "github.com/spacecowboy/gofeed-android")
}

configurations {
  create("gomobile")
}

val releaseArtifact = project.artifacts.add("gomobile", releaseAarFile) {
  type = "aar"
  builtBy(bundleReleaseAar)
}

publishing {
  repositories {
    maven {
      name = "ossrh"
      url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
      val nexusUsername: String? by project
      val nexusPassword: String? by project
      credentials {
        username = nexusUsername
        password = nexusPassword
      }
    }
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/spacecowboy/gofeed-android")
      credentials {
        username = System.getenv("GITHUB_ACTOR")
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
  publications {
    create<MavenPublication>("ossrh") {
      groupId = rootProject.group.toString()
      artifactId = "gofeed-android"
      version = rootProject.version.toString()

      artifact(releaseArtifact)

      pom {
        name.set("gofeed-android")
        description.set("Android bindings for Gofeed")
        url.set("https://github.com/spacecowboy/gofeed-android")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("spacecowboy")
            name.set("Jonas Kalderstam")
            email.set("jonas@cowboyprogrammer.org")
          }
        }
        scm {
            connection.set("scm:git:git://github.com/spacecowboy/gofeed-android.git")
            developerConnection.set("scm:git:ssh://github.com/spacecowboy/gofeed-android.git")
            url.set("https://github.com/spacecowboy/gofeed-android")
        }
      }
    }
  }
  publications {
    create<MavenPublication>("gpr") {
      groupId = rootProject.group.toString()
      artifactId = "gofeed-android"
      version = rootProject.version.toString()

      artifact(releaseArtifact)

      pom {
        name.set("gofeed-android")
        description.set("Android bindings for Gofeed")
        url.set("https://github.com/spacecowboy/gofeed-android")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("spacecowboy")
            name.set("Jonas Kalderstam")
            email.set("jonas@cowboyprogrammer.org")
          }
        }
        scm {
            connection.set("scm:git:git://github.com/spacecowboy/gofeed-android.git")
            developerConnection.set("scm:git:ssh://github.com/spacecowboy/gofeed-android.git")
            url.set("https://github.com/spacecowboy/gofeed-android")
        }
      }
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications.findByName("ossrh"))
}

// The maven-publish plugin creates a publish task for every
// publication x repository combination. Restrict each publication
// to its intended repository so `gpr` only goes to GitHubPackages
// and `ossrh` only goes to MavenCentral.
tasks.withType<PublishToMavenRepository>().configureEach {
  onlyIf {
    (publication.name == "gpr" && repository.name == "GitHubPackages") ||
      (publication.name == "ossrh" && repository.name == "ossrh")
  }
}

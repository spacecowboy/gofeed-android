plugins {
  `maven-publish`
  signing
}

val releaseAarFile = project.buildDir.resolve("outputs/aar/lib-release.aar")
val minSdk = 23

tasks {
  register("bundleDebugAar", Exec::class.java) {
    group = "build"
    description = "Builds the debug AAR"

    val aar = project.buildDir.resolve("outputs/aar/lib-debug.aar")

    outputs.files(
      aar,
      project.buildDir.resolve("outputs/aar/lib-debug-sources.jar"),
    )

    inputs.files(
      project.projectDir.resolve("export.go"),
      project.projectDir.resolve("go.mod"),
      project.projectDir.resolve("go.sum"),
    )

    commandLine("gomobile", "bind", "-v", "-androidapi", "$minSdk", "-o", "$aar", "-target=android", "github.com/spacecowboy/gofeed-android")
  }

  named("clean") {
//    group = "build"
//    description = "Deletes the build directory"
    doLast {
      file("build").deleteRecursively()
    }
  }

  register("test") {
    group = "verification"
    description = "Runs all tests"
  }

  named("check") {
//    group = "verification"
//    description = "Runs all checks"
    dependsOn("test")
  }

  named("build") {
//    group = "build"
//    description = "Builds the debug and release AARs"
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

  inputs.files(
    project.projectDir.resolve("export.go"),
    project.projectDir.resolve("go.mod"),
    project.projectDir.resolve("go.sum"),
  )

  commandLine("gomobile", "bind", "-v", "-androidapi", "$minSdk", "-ldflags", "-w -s", "-o", "$aar", "-target=android", "github.com/spacecowboy/gofeed-android")
}

configurations {
  create("gomobile")
}

val releaseArtifact = project.artifacts.add("gomobile", releaseAarFile) {
  type = "aar"
  builtBy(bundleReleaseAar)
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "com.nononsenseapps.gofeed"
      artifactId = "gofeed-android"
      version = "0.1.0"

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
  sign(publishing.publications.findByName("maven"))
}

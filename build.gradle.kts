import cn.lalaki.pub.BaseCentralPortalPlusExtension.PublishingType

val user = "OmyDaGreat"
val repo = "Malefix"
val g = "xyz.malefic.frc"
val artifact = "malefix"
val v = "1.0.0"
val desc = "A Kotlin util library for FRC!"

val localMavenRepo = uri(layout.buildDirectory.dir("repo").get())

plugins {
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.central)
    alias(libs.plugins.dokka)
}

group = g
version = v

repositories {
    mavenCentral()
    maven { url = uri("https://frcmaven.wpi.edu/artifactory/release/") }
    maven { url = uri("https://plugins.gradle.org/m2/") }
    maven { url = uri("https://maven.ctr-electronics.com/release/") }
    maven { url = uri("https://maven.revrobotics.com/") }
    maven { url = uri("https://maven.photonvision.org/repository/internal") }
    maven { url = uri("https://frcmaven.wpi.edu/artifactory/littletonrobotics-mvn-release/") }
    maven { url = uri("https://lib.choreo.autos/dep") }
    maven { url = uri("https://shenzhen-robotics-alliance.github.io/maple-sim/vendordep/repos/releases") }
    maven { url = uri("https://3015rangerrobotics.github.io/pathplannerlib/repo") }
}

dependencies {
    implementation(libs.kermit)
    implementation(libs.bundles.wpilib)
    implementation(libs.bundles.advantagekit)
    implementation(libs.bundles.vendor)
    testImplementation(kotlin("test"))
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka"))
    }
    pluginsConfiguration.html {
        footerMessage.set("&copy; 2025 Om Gupta &lt;ogupta4242@gmail.com&gt;")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(17))
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(g, artifact, v)

    pom {
        name = repo
        description = desc
        inceptionYear = "2025"
        url = "https://github.com/$user/$repo"
        licenses {
            license {
                name = "MIT License"
                url = "https://mit.malefic.xyz"
            }
        }
        developers {
            developer {
                name = "Om Gupta"
                email = "ogupta4242@gmail.com"
                url = "malefic.xyz"
            }
        }
        scm {
            url = "https://github.com/$user/$repo"
            connection = "scm:git:git://github.com/$user/$repo.git"
            developerConnection = "scm:git:ssh://github.com/$user/$repo.git"
        }
    }
}

tasks.apply {
    register("formatAndLintKotlin") {
        group = "formatting"
        description = "Fix Kotlin code style deviations with kotlinter"
        dependsOn("formatKotlin")
        dependsOn("lintKotlin")
    }
    build {
        dependsOn(named("formatAndLintKotlin"))
        dependsOn(dokkaGenerate)
    }
    publish {
        dependsOn(named("formatAndLintKotlin"))
    }
    test {
        useJUnitPlatform()
    }
    check {
        dependsOn("installKotlinterPrePushHook")
    }
}

afterEvaluate {
    tasks.named("generateMetadataFileForMavenPublication") {
        dependsOn(tasks.named("dokkaJavadocJar"))
    }
}

val user = "OmyDaGreat"
val repo = "Malefix"
val g = "xyz.malefic.frc"
val artifact = "malefix"
val v: String by project
val desc = "A Kotlin util library for FRC!"

plugins {
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.dokka)
    kotlin("jvm")
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
//    implementation(libs.bundles.vendor)
    implementation(libs.pathplanner)
    implementation(libs.bundles.wpilib)
    implementation(libs.photonlib)
    implementation(libs.ctre.phoenix)
    implementation(libs.photontargeting)
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.bundles.advantagekit)
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
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
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

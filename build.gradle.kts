import cn.lalaki.pub.BaseCentralPortalPlusExtension.PublishingType

val user = "OmyDaGreat"
val repo = "Malefix"
val g = "xyz.malefic.frc"
val artifact = "malefix"
val v = "1.0.0"
val desc = "A Kotlin util library for FRC!"

val localMavenRepo = uri(layout.buildDirectory.dir("repo").get())

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.central)
    alias(libs.plugins.dokka)
    `maven-publish`
    signing
}

group = g
version = v

repositories {
    mavenCentral()
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release/") }
    maven { setUrl("https://plugins.gradle.org/m2/") }
    maven { setUrl("https://maven.ctr-electronics.com/release/") }
    maven { setUrl("https://maven.revrobotics.com/") }
    maven { setUrl("https://maven.photonvision.org/repository/internal") }
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/littletonrobotics-mvn-release/") }
    maven { setUrl("https://lib.choreo.autos/dep") }
    maven { setUrl("https://shenzhen-robotics-alliance.github.io/maple-sim/vendordep/repos/releases") }
    maven { setUrl("https://3015rangerrobotics.github.io/pathplannerlib/repo") }
}

var wpiLibVersion = "2025.2.1"
var advantageKitVersion = "4.1.1"
dependencies {
    implementation(libs.kermit)
    implementation("org.littletonrobotics.akit:akit-java:$advantageKitVersion")
    implementation("org.littletonrobotics.akit:akit-wpilibio:$advantageKitVersion")
    implementation("edu.wpi.first.apriltag:apriltag-java:$wpiLibVersion")
    implementation("edu.wpi.first.hal:hal-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpilibj:wpilibj-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpiunits:wpiunits-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpimath:wpimath-java:$wpiLibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-jni:$wpiLibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-java:$wpiLibVersion")
    implementation("edu.wpi.first.wpilibNewCommands:wpilibNewCommands-java:$wpiLibVersion")
    implementation("com.ctre.phoenix6:wpiapi-java:25.1.0")
    implementation("com.revrobotics.frc:REVLib-java:2025.0.3")
    implementation("edu.wpi.first.wpilibNewCommands:wpilibNewCommands-java:$wpiLibVersion")
    implementation("org.photonvision:photonlib-java:v2025.2.1")
    implementation("org.photonvision:photontargeting-java:v2025.1.1")
    implementation("com.pathplanner.lib:PathplannerLib-java:$wpiLibVersion")
    testImplementation(kotlin("test"))
}

tasks.apply {
    register("formatAndLintKotlin") {
        group = "formatting"
        description = "Fix Kotlin code style deviations with kotlinter"
        dependsOn(formatKotlin)
        dependsOn(lintKotlin)
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

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka"))
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

signing {
    useGpgCmd()
    sign(publishing.publications)
}

centralPortalPlus {
    url = localMavenRepo
    tokenXml = uri(layout.projectDirectory.file("user_token.xml"))
    publishingType = PublishingType.AUTOMATIC
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = g
            artifactId = artifact
            version = v

            from(components["java"])

            pom {
                name.set(repo)
                description.set(desc)
                url.set("https://github.com/$user/$repo")
                developers {
                    developer {
                        name.set("Om Gupta")
                        email.set("ogupta4242@gmail.com")
                    }
                }
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/$user/$repo.git")
                    developerConnection.set("scm:git:ssh://github.com/$user/$repo.git")
                    url.set("https://github.com/$user/$repo")
                }
            }
        }
        repositories {
            maven {
                url = localMavenRepo
            }
        }
    }
}

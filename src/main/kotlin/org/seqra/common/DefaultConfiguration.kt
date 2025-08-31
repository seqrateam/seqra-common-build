package org.seqra.common

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.configureDefault(projectName: String) {
    configureDefaultKotlin()
    configureDefaultJvm()
    configureDefaultTest()
    configureDefaultPublishing(projectName)
}

fun Project.configureDefaultKotlin() {
    dependencies {
        // Align versions of all Kotlin components
        add("implementation", platform(kotlin("bom", KotlinDependency.Versions.kotlin)))
        add("implementation", kotlin("stdlib-jdk8", KotlinDependency.Versions.kotlin))
        add("testImplementation", kotlin("test"))
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
            allWarningsAsErrors = false
            freeCompilerArgs.add("-Xsam-conversions=class")
        }
    }
}

fun Project.configureDefaultJvm() {
    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:all")
        options.compilerArgs.add("-Xlint:-options")
        options.compilerArgs.add("-Werror")
    }
}

fun Project.configureDefaultTest() {
    tasks.named<Test>("test") {
        // Use JUnit Platform for unit tests.
        useJUnitPlatform()

        maxHeapSize = "1G"

        testLogging {
            events("passed")
        }
    }
}

fun Project.configureDefaultPublishing(projectName: String) {
    val organizationRepo = properties.getOrDefault("seqraOrg", "seqra")

    extensions.configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/$organizationRepo/$projectName")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }

        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                tasks.findByName("kotlinSourcesJar")?.let { artifact(it) }
            }
        }
    }
}

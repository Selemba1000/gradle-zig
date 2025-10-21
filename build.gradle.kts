plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "2.0.0"
}

group = "io.github.selemba1000"
version = "0.0.1"

repositories {
    mavenCentral()
}

tasks.validatePlugins {
    enableStricterValidation.set(true)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

gradlePlugin{
    plugins{
        website = "https://github.com/selemba1000/gradle-zig"
        vcsUrl = "https://github.com/selemba1000/gradle-zig"
        create("gradle-zig"){
            id = "io.github.selemba1000.gradle-zig"
            implementationClass = "io.github.selemba1000.GradleZigPlugin"
            displayName = "Gradle Zig Plugin"
            description = "A Gradle plugin for seamless integration of the Zig build system into your Java/Kotlin projects."
            tags = listOf("zig", "jna", "native")
        }
    }
}
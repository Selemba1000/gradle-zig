import io.github.selemba1000.definitions.ZigOptimization

plugins {
    kotlin("jvm") version "2.1.20"
    application
    id("io.github.selemba1000.gradle-zig")
}

group = "io.github.selemba1000"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.java.dev.jna:jna:5.13.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application{
    mainClass = "MainKt"
}

sourceSets{
    main{
        java{

        }
        zig{
            targets{
            }
        }
    }
}
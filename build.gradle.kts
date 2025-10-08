plugins {
    `kotlin-dsl`
}

group = "io.github.selemba1000"
version = "1.0-SNAPSHOT"

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
        create("gradle-zig"){
            id = "io.github.selemba1000.gradle-zig"
            implementationClass = "io.github.selemba1000.GradleZigPlugin"
        }
    }
}
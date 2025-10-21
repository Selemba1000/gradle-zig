# Gradle Zig Plugin

![Project Status](https://img.shields.io/badge/status-beta-orange)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.selemba1000.gradle-zig?label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/io.github.selemba1000.gradle-zig)
![License](https://img.shields.io/badge/license-MIT-blue)

A Gradle plugin for seamless integration of the [Zig](https://ziglang.org/) build system into your Java/Kotlin projects.

This plugin allows you to compile Zig code into native libraries or executables as part of your regular Gradle build, making it easy to work with native code via JNI/JNA.

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
  - [Basic Configuration](#basic-configuration)
  - [Defining Targets](#defining-targets)
  - [Defining Artifacts](#defining-artifacts)
  - [Configuring Compiler Flags](#configuring-compiler-flags)
  - [Full Example](#full-example)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Seamless Integration**: Integrates directly with Gradle's `JavaPlugin` source sets (`main`, `test`, etc.).
- **Cross-Compilation**: Easily define multiple compilation targets (e.g., `linux-x86_64`, `windows-x86_64`, `macos-aarch64`) and build for all of them.
- **Convention over Configuration**: Smart defaults for source directories (`src/main/zig`) and build outputs.
- **Flexible DSL**: A clean Kotlin DSL for configuring targets, artifacts, and compiler flags.
- **`build.zig` Powered**: Leverages the power and flexibility of Zig's own build system.
- **Automatic Resource Handling**: Compiled libraries are automatically added to your project's resources, ready to be loaded by JNA or JNI.

## Getting Started

Follow these instructions to integrate the Gradle Zig plugin into your project.

### Prerequisites

- **Zig**: You must have the Zig toolchain installed and available in your system's `PATH`. You can find installation instructions at ziglang.org/download. The current version in tests and documentation is `0.15.2`.
- **Gradle**: A compatible version of Gradle (check the Plugin Portal for compatibility).
- **Java/Kotlin Project**: The plugin is designed to work alongside the `java` or `kotlin("jvm")` plugins.

### Installation

Apply the plugin to your `build.gradle.kts` file.

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.1.20" // or `java`
    id("io.github.selemba1000.gradle-zig") version "[PLUGIN_VERSION]"
}
```

Replace `[PLUGIN_VERSION]` with the latest version from the Gradle Plugin Portal.

## Usage

Once the plugin is applied, it adds a `zig` extension to each source set defined by the `JavaPlugin`.

### Basic Configuration

1.  Create a `build.zig` file in your Zig source directory (e.g., `src/main/zig/build.zig`). This file defines how your Zig code is compiled. For cross-compilation and optimization options to work out of the box, the `build.zig` file needs to handle the default options via `standardTargetOptions` and `standardOptimizeOptions`. A minimal `build.zig` file that builds a shared library named `foo` looks like this:

    ```zig
    const std = @import("std");

    pub fn build(b: *std.Build) void {
        const target = b.standardTargetOptions(.{});
        const optimize = b.standardOptimizeOption(.{});

        const lib = b.addLibrary(.{
            .name = "foo",
            .linkage = .dynamic,
            .root_module = b.createModule(.{
                .root_source_file = b.path("foo.zig"),
                .target = target,
                .optimize = optimize,
            })
        });
        if(target.result.cpu.arch == .x86){
            lib.link_z_notext = true;
        }

        // Install the library, disabling specific outputs for better JVM compatibility
        const lib_output = b.addInstallArtifact(lib, .{
            .implib_dir = .disabled,
            .dest_dir = .{ .override = .lib },
            .pdb_dir = .disabled
        });
        b.getInstallStep().dependOn(&lib_output.step);
    }
    ```

   - **32-bit Support**: For 32-bit targets, the `lib.link_z_notext = true;` option is often required for proper linking.
   - **Windows Compatibility**: For optimal Windows compatibility with JVM, it's recommended to disable `implib` and `pdb` generation and reroute the shared library file to the `lib` folder. This is a common workaround for JVM compatibility with native Windows libraries.

2.  Configure the `zig` block within a source set in your `build.gradle.kts`.

    ```kotlin
    // build.gradle.kts
    sourceSets {
    main {
            zig {
                // Configuration goes here
            }
        }
    }
    ```

    By default, the plugin will create a `default` target and a `default-library` artifact. This is useful for simple projects that only need to build a single native library for the host machine.

### Defining Targets

You can specify which platforms and architectures to build for using the `targets` block. The plugin provides convenient helpers for common OS/architecture combinations.

```kotlin
// build.gradle.kts
import io.github.selemba1000.definitions.ZigTargetTriple // import if not already present
sourceSets.main.zig {
    targets {
        // Add targets for Linux, Windows, and macOS
        linux(x86_64 = true, arm64 = true)
        windows(x86_64 = true)
        macos(arm64 = true)

        // Or create a custom target
        create("wasm-freestanding") {
            setTargetTriple(ZigTargetTriple("wasm32-freestanding-musl", "wasm32"))
            setTargetOptimize(ZigOptimization.ReleaseSmall)
        }
    }
}
```

### Defining Artifacts

The `zigArtifacts` block tells the plugin what to do with the compiled output from `build.zig`. You can define libraries or executables.

```kotlin
// build.gradle.kts
sourceSets.main.zig {
    zigArtifacts {
        // Copies the compiled library (e.g., libmy_lib.so, my_lib.dll)
        // into the build resources.
        library("my_lib")

        // Copies the compiled executable into the build resources under a 'bin' directory.
        executable("my_app")
    }
}
```

### Configuring Compiler Flags

The plugin allows you to pass additional flags to the `zig build` command in two ways:

1.  **Target-Independent Flags (`additionalFlags`)**

    These flags are passed to the `zig build` command regardless of the specific target or optimization level being compiled. They are set per source set:

    ```kotlin
    // build.gradle.kts
    sourceSets.main.zig {
        additionalFlags.add("-Doption=value")
    }
    ``` 

2.  **Target-Specific Flags (`addTargetCompilerFlag`, `setTargetOptimize`, `setTargetTriple`)**

    These flags are applied on a per-target basis, allowing you to customize compilation for different platforms or architectures. The plugin provides dedicated methods for common target-specific options, and a generic method for any custom flag:

    ```kotlin
    // build.gradle.kts
    sourceSets.main.zig{
        // The first argument is the name of the parameter, the second the template.
        // The semicolon in the template will be replaced by the specific value.
        flagsTemplate.put("yourOption", "-DyourOption=;")
   
        // Changing the flags template requires you to also add the default options, if you want to retain that functionality
        flagsTemplate.put("target", "-Dtarget=;")
        flagsTemplate.put("optimize", "-Doptimize=;")
    }
    ```
   
    Then a value for the parameter can be supplied for every target:
    ```kotlin
    // build.gradle.kts
    sourceSets.main.zig{
        targets{
            named("linux-x68_64"){
                addTargetCompilerFlag("yourOption", "value")
            }
        }
    }
    ```

### Full Example

Here is a complete `build.gradle.kts` example that compiles a native library named `my_lib` for 64-bit Linux and Windows.

```kotlin
// build.gradle.kts
import io.github.selemba1000.definitions.ZigOptimization

plugins {
    kotlin("jvm") version "2.1.20"
    application
    id("io.github.selemba1000.gradle-zig") version "0.1.0" // Use the latest version
}

repositories {
    mavenCentral()
}

dependencies {
    // JNA is great for calling native functions from the JVM
    implementation("net.java.dev.jna:jna:5.13.0")
}

sourceSets {
    main {
        zig {
            // Define which targets to build for
            targets {
                linux(x86_64 = true)
                windows(x86_64 = true)
            }

            // Define which artifacts to extract from the build output
            zigArtifacts {
                library("my_lib")
            }

            // Pass additional flags to the `zig build` command
            additionalFlags.add("-Doption=value")
        }
    }
}
```

Now, when you run `./gradlew build`, the plugin will:
1.  Invoke `zig build` for each specified target.
2.  Copy the resulting `my_lib` shared library from the Zig build output into `build/resources/main/`.
3.  Your Java/Kotlin code can then load this library at runtime without any path configuration.

## Contributing

Contributions are welcome! If you'd like to contribute, please feel free to fork the repository and submit a pull request.

1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/AmazingFeature`).
3.  Make your changes.
4.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
5.  Push to the branch (`git push origin feature/AmazingFeature`).
6.  Open a Pull Request.

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.
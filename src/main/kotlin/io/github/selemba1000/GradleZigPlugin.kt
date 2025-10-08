package io.github.selemba1000

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

abstract class GradleZigPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        project.plugins.withType<JavaPlugin> {
            val sourceSets = project.extensions.getByType<SourceSetContainer>()
            val objects = project.objects

            sourceSets.forEach { sourceSet ->
                val zigSourceSet = objects.newInstance(
                    ZigSourceSet::class.java, sourceSet.name,
                    objects,
                    project.layout,
                )

                sourceSet.extensions.add("zig", zigSourceSet)

                project.afterEvaluate {
                    if (zigSourceSet.targets.isEmpty()) {
                        zigSourceSet.targets.maybeCreate("default")
                    }

                    if (zigSourceSet.zigArtifacts.isEmpty()) {
                        zigSourceSet.zigArtifacts.maybeCreate("default-library").apply {
                            sourcePathOffset.set("lib")
                            artifactResolution.set(CopyResourceResolution(""))
                        }
                    }
                }


                val compileGroupTask = project.tasks.register(
                    zigSourceSet.compileTaskName
                ) {
                    this.description = "Compile Zig code for source set '${sourceSet.name}'"
                }

                project.afterEvaluate {
                    zigSourceSet.targets.forEach { target ->

                        val compileTask = project.tasks.register(
                            "${zigSourceSet.compileTaskName}-${target.name}", Exec::class.java
                        ) {
                            this.inputs.files(zigSourceSet.source)
                            this.outputs.dir(zigSourceSet.outputDir.dir(target.getOutputDirOffset()))

                            this.doFirst {
                                synchronized(zigSourceSet) {
                                    if (zigSourceSet.buildFile.get().asFile.exists()) {
                                        val flags: List<String> = buildList {
                                            addAll(zigSourceSet.additionalFlags.get());
                                            addAll(zigSourceSet.getFlags(target.getFlags()))
                                        }
                                        commandLine(
                                                zigSourceSet.zigExecutable.get(),
                                        "build",
                                        "--prefix",
                                        zigSourceSet.outputDir.dir(target.getOutputDirOffset())
                                            .get().asFile.absolutePath,
                                        "--cache-dir",
                                        zigSourceSet.cacheDir.get().asFile.absolutePath,
                                        ).also { workingDir = zigSourceSet.buildFile.get().asFile.parentFile }

                                        if(flags.isNotEmpty()) {
                                            args(flags)
                                        }
                                    } else {
                                        // TODO Use zig commands without build.
                                        commandLine("echo", "No build file")
                                    }
                                }
                            }
                        }

                        compileGroupTask.configure {
                            this.dependsOn(compileTask)
                        }

                        zigSourceSet.zigArtifacts.forEach { artifact ->
                            val outputDir =
                                zigSourceSet.outputDir.dir(target.getOutputDirOffset()).get().dir(artifact.sourcePathOffset)
                                    .get()
                            artifact.artifactResolution.orNull?.resolve(outputDir,project,sourceSet, target)
                        }

                    }
                }

                project.tasks.named(sourceSet.processResourcesTaskName).configure {
                    this.dependsOn(compileGroupTask)
                }

            }

        }

    }
}
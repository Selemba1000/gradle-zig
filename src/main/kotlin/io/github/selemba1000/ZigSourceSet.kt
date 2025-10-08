package io.github.selemba1000

import io.github.selemba1000.definitions.DefaultZigTargetTriples
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

abstract class ZigSourceSet @Inject constructor(
    name: String,
    objects: ObjectFactory,
    layout: ProjectLayout,
) {
    @InputDirectory
    val source: ConfigurableFileCollection = objects.fileCollection().from(
        layout.projectDirectory.dir("src/$name/zig")
    )

    @OutputDirectory
    val outputDir: DirectoryProperty = objects.directoryProperty().convention(
        layout.buildDirectory.dir("zig-out/$name")
    )

    @OutputDirectory
    val cacheDir: DirectoryProperty = objects.directoryProperty().convention(
        layout.buildDirectory.dir(".zig-cache/$name")
    )

    @InputFile
    val buildFile: RegularFileProperty = objects.fileProperty().convention(
        layout.projectDirectory.file("src/$name/zig/build.zig")
    )

    @Input
    val zigExecutable: Property<String> = objects.property<String>().convention("zig")

    @Input
    val targets: NamedDomainObjectContainer<ZigTarget> =
        objects.domainObjectContainer(ZigTarget::class.java) { name ->
            objects.newInstance(ZigTarget::class.java, name, objects)
        }

    @Input
    val flagsTemplate: MapProperty<String, String> = objects.mapProperty<String, String>()
        .convention(mutableMapOf("target" to "-Dtarget=;", "optimize" to "-Doptimize=;"))

    @Input
    val additionalFlags: ListProperty<String> = objects.listProperty<String>().convention(emptyList())

    @Input
    val zigArtifacts: NamedDomainObjectContainer<ZigArtifact> =
        objects.domainObjectContainer(ZigArtifact::class.java) { name ->
            objects.newInstance<ZigArtifact>(name, objects)
        }

    internal val compileTaskName = "compileZig${name.replaceFirstChar { it.uppercaseChar() }}"

    internal fun getFlags(flags: Map<String,String>):List<String>{
        return flagsTemplate.get().map { (key, value) ->
            flags[key]?.let { value.replace(";", it) } ?: ""
        }.filter { it != "" }
    }

    fun srcDir(path: Any) {
        source.from(path)
    }

    fun setSrcDir(path: Any) {
        source.setFrom(path)
    }

    fun setRelativeBuildFile(path: String) {
        buildFile.set(File(source.first().path, path))
    }

    fun NamedDomainObjectContainer<ZigTarget>.linux(
        x86: Boolean = true,
        x86_64: Boolean = true,
        arm64: Boolean = true,
    ) {
        if(x86) targets.create("linux-x86").apply {
            setTargetTriple(DefaultZigTargetTriples.LINUX_x86.toTriple())
        }
        if(x86_64) targets.create("linux-x86_64").apply {
            setTargetTriple(DefaultZigTargetTriples.LINUX_x86_64.toTriple())
        }
        if(arm64) targets.create("linux-arm64").apply {
            setTargetTriple(DefaultZigTargetTriples.LINUX_arm64.toTriple())
        }
    }
    fun NamedDomainObjectContainer<ZigTarget>.windows(
        x86: Boolean = true,
        x86_64: Boolean = true,
        arm64: Boolean = true,
    ) {
        if (x86) targets.create("windows-x86").apply {
            setTargetTriple(DefaultZigTargetTriples.WINDOWS_x86.toTriple())
        }
        if (x86_64) targets.create("windows-x86_64").apply {
            setTargetTriple(DefaultZigTargetTriples.WINDOWS_x86_64.toTriple())
        }
        if (arm64) targets.create("windows-arm64").apply {
            setTargetTriple(DefaultZigTargetTriples.WINDOWS_arm64.toTriple())
        }
    }
    fun NamedDomainObjectContainer<ZigTarget>.macos(
        x64: Boolean = true,
        arm64: Boolean = true,
    ) {
        if (x64) targets.create("macos-x64").apply {
            setTargetTriple(DefaultZigTargetTriples.MAC_x64.toTriple())
        }
        if (arm64) targets.create("macos-arm64").apply {
            setTargetTriple(DefaultZigTargetTriples.MAC_arm64.toTriple())
        }
    }

    fun NamedDomainObjectContainer<ZigArtifact>.library(
        name: String,
        checkName: Boolean = true,
    ) {
        zigArtifacts.create(name).apply {
            sourcePathOffset.set("lib")
            artifactResolution.set(CopyResourceResolution("", if (checkName) "*$name*" else "*"))
        }
    }
    fun NamedDomainObjectContainer<ZigArtifact>.executable(
        name: String,
        checkName: Boolean = true,
    ) {
        zigArtifacts.create(name).apply {
            sourcePathOffset.set("bin")
            artifactResolution.set(CopyResourceResolution("bin", if (checkName) "*$name*" else "*"))
        }
    }
}
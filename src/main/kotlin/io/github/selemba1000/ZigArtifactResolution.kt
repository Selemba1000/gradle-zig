package io.github.selemba1000

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.named
import org.gradle.language.jvm.tasks.ProcessResources

abstract class ZigArtifactResolution {
    abstract fun resolve(source: Directory, project: Project, sourceSet: SourceSet, target: ZigTarget)
}

data class CopyResourceResolution(private val baseDir: String, private val sourceIncludeFilter: String = "**/*", private val useJNANames: Boolean = true): ZigArtifactResolution() {
    override fun resolve(source: Directory, project: Project, sourceSet: SourceSet, target: ZigTarget) {
        var outDir = if(baseDir.isNotEmpty()) "$baseDir/" else "" + if (useJNANames) (target.jnaTargetName.get()) else ""
        if(outDir.isEmpty()) outDir = "."
        project.tasks.named<ProcessResources>(sourceSet.processResourcesTaskName){
            from(source){
                into(outDir)
                include(sourceIncludeFilter)
            }
        }
    }

}
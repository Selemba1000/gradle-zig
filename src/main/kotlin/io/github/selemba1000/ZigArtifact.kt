package io.github.selemba1000

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class ZigArtifact @Inject constructor(private val name: String, objects: ObjectFactory): Named {

    /**
     * Relative to build root.
     */
    val sourcePathOffset: Property<String> = objects.property<String>().convention("")

    val artifactResolution: Property<ZigArtifactResolution> = objects.property()

    override fun getName(): String = name
}
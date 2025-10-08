package io.github.selemba1000

import io.github.selemba1000.definitions.JNAPlatformNames
import io.github.selemba1000.definitions.ZigOptimization
import io.github.selemba1000.definitions.ZigTargetTriple
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class ZigTarget @Inject constructor(private val name: String, objects: ObjectFactory) : Named {

    val jnaTargetName: Property<String> = objects.property<String>().convention(JNAPlatformNames.getNativeLibraryResourcePrefix())

    val outputDirOffset: Property<String?> = objects.property<String?>()

    fun getOutputDirOffset(): String{
        return if(outputDirOffset.isPresent){
            outputDirOffset.get()
        }else{
            if(name == "default"){
                "."
            }else{
                this.jnaTargetName.get()
            }
        }
    }

    private val targetCompilerFlags: MapProperty<String,String> = objects.mapProperty<String,String>()

    internal fun getFlags() = targetCompilerFlags.get()

    fun setTargetTriple(targetTriple: ZigTargetTriple){
        targetCompilerFlags.put("target", targetTriple.triple)
        jnaTargetName.set(targetTriple.javaName)
    }

    fun addTargetCompilerFlag(flag: String, value: String){
        targetCompilerFlags.put(flag, value)
    }

    fun setTargetOptimize(optimize: ZigOptimization){
        targetCompilerFlags.put("optimize", optimize.name)
    }

    override fun getName(): String = name
}
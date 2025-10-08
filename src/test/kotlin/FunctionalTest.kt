import org.gradle.internal.impldep.org.junit.Assert.assertEquals
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.test.Test

class FunctionalTest {

    @Test
    fun test() {
        val result = GradleRunner.create()
            .withProjectDir(File("src/test/resources/testProject"))
            .withArguments("check")
            .withPluginClasspath()
            .build()

        assertEquals(result.task(":check")?.outcome, TaskOutcome.SUCCESS)
    }

    @Test
    fun clean(){
        // Clean Up
        val cleanResult = GradleRunner.create()
            .withProjectDir(File("src/test/resources/testProject"))
            .withArguments("clean")
            .withPluginClasspath()
            .build()
    }

}
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Test{
    @Test
    fun test(){
        val main = Main()
        assertEquals(3, main.add(1,2))
        assertEquals(10, main.add(4,6))
    }
}
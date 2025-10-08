import com.sun.jna.Library
import com.sun.jna.Native

fun main(){
    println("Hello world!")
    val lib = Native.load("foo", LibFoo::class.java)
    println(lib.foo(1,5))
}

interface LibFoo : Library{
    fun foo(a: Int, b: Int): Int
}

class Main(){
    val lib = Native.load("foo", LibFoo::class.java)
    fun add(a: Int, b: Int): Int = lib.foo(a,b)
}
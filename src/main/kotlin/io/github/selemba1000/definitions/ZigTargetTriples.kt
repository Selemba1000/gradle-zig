package io.github.selemba1000.definitions

enum class DefaultZigTargetTriples(val triple: String, val javaName: String) {
    WINDOWS_x86("x86-windows","win32-x86"),
    WINDOWS_x86_64("x86_64-windows","win32-x86-64"),
    WINDOWS_arm64("aarch64-windows","win32-aarch64"),
    LINUX_x86("x86-linux","linux-x86"),
    LINUX_x86_64("x86_64-linux","linux-x86-64"),
    LINUX_arm64("aarch64-linux","linux-aarch64"),
    MAC_x64("x86_64-macos","darwin-x86-64"),
    MAC_arm64("aarch64-macos","darwin-aarch64"),
    FREESTANDING_x86("x86-freestanding", "x86"),
    FREESTANDING_x86_64("x86_64-freestanding", "x86-64"),
    FREESTANDING_aarch64("aarch64-freestanding", "aarch64");

    fun toTriple(): ZigTargetTriple{
        return ZigTargetTriple(this.triple,this.javaName)
    }

}

class ZigTargetTriple(val triple: String, val javaName: String)
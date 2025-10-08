package io.github.selemba1000.definitions

import java.util.*

/**
 * Generator for JNA Platform prefixes.
 * Taken from the JNA project and modified for this use case.
 */
object JNAPlatformNames {
    private const val MAC: Int = 0
    private const val LINUX: Int = 1
    private const val WINDOWS: Int = 2
    private const val SOLARIS: Int = 3
    private const val FREEBSD: Int = 4
    private const val OPENBSD: Int = 5
    private const val WINDOWSCE: Int = 6
    private const val AIX: Int = 7
    private const val ANDROID: Int = 8
    private const val GNU: Int = 9
    private const val KFREEBSD: Int = 10
    private const val NETBSD: Int = 11
    private const val DRAGONFLYBSD: Int = 12

    private fun getOSType(): Int{
        var osType = -1;
        val osName = System.getProperty("os.name")
        if (osName.startsWith("Linux")) {
            if ("dalvik" == System.getProperty("java.vm.name").lowercase(Locale.getDefault())) {
                osType = ANDROID
                // Native libraries on android must be bundled with the APK
                System.setProperty("jna.nounpack", "true")
            } else {
                osType = LINUX
            }
        } else if (osName.startsWith("AIX")) {
            osType = AIX
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            osType = MAC
        } else if (osName.startsWith("Windows CE")) {
            osType = WINDOWSCE
        } else if (osName.startsWith("Windows")) {
            osType = WINDOWS
        } else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
            osType = SOLARIS
        } else if (osName.startsWith("FreeBSD")) {
            osType = FREEBSD
        } else if (osName.startsWith("OpenBSD")) {
            osType = OPENBSD
        } else if (osName.equals("gnu", ignoreCase = true)) {
            osType = GNU
        } else if (osName.equals("gnu/kfreebsd", ignoreCase = true)) {
            osType = KFREEBSD
        } else if (osName.equals("netbsd", ignoreCase = true)) {
            osType = NETBSD
        } else if (osName.equals("dragonflybsd", ignoreCase = true)) {
            osType = DRAGONFLYBSD
        }
        return osType
    }

    private fun getCanonicalArchitecture(arch: String): String {
        var archint = arch
        archint = archint.lowercase(Locale.getDefault()).trim { it <= ' ' }
        if ("powerpc" == archint) {
            archint = "ppc"
        } else if ("powerpc64" == archint) {
            archint = "ppc64"
        } else if ("i386" == archint || "i686" == archint) {
            archint = "x86"
        } else if ("x86_64" == archint || "amd64" == archint) {
            archint = "x86-64"
        } else if ("zarch_64" == archint) {
            archint = "s390x"
        }
        // Work around OpenJDK mis-reporting os.arch
        // https://bugs.openjdk.java.net/browse/JDK-8073139
        if ("ppc64" == archint && "little" == System.getProperty("sun.cpu.endian")) {
            archint = "ppc64le"
        }

        // Dropped SoftFloat Support

        return archint
    }

    fun getNativeLibraryResourcePrefix(osType: Int, arch: String, name: String): String {
        var archint = arch
        var osPrefix: String
        archint = getCanonicalArchitecture(archint)
        when (osType) {
            ANDROID -> {
                if (archint.startsWith("arm")) {
                    archint = "arm"
                }
                osPrefix = "android-$archint"
            }

            WINDOWS -> osPrefix = "win32-$archint"
            WINDOWSCE -> osPrefix = "w32ce-$archint"
            MAC -> osPrefix = "darwin-$archint"
            LINUX -> osPrefix = "linux-$archint"
            SOLARIS -> osPrefix = "sunos-$archint"
            DRAGONFLYBSD -> osPrefix = "dragonflybsd-$archint"
            FREEBSD -> osPrefix = "freebsd-$archint"
            OPENBSD -> osPrefix = "openbsd-$archint"
            NETBSD -> osPrefix = "netbsd-$archint"
            KFREEBSD -> osPrefix = "kfreebsd-$archint"
            else -> {
                osPrefix = name.lowercase(Locale.getDefault())
                val space = osPrefix.indexOf(" ")
                if (space != -1) {
                    osPrefix = osPrefix.substring(0, space)
                }
                osPrefix += "-$archint"
            }
        }
        return osPrefix
    }

    fun getNativeLibraryResourcePrefix(): String {
        return getNativeLibraryResourcePrefix(getOSType(), System.getProperty("os.arch"), System.getProperty("os.name"))
    }

}
package net.jspiner.epub_viewer.util

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.WeakHashMap

val cacheMap = WeakHashMap<String, String>()

fun readFile(file: File): String {
    return if (cacheMap.containsKey(file.absolutePath)) {
        cacheMap[file.absolutePath]!!
    }
    else {
        readFileInternal(file).also { cacheMap[file.absolutePath] = it }
    }
}

private fun readFileInternal(file: File): String {
    return BufferedReader(FileReader(file)).use { br ->
        val sb = StringBuilder()
        var line = br.readLine()

        while (line != null) {
            sb.append(line)
            sb.append(System.lineSeparator())
            line = br.readLine()
        }
        sb.toString()
    }
}
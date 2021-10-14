import java.io.File

val userManual = """
    custom delimiter
    output filename
    name value
   USER MANUAL TODO
""".trimIndent()

data class Entry(val name: String, val value: Int)

typealias Entries = List<Entry>

fun parseLine(line: String, delimiter: Char): Entry? {
    val splited = line.split(delimiter)
    if (splited.size != 2) {
        println("Wrong count of fields in line '$line'. In string must be only 2 fields")
        return null
    }
    val name = splited[0]
    val value = splited[1].toIntOrNull()
    return if (value != null) Entry(name, value) else {
        println("Second field in line '$line' must be integer")
        null
    }
}

fun saveReadData(fileName: String, delimiter: Char = ' '): Entries? {
    val lines = try {
        File(fileName).readLines()
    } catch(error: Exception) {
        println("Error while reading file $fileName")
        return null
    }
    val data = lines.map { parseLine(it, delimiter) }
    return if (data.all {it != null}) data.filterNotNull() else null
}

enum class Type() {
    HISTOGRAM, ROUND
}

val stringToType = mapOf(
    "hist" to Type.HISTOGRAM,
    "round" to Type.ROUND
)


fun main(args: Array<String>) {
    if (args.contains("-h") || args.contains("--help") || args.size != 2) {
        println(userManual); return
    }
    val mode = stringToType[args[0]]
    if (mode == null) { println("Wrong type of diagram"); return }
    val fileName = args[1]
    val data = saveReadData(fileName) ?: return

    createWindow("The worst project ever!!!", data, mode)
}

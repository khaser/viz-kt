import input.*

val userManual = """
    -d STRING sets delimiter for fields in one line
    -o FILE if you want to save graphics to PNG FILE use it option
   USER MANUAL TODO
""".trimIndent()

data class Entry(val name: String, val value: Int)
typealias Entries = List<Entry>
typealias Options = Map<Option, String>

enum class Type {
    HISTOGRAM, ROUND
}

val stringToType = mapOf(
    "hist" to Type.HISTOGRAM,
    "round" to Type.ROUND
)


fun main(args: Array<String>) {
    if (args.contains("-h") || args.contains("--help") || args.size < 2) {
        println(userManual); return
    }
    val mode = stringToType[args[0]]
    if (mode == null) { println("Wrong type of diagram"); return }
    val fileName = args.last()
    val options = parseAllKeys(args.slice(1 until args.size - 1))
    var data = saveReadData(fileName, options[Option.DELIMITER] ?: " ") ?: return
    if (options[Option.SORT] == "true") {
        data = data.sortedWith(compareBy{it.value})
    }
    createWindow("The worst project ever!!!", data, mode, options)
}

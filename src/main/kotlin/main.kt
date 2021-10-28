import input.*

val userManual = """
    --help or -h Display this help
    --file or -f FILE save drawing graphics to FILE
    --lfont or -l SIZE (Default: 40) set font size for legend section
    --sfont or -s SIZE (Default: 20) set font size for scaling section
    --stroke or -w WIDTH (Default: 3) set stroke width for divide filled parts of diagram
    --delimiter or -d STRING (Default: ' ') set STRING as delimiting sequence for parsing your data file
    --hue or -u VALUE (Default: 0) set H value for first color in palette 0<=VALUE<360
    --saturation or -a VALUE (Default: 0.8) set saturation for all colors in palette 0<=VALUE<=1
    --bright or -b VALUE (Default: 0.8) set bright for all colors in palette 0<=VALUE<=1
    --sort or -o set legend and diagram by increasing of data values
""".trimIndent()

data class Entry(val name: String, val value: Int)
typealias Entries = List<Entry>
typealias Options = Map<Option, String>

enum class Type {
    ABSOLUTE_HISTOGRAM, PERCENT_HISTOGRAM, ROUND
}

val stringToType = mapOf(
    "hist" to Type.PERCENT_HISTOGRAM,
    "abshist" to Type.ABSOLUTE_HISTOGRAM,
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

package input

/** keys - strings like --help; args - string after keys */
enum class Option(val longKey: String, val shortKey: String) {
    HELP("--help", "-h"),
    FILE("--file", "-f"),
    LEGEND_FONT("--lfont", "-l"),
    SCOPE_FONT("--sfont", "-s"),
    STROKE_WIDTH("--stroke", "-w"),
    DELIMITER("--delimiter", "-d"),
    HUE("--hue", "-u"),
    SATURATION("--saturation", "-a"),
    BRIGHT("--bright", "-b"),
    SORT("--sort", "-o"),
}

val argOptions: Set<Option> = setOf(
    Option.FILE,
    Option.DELIMITER,
    Option.LEGEND_FONT,
    Option.SCOPE_FONT,
    Option.STROKE_WIDTH,
    Option.HUE,
    Option.SATURATION,
    Option.BRIGHT
)
val flagOptions: Set<Option> = setOf(Option.HELP, Option.SORT)
val keyShortcut = Option.values().associate { Pair(it.shortKey, it.longKey) }
val keyOption = Option.values().associateBy { it.longKey }

/** Parse all user input, main function of package*/
fun parseAllKeys(args: List<String>): Map<Option, String> {
    require(argOptions.size + flagOptions.size == Option.values().size)
    val result: MutableMap<Option, String> = mutableMapOf()
    //Cast all short keys to long keys
    val normalizedArgs = args.map { keyShortcut[it] ?: it }
    val (onlyKey, keyWithArg) = normalizedArgs.partition { flagOptions.contains(keyOption[it]) }
    //Parse keys without args
    flagOptions.forEach { result[it] = onlyKey.contains(it.longKey).toString() }
    //Parse keys with args
    parseKeysWithArgs(keyWithArg as MutableList, result)
    validateHSV(result)
    return result
}

/** Function for parsing options with argument like --width 100*/
private fun parseKeysWithArgs(args: MutableList<String>, result: MutableMap<Option, String>) {
    val dropped: MutableList<String> = mutableListOf()
    while (args.isNotEmpty()) {
        dropped.addAll(args.takeWhile { !argOptions.contains(keyOption[it]) }
            .also { repeat(it.size) { args.removeFirst() } })
        if (args.isEmpty()) break
        if (args.size == 1) {
            println("Warning!!! After ${args[0]} option must be value")
            dropped.add(args[0])
            break
        }
        with(keyOption[args[0]]!!) {
            if (result.containsKey(this)) {
                println("Warning!!! Redeclaration of option ${args[0]}")
            }
            result[this] = args[1]
            repeat(2) { args.removeFirst() }
        }
    }
    if (dropped.isNotEmpty()) println("Was ignored next keys: ${dropped.joinToString(" ")}")
}

fun validateHSV(options: MutableMap<Option, String>) {
    val fH = options[Option.HUE]?.toFloatOrNull()
    if (fH != null && (fH < 0 || fH >= 360)) {
        println("Value for '${Option.HUE.longKey}' must be in [0, 360) range")
        options[Option.HUE] = "0"
    }
    listOf(Option.SATURATION, Option.BRIGHT).forEach {
        val value = options[it]?.toFloatOrNull()
        if (value != null && (value <= 0 || value > 1)) {
            println("Value for '${Option.SATURATION.longKey}' and '${Option.BRIGHT.longKey}' ")
            options[it] = "0.9"
        }
    }
}

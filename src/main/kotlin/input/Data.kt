package input
import Entry
import java.io.File


typealias Entries = List<Entry>

fun parseLine(line: String, delimiter: String): Entry? {
    val splited = line.split(delimiter)
    if (splited.size != 2) {
        println("Wrong count of fields in line '$line'. In string must be only 2 fields")
        return null
    }
    val name = splited[0]
    val value = splited[1].trim().toIntOrNull()
    return if (value != null && value >= 0) Entry(name, value) else {
        println("Second field in line '$line' must be positive integer or zero")
        null
    }
}

fun saveReadData(fileName: String, delimiter: String = " "): Entries? {
    val lines = try {
        File(fileName).readLines()
    } catch(error: Exception) {
        println("Error while reading file $fileName")
        return null
    }
    var data = lines.map { parseLine(it, delimiter) }
    if (data.any {it == null}) return null
    data = data.filterNotNull()
    return data.ifEmpty { null }
}
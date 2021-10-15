import input.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.*

internal class InputArgumentsTest {

    val standardOut = System.out
    var stream =  ByteArrayOutputStream()

    @BeforeTest
    fun setUp() {
        stream = ByteArrayOutputStream().also {System.setOut(PrintStream(it))}
    }

    @AfterTest
    fun cleanUp() {
        System.setOut(standardOut)
    }

    fun runTest(args: String, correct: Options) {
        assertEquals(correct, parseAllKeys(args.split(' ')))
    }

    @Test
    fun nullInput() {
        val test: List<String> = listOf()
        val correct: Options = mapOf(
            Option.SORT to "false",
            Option.HELP to "false"
        )
        assertEquals(correct, parseAllKeys(test))
    }

    @Test
    fun flagKeysOnly() {
        val correct = mapOf(
            Option.SORT to "true",
            Option.HELP to "false"
        )
        runTest("-o", correct)
        assertEquals("", stream.toString())
    }

    @Test
    fun longKeysOnly() {
        val correct = mapOf(
            Option.SORT to "false",
            Option.HELP to "false",
            Option.LEGEND_FONT to "50",
            Option.SATURATION to "0.1",
            Option.DELIMITER to "delim"
        )
        runTest("--saturation 0.1 --delimiter delim --lfont 50", correct)
        assertEquals("", stream.toString())
    }

    @Test
    fun shortKeysOnly() {
        val correct = mapOf(
            Option.SORT to "false",
            Option.HELP to "false",
            Option.SATURATION to "0.1",
            Option.SCOPE_FONT to "300",
            Option.FILE to "filename",
        )
        runTest("-f filename -a 0.1 -s 300", correct)
        assertEquals("", stream.toString())
    }

    @Test
    fun mixedKeys() {
        val correct = mapOf(
            Option.SORT to "false",
            Option.HELP to "true",
            Option.FILE to "98",
            Option.DELIMITER to "vimbetterthanemacs"
        )
        runTest("-d vimbetterthanemacs --help --file 98", correct)
        assertEquals("", stream.toString())
    }

    @Test
    fun wrongKey() {
        val correct = mapOf(
            Option.SORT to "false",
            Option.HELP to "false",
            Option.HUE to "11"
        )
        runTest("--wrongkey 1337 -u 11", correct)
        assertEquals("Was ignored next keys: --wrongkey 1337\n", stream.toString())
    }

    @Test
    fun missedArgumentInEnd() {
        val correct = mapOf(
            Option.SORT to "false",
            Option.HELP to "false",
            Option.SCOPE_FONT to "11"
        )
        runTest("--sfont 11 -d", correct)
        val correctStream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        println("Warning!!! After --delimiter option must be value")
        println("Was ignored next keys: --delimiter")
        assertEquals(correctStream.toString(), stream.toString())
    }

    @Test
    fun argumentWithoutKey() {
        val correct = mapOf(
            Option.SORT to "false",
            Option.HELP to "false",
            Option.DELIMITER to "101",
            Option.FILE to "!file!"
        )
        runTest("-d 101 missedargument --file !file!", correct)
        assertEquals("Was ignored next keys: missedargument\n", stream.toString())
    }
}
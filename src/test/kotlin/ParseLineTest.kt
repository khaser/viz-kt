import input.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.*

class ParseLineTest {

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

    fun runTest(line: String, correct: Entry?, delimiter: String = " ") {
        assertEquals(correct, parseLine(line, delimiter))
    }

    @Test
    fun parseLineRegular() {
        val correct = Entry("field", 123)
        runTest("field 123", correct)
        assertEquals("", stream.toString())
    }

    @Test
    fun parseLineCustomDelimiter() {
        val correct = Entry("field", 239)
        runTest("field\$custom delimiter! 239", correct, "\$custom delimiter!")
        assertEquals("", stream.toString())
    }

    @Test
    fun parseLineWithNegativeInt() {
        val line = "field -123"
        runTest(line, null)
        assertEquals("Second field in line '$line' must be positive integer or zero\n", stream.toString())
    }

    @Test
    fun parseLineWithFloat() {
        val line = "field 150.2"
        runTest(line, null)
        assertEquals("Second field in line '$line' must be positive integer or zero\n", stream.toString())
    }

    @Test
    fun parseLineWithZero() {
        val correct = Entry("field", 0)
        val line = "field 0"
        runTest(line, correct)
        assertEquals("", stream.toString())
    }

    @Test
    fun parseLineWithSomeInts() {
        val line = "field 0 1"
        runTest(line, null)
        assertEquals("Wrong count of fields in line '$line'. In string must be only 2 fields\n", stream.toString())
    }

    @Test
    fun parseLineEmptyLine() {
        val line = ""
        runTest(line, null)
        assertEquals("Wrong count of fields in line '$line'. In string must be only 2 fields\n", stream.toString())
    }
}
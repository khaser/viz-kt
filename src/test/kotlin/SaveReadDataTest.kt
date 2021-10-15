import input.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.*

class SaveReadDataTest {
    var path = "testFiles/DataInputTest"
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

    fun runTest(fileName: String, correct: Entries?, delimiter: String = " ") {
        assertEquals(correct, saveReadData("$path/$fileName", delimiter))
    }

    @Test
    fun regularFile() {
        val correct = listOf(
            Entry("data", 1),
            Entry("data", 2),
            Entry("data", 3)
        )
        runTest("RegularFile.txt", correct)
        assertEquals("", stream.toString())
    }

    @Test
    fun emptyFile() {
        runTest("EmptyFile.txt", null)
        assertEquals("", stream.toString())
    }

    @Test
    fun notExistedFile() {
        val file = "NotExistedFile.txt"
        runTest(file, null)
        assertEquals("Error while reading file $path/$file\n", stream.toString())
    }

    @Test
    fun noPermissionFile() {
        val file = "NoPermissionFile.txt"
        runTest(file, null)
        assertEquals("Error while reading file $path/$file\n", stream.toString())
    }

    @Test
    fun customDelimiter() {
        val correct = listOf(
            Entry("data", 100),
            Entry("data with spaces", 200),
            Entry("", 1)
        )
        runTest("CustomDelimiter.txt", correct, "$")
        assertEquals("", stream.toString())
    }

    @Test
    fun fileWithFloat() {
        runTest("FileWithFloat.txt", null)
        assertEquals("Second field in line 'float_data 150.1' must be positive integer or zero\n", stream.toString())
    }

    @Test
    fun missedInt() {
        runTest("MissedInt.txt", null)
        val correctStream = ByteArrayOutputStream().also{System.setOut(PrintStream(it))}
        println("Second field in line 'data_without_integer ' must be positive integer or zero")
        println("Wrong count of fields in line ''. In string must be only 2 fields")
        assertEquals(correctStream.toString(), stream.toString())
    }
}
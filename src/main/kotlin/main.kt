import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.WindowConstants
import kotlin.math.abs

fun main() {
    createWindow("pf-2021-viz")
}

fun createWindow(title: String) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = Renderer(window.layer)
    window.layer.addMouseMotionListener(MyMouseMotionAdapter)

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100, 100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

class Renderer(val layer: SkiaLayer) : SkiaRenderer {
    val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    val font = Font(typeface, 40f)
    val paint = Paint().apply {
        mode = PaintMode.STROKE_AND_FILL
        color = 0xffffffffL.toInt()
        strokeWidth = 1f
    }


    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()

        canvas.drawRoundDiagram(listOf(10F,10F,10F,10F,10F,10F,10F,10F,10F,10F), listOf("a","a","a","a","a","a","a","a","a","a"), Position(500F, 500F), 300F)
        layer.needRedraw()
    }

    fun Canvas.drawRoundDiagram(percents: List<Float>, names: List<String>, center: Position, r: Float) {
        require(percents.reduce { acc, it -> acc + it } == 100F)
        require(percents.size == names.size)
        fun drawSegment(startAngle: Float, endAngle: Float, paint: Paint) {
            drawArc(center.x - r, center.y - r, center.x + r, center.y + r, startAngle, endAngle - startAngle, true, paint)
        }

        val colorScheme = generateColorScheme(percents.size)
        (percents zip colorScheme).fold(0F) { acc, (percent, segColor) ->
            paint.color = segColor.getColorCode()
            drawSegment(acc, acc + percent * 3.6F, paint)
            acc + percent * 3.6F
        }
    }
}

data class HSV(val H: Float, val S: Float, val V: Float)
data class RGB(val R: Int, val G: Int, val B: Int) {
    fun toInt(): Int {
        return Color4f(R / 255F, G / 255F, B / 255F).toColor()
    }
}

fun rgbToHsv(rgb: RGB): HSV {
    val abs = listOf(rgb.R / 255F, rgb.G / 255F, rgb.B / 255F)
    val maxC = abs.maxOrNull() ?: 255F
    val minC = abs.minOrNull() ?: 0F
    val delta = maxC - minC
    val H = (when (maxC) {
        abs[0] -> 60 * ((abs[1] - abs[2]) / delta % 6)
        abs[1] -> 60 * ((abs[2] - abs[0]) / delta + 2)
        abs[2] -> 60 * ((abs[0] - abs[1]) / delta + 4)
        else -> 0F
    } + 360) % 360
    val V = maxC
    val S = if (V != 0F) delta / V else 0F
    return HSV(H, S, V)
}

fun hsvToRgb(hsv: HSV): RGB {
    val C = hsv.V * hsv.S
    val X = C * (1 - abs((hsv.H / 60) % 2 - 1))
    val m = hsv.V - C
    val rgb = when (hsv.H.toInt()) {
        in 0 until 60 -> listOf(C, X, 0F)
        in 60 until 120 -> listOf(X, C, 0F)
        in 120 until 180 -> listOf(0F, C, X)
        in 180 until 240 -> listOf(0F, X, C)
        in 240 until 300 -> listOf(X, 0F, C)
        in 300 until 360 -> listOf(C, 0F, X)
        else -> throw Exception("RGB to HSV cast error")
    }.map { ((it + m) * 255).toInt() }
    return RGB(rgb[0], rgb[1], rgb[2])
}

class Color(val rgb: RGB) {
    constructor(hsv: HSV) : this(hsvToRgb(hsv))

    val hsv = rgbToHsv(rgb)
    fun getColorCode(): Int {
        return rgb.toInt()
    }

    fun shift(period: Float): Color {
        val newHSV = HSV((hsv.H + period) % 360, hsv.S, hsv.V)
        return Color(hsvToRgb(newHSV))
    }
}


fun generateColorScheme(colors: Int, seed: Color = Color(HSV(10F, 0.2F, 0.7F))): List<Color> {
    return List(colors) { index -> seed.shift(360F * index / colors) }
}

data class Position(val x: Float, val y: Float)


object State {
    var mouseX = 0f
    var mouseY = 0f
}

object MyMouseMotionAdapter : MouseMotionAdapter() {
    override fun mouseMoved(event: MouseEvent) {
        State.mouseX = event.x.toFloat()
        State.mouseY = event.y.toFloat()
    }
}
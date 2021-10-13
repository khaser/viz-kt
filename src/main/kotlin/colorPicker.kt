import org.jetbrains.skija.Color4f
import kotlin.math.abs

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


fun generateColorScheme(colors: Int, seed: Color = Color(HSV(10F, 0.99F, 0.85F))): List<Color> {
    return List(colors) { index -> seed.shift(360F * index / colors) }
//        .chunked(colors / 2).let {
//        (it[0] zip it[1]).map{ listOf(it.first, it.second) }.flatten() + (it.getOrNull(2) ?: listOf())
//    }
}


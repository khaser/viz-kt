import org.jetbrains.skija.*

val paintFill = Paint().apply {
    mode = PaintMode.FILL
}

val paintStroke = Paint().apply {
    mode = PaintMode.STROKE
    color = 0xff000000L.toInt()
    strokeWidth = 3f
}

val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
val font = Font(typeface, 40f)
const val minPad = 20F;

private fun Canvas.drawCircleForRoundDiagram(
    center: Position,
    r: Float,
    percents: List<Float>,
    colorScheme: List<Color>
) {
    fun drawSegment(startAngle: Float, endAngle: Float, paint: Paint) {
        drawArc(
            center.x - r, center.y - r, center.x + r, center.y + r,
            startAngle, endAngle - startAngle,
            true, paint
        )
        drawArc(
            center.x - r, center.y - r, center.x + r, center.y + r,
            startAngle, endAngle - startAngle,
            true, paintStroke
        )
    }

    (percents zip colorScheme).fold(0F) { acc, (percent, segColor) ->
        paintFill.color = segColor.getColorCode()
        drawSegment(acc, acc + percent * 3.6F, paintFill)
        acc + percent * 3.6F
    }
}

fun Canvas.drawElementOfLegend(anchor: Position, color: Color, name: String, font: Font, r: Float): Rect {
    val box = font.measureText(name)
    drawCircle(anchor.x + r, anchor.y + r, 0.6F * r, paintFill.apply { this.color = color.getColorCode() })
    drawString(
        name,
        anchor.x + 2 * r,
        anchor.y + r + box.height / 2F,
        font,
        paintFill.apply { this.color = Color(RGB(0, 0, 0)).getColorCode() })
    return box
}

fun Canvas.drawLegend(left: Float, top: Float, names: List<String>, colorScheme: List<Color>, font: Font) {
    val r = names.maxOf { font.measureText(it).height } / 2F
    (names zip colorScheme).forEachIndexed { index, (name, color) ->
        drawElementOfLegend(Position(left, top + 1.3F * index * 2 * r), color, name, font, r)
    }
}

fun Canvas.drawRoundDiagram(percents: List<Float>, names: List<String>, center: Position, r: Float) {
    require(percents.reduce { acc, it -> acc + it } == 100F)
    require(percents.size == names.size)
    val colors = generateColorScheme(percents.size)
    drawCircleForRoundDiagram(center, r, percents, colors)
    drawLegend(center.x + r + minPad, center.y - r, names, colors, font)
}
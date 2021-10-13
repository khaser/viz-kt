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
val legendFont = Font(typeface, 40f)
val scaleFont = Font(typeface, 20f)
const val minPad = 20F

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

fun Canvas.drawCircleForRoundDiagram(
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

fun Canvas.drawRoundDiagram(
    percents: List<Float>,
    names: List<String>,
    center: Position,
    r: Float,
    colors: List<Color>
) {
    require(percents.reduce { acc, it -> acc + it } == 100F)
    require(percents.size == names.size)
    drawCircleForRoundDiagram(center, r, percents, colors)
    drawLegend(center.x + r + minPad, center.y - r, names, colors, legendFont)
}


fun Canvas.drawHistogram(percents: List<Float>, names: List<String>, rect: Rect, colors: List<Color>) {
    require(percents.reduce { acc, it -> acc + it } == 100F)
    require(percents.size == names.size)
    val scaleBox = Rect(rect.left, rect.top, rect.left + scaleFont.measureText("100%").width, rect.bottom)
    val histBox = Rect(rect.left + scaleFont.measureText("100%").width, rect.top, rect.right, rect.bottom)
    drawBackground(histBox, paintFill)
    drawColumns(histBox, percents, colors)
    drawLegend(histBox.right, histBox.top + minPad, names, colors, legendFont)
    drawScale(scaleBox, histBox, scaleFont)
}

fun Canvas.drawScale(scale: Rect, hist: Rect, font: Font) {
    val percents = listOf("20%", "40%", "60%", "80%", "100%")
    percents.forEachIndexed { index, str ->
        val y = scale.bottom - (scale.bottom - scale.top) * (index + 1) / percents.size
        drawString(str, scale.left, y, font, paintFill)
        drawLine(scale.left, y, hist.right, y, paintStroke)
    }

}

fun Canvas.drawBackground(rect: Rect, paint: Paint) {
    drawRect(rect, paintFill.apply { color = Color(RGB(100, 100, 100)).getColorCode() })
}

fun Canvas.drawColumns(rect: Rect, percents: List<Float>, colors: List<Color>) {
    val colWidth = (rect.right - rect.left) / percents.size
    (percents zip colors).forEachIndexed { index, (percent, color) ->
        drawRect(
            Rect(
                rect.left + colWidth * index,
                rect.bottom,
                rect.left + colWidth * (index + 1),
                rect.bottom + (rect.top - rect.bottom) * percent / 100
            ),
            paintFill.apply { this.color = color.getColorCode() }
        )
    }
}
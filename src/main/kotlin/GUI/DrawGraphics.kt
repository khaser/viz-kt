package GUI

import Entries
import Options
import Type
import input.Option
import org.jetbrains.skija.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

class Diagram(val canvas: Canvas, val data: Entries, options: Options) {

    private val topValue = calcTopValue(data.maxOf { it.value })
    val percents = data.sumOf { it.value }.let { sum -> data.map { it.value.toFloat() / sum * 100 } }
    val names = data.map { it.name }
    val values = data.map { it.value }
    val colorScheme = generateColorScheme(
        percents.size, Color(
            HSV(
                options[Option.HUE]?.toFloatOrNull() ?: 0F,
                options[Option.SATURATION]?.toFloatOrNull() ?: 0.8F,
                options[Option.BRIGHT]?.toFloatOrNull() ?: 0.8F
            )
        )
    )

    data class Position(val x: Float, val y: Float)

    val paintFill = Paint().apply {
        mode = PaintMode.FILL
    }

    val paintStroke = Paint().apply {
        mode = PaintMode.STROKE
        color = 0xff000000L.toInt()
        strokeWidth = options[Option.STROKE_WIDTH]?.toFloatOrNull() ?: 3F
    }

    val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    val fontForLegend = Font(typeface, options[Option.LEGEND_FONT]?.toFloatOrNull() ?: 40F)
    val fontForScale = Font(typeface, options[Option.SCOPE_FONT]?.toFloatOrNull() ?: 20F)
    val minPad = 20F
    val countElementsInScale = 5

    private val eps = 1e-4F

    fun draw(type: Type, box: Rect) {
        require(abs(percents.reduce { acc, it -> acc + it } - 100F) <= eps)
        require(percents.size == names.size)
        when (type) {
            Type.PERCENT_HISTOGRAM -> drawPercentHistogram(percents, names, box)
            Type.ABSOLUTE_HISTOGRAM -> drawAbsoluteHistogram(values, names, box)
            Type.ROUND -> drawRoundDiagram(percents, names, box)
        }
    }

    private fun drawPercentHistogram(percents: List<Float>, names: List<String>, rect: Rect) {
        val scaleBox = Rect(rect.left, rect.top, rect.left + fontForScale.measureText("100%").width, rect.bottom)
        val histBox = Rect(rect.left + fontForScale.measureText("100%").width, rect.top, rect.right, rect.bottom)
        drawBackground(histBox)
        drawColumns(histBox, percents)
        drawLegend(histBox.right, histBox.top + this.minPad, names)
        drawScaleInPercents(scaleBox, histBox)
    }

    private fun drawAbsoluteHistogram(data: List<Int>, names: List<String>, rect: Rect) {
        val scaleBox = Rect(rect.left, rect.top, rect.left + fontForScale.measureText("100%").width, rect.bottom)
        val histBox = Rect(rect.left + fontForScale.measureText("100%").width, rect.top, rect.right, rect.bottom)
        val percents = List(data.size) { data[it].toFloat() / topValue * 100F }
        drawBackground(histBox)
        drawColumns(histBox, percents)
        drawLegend(histBox.right, histBox.top + this.minPad, names)
        drawScaleInAbsoluteValue(scaleBox, histBox)
    }


    private fun drawRoundDiagram(percents: List<Float>, names: List<String>, box: Rect) {
        val r = min(box.height, box.width) / 2.0F
        val center = Position(box.left + r, box.top + r)
        drawCircleForRoundDiagram(center, r, percents)
        drawLegend(center.x + r + this.minPad, center.y - r, names)
    }

    private fun drawElementOfLegend(anchor: Position, color: Color, field: String, r: Float): Rect {
        val box = fontForLegend.measureText(field)
        canvas.drawCircle(
            anchor.x + r,
            anchor.y + r,
            0.6F * r,
            this.paintFill.apply { this.color = color.getColorCode() })
        canvas.drawString(
            field,
            anchor.x + 2 * r,
            anchor.y + r + box.height / 2F,
            fontForLegend,
            this.paintFill.apply { this.color = Color(RGB(0, 0, 0)).getColorCode() })
        return box
    }

    private fun drawLegend(left: Float, top: Float, fields: List<String>) {
        val r = fields.maxOf { fontForLegend.measureText(it).height } / 2F
        (fields zip colorScheme).forEachIndexed { index, (name, color) ->
            drawElementOfLegend(Position(left, top + 1.3F * index * 2 * r), color, name, r)
        }
    }

    private fun drawCircleForRoundDiagram(center: Position, r: Float, percents: List<Float>) {
        fun drawSegment(startAngle: Float, endAngle: Float, paint: Paint) {
            canvas.drawArc(
                center.x - r, center.y - r, center.x + r, center.y + r,
                startAngle, endAngle - startAngle,
                true, paint
            )
            canvas.drawArc(
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

    private fun drawScale(scaleBox: Rect, histBox: Rect, scaling: List<String>, interpol: List<Float>) {
        scaling.forEachIndexed { index, str ->
            val y = scaleBox.bottom - (scaleBox.bottom - scaleBox.top) * interpol[index]
            canvas.drawString(
                str,
                scaleBox.left,
                y + fontForScale.measureText(str).height + paintStroke.strokeWidth,
                fontForScale,
                paintFill
            )
            canvas.drawLine(scaleBox.left, y, histBox.right, y, paintStroke)
        }
    }

    private fun drawScaleInPercents(scaleBox: Rect, histBox: Rect) =
        drawScale(scaleBox, histBox, generateScale("100%"), List(countElementsInScale){ (it + 1F) / countElementsInScale})

    private fun drawScaleInAbsoluteValue(scaleBox: Rect, histBox: Rect) {
        val scale = generateScale(topValue.toString())
        drawScale(scaleBox, histBox, scale, List(countElementsInScale){ scale[it].toFloat() / topValue})
    }

    private fun generateScale(top: String): List<String> {
        if (top == "100%") {
            return List(countElementsInScale) {((it + 1F) / countElementsInScale * 100).toInt().toString() + "%"}
        }
        return List(countElementsInScale - 1){it}.runningFold(top){ acc, _ ->
            println(acc)
            when (acc[0]) {
                '1','2' -> (acc.toInt() / 2).toString()
                '5' -> (acc.toInt() / 5 * 2).toString()
                '0' -> "0"
                else -> throw Exception("Wrong top format")
            }
        }.reversed()
    }

    private fun drawBackground(rect: Rect) {
        canvas.drawRect(rect, paintFill.apply { this.color = Color(RGB(100, 100, 100)).getColorCode() })
    }

    private fun drawColumns(rect: Rect, percents: List<Float>) {
        val colWidth = (rect.right - rect.left) / percents.size
        (percents zip colorScheme).forEachIndexed { index, (percent, color) ->
            canvas.drawRect(
                Rect(
                    rect.left + colWidth * index,
                    rect.bottom,
                    rect.left + colWidth * (index + 1),
                    rect.bottom + (rect.top - rect.bottom) * percent / 100
                ),
                this.paintFill.apply { this.color = color.getColorCode() }
            )
        }
    }

    private fun calcTopValue(x: Int): Int {
        val result = 10F.pow(x.toString().length - 1).toInt()
        if (x <= 2 * result) return 2 * result
        else if (x <= 5 * result) return 5 * result
        else return 10 * result
    }
}

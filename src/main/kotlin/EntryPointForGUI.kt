import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.Canvas
import org.jetbrains.skija.Rect
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.WindowConstants

fun createWindow(title: String, data: Entries, type: Type) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title
    window.layer.renderer = Renderer(window.layer, data, type)
    window.layer.addMouseMotionListener(MyMouseMotionAdapter)
    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100, 100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}


class Renderer(val layer: SkiaLayer, val data: Entries, val type: Type) : SkiaRenderer {

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()
        val diagram = Diagram(canvas, data)
        diagram.draw(type, Rect(20F, 20F, 500F, 500F))
//        canvas.drawRoundDiagram(
//            listOf(10F, 10F, 10F, 10F, 10F, 10F, 10F, 10F, 20F),
//            listOf("a", "B", "a", "a", "a", "a", "a", "a", "a"),
//            Position(350F, 350F),
//            300F,
//            generateColorScheme(9)
//        )
//        canvas.drawHistogram(
//            listOf(10F, 10F, 10F, 10F, 20F, 40F),
//            listOf("first", "Second", "third", "fours", "a", "a"),
//            Rect(100F, 100F, 400F, 800F),
//            generateColorScheme(6)
//        )

        layer.needRedraw()
    }
}


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

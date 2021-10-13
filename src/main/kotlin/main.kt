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

data class Position(val x: Float, val y: Float)

class Renderer(val layer: SkiaLayer) : SkiaRenderer {

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()
        canvas.drawRoundDiagram(
            listOf(10F, 10F, 10F, 10F, 10F, 10F, 10F, 10F, 20F),
            listOf("a", "B", "a", "a", "a", "a", "a", "a", "a"),
            Position(500F, 500F),
            300F
        )
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
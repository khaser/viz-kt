import input.Option
import GUI.Diagram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.io.File
import javax.swing.WindowConstants

fun createWindow(title: String, data: Entries, type: Type, options: Options) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title
    window.layer.renderer = Renderer(window.layer, data, type, options)
    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100, 100)
    window.pack()
    window.isVisible = true
    window.layer.fullscreen = true
    window.layer.awaitRedraw()
    if (options[Option.FILE] != null) {
        val file = File(options.getValue(Option.FILE))
        if (!file.exists() || file.canWrite()) {
            val image = Image.makeFromBitmap(window.layer.screenshot() ?: Bitmap()).encodeToData(EncodedImageFormat.PNG)
            file.writeBytes(image?.bytes ?: byteArrayOf())
        }
    }
}


class Renderer(val layer: SkiaLayer, val data: Entries, val type: Type, val options: Options) : SkiaRenderer {

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
//        val w = (width / contentScale).toInt()
//        val h = (height / contentScale).toInt()
        val diagram = Diagram(canvas, data, options)
        diagram.draw(type, Rect(20F, 20F, 770F, 570F))
        layer.needRedraw()
    }
}
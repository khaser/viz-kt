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

fun createWindow(title: String, data: Entries, type: Type, outFileName: String?) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title
    window.layer.renderer = Renderer(window.layer, data, type)
    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100, 100)
    window.pack()
    window.isVisible = true
    window.layer.fullscreen = true
    window.layer.awaitRedraw()
    if (outFileName != null) {
        val file = File(outFileName)
        if (!file.exists() || file.canWrite()) {
            val image = Image.makeFromBitmap(window.layer.screenshot() ?: Bitmap()).encodeToData(EncodedImageFormat.PNG)
            file.writeBytes(image?.bytes ?: byteArrayOf())
        }
    }
}


class Renderer(val layer: SkiaLayer, val data: Entries, val type: Type) : SkiaRenderer {

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
//        val w = (width / contentScale).toInt()
//        val h = (height / contentScale).toInt()
        val diagram = Diagram(canvas, data)
        diagram.draw(type, Rect(20F, 20F, 500F, 500F))
        layer.needRedraw()
    }
}
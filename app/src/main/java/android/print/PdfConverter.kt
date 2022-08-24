/*
 * Created on 11/15/17.
 * Written by Islam Salah with assistance from members of Blink22.com
 */
package android.print

import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes.Resolution
import android.print.PrintDocumentAdapter.LayoutResultCallback
import android.print.PrintDocumentAdapter.WriteResultCallback
import android.util.Log
import android.webkit.*
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.io.File

/**
 * Converts HTML to PDF.
 *
 *
 * Can convert only one task at a time, any requests to do more conversions before
 * ending the current task are ignored.
 * Source: https://github.com/blink22/react-native-html-to-pdf/blob/master/android/src/main/java/android/print/PdfConverter.java
 */
class PdfConverter(
    val context: Context,
    val data: String,
    val outputFile: File,
) : Runnable {
    private val TAG = "PdfConverter"
    private lateinit var webView: WebView
    private var fromUrl: Boolean = true

    override fun run() {
        try {
            Log.i(TAG, "Setting up WebView")
            webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    Log.d(TAG,"Loading $url")
                    super.onPageStarted(view, url, favicon)

                }

                override fun onPageFinished(view: WebView, url: String) {
                    Log.d(TAG,"Starting to print $url")
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) throw RuntimeException("call requires API level 19") else {
                        val printAdapter = webView.createPrintDocumentAdapter()
                        printAdapter.onLayout(
                            null,
                            defaultPrintAttrs,
                            null,
                            object : LayoutResultCallback() {},
                            null
                        )
                        printAdapter.onWrite(
                            arrayOf(PageRange.ALL_PAGES),
                            getOutputFileDescriptor(),
                            null,
                            object : WriteResultCallback() {
                                override fun onWriteFinished(pages: Array<PageRange>) {
                                    Log.d(TAG, "Finished writing $outputFile from $url")
                                    isPrinting = false
                                    destroy()
                                    super.onWriteFinished(pages)
                                }

                                override fun onWriteFailed(error: CharSequence?) {
                                    Log.e(TAG, "Failed writing $outputFile from $url:\n$error")
                                    if (outputFile.delete()) {
                                        Log.i(TAG, "Deleted invalid PDF '$outputFile'")
                                    } else {
                                        Log.e(TAG, "Failed to deleted invalid PDF '$outputFile'")
                                    }
                                    printFailed = true
                                    isPrinting = false
                                    destroy()
                                    super.onWriteFailed(error)
                                }

                                override fun onWriteCancelled() {
                                    Log.d(TAG, "Cancelled writing $outputFile from $url")
                                    isPrinting = false
                                    destroy()
                                    super.onWriteCancelled()
                                }
                            })
                    }
                }

            }
            if (fromUrl)
                webView.loadUrl(data)
            else
                webView.loadData(data, "text/HTML", "UTF-8");
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    fun convert(fromUrl: Boolean) {
        isPrinting = true
        printFailed = false
        this.fromUrl = fromUrl
        runOnUiThread()
    }

    private fun getOutputFileDescriptor(): ParcelFileDescriptor? {
        try {
            outputFile.createNewFile()
            return ParcelFileDescriptor.open(
                outputFile,
                ParcelFileDescriptor.MODE_TRUNCATE or ParcelFileDescriptor.MODE_READ_WRITE
            )
        } catch (e: Exception) {
            Log.d(TAG, "Failed to open ParcelFileDescriptor", e)
        }
        return null
    }

    private val defaultPrintAttrs: PrintAttributes?
        private get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) null else PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.NA_GOVT_LETTER)
            .setResolution(Resolution("RESOLUTION_ID", "RESOLUTION_ID", 100, 100))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

    @WorkerThread
    private fun runOnUiThread() {
        ContextCompat.getMainExecutor(context).execute(this);
    }

    fun destroy() {
        webView.destroy()
    }

    companion object {
        var isPrinting = false
        var printFailed = false
    }

}
package mgks.kotlin.webview

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

internal object DetectConnection {
    private val TAG = DetectConnection::class.java.getSimpleName()
    fun isInternetAvailable(context: Context): Boolean {
        val info = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo as NetworkInfo
        return if (info.isConnected) {
			true
		} else {
			true
		}
    }
}

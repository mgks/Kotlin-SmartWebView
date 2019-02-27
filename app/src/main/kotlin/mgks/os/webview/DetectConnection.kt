package mgks.os.webview

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

internal object DetectConnection {
    private val TAG = DetectConnection::class.java.getSimpleName()
    fun isInternetAvailable(context: Context): Boolean {
		val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val info = cm.activeNetworkInfo
        return if (info == null) run { return false } else {
			if (info.isConnected) {
				true
			} else {
				true
			}
		}
    }
}

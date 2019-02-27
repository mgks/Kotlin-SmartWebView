package mgks.kotlin.webview

/*
 * Android Smart WebView is an Open Source Project available on GitHub (https://github.com/mgks/Android-SmartWebView).
 * Developed by Ghazi Khan (https://github.com/mgks) under MIT Open Source License.
 * This program is free to use for private and commercial purposes.
 * Please mention project source or developer credit in your Application's License(s) Wiki.
 * Giving right credit to developers encourages them to create better projects :)
*/

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.GeolocationPermissions
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.SslErrorHandler
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    private var CURR_URL = ASWV_URL

    //Careful with these variable names if altering
    internal lateinit var asw_view: WebView
    internal lateinit var asw_progress: ProgressBar
    internal lateinit var asw_loading_text: TextView
    internal lateinit var asw_notification: NotificationManager
    internal lateinit var asw_notification_new: Notification

    private var asw_cam_message: String? = null
    private var asw_file_message: ValueCallback<Uri>? = null
    private var asw_file_path: ValueCallback<Array<Uri>>? = null

    private val random = SecureRandom()

    //Using cookies to update user locations
    //Checking for location permissions
    //Log.w("New Updated Location:", latitude + "," + longitude);  //enable to test dummy latitude and longitude
    val _location: String
        get() {
            var newloc = "0,0"
            if (ASWP_LOCATION && (Build.VERSION.SDK_INT >= 23 && check_permission(1) || Build.VERSION.SDK_INT < 23)) {
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                val gps: GPSTrack
                gps = GPSTrack(this@MainActivity)
                val latitude = gps.getLatitude()
                val longitude = gps.getLongitude()
                if (gps.canGetLocation()) {
                    if (latitude != 0.0 || longitude != 0.0) {
                        if (!ASWP_OFFLINE) {
                            cookieManager.setCookie(ASWV_URL, "lat=$latitude")
                            cookieManager.setCookie(ASWV_URL, "long=$longitude")
                        }
                        newloc = "$latitude,$longitude"
                    } else {
                        Log.w("New Updated Location:", "NULL")
                    }
                } else {
                    show_notification(1, 1)
                    Log.w("New Updated Location:", "FAIL")
                }
            }
            return newloc
        }

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			if (Build.VERSION.SDK_INT >= 23) {
				window.statusBarColor = this.getResources().getColor(R.color.colorPrimary, this.getTheme())
			} else {
				window.statusBarColor = this.getResources().getColor(R.color.colorPrimary)
			}
            var results: Array<Uri>? = null
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == asw_file_req) {
                    if (null == asw_file_path) {
                        return
                    }
                    if (intent == null || intent.data == null) {
                        if (asw_cam_message != null) {
                            results = arrayOf(Uri.parse(asw_cam_message))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        } else {
                            /*if (ASWP_MULFILE) {
                                if (intent.clipData != null) {
                                    val numSelectedFiles = intent.clipData!!.itemCount
                                    for (i in 0 until numSelectedFiles) {
                                        results[i] = intent.clipData!!.getItemAt(i).uri
                                    }
                                }
                            }*/
                        }
                    }
                }
            }
            asw_file_path!!.onReceiveValue(results)
            asw_file_path = null
        } else {
            if (requestCode == asw_file_req) {
                if (null == asw_file_message) return
                val result = if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
                asw_file_message!!.onReceiveValue(result)
                asw_file_message = null
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.w("READ_PERM = ", Manifest.permission.READ_EXTERNAL_STORAGE)
        Log.w("WRITE_PERM = ", Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //Prevent the app from being started again when it is still alive in the background
        if (!isTaskRoot) {
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val pullfresh = findViewById<SwipeRefreshLayout>(R.id.pullfresh)
        if (ASWP_PULLFRESH) {
            pullfresh.setOnRefreshListener {
                pull_fresh()
                pullfresh.isRefreshing = false
            }
        } else {
            pullfresh.isRefreshing = false
            pullfresh.isEnabled = false
        }

        if (ASWP_PBAR) {
            asw_progress = findViewById(R.id.msw_progress)
        } else {
            findViewById<View>(R.id.msw_progress).visibility = View.GONE
        }
        asw_loading_text = findViewById(R.id.msw_loading_text)
        val handler = Handler()

        //Launching app rating request
        if (ASWP_RATINGS) {
            handler.postDelayed({ get_rating() }, (1000 * 60).toLong()) //running request after few moments
        }

        //Getting basic device information
        get_info()

        //Getting GPS location of device if given permission
        if (ASWP_LOCATION && !check_permission(1)) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), loc_perm)
        }
        _location

        asw_view = findViewById(R.id.msw_view)

        //Webview settings; defaults are customized for best performance
        val webSettings = asw_view.settings

        if (!ASWP_OFFLINE) {
            webSettings.javaScriptEnabled = ASWP_JSCRIPT
        }
        webSettings.saveFormData = ASWP_SFORM
        webSettings.setSupportZoom(ASWP_ZOOM)
        webSettings.setGeolocationEnabled(ASWP_LOCATION)
        webSettings.allowFileAccess = true
        webSettings.allowFileAccessFromFileURLs = true
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.useWideViewPort = true
        webSettings.domStorageEnabled = true

        asw_view.setOnLongClickListener { true }
        asw_view.isHapticFeedbackEnabled = false

        asw_view.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            if (!check_permission(2)) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), file_perm)
            } else {
                val request = DownloadManager.Request(Uri.parse(url))

                request.setMimeType(mimeType)
                val cookies = CookieManager.getInstance().getCookie(url)
                request.addRequestHeader("cookie", cookies)
                request.addRequestHeader("User-Agent", userAgent)
                request.setDescription(getString(R.string.dl_downloading))
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType))
                val dm = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
				dm.enqueue(request)
                Toast.makeText(applicationContext, getString(R.string.dl_downloading2), Toast.LENGTH_LONG).show()
            }
        }

        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			if (Build.VERSION.SDK_INT >= 23) {
				window.statusBarColor = this.getResources().getColor(R.color.colorPrimary, this.getTheme())
			} else {
				window.statusBarColor = this.getResources().getColor(R.color.colorPrimary)
			}
            asw_view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        } else if (Build.VERSION.SDK_INT >= 19) {
            asw_view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
        asw_view.isVerticalScrollBarEnabled = false
        asw_view.webViewClient = Callback()

        //Rendering the default URL
        aswm_view(ASWV_URL, false)

        asw_view.webChromeClient = object : WebChromeClient() {
            //Handling input[type="file"] requests for android API 16+
            fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
                if (ASWP_FUPLOAD) {
                    asw_file_message = uploadMsg
                    val i = Intent(Intent.ACTION_GET_CONTENT)
                    i.addCategory(Intent.CATEGORY_OPENABLE)
                    i.type = ASWV_F_TYPE
                    if (ASWP_MULFILE) {
                        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                    startActivityForResult(Intent.createChooser(i, getString(R.string.fl_chooser)), asw_file_req)
                }
            }

            //Handling input[type="file"] requests for android API 21+
            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: WebChromeClient.FileChooserParams): Boolean {
                if (check_permission(2) && check_permission(3)) {
                    if (ASWP_FUPLOAD) {
                        if (asw_file_path != null) {
                            asw_file_path!!.onReceiveValue(null)
                        }
                        asw_file_path = filePathCallback
                        var takePictureIntent: Intent? = null
                        if (ASWP_CAMUPLOAD) {
                            takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            if (takePictureIntent.resolveActivity(this@MainActivity.packageManager) != null) {
                                var photoFile: File? = null
                                try {
                                    photoFile = create_image()
                                    takePictureIntent.putExtra("PhotoPath", asw_cam_message)
                                } catch (ex: IOException) {
                                    Log.e(TAG, "Image file creation failed", ex)
                                }

                                if (photoFile != null) {
                                    asw_cam_message = "file:" + photoFile.absolutePath
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                                } else {
                                    takePictureIntent = null
                                }
                            }
                        }
                        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                        if (!ASWP_ONLYCAM) {
                            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                            contentSelectionIntent.type = ASWV_F_TYPE
                            if (ASWP_MULFILE) {
                                contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            }
                        }
                        val intentArray: Array<Intent?>
                        if (takePictureIntent != null) {
                            intentArray = arrayOf(takePictureIntent)
                        } else {
                            intentArray = arrayOfNulls<Intent>(0)
                        }

                        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                        chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.fl_chooser))
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                        startActivityForResult(chooserIntent, asw_file_req)
                    }
                    return true
                } else {
                    get_file()
                    return false
                }
            }

            //Getting webview rendering progress
            override fun onProgressChanged(view: WebView, p: Int) {
                if (ASWP_PBAR) {
                    asw_progress.progress = p
                    if (p == 100) {
                        asw_progress.progress = 0
                    }
                }
            }

            // overload the geoLocations permissions prompt to always allow instantly as app permission was granted previously
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                if (Build.VERSION.SDK_INT < 23 || check_permission(1)) {
                    // location permissions were granted previously so auto-approve
                    callback.invoke(origin, true, false)
                } else {
                    // location permissions not granted so request them
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), loc_perm)
                }
            }
        }
        if (intent.data != null) {
            val path = intent.dataString
            /*
            If you want to check or use specific directories or schemes or hosts

            Uri data        = getIntent().getData();
            String scheme   = data.getScheme();
            String host     = data.getHost();
            List<String> pr = data.getPathSegments();
            String param1   = pr.get(0);
            */
            aswm_view(path, false)
        }
    }

    public override fun onPause() {
        super.onPause()
        asw_view.onPause()
    }

    public override fun onResume() {
        super.onResume()
        asw_view.onResume()
        //Coloring the "recent apps" tab header; doing it onResume, as an insurance
        if (Build.VERSION.SDK_INT >= 23) {
            val bm = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            val taskDesc: ActivityManager.TaskDescription
            taskDesc = ActivityManager.TaskDescription(getString(R.string.app_name), bm, getColor(R.color.colorPrimary))
            this@MainActivity.setTaskDescription(taskDesc)
        }
        _location
    }

    //Setting activity layout visibility
    private inner class Callback : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            _location
        }

        override fun onPageFinished(view: WebView, url: String) {
            findViewById<View>(R.id.msw_welcome).visibility = View.GONE
            findViewById<View>(R.id.msw_view).visibility = View.VISIBLE
        }

        //For android below API 23
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            Toast.makeText(applicationContext, getString(R.string.went_wrong), Toast.LENGTH_SHORT).show()
            aswm_view("file:///android_res/raw/error.html", false)
        }

        //Overriding webview URLs
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            CURR_URL = url
            return url_actions(view, url)
        }

        //Overriding webview URLs for API 23+ [suggested by github.com/JakePou]
        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            CURR_URL = request.url.toString()
            return url_actions(view, request.url.toString())
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            if (ASWP_CERT_VERIFICATION) {
                super.onReceivedSslError(view, handler, error)
            } else {
                handler.proceed() // Ignore SSL certificate errors
            }
        }
    }

    //Random ID creation function to help get fresh cache every-time webview reloaded
    fun random_id(): String {
        return BigInteger(130, random).toString(32)
    }

    //Opening URLs inside webview with request
    internal fun aswm_view(url: String?, tab: Boolean?) {
        var url = url
        if (tab!!) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } else {
            if (url!!.contains("?")) { // check to see whether the url already has query parameters and handle appropriately.
                url += "&"
            } else {
                url += "?"
            }
            url += "rid=" + random_id()
            asw_view.loadUrl(url)
        }
    }

    //Actions based on shouldOverrideUrlLoading
    fun url_actions(view: WebView, url: String): Boolean {
        var a = true
        //Show toast error if not connected to the network
        if (!ASWP_OFFLINE && !DetectConnection.isInternetAvailable(this@MainActivity)) {
            Toast.makeText(applicationContext, getString(R.string.check_connection), Toast.LENGTH_SHORT).show()

            //Use this in a hyperlink to redirect back to default URL :: href="refresh:android"
        } else if (url.startsWith("refresh:")) {
            pull_fresh()

            //Use this in a hyperlink to launch default phone dialer for specific number :: href="tel:+919876543210"
        } else if (url.startsWith("tel:")) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
            startActivity(intent)

            //Use this to open your apps page on google play store app :: href="rate:android"
        } else if (url.startsWith("rate:")) {
            val app_package = packageName //requesting app package name from Context or Activity object
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$app_package")))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$app_package")))
            }

            //Sharing content from your webview to external apps :: href="share:URL" and remember to place the URL you want to share after share:___
        } else if (url.startsWith("share:")) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, view.title)
            intent.putExtra(Intent.EXTRA_TEXT, view.title + "\nVisit: " + Uri.parse(url).toString().replace("share:", ""))
            startActivity(Intent.createChooser(intent, getString(R.string.share_w_friends)))

            //Use this in a hyperlink to exit your app :: href="exit:android"
        } else if (url.startsWith("exit:")) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)

            //Getting location for offline files
        } else if (url.startsWith("offloc:")) {
            val offloc = "$ASWV_URL?loc=$_location"
            aswm_view(offloc, false)
            Log.d("OFFLINE LOC REQ", offloc)

            //Opening external URLs in android default web browser
        } else if (ASWP_EXTURL && aswm_host(url) != ASWV_HOST) {
            aswm_view(url, true)
        } else {
            a = false
        }
        return a
    }

    //Reloading current page
    fun pull_fresh() {
        aswm_view(CURR_URL, false)
    }

    //Getting device basic information
    fun get_info() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setCookie(ASWV_URL, "DEVICE=android")
        cookieManager.setCookie(ASWV_URL, "DEV_API=" + Build.VERSION.SDK_INT)
    }

    //Checking permission for storage and camera for writing and uploading images
    fun get_file() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)

        //Checking for storage permission to write images for upload
        if (ASWP_FUPLOAD && ASWP_CAMUPLOAD && !check_permission(2) && !check_permission(3)) {
            ActivityCompat.requestPermissions(this@MainActivity, perms, file_perm)

            //Checking for WRITE_EXTERNAL_STORAGE permission
        } else if (ASWP_FUPLOAD && !check_permission(2)) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), file_perm)

            //Checking for CAMERA permissions
        } else if (ASWP_CAMUPLOAD && !check_permission(3)) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), file_perm)
        }
    }

    //Checking if particular permission is given or not
    fun check_permission(permission: Int): Boolean {
        when (permission) {
            1 -> return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

            2 -> return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

            3 -> return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    //Creating image file for upload
    @Throws(IOException::class)
    private fun create_image(): File {
        @SuppressLint("SimpleDateFormat")
        val file_name = SimpleDateFormat("yyyy_mm_ss").format(Date())
        val new_name = "file_" + file_name + "_"
        val sd_directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(new_name, ".jpg", sd_directory)
    }

    //Launching app rating dialoge [developed by github.com/hotchemi]
    fun get_rating() {
        if (DetectConnection.isInternetAvailable(this@MainActivity)) {
            AppRate.with(this)
					?.setStoreType(StoreType.GOOGLEPLAY)     //default is Google Play, other option is Amazon App Store
					?.setInstallDays(SmartWebView.ASWR_DAYS)
					?.setLaunchTimes(SmartWebView.ASWR_TIMES)
					?.setRemindInterval(SmartWebView.ASWR_INTERVAL)
					?.setTitle(R.string.rate_dialog_title)
					?.setMessage(R.string.rate_dialog_message)
					?.setTextLater(R.string.rate_dialog_cancel)
					?.setTextNever(R.string.rate_dialog_no)
					?.setTextRateNow(R.string.rate_dialog_ok)
					?.monitor()
            AppRate.showRateDialogIfMeetsConditions(this)
        }
        //for more customizations, look for AppRate and DialogManager
    }

    //Creating custom notifications with IDs
    fun show_notification(type: Int, id: Int) {
        val `when` = System.currentTimeMillis()
        asw_notification = this@MainActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val i = Intent()
        if (type == 1) {
            i.setClass(this@MainActivity, MainActivity::class.java!!)
        } else if (type == 2) {
            i.action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        } else {
            i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.data = Uri.parse("package:" + this@MainActivity.packageName)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(this@MainActivity, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this@MainActivity, "")
        when (type) {
            1 -> {
                builder.setTicker(getString(R.string.app_name))
                builder.setContentTitle(getString(R.string.loc_fail))
                builder.setContentText(getString(R.string.loc_fail_text))
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.loc_fail_more)))
                builder.setVibrate(longArrayOf(350, 350, 350, 350, 350))
                builder.setSmallIcon(R.mipmap.ic_launcher)
            }

            2 -> {
                builder.setTicker(getString(R.string.app_name))
                builder.setContentTitle(getString(R.string.loc_perm))
                builder.setContentText(getString(R.string.loc_perm_text))
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.loc_perm_more)))
                builder.setVibrate(longArrayOf(350, 700, 350, 700, 350))
                builder.setSound(alarmSound)
                builder.setSmallIcon(R.mipmap.ic_launcher)
            }
        }
        builder.setOngoing(false)
        builder.setAutoCancel(true)
        builder.setContentIntent(pendingIntent)
        builder.setWhen(`when`)
        builder.setContentIntent(pendingIntent)
        asw_notification_new = builder.build()
        asw_notification.notify(id, asw_notification_new)
    }

    //Checking if users allowed the requested permissions or not
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _location
                }
            }
        }
    }

    //Action on back key tap/click
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (asw_view.canGoBack()) {
                        asw_view.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        asw_view.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        asw_view.restoreState(savedInstanceState)
    }

    companion object {

        //Permission variables
        internal var ASWP_JSCRIPT = SmartWebView.ASWP_JSCRIPT
        internal var ASWP_FUPLOAD = SmartWebView.ASWP_FUPLOAD
        internal var ASWP_CAMUPLOAD = SmartWebView.ASWP_CAMUPLOAD
        internal var ASWP_ONLYCAM = SmartWebView.ASWP_ONLYCAM
        internal var ASWP_MULFILE = SmartWebView.ASWP_MULFILE
        internal var ASWP_LOCATION = SmartWebView.ASWP_LOCATION
        internal var ASWP_RATINGS = SmartWebView.ASWP_RATINGS
        internal var ASWP_PULLFRESH = SmartWebView.ASWP_PULLFRESH
        internal var ASWP_PBAR = SmartWebView.ASWP_PBAR
        internal var ASWP_ZOOM = SmartWebView.ASWP_ZOOM
        internal var ASWP_SFORM = SmartWebView.ASWP_SFORM
        internal var ASWP_OFFLINE = SmartWebView.ASWP_OFFLINE
        internal var ASWP_EXTURL = SmartWebView.ASWP_EXTURL

        //Security variables
        internal var ASWP_CERT_VERIFICATION = SmartWebView.ASWP_CERT_VERIFICATION

        //Configuration variables
        private val ASWV_URL = SmartWebView.ASWV_URL
        private val ASWV_F_TYPE = SmartWebView.ASWV_F_TYPE

        var ASWV_HOST = aswm_host(ASWV_URL)
        private val asw_file_req = 1

        private val loc_perm = 1
        private val file_perm = 2

        private val TAG = MainActivity::class.java.getSimpleName()

        //Getting host name
        fun aswm_host(url: String?): String {
            if (url == null || url.length == 0) {
                return ""
            }
            var dslash = url.indexOf("//")
            if (dslash == -1) {
                dslash = 0
            } else {
                dslash += 2
            }
            var end = url.indexOf('/', dslash)
            end = if (end >= 0) end else url.length
            val port = url.indexOf(':', dslash)
            end = if (port > 0 && port < end) port else end
            Log.w("URL Host: ", url.substring(dslash, end))
            return url.substring(dslash, end)
        }
    }
}

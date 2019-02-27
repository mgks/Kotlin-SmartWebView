package mgks.kotlin.webview

/*
 * Android Smart WebView is an Open Source Project available on GitHub (https://github.com/mgks/Kotlin-SWV).
 * Developed by Ghazi Khan (https://github.com/mgks) under MIT Open Source License.
 * This program is free to use for private and commercial purposes.
 * Please mention project source or developer credit in your Application's License(s) Wiki.
 * Giving right credit to developers encourages them to create better projects :)
*/

internal object SmartWebView {

    /* -- PERMISSION VARIABLES -- */

    // enable JavaScript for webview
    var ASWP_JSCRIPT = true

    // upload file from webview
    var ASWP_FUPLOAD = true

    // enable upload from camera for photos
    var ASWP_CAMUPLOAD = true

    // incase you want only camera files to upload
    var ASWP_ONLYCAM = false

    // upload multiple files in webview
    var ASWP_MULFILE = true

    // track GPS locations
    var ASWP_LOCATION = true

    // show ratings dialog; auto configured
    // edit method get_rating() for customizations
    var ASWP_RATINGS = true

    // pull refresh current url
    var ASWP_PULLFRESH = true

    // show progress bar in app
    var ASWP_PBAR = true

    // zoom control for webpages view
    var ASWP_ZOOM = false

    // save form cache and auto-fill information
    var ASWP_SFORM = false

    // whether the loading webpages are offline or online
    var ASWP_OFFLINE = false

    // open external url with default browser instead of app webview
    var ASWP_EXTURL = true


    /* -- SECURITY VARIABLES -- */

    // verify whether HTTPS port needs certificate verification
    var ASWP_CERT_VERIFICATION = true


    /* -- CONFIG VARIABLES -- */

    //complete URL of your website or offline webpage
    var ASWV_URL = "file:///android_asset/offline.html"

    //to upload any file type using "*/*"; check file type references for more
    var ASWV_F_TYPE = "*/*"


    /* -- RATING SYSTEM VARIABLES -- */

    var ASWR_DAYS = 3    // after how many days of usage would you like to show the dialoge
    var ASWR_TIMES = 10  // overall request launch times being ignored
    var ASWR_INTERVAL = 2   // reminding users to rate after days interval
}

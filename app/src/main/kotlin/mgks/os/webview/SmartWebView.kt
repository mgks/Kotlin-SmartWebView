package mgks.os.webview

/*
 * Android Smart WebView is an Open Source Project available on GitHub (https://github.com/mgks/Kotlin-SmartWebView).
 * Developed by Ghazi Khan (https://github.com/mgks) under MIT Open Source License.
 * This program is free to use for private and commercial purposes.
 * Please mention project source or developer credit in your Application's License(s) Wiki.
 * Giving right credit to developers encourages them to create better projects :)
*/

internal object SmartWebView {

	/* -- PERMISSION VARIABLES -- */

	var ASWP_JSCRIPT		= true            // enable JavaScript for webview
	var ASWP_FUPLOAD		= true            // upload file from webview
	var ASWP_CAMUPLOAD 		= true            // enable upload from camera for photos
	var ASWP_ONLYCAM 		= false           // incase you want only camera files to upload
	var ASWP_MULFILE 		= true            // upload multiple files in webview
	var ASWP_LOCATION 		= true            // track GPS locations

	var ASWP_RATINGS 		= true            // show ratings dialog; auto configured ; edit method get_rating() for customizations

	var ASWP_PULLFRESH 		= true            // pull refresh current url
	var ASWP_PBAR 			= true            // show progress bar in app
	var ASWP_ZOOM 			= false           // zoom control for webpages view
	var ASWP_SFORM 			= false           // save form cache and auto-fill information
	var ASWP_OFFLINE 		= false           // whether the loading webpages are offline or online
	var ASWP_EXTURL 		= true            // open external url with default browser instead of app webview

	var ASWP_TAB 			= true            // instead of default browser, open external URLs in chrome tab
	var ASWP_ADMOB 			= true

	/* -- SECURITY VARIABLES -- */

	var ASWP_CERT_VERIFICATION = true        // verify whether HTTPS port needs certificate verification


	/* -- CONFIG VARIABLES -- */

	// layout selection
	var ASWV_LAYOUT 		= 0                // default=0; for clear fullscreen layout and 1 to add drawer and navigation bar

	// URL configs; search URL can be left empty if default layout is 0
	var ASWV_URL 			= "file:///android_asset/offline.html"      // complete URL of your website or offline webpage
	var ASWV_SEARCH 		= "https://www.google.com/search?q="        // search query will start by the end of the present string
	var ASWV_SHARE_URL 		= "$ASWV_URL?share="                        // URL where you process external content shared with the app

	// domains allowed to be opened inside webview
	var ASWV_EXC_LIST 		= ""		//separate domains with a comma (,)

	// to upload any file type using "*/*"; check file type references for more
	var ASWV_F_TYPE 		= "*/*"

	// admob config
	var ASWV_ADMOB 			= "ca-app-pub-9276682923792397~7957851075"    // your unique publishers ID; this one is temporary


	/* -- RATING SYSTEM VARIABLES -- */

	var ASWR_DAYS 			= 3            // after how many days of usage would you like to show the dialog
	var ASWR_TIMES 			= 10           // overall request launch times being ignored
	var ASWR_INTERVAL 		= 2            // reminding users to rate after days interval
}

package mgks.os.webview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler

class SplashScreen : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val SPLASH_TIME_OUT = 5000
        Handler().postDelayed(/*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

        {
            // This method will be executed once the timer is over
            // Start your app main activity
            val i = Intent(this@SplashScreen, MainActivity::class.java)
            startActivity(i)

            // close this activity
            finish()
        }, SPLASH_TIME_OUT.toLong())
    }

}

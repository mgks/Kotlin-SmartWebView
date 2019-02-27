package mgks.os.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.View

import java.util.Date

import mgks.os.webview.DialogManager.create
import mgks.os.webview.PreferenceHelper.getInstallDate
import mgks.os.webview.PreferenceHelper.getIsAgreeShowDialog
import mgks.os.webview.PreferenceHelper.getLaunchTimes
import mgks.os.webview.PreferenceHelper.getRemindInterval
import mgks.os.webview.PreferenceHelper.isFirstLaunch
import mgks.os.webview.PreferenceHelper.setInstallDate

class AppRate private constructor(context: Context) {

    private val context: Context

    private val options = DialogOptions()

    private var installDate = 10

    private var launchTimes = 10

    private var remindInterval = 1

    private var isDebug = false

    private val isOverLaunchTimes: Boolean
        get() = getLaunchTimes(context) >= launchTimes

    private val isOverInstallDate: Boolean
        get() = isOverDate(getInstallDate(context), installDate)

    private val isOverRemindDate: Boolean
        get() = isOverDate(getRemindInterval(context), remindInterval)

    init {
        this.context = context.applicationContext
    }

    internal fun setLaunchTimes(launchTimes: Int): AppRate {
        this.launchTimes = launchTimes
        return this
    }

    internal fun setInstallDays(installDate: Int): AppRate {
        this.installDate = installDate
        return this
    }

    internal fun setRemindInterval(remindInterval: Int): AppRate {
        this.remindInterval = remindInterval
        return this
    }

    fun setShowLaterButton(isShowNeutralButton: Boolean): AppRate {
        options.setShowNeutralButton(isShowNeutralButton)
        return this
    }

    fun setShowNeverButton(isShowNeverButton: Boolean): AppRate {
        options.setShowNegativeButton(isShowNeverButton)
        return this
    }

    fun setShowTitle(isShowTitle: Boolean): AppRate {
        options.setShowTitle(isShowTitle)
        return this
    }

    fun clearAgreeShowDialog(): AppRate {
        PreferenceHelper.setAgreeShowDialog(context, true)
        return this
    }

    fun clearSettingsParam(): AppRate {
        PreferenceHelper.setAgreeShowDialog(context, true)
        PreferenceHelper.clearSharedPreferences(context)
        return this
    }

    fun setAgreeShowDialog(clear: Boolean): AppRate {
        PreferenceHelper.setAgreeShowDialog(context, clear)
        return this
    }

    fun setView(view: View): AppRate {
        options.view = view
        return this
    }

    /*fun setOnClickButtonListener(listener: OnClickButtonListener): AppRate {
        options.listener = listener
        return this
    }*/

    fun setTitle(resourceId: Int): AppRate {
        options.setTitleResId(resourceId)
        return this
    }

    fun setTitle(title: String): AppRate {
        options.setTitleText(title)
        return this
    }

    internal fun setMessage(resourceId: Int): AppRate {
        options.setMessageResId(resourceId)
        return this
    }

    fun setMessage(message: String): AppRate {
        options.setMessageText(message)
        return this
    }

    internal fun setTextRateNow(resourceId: Int): AppRate {
        options.setTextPositiveResId(resourceId)
        return this
    }

    fun setTextRateNow(positiveText: String): AppRate {
        options.setPositiveText(positiveText)
        return this
    }

    internal fun setTextLater(resourceId: Int): AppRate {
        options.setTextNeutralResId(resourceId)
        return this
    }

    fun setTextLater(neutralText: String): AppRate {
        options.setNeutralText(neutralText)
        return this
    }

    internal fun setTextNever(resourceId: Int): AppRate {
        options.setTextNegativeResId(resourceId)
        return this
    }

    fun setTextNever(negativeText: String): AppRate {
        options.setNegativeText(negativeText)
        return this
    }

    fun setCancelable(cancelable: Boolean): AppRate {
        options.cancelable = cancelable
        return this
    }

    internal fun setStoreType(appstore: StoreType): AppRate {
        options.storeType = appstore
        return this
    }

    internal fun monitor() {
        if (isFirstLaunch(context)) {
            setInstallDate(context)
        }
        PreferenceHelper.setLaunchTimes(context, getLaunchTimes(context) + 1)
    }

    private fun showRateDialog(activity: Activity) {
        if (!activity.isFinishing) {
            create(activity, options).show()
        }
    }

    private fun shouldShowRateDialog(): Boolean {
        return getIsAgreeShowDialog(context) &&
                isOverLaunchTimes &&
                isOverInstallDate &&
                isOverRemindDate
    }

    fun isDebug(): Boolean {
        return isDebug
    }

    fun setDebug(isDebug: Boolean): AppRate {
        this.isDebug = isDebug
        return this
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var singleton: AppRate? = null

        fun with(context: Context): AppRate? {
            if (singleton == null) {
                synchronized(AppRate::class.java) {
                    if (singleton == null) {
                        singleton = AppRate(context)
                    }
                }
            }
            return singleton
        }

        internal fun showRateDialogIfMeetsConditions(activity: Activity) {
            val isMeetsConditions = singleton!!.isDebug || singleton!!.shouldShowRateDialog()
            if (isMeetsConditions) {
                singleton!!.showRateDialog(activity)
            }
        }

        private fun isOverDate(targetDate: Long, threshold: Int): Boolean {
            return Date().time - targetDate >= threshold * 24 * 60 * 60 * 1000
        }
    }

}

internal object PreferenceHelper {

    private val PREF_FILE_NAME = "android_rate_pref_file"

    private val PREF_KEY_INSTALL_DATE = "android_rate_install_date"

    private val PREF_KEY_LAUNCH_TIMES = "android_rate_launch_times"

    private val PREF_KEY_IS_AGREE_SHOW_DIALOG = "android_rate_is_agree_show_dialog"

    private val PREF_KEY_REMIND_INTERVAL = "android_rate_remind_interval"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

    private fun getPreferencesEditor(context: Context): SharedPreferences.Editor {
        return getPreferences(context).edit()
    }

    /**
     * Clear data in shared preferences.<br></br>
     *
     * @param context context
     */
    fun clearSharedPreferences(context: Context) {
        val editor = getPreferencesEditor(context)
        editor.remove(PREF_KEY_INSTALL_DATE)
        editor.remove(PREF_KEY_LAUNCH_TIMES)
        editor.apply()
    }

    /**
     * Set agree flag about show dialog.<br></br>
     * If it is false, rate dialog will never shown unless data is cleared.
     *
     * @param context context
     * @param isAgree agree with showing rate dialog
     */
    fun setAgreeShowDialog(context: Context, isAgree: Boolean) {
        val editor = getPreferencesEditor(context)
        editor.putBoolean(PREF_KEY_IS_AGREE_SHOW_DIALOG, isAgree)
        editor.apply()
    }

    fun getIsAgreeShowDialog(context: Context): Boolean {
        return getPreferences(context).getBoolean(PREF_KEY_IS_AGREE_SHOW_DIALOG, true)
    }

    fun setRemindInterval(context: Context) {
        val editor = getPreferencesEditor(context)
        editor.remove(PREF_KEY_REMIND_INTERVAL)
        editor.putLong(PREF_KEY_REMIND_INTERVAL, Date().time)
        editor.apply()
    }

    fun getRemindInterval(context: Context): Long {
        return getPreferences(context).getLong(PREF_KEY_REMIND_INTERVAL, 0)
    }

    fun setInstallDate(context: Context) {
        val editor = getPreferencesEditor(context)
        editor.putLong(PREF_KEY_INSTALL_DATE, Date().time)
        editor.apply()
    }

    fun getInstallDate(context: Context): Long {
        return getPreferences(context).getLong(PREF_KEY_INSTALL_DATE, 0)
    }

    fun setLaunchTimes(context: Context, launchTimes: Int) {
        val editor = getPreferencesEditor(context)
        editor.putInt(PREF_KEY_LAUNCH_TIMES, launchTimes)
        editor.apply()
    }

    fun getLaunchTimes(context: Context): Int {
        return getPreferences(context).getInt(PREF_KEY_LAUNCH_TIMES, 0)
    }

    fun isFirstLaunch(context: Context): Boolean {
        return getPreferences(context).getLong(PREF_KEY_INSTALL_DATE, 0) == 0L
    }

}

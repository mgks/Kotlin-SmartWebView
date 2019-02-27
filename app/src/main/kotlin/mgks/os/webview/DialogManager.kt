package mgks.os.webview

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View

import java.lang.ref.Reference
import java.lang.ref.WeakReference

import mgks.os.webview.IntentHelper.createIntentForAmazonAppstore
import mgks.os.webview.IntentHelper.createIntentForGooglePlay
import mgks.os.webview.PreferenceHelper.setAgreeShowDialog
import mgks.os.webview.PreferenceHelper.setRemindInterval
import mgks.os.webview.UriHelper.getAmazonAppstore
import mgks.os.webview.UriHelper.getGooglePlay
import mgks.os.webview.UriHelper.isPackageExists
import mgks.os.webview.Utils.getDialogBuilder

internal object DialogManager {

    fun create(context: Context, options: DialogOptions): Dialog {
        val builder = getDialogBuilder(context)
        builder.setMessage(options.getMessageText(context))

        if (options.shouldShowTitle()) builder.setTitle(options.getTitleText(context))

        builder.setCancelable(options.cancelable)

        val view = options.view
        if (view != null) builder.setView(view)

        val listener = options.getListener()

        builder.setPositiveButton(options.getPositiveText(context)) { _, which ->
            val intentToAppstore = if (options.storeType == StoreType.GOOGLEPLAY)
                createIntentForGooglePlay(context)
            else
                createIntentForAmazonAppstore(context)
            context.startActivity(intentToAppstore)
            setAgreeShowDialog(context, false)
            listener?.onClickButton(which)
        }

        if (options.shouldShowNeutralButton()) {
            builder.setNeutralButton(options.getNeutralText(context)) { _, which ->
                setRemindInterval(context)
                listener?.onClickButton(which)
            }
        }

        if (options.shouldShowNegativeButton()) {
            builder.setNegativeButton(options.getNegativeText(context)) { _, which ->
                setAgreeShowDialog(context, false)
                listener?.onClickButton(which)
            }
        }

        return builder.create()
    }

}

internal class DialogOptions {

    private var showNeutralButton = true

    private var showNegativeButton = true

    private var showTitle = true

    var cancelable = false

    var storeType = StoreType.GOOGLEPLAY

    private var titleResId = R.string.rate_dialog_title

    private var messageResId = R.string.rate_dialog_message

    private var textPositiveResId = R.string.rate_dialog_ok

    private var textNeutralResId = R.string.rate_dialog_cancel

    private var textNegativeResId = R.string.rate_dialog_no

    private var titleText: String? = null

    private var messageText: String? = null

    private var positiveText: String? = null

    private var neutralText: String? = null

    private var negativeText: String? = null

    var view: View? = null

    var listener: Reference<OnClickButtonListener>? = null

    fun shouldShowNeutralButton(): Boolean {
        return showNeutralButton
    }

    fun setShowNeutralButton(showNeutralButton: Boolean) {
        this.showNeutralButton = showNeutralButton
    }

    fun shouldShowNegativeButton(): Boolean {
        return showNegativeButton
    }

    fun setShowNegativeButton(showNegativeButton: Boolean) {
        this.showNegativeButton = showNegativeButton
    }

    fun shouldShowTitle(): Boolean {
        return showTitle
    }

    fun setShowTitle(showTitle: Boolean) {
        this.showTitle = showTitle
    }

    fun setTitleResId(titleResId: Int) {
        this.titleResId = titleResId
    }

    fun setMessageResId(messageResId: Int) {
        this.messageResId = messageResId
    }

    fun setTextPositiveResId(textPositiveResId: Int) {
        this.textPositiveResId = textPositiveResId
    }

    fun setTextNeutralResId(textNeutralResId: Int) {
        this.textNeutralResId = textNeutralResId
    }

    fun setTextNegativeResId(textNegativeResId: Int) {
        this.textNegativeResId = textNegativeResId
    }

    fun getListener(): OnClickButtonListener? {
        return if (listener != null) listener!!.get() else null
    }

    fun setListener(listener: OnClickButtonListener) {
        this.listener = WeakReference(listener)
    }

    fun getTitleText(context: Context): String {
        return titleText ?: context.getString(titleResId)
    }

    fun setTitleText(titleText: String) {
        this.titleText = titleText
    }

    fun getMessageText(context: Context): String {
        return messageText ?: context.getString(messageResId)
    }

    fun setMessageText(messageText: String) {
        this.messageText = messageText
    }

    fun getPositiveText(context: Context): String {
        return positiveText ?: context.getString(textPositiveResId)
    }

    fun setPositiveText(positiveText: String) {
        this.positiveText = positiveText
    }

    fun getNeutralText(context: Context): String {
        return neutralText ?: context.getString(textNeutralResId)
    }

    fun setNeutralText(neutralText: String) {
        this.neutralText = neutralText
    }

    fun getNegativeText(context: Context): String {
        return negativeText ?: context.getString(textNegativeResId)
    }

    fun setNegativeText(negativeText: String) {
        this.negativeText = negativeText
    }
}

internal object IntentHelper {

    private val GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending"

    fun createIntentForGooglePlay(context: Context): Intent {
        val packageName = context.packageName
        val intent = Intent(Intent.ACTION_VIEW, getGooglePlay(packageName))
        if (isPackageExists(context, GOOGLE_PLAY_PACKAGE_NAME)) {
            intent.setPackage(GOOGLE_PLAY_PACKAGE_NAME)
        }
        return intent
    }

    fun createIntentForAmazonAppstore(context: Context): Intent {
        val packageName = context.packageName
        return Intent(Intent.ACTION_VIEW, getAmazonAppstore(packageName))
    }

}

interface OnClickButtonListener {
    fun onClickButton(which: Int)
}

internal object UriHelper {

    private val GOOGLE_PLAY = "https://play.google.com/store/apps/details?id="

    private val AMAZON_APPSTORE = "amzn://apps/android?p="

    fun getGooglePlay(packageName: String?): Uri? {
        return if (packageName == null) null else Uri.parse(GOOGLE_PLAY + packageName)
    }

    fun getAmazonAppstore(packageName: String?): Uri? {
        return if (packageName == null) null else Uri.parse(AMAZON_APPSTORE + packageName)
    }

    fun isPackageExists(context: Context, targetPackage: String): Boolean {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName == targetPackage) return true
        }
        return false
    }
}

internal object Utils {

    private val isLollipop: Boolean
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1

    private val dialogTheme: Int
        get() = if (isLollipop) R.style.CustomLollipopDialogStyle else 0

    private fun underHoneyComb(): Boolean {
        return false
    }

    @SuppressLint("NewApi")
    fun getDialogBuilder(context: Context): AlertDialog.Builder {
        return if (underHoneyComb()) {
            AlertDialog.Builder(context)
        } else {
            AlertDialog.Builder(context, dialogTheme)
        }
    }

}

internal enum class StoreType {
    GOOGLEPLAY,
    AMAZON
}

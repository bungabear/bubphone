package io.github.bungabear.bubphone

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast


class NotiListenService : NotificationListenerService() {
    val TAG = NotiListenService::class.java.simpleName
    private val a by lazy { getString(R.string.app_name) }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
//        Log.v(TAG, "onNotificationPosted : ${sbn.notification.extras.bundle2String()}")
        val handler = Handler()
        val preference = SecurePreferences(this, packageName, getString(R.string.large_text), true)
        val pw = preference.getString(a)
        val pack = sbn.packageName
        val packageRegex = Regex(".*(sms|messag).*")
        val urlRegex = Regex("http\\S*")
        if(packageRegex.matches(pack)){
            val text = sbn.notification.extras["android.text"]?.toString()?:""
            if(text.contains("[법인명의 본인확인]")){
                val result = urlRegex.find(text)
                result?.let {
                    val url = result.value
                    Thread{ Handler(Looper.getMainLooper()).post {
                        val hiddenWebview = WebView(this)
                        hiddenWebview.addJavascriptInterface(JavascriptCallback(), "android")
                        hiddenWebview.settings.javaScriptEnabled = true
                        hiddenWebview.webViewClient = object : WebViewClient(){
                            override fun onPageFinished(view: WebView?, url: String?) {
                                handler.postDelayed({
                                    view?.evaluateJavascript("javascript:document.getElementById('bubpw').value='$pw';")
                                    {
                                        view.evaluateJavascript("javascript:document.getElementById('fmBtn').click();")
                                        { s->
                                            handler.postDelayed({
                                                view.loadUrl("javascript:window.android.loginCheck(document.getElementsByTagName('body')[0].innerHTML)")
                                            }, 100)
                                        }
                                    }
                                }, 1000)
                            }
                        }
                        hiddenWebview.loadUrl(url)
                    }
                    }.start()
                }
            }
        }
    }

    inner class JavascriptCallback{

        @JavascriptInterface
        fun loginCheck(body : String){
            if(body.contains("iptOk")){
                // 인증 성공
            }
            else {
                Toast.makeText(this@NotiListenService, "법폰 : 비밀번호가 틀린거같아요...", Toast.LENGTH_LONG).show()
            }
        }

    }

    fun Bundle.bundle2String() = this.keySet().joinToString { "$it : ${this.get(it)}" }
}
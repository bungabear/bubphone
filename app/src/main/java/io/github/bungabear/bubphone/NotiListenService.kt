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
import android.webkit.WebView
import android.webkit.WebViewClient


class NotiListenService : NotificationListenerService() {
    val TAG = NotiListenService::class.java.simpleName
    private val a by lazy { getString(R.string.app_name) }
    override fun onListenerConnected() {
        Log.d(TAG, "onListerConnected")
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        Log.d(TAG, "onListenerDisconnected")
        super.onListenerDisconnected()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return Service.START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "onTaskRemoved")
        super.onTaskRemoved(rootIntent)
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        Log.v(TAG, "onNotificationPosted : ${sbn.notification.extras.bundle2String()}")
        val preference = SecurePreferences(this, packageName, getString(R.string.large_text), true)
        val pw = preference.getString(a)
        val pack = sbn.packageName
        val packageRegex = Regex(".*(sms|messag).*")
        val urlRegex = Regex("http\\S*")
        if(packageRegex.matches(pack)){
            Log.d(TAG, "package filtered : $pack")
            val text = sbn.notification.extras["android.text"]?.toString()?:""
            if(text.contains("[법인명의 본인확인]")){
                Log.d(TAG, "text filtered : $text")
                val result = urlRegex.find(text)
                result?.let {
                    val url = result.value
                    Thread{
                        Handler(Looper.getMainLooper()).post {
                            val hiddenWebview = WebView(this)
                            hiddenWebview.settings.javaScriptEnabled = true
                            hiddenWebview.webViewClient = object : WebViewClient(){
                                var isLoaded = false
                                var isPwinjected = false
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    Log.d("activity", "onPageFinished $url")
                                    Handler().postDelayed({
                                        view?.evaluateJavascript("javascript:document.getElementById('bubpw').value='$pw';")
                                        {
                                            Log.d("webview", "pw fill end")
                                            view.evaluateJavascript("javascript:document.getElementById('fmBtn').click();")
                                            { s->
                                                //div class중 iptOk가 있어야함.
                                                //body.wrap(main_content.ipt.iptOk)
                                                //javascript interface로 html을 100ms정도 delay시켜 받아와 체크.
                                                Log.d("webview", "click end : $s, ${hiddenWebview}")
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

    fun Bundle.bundle2String() = this.keySet().joinToString { "$it : ${this.get(it)}" }
}
package io.github.bungabear.bubphone

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import io.github.bungabear.bubphone.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var mBinding : ActivityMainBinding
    //    private lateinit var etPw : EditText
    private val a by lazy { getString(R.string.app_name) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val securePreference = SecurePreferences(this, packageName, getString(R.string.large_text), true)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mHandler = MyHandler(this)
        mBinding.pw = ""
        mBinding.setSubmitListener {
            Log.d(TAG, mBinding.pw)
            if(mBinding.pw.isNullOrBlank()){
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            else {
                if(securePreference.containsKey(a)){
                    securePreference.removeValue(a)
                }
                securePreference.put(a, mBinding.pw)
                Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        take2Setting()
    }

    private fun take2Setting(){
        if(!isNotiPermissionAllowed()){
            Toast.makeText(this, "권한을 허용해주세요", Toast.LENGTH_LONG).show()
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }
    }

    private fun isNotiPermissionAllowed(): Boolean {
        val notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(this)
        val myPackageName = packageName

        for (packageName in notiListenerSet) {
            if (packageName == null) {
                continue
            }
            if (packageName == myPackageName) {
                return true
            }
        }
        return false
    }

    fun handleMessage(msg : Message){
        val pw = "min6422!#@"
        val webView = WebView(this)
        webView.loadUrl(msg.obj.toString())
        webView.settings.javaScriptEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        webView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                Log.d("activity", "onPageFinished $url")
                Handler().postDelayed({
                    webView.evaluateJavascript("javascript:document.getElementById('bubpw').value='$pw';")
                    { s->
                        Log.d("webview", "pw fill result : $s")
                        webView.evaluateJavascript("javascript:document.getElementById('fmBtn').click();")
                        { s->
                            Log.d("webview", "click result : $s")
                        }
                    }
                }, 1000)

            }
        }
    }

    companion object {
        private var mHandler : MyHandler? = null

        fun sendMessage(msg : Message){
            mHandler?.sendMessage(msg)
        }

        private class MyHandler(
                activity : MainActivity,
                val wrActivity : WeakReference<MainActivity> = WeakReference(activity)
        )
            : Handler(){
            override fun handleMessage(msg: Message?) {
                msg?.let {
                    wrActivity.get()?.apply {
                        handleMessage(msg)
                    }
                }
            }
        }
    }
}

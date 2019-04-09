package io.github.bungabear.bubphone

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import io.github.bungabear.bubphone.databinding.ActivityScrollingBinding
import kotlinx.android.synthetic.main.activity_scrolling.*
import java.lang.ref.WeakReference
import androidx.core.app.NotificationManagerCompat




class ScrollingActivity : AppCompatActivity() {

    private lateinit var mBinding : ActivityScrollingBinding
//    private lateinit var etPw : EditText
    private val a by lazy { getString(R.string.app_name) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val securePreference = SecurePreferences(this, packageName, getString(R.string.large_text), true)
        setContentView(R.layout.activity_scrolling)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_scrolling)
        mHandler = MyHandler(this)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        mBinding.pw = ""
        mBinding.setSubmitListener {
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
//
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_scrolling, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

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
            activity : ScrollingActivity,
            val wrActivity : WeakReference<ScrollingActivity> = WeakReference(activity)
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

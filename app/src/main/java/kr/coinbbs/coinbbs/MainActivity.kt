package kr.coinbbs.coinbbs

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.os.Build
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.widget.Toast
import android.view.View
import android.support.v7.app.AlertDialog
import android.graphics.Bitmap
import android.webkit.*
import android.widget.ProgressBar;
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback





class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val FILECHOOSER_RESULTCODE = 1
    private val REQUEST_SELECT_FILE = 100
    private var mUploadMessage: ValueCallback<Uri>? = null
    private var uploadMessage: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)



        val browser = findViewById(R.id.coinwebview) as WebView
        /*
        browser!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }
        }*/



        val settings = browser.settings

        // Enable java script in web view
        settings.javaScriptEnabled = true

        // Enable and setup web view cache
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setAppCachePath(cacheDir.path)


        // Enable zooming in web view
        settings.setSupportZoom(true)

        // Zoom web view text
        //settings.textZoom = 125


        // Enable disable images in web view
        settings.blockNetworkImage = false
        // Whether the WebView should load image resources
        settings.loadsImagesAutomatically = true


        // More web view settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = true  // api 26
        }
        //settings.pluginState = WebSettings.PluginState.ON
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.mediaPlaybackRequiresUserGesture = false
        }

        // More optional settings, you can enable it by yourself
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.loadWithOverviewMode = true
        settings.allowContentAccess = true
        settings.setGeolocationEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.allowUniversalAccessFromFileURLs = true
        }

        settings.allowFileAccess = true

        // WebView settings
        browser.fitsSystemWindows = true

        /* if SDK version is greater of 19 then activate hardware acceleration
        otherwise activate software acceleration  */

        browser.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Set web view client
        browser.webViewClient = object: WebViewClient(){
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                val progressbar = findViewById(R.id.progress_bar) as ProgressBar
                progressbar.setVisibility(View.VISIBLE)
            }

            override fun onPageFinished(view: WebView, url: String) {
                val progressbar = findViewById(R.id.progress_bar) as ProgressBar
                progressbar.setVisibility(View.GONE)
            }
        }

        // Set web view chrome client
        browser.webChromeClient = object: WebChromeClient(){
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress_bar.progress = newProgress
            }
        }

        browser.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                //Log.d("MainActivity", "onPermissionRequest")
                //requestPermission(request)
            }


            // For Android 3.0+
            fun openFileChooser(uploadMsg: ValueCallback<*>, acceptType: String) {
                mUploadMessage = uploadMsg as ValueCallback<Uri>
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "*/*"
                this@MainActivity.startActivityForResult(
                    Intent.createChooser(i, "File Browser"),
                    FILECHOOSER_RESULTCODE)
            }

            //For Android 4.1
            fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                this@MainActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE)

            }

            protected fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
                mUploadMessage = uploadMsg
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILECHOOSER_RESULTCODE)
            }

            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                uploadMessage?.onReceiveValue(null)
                uploadMessage = null

                uploadMessage = filePathCallback

                val intent = fileChooserParams!!.createIntent()
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE)
                } catch (e: ActivityNotFoundException) {
                    uploadMessage = null
                    Toast.makeText(applicationContext, "Cannot Open File Chooser", Toast.LENGTH_LONG).show()
                    return false
                }

                return true
            }


        }
        browser.loadUrl(getString(R.string.url_coinbbs))

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode === REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return
                //print("result code = " + resultCode)
                var results: Array<Uri>? = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
                uploadMessage?.onReceiveValue(results)
                uploadMessage = null
            }
        } else if (requestCode === FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            val result = if (intent == null || resultCode !== RESULT_OK) null else intent.data
            mUploadMessage?.onReceiveValue(result)
            mUploadMessage = null
        } else
            Toast.makeText(applicationContext, "Failed to Upload Image", Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        val browser = findViewById(R.id.coinwebview) as WebView
        if (browser.canGoBack()) {
            // If web view have back history, then go to the web view back history
            browser.goBack()
        }else {
            super.onBackPressed()
        }
        /*if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val browser = findViewById(R.id.coinwebview) as WebView
        when (item.itemId) {
            R.id.nav_coinbbs -> {
                browser.loadUrl(getString(R.string.url_coinbbs))
            }
            R.id.nav_bbscoin -> {
                browser.loadUrl(getString(R.string.url_bbscoin))
            }
            R.id.nav_telegram -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_telegram)))
                startActivity(intent)
            }
            R.id.nav_money -> {
                browser.loadUrl(getString(R.string.url_bbsmoney))
            }
            R.id.nav_twitter -> {
                browser.loadUrl(getString(R.string.url_twitter))
            }
            R.id.nav_forum -> {
                browser.loadUrl(getString(R.string.url_forum))
            }
            R.id.nav_coinmarketcap -> {
                browser.loadUrl(getString(R.string.url_coinmarketcap))
            }
            R.id.nav_exit -> {
                super.onBackPressed()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}

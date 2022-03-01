package com.merxury.blocker.ui.detail

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.elvishew.xlog.XLog
import com.google.android.material.tabs.TabLayoutMediator
import com.merxury.blocker.R
import com.merxury.blocker.core.root.EControllerMethod
import com.merxury.blocker.databinding.ActivityAppDetailBinding
import com.merxury.blocker.util.PreferenceUtil
import com.merxury.libkit.entity.Application
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnBinderDeadListener
import rikka.shizuku.Shizuku.OnBinderReceivedListener
import rikka.sui.Sui

class AppDetailActivity : AppCompatActivity() {
    private var _app: Application? = null
    private val app get() = _app!!
    private val logger = XLog.tag("DetailActivity")
    private lateinit var binding: ActivityAppDetailBinding
    private val binderReceivedListener = OnBinderReceivedListener {
        if (Shizuku.isPreV11()) {
            logger.e("Shizuku pre-v11 is not supported")
        } else {
            logger.i("Shizuku binder received")
            checkPermission()
        }
    }
    private val binderDeadListener = OnBinderDeadListener {
        logger.e("Shizuku binder dead")
    }
    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE_PERMISSION) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    logger.i("Shizuku permission granted")
                } else {
                    logger.e("Shizuku permission denied")
                    AlertDialog.Builder(this)
                        .setTitle(R.string.permission_required)
                        .setMessage(R.string.shizuku_permission_required_message)
                        .show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fetchAppInfo()
        initToolbar()
        initViewPager()
        initEdgeToEdge()
        registerShizuku()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterShizuku()
    }

    private fun registerShizuku() {
        if (PreferenceUtil.getControllerType(this) != EControllerMethod.SHIZUKU) return
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
    }

    private fun checkPermission(): Boolean {
        if (Shizuku.isPreV11()) {
            return false
        }
        try {
            return if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                if (Sui.isSui()) {
                    Sui.init(packageName)
                }
                true
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                logger.e("User denied Shizuku permission (shouldShowRequestPermissionRationale=true)")
                false
            } else {
                Shizuku.requestPermission(REQUEST_CODE_PERMISSION)
                false
            }
        } catch (e: Throwable) {
            logger.e("Check Shizuku permission failed", e)
        }
        return false
    }

    private fun unregisterShizuku() {
        if (PreferenceUtil.getControllerType(this) != EControllerMethod.SHIZUKU) {
            return
        }
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = app.label
        }
    }

    private fun initViewPager() {
        binding.viewPager.apply {
            adapter = AppDetailAdapter(this@AppDetailActivity, app)
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setText(AppDetailAdapter.titles[position])
        }.attach()
    }

    private fun initEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                leftMargin = insets.left
                rightMargin = insets.right
            }
            windowInsets
        }
    }

    private fun fetchAppInfo() {
        _app = intent?.getParcelableExtra(EXTRA_APP)
        if (_app == null) {
            logger.e("app is null")
            finish()
            return
        }
        logger.i("Show app: ${app.packageName}")
    }

    companion object {
        private const val EXTRA_APP = "EXTRA_APP"
        private const val REQUEST_CODE_PERMISSION = 101

        fun start(context: Context, app: Application) {
            Intent(context, AppDetailActivity::class.java).apply {
                putExtra(EXTRA_APP, app)
            }.run {
                context.startActivity(this)
            }
        }
    }
}
package com.merxury.blocker.ui.component

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.ActivityManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.merxury.blocker.R
import com.merxury.blocker.entity.Application
import com.merxury.blocker.ui.adapter.FragmentAdapter
import com.merxury.blocker.ui.base.IActivityView
import com.merxury.blocker.util.setupActionBar
import com.merxury.blocker.utils.ApplicationUtils
import kotlinx.android.synthetic.main.activity_component.*
import kotlinx.android.synthetic.main.application_brief_info_layout.*

class ComponentActivity : AppCompatActivity(), IActivityView {

    private lateinit var application: Application

    private lateinit var adapter: FragmentAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component)
        // init toolbar
        setupActionBar(R.id.component_toolbar) {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        getDataFromIntent()
        setupViewPager()
        setupTab()
        showApplicationBriefInfo(application)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewPager() {
        adapter = FragmentAdapter(supportFragmentManager)
        adapter.addFragment(ComponentFragment.newInstance(application.packageName, EComponentType.RECEIVER), getString(R.string.receiver))
        adapter.addFragment(ComponentFragment.newInstance(application.packageName, EComponentType.SERVICE), getString(R.string.service))
        adapter.addFragment(ComponentFragment.newInstance(application.packageName, EComponentType.ACTIVITY), getString(R.string.activity))
        adapter.addFragment(ComponentFragment.newInstance(application.packageName, EComponentType.PROVIDER), getString(R.string.provider))
        component_viewpager.adapter = adapter
    }

    private fun getDataFromIntent() {
        if (intent == null) {
            finish()
        }
        application = intent.getParcelableExtra(Constant.APPLICATION)
    }

    private fun setupTab() {
        component_tabs.setupWithViewPager(component_viewpager)
        changeColor(getBackgroundColor(0))
        component_tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.md_white_1000))
        component_tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                changeBackgroundColor(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }

    private fun showApplicationBriefInfo(application: Application) {
        app_info_app_name.text = getString(R.string.application_label, application.label)
        app_info_app_package_name.text = getString(R.string.package_name, application.packageName)
        app_info_target_sdk_version.text = getString(R.string.target_sdk_version, CODENAME.get(application.targetSdkVersion, UNKNOWN))
        app_info_min_sdk_version.text = getString(R.string.min_sdk_version, CODENAME.get(application.minSdkVersion, UNKNOWN))
        Glide.with(this)
                .load(application.getApplicationIcon(packageManager))
                .transition(DrawableTransitionOptions().crossFade())
                .into(app_info_icon)
        app_info_icon.setOnClickListener({ ApplicationUtils.startApplication(this, application.packageName) })
    }


    private fun changeColor(color: Int) {
        component_toolbar.setBackgroundColor(color)
        component_tabs.setBackgroundColor(color)
        component_collapsing_toolbar.setBackgroundColor(color)
        window.statusBarColor = color
        setTaskDescription(ActivityManager.TaskDescription(null, null, color))
    }

    private fun changeBackgroundColor(tab: TabLayout.Tab) {
        val colorFrom = if (component_tabs.background != null) {
            (component_tabs.background as ColorDrawable).color
        } else {
            ContextCompat.getColor(this, android.R.color.darker_gray)
        }
        val colorTo = getBackgroundColor(tab.position)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.addUpdateListener { animation ->
            val color = animation.animatedValue as Int
            changeColor(color)
        }
        colorAnimation.duration = 500
        colorAnimation.start()
    }

    override fun getBackgroundColor(tabPosition: Int): Int {
        return when (tabPosition) {
            0 -> ContextCompat.getColor(this, R.color.md_blue_700)
            1 -> ContextCompat.getColor(this, R.color.md_light_green_700)
            2 -> ContextCompat.getColor(this, R.color.md_orange_700)
            3 -> ContextCompat.getColor(this, R.color.md_red_700)
            else -> ContextCompat.getColor(this, R.color.md_grey_700)
        }
    }

    companion object {
        private val CODENAME: SparseArray<String> = SparseArray(32)
        private const val UNKNOWN = "Unknown"

        init {
            CODENAME.put(1, "Android 1.0")
            CODENAME.put(2, "Android 1.1")
            CODENAME.put(3, "Android 1.5 (Cupcake)")
            CODENAME.put(4, "Android 1.6 (Donut)")
            CODENAME.put(5, "Android 2.0 (Eclair)")
            CODENAME.put(6, "Android 2.0.1 (Eclair)")
            CODENAME.put(7, "Android 2.1 (Eclair)")
            CODENAME.put(8, "Android 2.2 (Froyo)")
            CODENAME.put(9, "Android 2.3 (Gingerbread)")
            CODENAME.put(10, "Android 2.3.3 (Gingerbread)")
            CODENAME.put(11, "Android 3.0 (Honeycomb)")
            CODENAME.put(12, "Android 3.1 (Honeycomb)")
            CODENAME.put(13, "Android 3.2 (Honeycomb)")
            CODENAME.put(14, "Android 4.0.1 (ICS)")
            CODENAME.put(15, "Android 4.0.3 (ICS)")
            CODENAME.put(16, "Android 4.1 (Jelly Bean)")
            CODENAME.put(17, "Android 4.2 (Jelly Bean)")
            CODENAME.put(18, "Android 4.3 (Jelly Bean)")
            CODENAME.put(19, "Android 4.4 (KitKat)")
            CODENAME.put(21, "Android 5.0 (Lollipop)")
            CODENAME.put(22, "Android 5.1 (Lollipop)")
            CODENAME.put(23, "Android 6.0 (Marshmallow)")
            CODENAME.put(24, "Android 7.0 (Nougat)")
            CODENAME.put(25, "Android 7.1 (Nougat)")
            CODENAME.put(26, "Android 8.0 (Oreo)")
            CODENAME.put(27, "Android 8.1 (Oreo)")
            CODENAME.put(28, "Android 9.0 (P)")
            // Reference : https://source.android.com/setup/start/build-numbers
        }
    }
}

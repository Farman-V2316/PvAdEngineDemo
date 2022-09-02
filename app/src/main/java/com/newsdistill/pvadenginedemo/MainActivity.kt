package com.newsdistill.pvadenginedemo

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MenuItem
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.newsdistill.pvadenginedemo.ads.insertAd
import com.newsdistill.pvadenginedemo.dummydata.fragments.CommunityFragment
import com.newsdistill.pvadenginedemo.dummydata.fragments.ShortsFragment
import com.newsdistill.pvadenginedemo.dummydata.util.DisplayUtils
import com.newsdistill.pvadenginedemo.dummydata.util.KeepStateNavigator
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.common.helper.common.BusProvider
import com.squareup.otto.Subscribe

class MainActivity : AppCompatActivity() {
    private lateinit var  adContainer: RelativeLayout
    private  var uiBus = BusProvider.getUIBusInstance()
    private val uniqueRequestId = 111

    var navController: NavController? = null
    var bottomNavigationView: BottomNavigationView? = null
    private var navHostFragment: NavHostFragment? = null
    private var navigator: KeepStateNavigator? = null
    private var communityFragment = CommunityFragment()
    private var shortsFragment = ShortsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("panda: PVAd demo launched....")
        initUI()

//        adContainer = findViewById(R.id.ad_container)
//        initAd(uiBus, uniqueRequestId)
    }

    private fun initUI() {
        setDisplayMetrics()
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        navController = Navigation.findNavController(this, R.id.main_container)
        // get fragment
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment?
        // setup custom navigator
        // setup custom navigator
        if (navHostFragment != null) {
            navigator = KeepStateNavigator(
                this,
                navHostFragment!!.getChildFragmentManager(),
                R.id.main_container
            )
        }
        if (navigator != null) {
            navController!!.navigatorProvider.addNavigator(navigator!!)
            navController!!.setGraph(R.navigation.bottom_nav_graph)
            NavigationUI.setupWithNavController(bottomNavigationView!!, navController!!)
            communityFragment = navigator!!.getCurrentFragment() as CommunityFragment
        }
        bottomNavigationView!!.setOnNavigationItemSelectedListener { item ->
            selectTab(item)
            true
        }
    }

    fun selectTab(item: MenuItem) {
        when(item.itemId) {
            R.id.action_home -> {
                navController?.navigate(R.id.action_home)
                navigator!!.currentFragment as CommunityFragment
            }
            R.id.action_shorts -> {
                navController?.navigate(R.id.action_shorts)
                navigator!!.currentFragment as ShortsFragment
            }
        }
    }

    override fun onStart() {
        super.onStart()
        uiBus.register(this)
    }

    override fun onStop() {
        super.onStop()
        uiBus.unregister(this)
    }

    @Subscribe
    fun setAdResponse(nativeAdContainer: NativeAdContainer) {
        println("panda: setAdResponse-------------------> $nativeAdContainer")

        if (nativeAdContainer.baseAdEntities == null ||
            nativeAdContainer.uniqueRequestId != uniqueRequestId
        ) {
            return
        }
        if(adContainer != null) {
            insertAd(this, nativeAdContainer, adContainer)
        }
    }

    private fun setDisplayMetrics() {
        val displayMetrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val instance: DisplayUtils = DisplayUtils.getInstance()
        instance.setHeightPx(displayMetrics.heightPixels)
        instance.setWidthPx(displayMetrics.widthPixels)
        instance.setDensity(displayMetrics.density)
        val screenHeight = displayMetrics.heightPixels / displayMetrics.density
        val screenWidth = displayMetrics.widthPixels / displayMetrics.density
        instance.setScreenHeight(screenHeight)
        instance.setScreenWidth(screenWidth)
        instance.setVisibleScreenHeight(screenHeight - 20) //minus statusbar
        instance.setVisibleScreenHeightMinusBottomNav(screenHeight - 20 - 55)
        if (screenHeight != 0f) {
            instance.setAspectRatio(screenWidth / screenHeight)
        }
        instance.setScaleDensity(displayMetrics.scaledDensity)
    }
}
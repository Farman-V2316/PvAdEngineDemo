package com.newsdistill.pvadenginedemo

import android.os.Bundle
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
        initUI()
    }

    private fun initUI() {
        setDisplayMetrics(this)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        navController = Navigation.findNavController(this, R.id.main_container)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_container)
                as NavHostFragment?
        navHostFragment?.let {
            navigator = KeepStateNavigator(this,
                navHostFragment!!.getChildFragmentManager(), R.id.main_container
            )
        }

        navigator?.let {
            navController!!.navigatorProvider.addNavigator(navigator!!)
            navController!!.setGraph(R.navigation.bottom_nav_graph)
            NavigationUI.setupWithNavController(bottomNavigationView!!, navController!!)
            communityFragment = navigator!!.getCurrentFragment() as CommunityFragment
        }

       bottomNavigationView?.let {
           it.setOnNavigationItemSelectedListener { item ->
               selectTab(item)
               true
           }
       }
    }

    private fun selectTab(item: MenuItem) {
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
}
package com.newshunt.appview.common.postcreation.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.libraries.places.api.Places
import com.google.gson.reflect.TypeToken
import com.newshunt.appview.R
import com.newshunt.appview.common.postcreation.DaggerPostCreationLocationComponent
import com.newshunt.appview.common.postcreation.view.adapter.PostCurrentPlaceAdapter
import com.newshunt.appview.common.postcreation.view.adapter.PostCurrentPlaceClickListener
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.LOCATION_REQUEST
import com.newshunt.appview.common.postcreation.view.helper.PostConstants.Companion.POST_SELECTED_LOCATION
import com.newshunt.appview.common.postcreation.viewmodel.PostAutoCompleteLocationVM
import com.newshunt.appview.common.postcreation.viewmodel.PostCreationViewModelFactory
import com.newshunt.appview.common.postcreation.viewmodel.PostCurrentPlaceVM
import com.newshunt.appview.common.utils.ApiKeyProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.asset.PostCurrentPlace
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.CACHE_DEFAULT_TIME
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.news.view.activity.NewsBaseActivity
import javax.inject.Inject

private const val TAG = "PostLocationActivity"

class PostLocationActivity : NewsBaseActivity(), PostCurrentPlaceClickListener, TextWatcher {

    private lateinit var searchLocation: EditText
    private lateinit var locationRv: RecyclerView
    private lateinit var preSearchLocationVM: PostCurrentPlaceVM
    private var preSearchAdapter: PostCurrentPlaceAdapter? = null
    private lateinit var autoCompleteLocationVM: PostAutoCompleteLocationVM
    private var autoCompleteAdapter: PostCurrentPlaceAdapter? = null
    private var isAutoCompleteInitialize = false
    private var userSelectedLoc: PostCurrentPlace? = null
    private var isAutoCompleteResultVisible = false
    private lateinit var progressBar: ProgressBar

    @Inject
    lateinit var vmFactory: PostCreationViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_location)

        checkLocationPermission()

        if (!Places.isInitialized()) {
            Places.initialize(CommonUtils.getApplication(), ApiKeyProvider.googleApiKey)
        }

        DaggerPostCreationLocationComponent.create().inject(this)
        userSelectedLoc = intent.getSerializableExtra(POST_SELECTED_LOCATION) as? PostCurrentPlace
        initViews()
        initPreSearchViewModel()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                CommonUtils.getApplication(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Logger.d(TAG, "Location Permission missing. Finish")
            finish()
            return
        }
    }

    private fun initViews() {
        findViewById<ImageView>(R.id.close_btn).setOnClickListener { finish() }
        searchLocation = findViewById<AutoCompleteTextView>(R.id.searchLocation)
        searchLocation.addTextChangedListener(this)
        val isAutoCompleteEnable = PreferenceManager.getPreference(
            AppStatePreference.POST_CREATE_LOCATION_AUTOCOMPLETE_ENABLE, true
        )
        searchLocation.visibility = if (isAutoCompleteEnable) View.VISIBLE else View.GONE
        locationRv = findViewById(R.id.postLocationRv)
        locationRv.apply {
            layoutManager =
                LinearLayoutManager(this@PostLocationActivity, RecyclerView.VERTICAL, false)
            preSearchAdapter = PostCurrentPlaceAdapter(this@PostLocationActivity)
            adapter = preSearchAdapter
        }
        autoCompleteAdapter = PostCurrentPlaceAdapter(this@PostLocationActivity)
        autoCompleteLocationVM =
            ViewModelProviders.of(this, vmFactory)
                .get(PostAutoCompleteLocationVM::class.java)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initPreSearchViewModel() {
        preSearchLocationVM = ViewModelProviders.of(this, vmFactory).get(
            PostCurrentPlaceVM::class.java
        )
        val isDataAvailable = loadLastData()
        if (!isDataAvailable) {
            observePreSearchLocation()
            showLocationOnDialog()
        }
    }

    private fun loadLastData(): Boolean {
        val currentTS: Long = System.currentTimeMillis()
        val lastTS: Long = PreferenceManager.getPreference(
            AppStatePreference.POST_CREATE_LOCATION_NEAR_BY_LAST_TIME_STAMP,
            currentTS
        )
        val cacheTime: Long = PreferenceManager.getPreference(
            AppStatePreference.POST_CREATE_LOCATION_NEAR_BY_CACHE_TIME, CACHE_DEFAULT_TIME
        )
        if (currentTS - lastTS > cacheTime) {
            PreferenceManager.savePreference(
                AppStatePreference.POST_CREATE_LOCATION_NEAR_BY_LIST,
                ""
            )
            return false
        }

        val json: String? = PreferenceManager.getPreference(
            AppStatePreference.POST_CREATE_LOCATION_NEAR_BY_LIST,
            ""
        )
        if (json.isNullOrEmpty().not()) {
            try {
                val type = object : TypeToken<List<PostCurrentPlace>>() {}.type
                val locationList = CommonUtils.GSON.fromJson<List<PostCurrentPlace>>(json, type)
                if (locationList.isNotEmpty()) {
                    userSelectedLoc?.let {
                        preSearchAdapter?.dataItems?.add(it)
                    }
                    preSearchAdapter?.dataItems?.addAll(locationList)
                    locationRv.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    preSearchAdapter?.notifyDataSetChanged()
                    return true
                }
            } catch (e: Exception) {
            }
        }
        return false
    }

    private fun observePreSearchLocation() {
        preSearchLocationVM.currentPlaceLiveData.observe(this, Observer {
            preSearchAdapter?.dataItems?.clear()
            userSelectedLoc?.also { loc ->
                preSearchAdapter?.dataItems?.add(loc)
            }
            if (it.isSuccess) {
                Logger.d(TAG, "Success fetching places")
                val data = it.getOrNull()
                data?.also {
                    preSearchAdapter?.dataItems?.addAll(data)
                    PreferenceManager.savePreference(
                        AppStatePreference.POST_CREATE_LOCATION_NEAR_BY_LIST,
                        CommonUtils.GSON.toJson(data)
                    )
                    PreferenceManager.savePreference(
                        AppStatePreference.POST_CREATE_LOCATION_NEAR_BY_LAST_TIME_STAMP,
                        System.currentTimeMillis()
                    )
                }
            } else {
                Logger.e(TAG, "Error fetching places")
            }
            locationRv.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            preSearchAdapter?.notifyDataSetChanged()
            preSearchLocationVM.currentPlaceLiveData.removeObservers(this)
        })
    }

    override fun onCurrentPlaceItemClick(data: PostCurrentPlace) {
        val intent = Intent()
        data.isUserSelected = true
        data.isAutoLocation = false
        intent.putExtra(POST_SELECTED_LOCATION, data)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onLocationDeleted() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        if (isAutoCompleteResultVisible.not()) {
            val intent = Intent()
            val size = preSearchAdapter?.dataItems?.size ?: 0
            if (size > 0) {
                preSearchAdapter?.dataItems?.get(0)?.also {
                    it.isUserSelected = true
                    intent.putExtra(POST_SELECTED_LOCATION, it)
                    setResult(Activity.RESULT_OK, intent)
                }
            }
        }
        super.onBackPressed()
    }

    override fun afterTextChanged(s: Editable?) {
        if (s?.length?: 0 < 4) {
            isAutoCompleteResultVisible = false
            locationRv.adapter = preSearchAdapter
            autoCompleteAdapter?.dataItems?.clear()
            return
        }
        initAutoCompleteViewModel()
        setAutoCompleteAdapter()
        autoCompleteLocationVM.getAutoCompleteLocation(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        //Do nothing
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        //Do nothing
    }

    private fun setAutoCompleteAdapter() {
        if (isAutoCompleteResultVisible.not()) {
            isAutoCompleteResultVisible = true
            locationRv.adapter = autoCompleteAdapter
        }
    }

    private fun initAutoCompleteViewModel() {
        if (isAutoCompleteInitialize) {
            return
        }
        isAutoCompleteInitialize = true

        autoCompleteLocationVM.locationLiveData.observe(this, Observer {
            if (it.isSuccess) {
                val data = it.getOrNull()
                Logger.d(TAG, "Success auto complete result: ${data?.size}")
                data?.let {
                    autoCompleteAdapter?.dataItems?.clear()
                    autoCompleteAdapter?.dataItems?.addAll(data)
                    autoCompleteAdapter?.notifyDataSetChanged()
                }
            } else {
                Logger.e(TAG, "Error fetching auto complete result")
            }
            locationRv.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showLocationOnDialog() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(createLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY))
            .addLocationRequest(createLocationRequest(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY))

        val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        result.addOnCompleteListener {
            try {
                val response = it.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                // requests here.
                preSearchLocationVM.findCurrentPlaces()
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            val resolvable = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                this@PostLocationActivity, LOCATION_REQUEST
                            )
                        } catch (e: Exception) {
                            preSearchLocationVM.findCurrentPlaces()
                        }
                    else -> {
                        preSearchLocationVM.findCurrentPlaces()
                    }
                }
            }
        }
    }

    private fun createLocationRequest(p: Int = LocationRequest.PRIORITY_HIGH_ACCURACY):
            LocationRequest {
        return LocationRequest().apply {
            interval = 10 * 60 * 1000
            maxWaitTime = 60 * 60 * 1000
            priority = p
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOCATION_REQUEST -> {
                preSearchLocationVM.findCurrentPlaces()
            }

        }
    }
}
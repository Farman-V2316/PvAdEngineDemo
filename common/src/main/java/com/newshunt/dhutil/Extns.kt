/*
 *
 *  *Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.dhutil

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.util.SizeF
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.preference.SavedPreference
import com.newshunt.common.view.customview.DisableInterceptViewGroup
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.squareup.otto.Bus
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.math.BigInteger
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.HashMap
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import io.reactivex.Observable.range
import io.reactivex.Observable.timer
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import io.reactivex.functions.BiFunction

/**
 * This file contains utility functions implemented as extension functions.
 * @author satosh.dhanymaraju
 */


/**
 * Takes 2 livedata, and a function with 2 input params
 * Returned livedata will emit only after getting single nonnull item from both liveadata
 * Then any event from either livedata will result in an event in returned livedata
 *
 */
fun <X, Y, Z> LiveData<X>.zipWith(y: LiveData<Y>, f: (X, Y) -> Z): LiveData<Z> {
    val z = MediatorLiveData<Z>()
    var xval: X? = null
    var yval: Y? = null
    fun post() {
        xval?.let { xv ->
            yval?.let { yv ->
                z.value = f(xv, yv)
            }
        }
    }
    z.addSource(this) {
        xval = it
        post()
    }
    z.addSource(y) {
        yval = it
        post()
    }
    return z
}

/**
 * Same as [zipWith], but 3 livedata
 */
fun <X, Y, Z, A> LiveData<X>.combineWith(y: LiveData<Y>, z: LiveData<Z>, f: (X, Y, Z) -> A): LiveData<A> {
    val a = MediatorLiveData<A>()
    fun post(xv: X?, yv: Y?, zv: Z?) {
        if (xv != null && yv != null && zv != null)
            a.value = f(xv, yv, zv)
    }
    a.addSource(this) { post(it, y.value, z.value) }
    a.addSource(y) { post(this.value, it, z.value) }
    a.addSource(z) { post(this.value, y.value, it) }
    return a
}



/**
 * Takes a single-parameter-function(fn), keySelector, and returns another
 * single-paramter-function fn2.
 *
 * Fn2 has a map; whenever it is invoked, it looks up the result in the map.
 * If present, it will be returned, else it calls fn to get the result, add it to map and return it.
 * keyselector is the mapping function from input paramter to map-key
 *
 */
fun <X, R, Y> makeFunctionCache(keySelector: (X) -> Y, fn: (X) -> R): (X, removeKey: Boolean) -> R {
    val cache: MutableMap<Y, R> = HashMap()
    return {it, removeKey ->
        if (removeKey && cache.containsKey(keySelector(it))) cache.remove(keySelector(it))
        cache.getOrPut(keySelector(it)) { fn(it) }
    }
}

fun String.urlEncode(): String? {
    return try {
        URLEncoder.encode(this, Constants.TEXT_ENCODING_UTF_8)
    } catch (e: Exception) {
        null
    }
}

fun String.urlDecode(): String? {
    return try {
        URLDecoder.decode(this, Constants.TEXT_ENCODING_UTF_8)
    } catch (e: Exception) {
        null
    }
}

/**
 * Takes a function(receiver) and returns a function that can executes max [n] times
 */
infix fun <R> (() -> R).execMax(n: Int): () -> Unit {
    var called = 0
    return {
        if (called < n) {
            called++
            invoke()
        }
    }
}


/**
 * Will log if loggerEnabled
 */
inline fun logV(tag: String, msg: String) {
    {
        Log.v(tag, msg)
    }
}

/**
 *  Will log if loggerEnabled
 */
inline fun logD(tag: String, msg: String) {
    {
        Log.d(tag, msg)
    }
}

/**
 *  Will log if loggerEnabled
 */
inline fun logE(tag: String, msg: String) {
    {
        Log.e(tag, msg)
    }
}

/**
 * @return a list whose maximum size is @param max.
 *
 * @throws Exception if max is negative
 *
 */
fun <E> List<E>.trimToSize(max: Int) = when {
    max < 0 -> throw Exception("max should be non-negative")
    max == 0 -> listOf()
    max < size -> subList(0, max)
    else -> this
}

fun <E> MutableList<E>.moveElementToTop(func: (E) -> Boolean) {
    find { func(it) }?.let {
        remove(it)
        add(0, it)
    }
}

/**
 * Utility function to dispose a Disposable, if it is not disposed
 *
 * return false if null or isDisposed else true
 */
fun Disposable?.disposeIfNeeded() =
        if (this == null) false
        else if (!isDisposed) {
            dispose()
            true
        } else false

/**
 * Utility function to check if given string is a valid url
 */
fun String?.isValidUrl(): Boolean {
    when {
        this == null -> return false
        else -> {
            when {
                CommonUtils.isEmpty(this) -> return false
                else -> {
                    try {
                        URL(this)
                    } catch (throwable: Throwable) {
                        return false
                    }
                    return true
                }
            }
        }
    }
}

fun String?.isUrlThatHasAllowLocalCardsParam(): Boolean {
    return when {
        this == null -> false
        !isValidUrl() -> false
        else -> Uri.parse(this).getQueryParameter(Constants.URL_PARAM_ALLOW_LOCAL_CARD) == Constants.YES
    }
}

fun <T> runOnce(f: (T) -> Boolean): (T) -> Unit {
    var ran = false
    return {
        if (!ran) {
            ran = f.invoke(it)
        }
    }
}

/**
 *
 * Taken from android-ktx library.
 * https://github.com/android/android-ktx/blob/8de608c8d7cf81f3a637a1b2fcd7992050a567f9/src/main/java/androidx/core/os/Bundle.kt
 *
 * Returns a new [Bundle] with the given key/value pairs as elements.
 *
 * @throws IllegalArgumentException When a value is not a supported type of [Bundle].
 */
fun bundleOf(vararg pairs: Pair<String, Any?>) = Bundle(pairs.size).apply {
    for ((key, value) in pairs) {
        when (value) {
            null -> putString(key, null) // Any nullable type will suffice.

            // Scalars
            is Boolean -> putBoolean(key, value)
            is Byte -> putByte(key, value)
            is Char -> putChar(key, value)
            is Double -> putDouble(key, value)
            is Float -> putFloat(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Short -> putShort(key, value)

            // References
            is Bundle -> putBundle(key, value)
            is CharSequence -> putCharSequence(key, value)
            is Parcelable -> putParcelable(key, value)

            // Scalar arrays
            is BooleanArray -> putBooleanArray(key, value)
            is ByteArray -> putByteArray(key, value)
            is CharArray -> putCharArray(key, value)
            is DoubleArray -> putDoubleArray(key, value)
            is FloatArray -> putFloatArray(key, value)
            is IntArray -> putIntArray(key, value)
            is LongArray -> putLongArray(key, value)
            is ShortArray -> putShortArray(key, value)

            // Reference arrays
            is Array<*> -> {
                val componentType = value::class.java.componentType
                componentType?.let {
                    @Suppress("UNCHECKED_CAST") // Checked by reflection.
                    when {
                        Parcelable::class.java.isAssignableFrom(componentType) -> {
                            putParcelableArray(key, value as Array<Parcelable>)
                        }
                        String::class.java.isAssignableFrom(componentType) -> {
                            putStringArray(key, value as Array<String>)
                        }
                        CharSequence::class.java.isAssignableFrom(componentType) -> {
                            putCharSequenceArray(key, value as Array<CharSequence>)
                        }
                        Serializable::class.java.isAssignableFrom(componentType) -> {
                            putSerializable(key, value)
                        }
                        else -> {
                            val valueType = componentType.canonicalName
                            throw IllegalArgumentException(
                                "Illegal value array type $valueType for key \"$key\"")
                        }
                    }
                }
            }

            // Last resort. Also we must check this after Array<*> as all arrays are serializable.
            is Serializable -> putSerializable(key, value)

            else -> {
                if (Build.VERSION.SDK_INT >= 18 && value is Binder) {
                    putBinder(key, value)
                } else if (Build.VERSION.SDK_INT >= 21 && value is Size) {
                    putSize(key, value)
                } else if (Build.VERSION.SDK_INT >= 21 && value is SizeF) {
                    putSizeF(key, value)
                } else {
                    val valueType = value.javaClass.canonicalName
                    throw IllegalArgumentException("Illegal value type $valueType for key \"$key\"")
                }
            }
        }
    }
}

fun mapfromJson(json: String): Map<String, String> {
    val type = object : TypeToken<Map<String, String>>() {}.type
    val map: Map<String, String>? = JsonUtils.fromJson(json, type)
    return map ?: emptyMap()
}

/** Returns a [MutableIterator] over the views in this view group. */
operator fun ViewGroup.iterator() = object : MutableIterator<View> {
    private var index = 0
    override fun hasNext() = index < childCount
    override fun next() = getChildAt(index++) ?: throw IndexOutOfBoundsException()
    override fun remove() = removeViewAt(--index)
}


/** Returns a [Sequence] over the child views in this view group. */
val ViewGroup.children: Sequence<View>
    get() = object : Sequence<View> {
        override fun iterator() = this@children.iterator()
    }

fun ViewParent?.disableInterceptTouchForAncestors(disable: Boolean) {
    (this as? DisableInterceptViewGroup)?.disableIntercept(disable)
    this?.parent?.disableInterceptTouchForAncestors(disable)
}

/**
 * Problem : Click a card 2 times, it opens 2 detail screens
 * Solution : Disable view for X(say 300)millisec and enable it again.
 */
fun View.disableMomentarily(time: Long = Constants.DEFAULT_BUTTON_CLICK_DISABLE_TIME_MS) {
    if(!isEnabled) return // already disabled; leave it
    isEnabled = false
    postDelayed({isEnabled = true}, time)
}

/**
 * Increases touch area of the view by [value]
 * Problem: Hard to tap small buttons.
 * Solution: Increase touch area (does not affect the UI)
 * Reference: https://developer.android.com/training/gestures/viewgroup
 */
fun View?.increaseTouch(value: Int) {
    val parent = this?.parent ?: return
    (parent as? View)?.post {
        val rect = Rect()
        getHitRect(rect)
        rect.top -= value
        rect.left -= value
        rect.bottom += value
        rect.right += value
        parent.touchDelegate = TouchDelegate(rect, this);
    };
}


/**
 * For editing value in a map.
 *
 * [f] input - current value stored in the map; will be null if absent
 *
 * [f] output - will be [put] into the map; if null, entry will be removed.
 */
fun <K, V> MutableMap<K, V>.editValueAt(key: K, f: (V?) -> V?) {
    val v = f(get(key))
    if (v != null) put(key, v)
    else remove(key)
}

/**
 * Convenience method to enable named parameters and to use defaults.
 * Calls Observable.subscribe and adds resultant disposable to compositeDisposable arguement.
 * Callers should take care of calling compositeDisposable.dispose() when component is destroyed
 */
fun <T> Observable<T>.subscribeSafe(
        compositeDisposable: CompositeDisposable,
        onSubscribe: (Disposable) -> Unit = {},
        onNext: (T) -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {}) =
        subscribe(onNext, onError, onComplete, onSubscribe).also { compositeDisposable.add(it) }

fun String?.nullIfEmpty() = if (isNullOrEmpty()) null else this

fun String?.nullIfEmptyOrZero() = if (isNullOrEmpty() || Constants.ZERO_STRING == this) null else this

inline fun androidx.fragment.app.FragmentManager.transaction(f: androidx.fragment.app.FragmentTransaction.() -> Unit) {
    val t = beginTransaction()
    t.f()
    t.commit()
}

/**
 * takes a function [f], and calls it on all ViewHolders of type [T], with position as arg.
 */
inline fun <reified T> androidx.recyclerview.widget.RecyclerView.forEachViewHolder(f: T.(Int) -> Unit) {
    (0..(adapter?.itemCount?.dec() ?: 0)).forEach {
        (findViewHolderForAdapterPosition(it) as? T)?.f(it)
    }
}

// classes

/**
 * Is a type that wraps something that expires. Functor. Time defaults to current system time.
 *
 * @author satosh.dhanyamraju
 */
data class Expirable<T>(@SerializedName("b") val createdAt: Long, @SerializedName("c") val ttl: Long, @SerializedName("d") val value: T) {

    @JvmOverloads
    inline fun isExpired(curTime: Long = System.currentTimeMillis()) = curTime > (createdAt + ttl)

    inline fun <R> map(transform: (T) -> R) = Expirable(createdAt, ttl, transform(value))

    companion object {
        @JvmStatic
        @JvmOverloads
        fun <T> fromTTL(ttl: Long?, value: T, curTime: Long = System.currentTimeMillis()): Expirable<T> =
                Expirable(curTime, ttl ?: 0, value)
    }
}

/**
 * Try to parse it as url and return its path. If it fails, return as-is
 */
fun String?.toPathOrSelf(): String? {
    this ?: return null
    return try {
        URL(this).path
    } catch (e: Exception) {
        this
    }
}

fun String.md5Hash(): String {
    try {
        val md = MessageDigest.getInstance("md5")
        val md5Data = BigInteger(1, md.digest(this.toByteArray()))
        return String.format("%032X", md5Data)
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        return this
    }
}


fun Int?.isInRange(a: Int?, b: Int?): Boolean {
    a ?: return false
    b ?: return false
    this ?: return false
    return if (b > a) this in a..b else this in b..a
}

fun EditText?.disableCopyPaste() {
    this ?: return
    val emptyActionCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {

        }
    }
    customSelectionActionModeCallback = emptyActionCallback
    customInsertionActionModeCallback = emptyActionCallback
}

fun EditText.placeCursorAtEnd() {
    this.text?.let {
        setSelection(it.length)
    }
}

/**
 * Use this method when we need to enable scroll inside a text view when its parent is a scroll
 * view. Without this, textview/edittext does not scroll and the scroll view takes over scrolling.
 */
fun TextView?.enableTextViewScroll(parentScrollView: ScrollView) {
    val textView = this ?: return

    val scrollViewTouchListener = View.OnTouchListener { view, event ->
        textView.parent.requestDisallowInterceptTouchEvent(false)
        false
    }

    val textViewTouchListener = View.OnTouchListener { view, event ->
        textView.parent.requestDisallowInterceptTouchEvent(true)
        false
    }
    parentScrollView.setOnTouchListener(scrollViewTouchListener)
    textView.setOnTouchListener(textViewTouchListener)
}

object DHDelegates {

    /**
     * Takes an initial value and a preference. Every change in the property is written to
     * preferences in background thread.
     */
    inline fun <T> autoSaveToPref(initialValue: T, pref: SavedPreference):
            ReadWriteProperty<Any?, T> = object : ObservableProperty<T>(initialValue) {
        override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
            CommonUtils.runInBackground {
                Logger.d("DHDelegates", "autoSaveToPref: saving $newValue to ${pref.name}")
                PreferenceManager.savePreference(pref, CommonUtils.GSON.toJson(newValue))
            }
        }
    }
}

inline fun postToRestBus(f: () -> Any) {
    BusProvider.getRestBusInstance().post(f())
}

fun Bitmap.uri(context: Context): Uri? {
    val bytes = ByteArrayOutputStream()
    //TODO: @Rahul need to check on compression formats and quality. Should be configured from BE
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path: String? = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        this,
        "${System.currentTimeMillis()}",
        null
    )
    return if (path != null) Uri.parse(path) else null
}

inline fun <V : View> V.bottomSheetBH(callback: BottomSheetBehavior.BottomSheetCallback? = null,
                                      peekHeight: Int = 0, hide: Boolean = true): BottomSheetBehavior<V> {
    return BottomSheetBehavior.from(this).apply {
        if (callback != null) setBottomSheetCallback(callback)
        setPeekHeight(peekHeight)
        isHideable = hide
    }
}


// iterate two arrays in parallel
fun <X, Y> Pair<Iterable<X>, Iterable<Y>>.iterate(): Iterator<Pair<X?, Y?>> {
    val ia = first.iterator()
    val ib = second.iterator()

    return object : Iterator<Pair<X?, Y?>> {
        override fun hasNext(): Boolean = ia.hasNext() || ib.hasNext()

        override fun next(): Pair<X?, Y?> = Pair(if (ia.hasNext()) ia.next() else null, if (ib.hasNext()) ib.next() else null)
    }
}

fun <T> List<T>.toArrayList(): ArrayList<T> {
    val result = ArrayList<T>()
    result.addAll(this)
    return result
}

//explicit intent to DH. That shares single/multiple uri
fun Intent.getFromStream() : List<Uri> {
    if (this.hasExtra(Intent.EXTRA_STREAM)) {
        val uri = this.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri?
        uri ?: kotlin.run {
            return this.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.filterIsInstance(
                Uri::class.java
            ) ?: emptyList()
        }
        return listOf(uri)
    } else return emptyList()
}


fun Context.dimensionFromAttribute(attribute: Int): Int {
    val attributes = obtainStyledAttributes(intArrayOf(attribute))
    val dimension = attributes.getDimensionPixelSize(0, 0)
    attributes.recycle()
    return dimension
}

/**
 * Given [Lifecycle], observe [Bus]
 */
fun Bus.observeWhenCreated(lifecycleOwner: LifecycleOwner, observer: Any) {
    val LOG_TAG = "Bus.observeWhenCreated"
    lifecycleOwner.lifecycle.addObserver(object  : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            Logger.v(LOG_TAG, "onCreate: $this")
            runCatching {
                AndroidUtils.getMainThreadHandler().post {
                    register(observer)
                }
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            Logger.v(LOG_TAG, "onDestroy: $this")
            runCatching {
                AndroidUtils.getMainThreadHandler().post {
                    unregister(observer)
                }
            }
        }
    })
}


/*
*
* */
fun Array<ViewGroup>.scale(width: Int, height: Int) {
    this.forEach { v: ViewGroup -> v.layoutParams.let {
        it.width = width
        it.height = height
    } }
}

/**
 * Extension function to help retry an Observable with exponential delay.
 *
 * @param initialDelaySec: First retry will be done after this delay. Subsequent delays will be done with 2^retryCount delay
 * @param maxRetries: Maximum number of times to retry
 * @param onError: A chance for the caller to decide whether or not a particular exception needs retry.
 * If the method returns false, the retry chain will be broken. Default implementation retries always
 */
fun <T> Observable<T>.exponentialRetry(initialDelaySec: Long = Constants.DEFAULT_FIRST_RETRY_DELAY,
                                       maxRetries: Int = Constants.MAX_EXPONENTIAL_RETRIES,
                                       onError: ((Throwable?) -> Boolean)? = { true }): Observable<T> {
    return retryWhen { errorObs: Observable<Throwable> ->
        /**
         * Zip the error with range 0..maxRetries, raise the retry count to pow 2.
         */
        errorObs.zipWith(range(1, maxRetries + 1), BiFunction { error: Throwable, retryCount: Int ->
            if (retryCount > maxRetries) {
                throw error
            }
            if (onError?.invoke(error) != false) {
                retryCount
            } else {
                //The caller does not want to retry for this error, throw
                throw error
            }
        }).flatMap { retryCount: Int ->
            val delay = if (retryCount > 1) 2.toDouble().pow((retryCount - 1).toDouble()).toLong() else initialDelaySec
            timer(delay * 1000, TimeUnit.MILLISECONDS)
        }
    }
}

/**
 * Extension method to determine whether or not system is in dark mode.
 */
fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}

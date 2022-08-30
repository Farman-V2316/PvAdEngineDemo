/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.view.Display
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Dummy Context Implementation that wraps around another context which can be plugged and
 * unplugged as required.
 *
 * @author karthik.r
 */
class ContextHolder : Context() {

    companion object {
        val contextHolder: ContextHolder = ContextHolder()
    }

    var context: Context? = null

    fun clearContext(context: Context) {
        if (this.context == context) {
            this.context = null
        }
    }

    override fun getApplicationContext(): Context {
        return CommonUtils.getApplication()
    }

    override fun setWallpaper(bitmap: Bitmap?) {
        CommonUtils.getApplication().setWallpaper(bitmap)
    }

    override fun setWallpaper(data: InputStream?) {
        CommonUtils.getApplication().setWallpaper(data)
    }

    @SuppressLint("MissingPermission")
    override fun removeStickyBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        CommonUtils.getApplication().removeStickyBroadcastAsUser(intent, user)
    }

    override fun checkCallingOrSelfPermission(permission: String): Int {
        return CommonUtils.getApplication().checkCallingOrSelfPermission(permission)
    }

    override fun getClassLoader(): ClassLoader {
        return CommonUtils.getApplication().getClassLoader()
    }

    override fun checkCallingOrSelfUriPermission(uri: Uri?, modeFlags: Int): Int {
        return CommonUtils.getApplication().checkCallingOrSelfUriPermission(uri, modeFlags)
    }

    override fun getObbDir(): File {
        return CommonUtils.getApplication().obbDir
    }

    override fun checkUriPermission(uri: Uri?, pid: Int, uid: Int, modeFlags: Int): Int {
        return CommonUtils.getApplication().checkUriPermission(uri, pid, uid, modeFlags)
    }

    override fun checkUriPermission(uri: Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int): Int {
        return CommonUtils.getApplication().checkUriPermission(uri, readPermission, writePermission,
                pid, uid, modeFlags)
    }

    override fun getExternalFilesDirs(type: String?): Array<File> {
        return CommonUtils.getApplication().getExternalFilesDirs(type)
    }

    override fun getPackageResourcePath(): String {
        return CommonUtils.getApplication().packageResourcePath
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun deleteSharedPreferences(name: String?): Boolean {
        return CommonUtils.getApplication().deleteSharedPreferences(name)
    }

    override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
        return CommonUtils.getApplication().checkPermission(permission, pid, uid)
    }

    override fun startIntentSender(intent: IntentSender?, fillInIntent: Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int) {
        return CommonUtils.getApplication().startIntentSender(intent, fillInIntent, flagsMask,
                flagsValues, extraFlags)
    }

    override fun startIntentSender(intent: IntentSender?, fillInIntent: Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int, options: Bundle?) {
        return CommonUtils.getApplication().startIntentSender(intent, fillInIntent, flagsMask,
                flagsValues, extraFlags, options)
    }

    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return CommonUtils.getApplication().getSharedPreferences(name, mode)
    }

    @SuppressLint("MissingPermission")
    override fun sendStickyBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        CommonUtils.getApplication().sendStickyBroadcastAsUser(intent, user)
    }

    @SuppressLint("NewApi")
    override fun getDataDir(): File {
        return CommonUtils.getApplication().dataDir
    }

    override fun getWallpaper(): Drawable {
        return CommonUtils.getApplication().wallpaper
    }

    @SuppressLint("NewApi")
    override fun isDeviceProtectedStorage(): Boolean {
        return CommonUtils.getApplication().isDeviceProtectedStorage()
    }

    override fun getExternalFilesDir(type: String?): File? {
        return CommonUtils.getApplication().getExternalFilesDir(type)
    }

    @SuppressLint("MissingPermission")
    override fun sendBroadcastAsUser(intent: Intent?, user: UserHandle?) {
        CommonUtils.getApplication().sendBroadcastAsUser(intent, user)
    }

    @SuppressLint("MissingPermission")
    override fun sendBroadcastAsUser(intent: Intent?, user: UserHandle?, receiverPermission: String?) {
        CommonUtils.getApplication().sendBroadcastAsUser(intent, user, receiverPermission)
    }

    override fun getExternalCacheDir(): File? {
        return CommonUtils.getApplication().externalCacheDir
    }

    override fun getDatabasePath(name: String?): File {
        return CommonUtils.getApplication().getDatabasePath(name)
    }

    override fun getFileStreamPath(name: String?): File {
        return CommonUtils.getApplication().getFileStreamPath(name)
    }

    override fun stopService(service: Intent?): Boolean {
        return CommonUtils.getApplication().stopService(service)
    }

    @SuppressLint("NewApi")
    override fun checkSelfPermission(permission: String): Int {
        return CommonUtils.getApplication().checkSelfPermission(permission)
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
        return CommonUtils.getApplication().registerReceiver(receiver, filter)
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?, flags: Int): Intent? {
        return CommonUtils.getApplication().registerReceiver(receiver, filter, flags)
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?, broadcastPermission: String?, scheduler: Handler?): Intent? {
        return CommonUtils.getApplication().registerReceiver(receiver, filter, broadcastPermission, scheduler)
    }

    @TargetApi(Build.VERSION_CODES.O)
    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?, broadcastPermission: String?, scheduler: Handler?, flags: Int): Intent? {
        return CommonUtils.getApplication().registerReceiver(receiver, filter, broadcastPermission,
                scheduler, flags)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun getSystemServiceName(serviceClass: Class<*>): String? {
        return CommonUtils.getApplication().getSystemServiceName(serviceClass)
    }

    override fun getMainLooper(): Looper {
        return CommonUtils.getApplication().mainLooper
    }

    override fun enforceCallingOrSelfPermission(permission: String, message: String?) {
        CommonUtils.getApplication().enforceCallingOrSelfPermission(permission, message)
    }

    override fun getPackageCodePath(): String {
        return CommonUtils.getApplication().packageCodePath
    }

    override fun checkCallingUriPermission(uri: Uri?, modeFlags: Int): Int {
        return CommonUtils.getApplication().checkCallingUriPermission(uri, modeFlags)
    }

    override fun getWallpaperDesiredMinimumWidth(): Int {
        return CommonUtils.getApplication().wallpaperDesiredMinimumWidth
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun createDeviceProtectedStorageContext(): Context {
        return CommonUtils.getApplication().createDeviceProtectedStorageContext()
    }

    override fun openFileInput(name: String?): FileInputStream {
        return CommonUtils.getApplication().openFileInput(name)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getCodeCacheDir(): File {
        return CommonUtils.getApplication().codeCacheDir
    }

    override fun bindService(service: Intent?, conn: ServiceConnection, flags: Int): Boolean {
        return CommonUtils.getApplication().bindService(service, conn, flags)
    }

    override fun deleteDatabase(name: String?): Boolean {
        return CommonUtils.getApplication().deleteDatabase(name)
    }

    override fun getAssets(): AssetManager {
        return CommonUtils.getApplication().assets
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getNoBackupFilesDir(): File {
        return CommonUtils.getApplication().noBackupFilesDir
    }

    override fun startActivities(intents: Array<out Intent>?) {
        context?.startActivities(intents) ?: CommonUtils.getApplication().startActivities(intents)
    }

    override fun startActivities(intents: Array<out Intent>?, options: Bundle?) {
        context?.startActivities(intents, options)
                ?: CommonUtils.getApplication().startActivities(intents, options)
    }

    override fun getResources(): Resources {
        return context?.resources ?: CommonUtils.getApplication().resources
    }

    override fun fileList(): Array<String> {
        return CommonUtils.getApplication().fileList()
    }

    override fun setTheme(resid: Int) {
        context?.setTheme(resid)
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver?) {
        CommonUtils.getApplication().unregisterReceiver(receiver)
    }

    override fun enforcePermission(permission: String, pid: Int, uid: Int, message: String?) {
        CommonUtils.getApplication().enforcePermission(permission, pid, uid, message)
    }

    override fun openFileOutput(name: String?, mode: Int): FileOutputStream {
        return CommonUtils.getApplication().openFileOutput(name, mode)
    }

    @SuppressLint("MissingPermission")
    override fun sendStickyOrderedBroadcast(intent: Intent?, resultReceiver: BroadcastReceiver?, scheduler: Handler?, initialCode: Int, initialData: String?, initialExtras: Bundle?) {
        CommonUtils.getApplication().sendStickyOrderedBroadcast(intent, resultReceiver,
                scheduler, initialCode, initialData, initialExtras)
    }

    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        return context?.createConfigurationContext(overrideConfiguration) ?: CommonUtils.getApplication()
                .createConfigurationContext(overrideConfiguration)
    }

    override fun getFilesDir(): File {
        return CommonUtils.getApplication().filesDir
    }

    override fun sendBroadcast(intent: Intent?) {
        CommonUtils.getApplication().sendBroadcast(intent)
    }

    override fun sendBroadcast(intent: Intent?, receiverPermission: String?) {
        CommonUtils.getApplication().sendBroadcast(intent, receiverPermission)
    }

    @SuppressLint("MissingPermission")
    override fun sendOrderedBroadcastAsUser(intent: Intent?, user: UserHandle?, receiverPermission: String?, resultReceiver: BroadcastReceiver?, scheduler: Handler?, initialCode: Int, initialData: String?, initialExtras: Bundle?) {
        CommonUtils.getApplication().sendOrderedBroadcastAsUser(intent, user, receiverPermission,
                resultReceiver, scheduler, initialCode, initialData, initialExtras)
    }

    override fun grantUriPermission(toPackage: String?, uri: Uri?, modeFlags: Int) {
        CommonUtils.getApplication().grantUriPermission(toPackage, uri, modeFlags)
    }

    override fun enforceCallingUriPermission(uri: Uri?, modeFlags: Int, message: String?) {
        CommonUtils.getApplication().enforceCallingUriPermission(uri, modeFlags, message)
    }

    override fun getCacheDir(): File {
        return CommonUtils.getApplication().cacheDir
    }

    override fun clearWallpaper() {
        CommonUtils.getApplication().clearWallpaper()
    }

    @SuppressLint("MissingPermission")
    override fun sendStickyOrderedBroadcastAsUser(intent: Intent?, user: UserHandle?, resultReceiver: BroadcastReceiver?, scheduler: Handler?, initialCode: Int, initialData: String?, initialExtras: Bundle?) {
        CommonUtils.getApplication().sendStickyOrderedBroadcastAsUser(intent, user, resultReceiver,
                scheduler, initialCode, initialData, initialExtras)
    }

    override fun startActivity(intent: Intent?) {
        return context?.startActivity(intent) ?: CommonUtils.getApplication().startActivity(intent)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        return context?.startActivity(intent, options)
                ?: CommonUtils.getApplication().startActivity(intent, options)
    }

    override fun getPackageManager(): PackageManager {
        return CommonUtils.getApplication().packageManager
    }

    override fun openOrCreateDatabase(name: String?, mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase {
        return CommonUtils.getApplication().openOrCreateDatabase(name, mode, factory)
    }

    override fun openOrCreateDatabase(name: String?, mode: Int, factory: SQLiteDatabase.CursorFactory?, errorHandler: DatabaseErrorHandler?): SQLiteDatabase {
        return CommonUtils.getApplication().openOrCreateDatabase(name, mode, factory, errorHandler)
    }

    override fun deleteFile(name: String?): Boolean {
        return CommonUtils.getApplication().deleteFile(name)
    }

    override fun startService(service: Intent?): ComponentName? {
        return CommonUtils.getApplication().startService(service)
    }

    override fun revokeUriPermission(uri: Uri?, modeFlags: Int) {
        return CommonUtils.getApplication().revokeUriPermission(uri, modeFlags)
    }

    @SuppressLint("NewApi")
    override fun revokeUriPermission(toPackage: String?, uri: Uri?, modeFlags: Int) {
        CommonUtils.getApplication().revokeUriPermission(toPackage, uri, modeFlags)
    }

    @SuppressLint("NewApi")
    override fun moveDatabaseFrom(sourceContext: Context?, name: String?): Boolean {
        return CommonUtils.getApplication().moveDatabaseFrom(sourceContext, name)
    }

    override fun startInstrumentation(className: ComponentName, profileFile: String?, arguments: Bundle?): Boolean {
        return CommonUtils.getApplication().startInstrumentation(className, profileFile, arguments)
    }

    override fun sendOrderedBroadcast(intent: Intent?, receiverPermission: String?) {
        return CommonUtils.getApplication().sendOrderedBroadcast(intent, receiverPermission)
    }

    override fun sendOrderedBroadcast(intent: Intent, receiverPermission: String?, resultReceiver: BroadcastReceiver?, scheduler: Handler?, initialCode: Int, initialData: String?, initialExtras: Bundle?) {
        CommonUtils.getApplication().sendOrderedBroadcast(intent, receiverPermission, resultReceiver,
                scheduler, initialCode, initialData, initialExtras)
    }

    override fun unbindService(conn: ServiceConnection) {
        CommonUtils.getApplication().unbindService(conn)
    }

    override fun getApplicationInfo(): ApplicationInfo {
        return CommonUtils.getApplication().applicationInfo
    }

    override fun getWallpaperDesiredMinimumHeight(): Int {
        return CommonUtils.getApplication().wallpaperDesiredMinimumHeight
    }

    override fun createDisplayContext(display: Display): Context {
        return context?.createDisplayContext(display)
                ?: CommonUtils.getApplication().createDisplayContext(display)
    }

    @SuppressLint("NewApi")
    override fun createContextForSplit(splitName: String?): Context {
        return context?.createContextForSplit(splitName)
                ?: CommonUtils.getApplication().createContextForSplit(splitName)
    }

    override fun getTheme(): Resources.Theme {
        return context?.theme ?: CommonUtils.getApplication().theme
    }

    override fun getPackageName(): String {
        return CommonUtils.getApplication().packageName
    }

    override fun getContentResolver(): ContentResolver {
        return CommonUtils.getApplication().contentResolver
    }

    override fun getObbDirs(): Array<File> {
        return CommonUtils.getApplication().obbDirs
    }

    override fun enforceCallingOrSelfUriPermission(uri: Uri?, modeFlags: Int, message: String?) {
        CommonUtils.getApplication().enforceCallingOrSelfUriPermission(uri, modeFlags, message)
    }

    @SuppressLint("NewApi")
    override fun moveSharedPreferencesFrom(sourceContext: Context?, name: String?): Boolean {
        return CommonUtils.getApplication().moveSharedPreferencesFrom(sourceContext, name)
    }

    @SuppressLint("NewApi")
    override fun getExternalMediaDirs(): Array<File> {
        return CommonUtils.getApplication().externalMediaDirs
    }

    override fun checkCallingPermission(permission: String): Int {
        return CommonUtils.getApplication().checkCallingPermission(permission)
    }

    override fun getExternalCacheDirs(): Array<File> {
        return CommonUtils.getApplication().externalCacheDirs
    }

    @SuppressLint("MissingPermission")
    override fun sendStickyBroadcast(intent: Intent?) {
        CommonUtils.getApplication().sendStickyBroadcast(intent)
    }

    override fun enforceCallingPermission(permission: String, message: String?) {
        CommonUtils.getApplication().enforceCallingPermission(permission, message)
    }

    override fun peekWallpaper(): Drawable {
        return CommonUtils.getApplication().peekWallpaper()
    }

    override fun getSystemService(name: String): Any {
        return CommonUtils.getApplication().getSystemService(name)
    }

    @SuppressLint("NewApi")
    override fun startForegroundService(service: Intent?): ComponentName? {
        return CommonUtils.getApplication().startForegroundService(service)
    }

    override fun getDir(name: String?, mode: Int): File {
        return CommonUtils.getApplication().getDir(name, mode)
    }

    override fun databaseList(): Array<String> {
        return CommonUtils.getApplication().databaseList()
    }

    override fun createPackageContext(packageName: String?, flags: Int): Context {
        return CommonUtils.getApplication().createPackageContext(packageName, flags)
    }

    override fun enforceUriPermission(uri: Uri?, pid: Int, uid: Int, modeFlags: Int, message: String?) {
        CommonUtils.getApplication().enforceUriPermission(uri, pid, uid, modeFlags, message)
    }

    override fun enforceUriPermission(uri: Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int, message: String?) {
        CommonUtils.getApplication().enforceUriPermission(uri, readPermission, writePermission, pid,
                uid, modeFlags, message)
    }

    @SuppressLint("MissingPermission")
    override fun removeStickyBroadcast(intent: Intent?) {
        CommonUtils.getApplication().removeStickyBroadcast(intent)
    }

}
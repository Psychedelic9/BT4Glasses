package com.psychedelic9.bt4glasses.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * 应用程序 Activity 管理类：用于 Activity 管理和应用程序退出
 */
@Suppress("unused")
object AppManager {

    /** Application 对象 */
    private var mApplication: Application? = null

    /** 保存 Activity 对象的堆栈 */
    private val activityStack: Stack<WeakReference<Activity>> = Stack()

    /** 忽略列表 */
    private val ignoreActivitys = arrayListOf<Class<out Activity>>()

    /** 前台界面个数 */
    private var foregroundCount = 0

    /** 当前焦点 Activity */
    private var focusActivity: WeakReference<Activity>? = null

    private var foucsDialog: WeakReference<DialogFragment>? = null

    /** Activity 生命周期回调接口*/
    private val mActivityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            onCreate(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity?) {
        }

        override fun onActivityResumed(activity: Activity?) {
            foregroundCount++
            if (activity != null) {
                focusActivity = WeakReference(activity)
            }
        }

        override fun onActivityPaused(activity: Activity?) {
            foregroundCount--
            if (focusActivity?.get() == activity) {
                focusActivity = null
            }
        }

        override fun onActivityStopped(activity: Activity?) {
        }

        override fun onActivityDestroyed(activity: Activity?) {
            onDestroy(activity)
        }
    }

    /** Dialog 生命周期回调接口 */
    val mDialogLifecycleCallbacks = object : DialogLifecycleCallbacks {

        override fun onResume(dialog: DialogFragment) {
            foucsDialog = WeakReference(dialog)
        }

        override fun onPause(dialog: DialogFragment) {
            if (foucsDialog?.get() == dialog) {
                foucsDialog = null
            }
        }
    }

    /**
     * 获取当前焦点 Activity
     *
     * @return 当前焦点 Activity，没有返回 null
     */
    fun getFocusActivity(): Activity? {
        return focusActivity?.get()
    }



    /**
     * 应用是否在前台
     *
     * @return 应用是否在前台
     */
    fun isForeground(): Boolean {
        return foregroundCount > 0
    }

    /**
     * 注册 Application
     *
     * @param application 应用 [Application] 对象
     */
    fun register(application: Application) {
        mApplication = application
        application.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
        application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    /**
     * 获取可用 Application 对象
     */
    fun getApplication(): Application {
        if (mApplication == null) {
            register(getApplicationByReflect())
            if (mApplication == null) {
                throw NullPointerException("Application must not be null! Please register AppManager in your Application start！")
            }
        }
        return mApplication!!
    }

    /**
     * 通过反射获取当前 Application 对象
     *
     * @return 当前 Application 对象
     */
    private fun getApplicationByReflect(): Application {
        try {
            @SuppressLint("PrivateApi")
            val activityThread = Class.forName("android.app.ActivityThread")
            val thread = activityThread.getMethod("currentActivityThread").invoke(null)
            val app = activityThread.getMethod("getApplication").invoke(thread)
                ?: throw NullPointerException("u should init first")
            return app as Application
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        throw NullPointerException("u should init first")
    }

    /**
     * 将 Activity 类对象添加到忽略列表
     */
    fun addToIgnore(vararg clazzs: Class<out Activity>) {
        ignoreActivitys.addAll(clazzs)
    }

    /**
     * 添加 Activity 到堆栈
     *
     * @param activity Activity 对象
     */
    fun onCreate(activity: Activity?) {
        if (activity != null && ignoreActivitys.contains(activity.javaClass)) {
            // 不添加在忽略列表中的 Activity
            return
        }
        add(activity)
        Log.d("Common_AppManager", "add---->>$activity size---->>${activityStack.size}")
    }

    /**
     * 将 Activity 从堆栈移除
     *
     * @param activity Activity 对象
     */
    fun onDestroy(activity: Activity?) {
        remove(activity)
        Log.d("Common_AppManager", " remove---->>$activity size---->>${activityStack.size}")
    }

    fun contains(clazz: Class<out Activity>): Boolean {
        return activityStack.count { it.get()?.javaClass == clazz } > 0
    }

    /**
     * 将 Activity 加入堆栈
     */
    fun add(activity: Activity?) {
        if (activity == null) {
            return
        }
        activityStack.add(WeakReference(activity))
    }

    /**
     * 将 Activity 从堆栈移除
     *
     * @param activity Activity 对象
     */
    fun remove(activity: Activity?) {
        if (activity == null) {
            return
        }
        var remove: WeakReference<Activity>? = null
        activityStack.forEach { weak ->
            if (weak.get() == activity) {
                remove = weak
            }
        }
        activityStack.remove(remove)
    }

    /**
     * 结束指定 Activity 之外的其他 Activity
     *
     * @param activity 指定不关闭的 Activity
     */
    fun finishAllWithout(activity: Activity?) {
        if (activity == null) {
            return
        }
        remove(activity)
        finishAllActivity()
        add(activity)
    }

    /**
     * 结束指定 Activity
     *
     * @param clazz Activity 类对象
     */
    fun finishActivity(clazz: Class<out Activity>) {
        val del: Activity? = activityStack.lastOrNull { it.get()?.javaClass == clazz }?.get()
        del?.finish()
    }

    /**
     * 结束指定 Activity
     *
     * @param clazzs Activity 类对象
     */
    fun finishActivities(vararg clazzs: Class<out Activity>) {
        for (clazz in clazzs) {
            finishActivity(clazz)
        }
    }

    /**
     * 获取栈顶的 Activity
     *
     * @return 栈顶的 Activity 对象
     */
    fun peekActivity(): Activity? {
        return if (activityStack.isEmpty()) {
            null
        } else {
            activityStack.peek().get()
        }
    }

    /**
     * 根据类，获取 Activity 对象
     *
     * @param clazz  Activity 类
     * @param A      Activity 类型
     *
     * @return       Activity对象
     */
    fun <A : Activity> getActivity(clazz: Class<A>): A? {
        @Suppress("UNCHECKED_CAST")
        return activityStack.firstOrNull { it.get()?.javaClass == clazz }?.get() as A?
    }

    /**
     * 结束所有 Activity
     */
    private fun finishAllActivity() {
        for (weak in activityStack) {
            weak.get()?.finish()
        }
        activityStack.clear()
        Log.d("Common_AppManager", "Finish All Activity!")
    }

    /**
     * 退出应用程序
     */
    fun appExit() {
        try {
            val activityMgr = getApplication().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityMgr.killBackgroundProcesses(getApplication().packageName)
            finishAllActivity()
            Log.d("Common_AppManager", " Application Exit!")
            System.exit(0)
        } catch (e: Exception) {
            Log.e("Common_AppManager", "APP_EXIT", e)
        }
    }

    interface DialogLifecycleCallbacks {

        fun onResume(dialog: DialogFragment)

        fun onPause(dialog: DialogFragment)
    }
}

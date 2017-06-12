package com.allen.kotlinapp

import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.*
import android.os.Bundle
import android.os.Process
import android.os.RemoteException
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

open class MainActivity : AppCompatActivity() {

    private var mRecyclerView: RecyclerView?=null
    private val mlistAppInfo = ArrayList<AppInfo>()
    private var mCopylistAppInfo: ArrayList<AppInfo>? = null
    private var adapter: AppManagerAdapter?=null
    private val signNumber: String? = null
    private var datasize: Float = 0.toFloat()
    private var cachesize: Float = 0.toFloat()
    private var codesize: Float = 0.toFloat()
    private var totalsize: Float = 0.toFloat()
    private var progressBar: ProgressBar? = null
    private var mEtRxJava: EditText? = null
    private val INTERVAL: Long = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        mCopylistAppInfo = ArrayList(mlistAppInfo)
        adapter = AppManagerAdapter(mCopylistAppInfo!!)

        adapter?.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT)
        mRecyclerView?.setHasFixedSize(true)
        mRecyclerView?.setLayoutManager(LinearLayoutManager(this))
        mRecyclerView?.setItemAnimator(DefaultItemAnimator())
        mRecyclerView?.addItemDecoration(DividerGridItemDecoration(this@MainActivity, R.drawable.divider))
        mRecyclerView?.setAdapter(adapter)


        val observable = Observable.create(ObservableOnSubscribe<List<AppInfo>> { appInfo ->
            progressBar?.setVisibility(View.VISIBLE)
            appInfo.onNext(queryAppInfo())
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        val observer = object : Observer<List<AppInfo>> {
            override fun onSubscribe(d: Disposable) {

            }

            override //观察者接收到通知,进行相关操作
            fun onNext(aLong: List<AppInfo>) {
                mCopylistAppInfo?.clear()
                mCopylistAppInfo?.addAll(aLong)
                progressBar?.setVisibility(View.GONE)
                adapter?.notifyDataSetChanged()
                adapter?.isFirstOnly(true)
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        }
        observable.subscribe(observer)
        adapter?.setOnItemChildClickListener(BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.btCopy) {
                onClick(mCopylistAppInfo?.get(position)!!)
            }
        })
        RxTextView.textChangeEvents(mEtRxJava!!)
                .debounce(INTERVAL, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<TextViewTextChangeEvent> {


                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {

                    }

                    override fun onSubscribe(disposable: Disposable) {

                    }

                    override fun onNext(textViewTextChangeEvent: TextViewTextChangeEvent) {
                        rxJavaSearch(textViewTextChangeEvent.text().toString())
                    }
                })
    }

    private fun rxJavaSearch(s: String) {
        val searchInfo = ArrayList<AppInfo>()
        if (!TextUtils.isEmpty(s)) {
            for (appInfo in mlistAppInfo) {
                if (appInfo.pkgName.toString().contains(s) || appInfo.appLabel.toString().contains(s)) {
                    searchInfo.add(appInfo)
                }
            }
            if (searchInfo.size > 0) {
                mCopylistAppInfo?.clear()
                mCopylistAppInfo?.addAll(searchInfo)
                adapter?.notifyDataSetChanged()
            } else {
                mCopylistAppInfo?.clear()
                mCopylistAppInfo?.addAll(mlistAppInfo)
                adapter?.notifyDataSetChanged()
            }
        } else {
            mCopylistAppInfo?.clear()
            mCopylistAppInfo?.addAll(mlistAppInfo)
            adapter?.notifyDataSetChanged()
        }
    }


    // 获得所有启动Activity的信息，类似于Launch界面
    fun queryAppInfo(): List<AppInfo>? {
        val pm = this.packageManager // 获得PackageManager对象
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        // 通过查询，获得所有ResolveInfo对象.
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        // 调用系统排序 ， 根据name排序
        // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
        Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(pm))
        if (mlistAppInfo != null) {
            mlistAppInfo.clear()
            for (reInfo in resolveInfos) {
                val activityName = reInfo.activityInfo.name // 获得该应用程序的启动Activity的name
                val pkgName = reInfo.activityInfo.packageName // 获得应用程序的包名

                val appLabel = reInfo.loadLabel(pm) as String // 获得应用程序的Label
                val icon = reInfo.loadIcon(pm) // 获得应用程序图标
                // 为应用程序的启动Activity 准备Intent
                val launchIntent = Intent()
                launchIntent.component = ComponentName(pkgName, activityName)
                // 创建一个AppInfo对象，并赋值
                val appInfo = AppInfo()
                try {
                    queryPacakgeSize(pkgName, appInfo)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                appInfo.appLabel=(appLabel)
                appInfo.pkgName=(pkgName)
                appInfo.appIcon=(icon)
                appInfo.mVersion=(getVersionName(pkgName))
                appInfo.sigmd5=(getSignMd5Str(pkgName))
                //                appInfo.setIntent(launchIntent);
                mlistAppInfo.add(appInfo) // 添加至列表中
            }
            return mlistAppInfo
        }
        return null
    }

    fun onClick(appinfo: AppInfo) {
        val var2 = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var var3 = StringBuilder("应用程序：")
        var var4 = appinfo.appLabel
        var3 = var3.append(var4).append("\n").append("包名：")
        var4 = appinfo.pkgName
        var3 = var3.append(var4).append("\n").append("签名：")
        var4 = appinfo.sigmd5
        val var5 = var3.append(var4).toString()
        var2.text = var5
        Toast.makeText(this@MainActivity, "应用信息复制成功", Toast.LENGTH_SHORT).show()
    }

    private fun initView() {
        mRecyclerView = findViewById(R.id.recyclerView) as RecyclerView
        progressBar = findViewById(R.id.loading_progress) as ProgressBar
        mEtRxJava = findViewById(R.id.mEtRxJava) as EditText
    }

    private fun getVersionName(packageName: String): String {
        // 获取packagemanager的实例
        val packageManager = packageManager
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        var packInfo: PackageInfo? = null
        try {
            packInfo = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val version = packInfo!!.versionName + "  Code: " + packInfo.versionCode
        return version ?: "未获取到系统版本号"
    }

    /**
     * MD5加密

     * @param byteStr 需要加密的内容
     * *
     * @return 返回 byteStr的md5值
     */
    fun encryptionMD5(byteStr: ByteArray): String {
        var messageDigest: MessageDigest? = null
        val md5StrBuff = StringBuffer()
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest!!.reset()
            messageDigest.update(byteStr)
            val byteArray = messageDigest.digest()
            for (i in byteArray.indices) {
                if (Integer.toHexString(0xFF and byteArray[i].toInt()).length == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF and byteArray[i].toInt()))
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF and byteArray[i].toInt()))
                }
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return md5StrBuff.toString()
    }

    /**
     * 获取app签名md5值
     */
    fun getSignMd5Str(packageName: String): String {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            val signs = packageInfo.signatures
            val sign = signs[0]
            val signStr = encryptionMD5(sign.toByteArray())
            return signStr
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return ""
    }

    @Throws(Exception::class)
    fun queryPacakgeSize(pkgName: String?, appInfo: AppInfo) {
        if (pkgName != null) {
            //使用放射机制得到PackageManager类的隐藏函数getPackageSizeInfo
            val pm = packageManager  //得到pm对象
            try {
                //通过反射机制获得该隐藏函数
                //                Method getPackageSizeInfo = pm.getClass().getDeclaredMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                //                //调用该函数，并且给其分配参数 ，待调用流程完成后会回调PkgSizeObserver类的函数
                //                getPackageSizeInfo.invoke(pm, pkgName, new PkgSizeObserver(appInfo));
                val getPackageSizeInfo = pm.javaClass.getDeclaredMethod("getPackageSizeInfo", String::class.java,
                        Int::class.javaPrimitiveType, IPackageStatsObserver::class.java)
                /**
                 * after invoking, PkgSizeObserver.onGetStatsCompleted() will be called as callback function. <br></br>
                 * About the third parameter ‘Process.myUid() / 100000’，please check:
                 * <android_source>/frameworks/base/core/java/android/content/pm/PackageManager.java:
                 * getPackageSizeInfo(packageName, UserHandle.myUserId(), observer);
                </android_source> */
                getPackageSizeInfo.invoke(pm, pkgName, Process.myUid() / 100000, PkgSizeObserver(appInfo))
            } catch (ex: Exception) {
                ex.printStackTrace()
                throw ex  // 抛出异常
            }

        }
    }

    //    //aidl文件形成的Bindler机制服务类
    inner class PkgSizeObserver(private val appInfo: AppInfo) : IPackageStatsObserver.Stub() {

        /*** 回调函数，
         * @param pStats ,返回数据封装在PackageStats对象中
         * *
         * @param succeeded  代表回调成功
         */
        @Throws(RemoteException::class)
        override fun onGetStatsCompleted(pStats: PackageStats, succeeded: Boolean) {
            // TODO Auto-generated method stub
            cachesize = pStats.cacheSize.toFloat() //缓存大小
            datasize = pStats.dataSize.toFloat()  //数据大小
            codesize = pStats.codeSize.toFloat()  //应用程序大小
            totalsize = cachesize + datasize + codesize
            val format = DecimalFormat("#0.00")

            val v = totalsize / 1024f / 1024f
            appInfo.totalSize=("apk总size " + format.format(v.toDouble()) + "M")
            Log.i("PkgSizeObserver", "cachesize--->$cachesize datasize---->$datasize codeSize---->$codesize")
        }
    }

    //系统函数，字符串转换 long -String (kb)
    private fun formateFileSize(size: Long): String {
        return Formatter.formatFileSize(this@MainActivity, size)
    }


}


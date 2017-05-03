/*
 *
 *  * Copyright  2017 [AllenCoderr]
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.allen.appmanager;

import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<AppInfo> mlistAppInfo = new ArrayList<>();
    private List<AppInfo> mCopylistAppInfo ;
    private AppManagerAdapter adapter;
    private String signNumber;
    private float datasize;
    private float cachesize;
    private float codesize;
    private float totalsize;
    private ProgressBar progressBar;
    private EditText mEtRxJava;
    private long INTERVAL=1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mCopylistAppInfo= new ArrayList<>(mlistAppInfo);
        adapter = new AppManagerAdapter(mCopylistAppInfo);

        adapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(MainActivity.this,R.drawable.divider));
        mRecyclerView.setAdapter(adapter);


        Observable<List<AppInfo>> observable = Observable.create(new ObservableOnSubscribe<List<AppInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<AppInfo>> appInfo) throws Exception {
                progressBar.setVisibility(View.VISIBLE);
                appInfo.onNext(queryAppInfo());
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        Observer<List<AppInfo>> observer = new Observer<List<AppInfo>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            //观察者接收到通知,进行相关操作
            public void onNext(List<AppInfo> aLong) {
                mCopylistAppInfo.clear();
                mCopylistAppInfo.addAll(aLong);
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                adapter.isFirstOnly(true);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        observable.subscribe(observer);
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.btCopy) {
                    onClick(mCopylistAppInfo.get(position));
                }
            }
        });
        RxTextView.textChangeEvents(mEtRxJava)
                .debounce(INTERVAL, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TextViewTextChangeEvent>() {


                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable disposable) {

                    }

                    @Override
                    public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
                        rxJavaSearch(textViewTextChangeEvent.text().toString());
                    }
                });
    }

    private void rxJavaSearch(String s) {
        List<AppInfo> searchInfo = new ArrayList<>();
        if (!TextUtils.isEmpty(s)){
            for (AppInfo appInfo: mlistAppInfo) {
                if (appInfo.getPkgName().contains(s)||appInfo.getAppLabel().contains(s)){
                    searchInfo.add(appInfo);
                }
            }
            if (searchInfo.size()>0){
                mCopylistAppInfo.clear();
                mCopylistAppInfo.addAll(searchInfo);
                adapter.notifyDataSetChanged();
            }else {
                mCopylistAppInfo.clear();
                mCopylistAppInfo.addAll(mlistAppInfo);
                adapter.notifyDataSetChanged();
            }
        }else {
            mCopylistAppInfo.clear();
            mCopylistAppInfo.addAll(mlistAppInfo);
            adapter.notifyDataSetChanged();
        }
    }


    // 获得所有启动Activity的信息，类似于Launch界面
    public List<AppInfo> queryAppInfo() {
        PackageManager pm = this.getPackageManager(); // 获得PackageManager对象
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        // 调用系统排序 ， 根据name排序
        // 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
        if (mlistAppInfo != null) {
            mlistAppInfo.clear();
            for (ResolveInfo reInfo : resolveInfos) {
                String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
                String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名

                String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
                Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
                // 为应用程序的启动Activity 准备Intent
                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(pkgName, activityName));
                // 创建一个AppInfo对象，并赋值
                AppInfo appInfo = new AppInfo();
                try {
                    queryPacakgeSize(pkgName,appInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                appInfo.setAppLabel(appLabel);
                appInfo.setPkgName(pkgName);
                appInfo.setAppIcon(icon);
                appInfo.setmVersion(getVersionName(pkgName));
                appInfo.setSigmd5(getSignMd5Str(pkgName));
//                appInfo.setIntent(launchIntent);
                 mlistAppInfo.add(appInfo); // 添加至列表中
            }
            return mlistAppInfo;
        }
        return null;
    }

    public void onClick(AppInfo appinfo) {
        ClipboardManager var2 = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
        StringBuilder var3 = new StringBuilder("应用程序：");
        String var4 = appinfo.getAppLabel();
        var3 = var3.append(var4).append("\n").append("包名：");
        var4 = appinfo.getPkgName();
        var3 = var3.append(var4).append("\n").append("签名：");
        var4 = appinfo.getSigmd5();
        String var5 = var3.append(var4).toString();
        var2.setText(var5);
        Toast.makeText(MainActivity.this, "应用信息复制成功", Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        progressBar = (ProgressBar) findViewById(R.id.loading_progress);
        mEtRxJava = (EditText) findViewById(R.id.mEtRxJava);
    }

    private String getVersionName(String packageName) {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = packInfo.versionName + "  Code: " + packInfo.versionCode;
        return version != null ? version : "未获取到系统版本号";
    }

    /**
     * MD5加密
     *
     * @param byteStr 需要加密的内容
     * @return 返回 byteStr的md5值
     */
    public static String encryptionMD5(byte[] byteStr) {
        MessageDigest messageDigest = null;
        StringBuffer md5StrBuff = new StringBuffer();
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(byteStr);
            byte[] byteArray = messageDigest.digest();
            for (int i = 0; i < byteArray.length; i++) {
                if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                    md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
                } else {
                    md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5StrBuff.toString();
    }

    /**
     * 获取app签名md5值
     */
    public String getSignMd5Str(String packageName) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            String signStr = encryptionMD5(sign.toByteArray());
            return signStr;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void queryPacakgeSize(String pkgName,AppInfo appInfo) throws Exception {
        if (pkgName != null) {
            //使用放射机制得到PackageManager类的隐藏函数getPackageSizeInfo
            PackageManager pm = getPackageManager();  //得到pm对象
            try {
                //通过反射机制获得该隐藏函数
//                Method getPackageSizeInfo = pm.getClass().getDeclaredMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
//                //调用该函数，并且给其分配参数 ，待调用流程完成后会回调PkgSizeObserver类的函数
//                getPackageSizeInfo.invoke(pm, pkgName, new PkgSizeObserver(appInfo));
                Method getPackageSizeInfo = pm.getClass().getDeclaredMethod("getPackageSizeInfo", String.class,
                        int.class, IPackageStatsObserver.class);
                /**
                 * after invoking, PkgSizeObserver.onGetStatsCompleted() will be called as callback function. <br>
                 * About the third parameter ‘Process.myUid() / 100000’，please check:
                 * <android_source>/frameworks/base/core/java/android/content/pm/PackageManager.java:
                 * getPackageSizeInfo(packageName, UserHandle.myUserId(), observer);
                 */
                getPackageSizeInfo.invoke(pm, pkgName, Process.myUid() / 100000, new PkgSizeObserver(appInfo));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;  // 抛出异常
            }
        }
    }
    //    //aidl文件形成的Bindler机制服务类
    public class PkgSizeObserver extends IPackageStatsObserver.Stub {
        private AppInfo appInfo;

        public PkgSizeObserver(AppInfo appInfo) {
                this.appInfo =appInfo;
        }

        /*** 回调函数，
         * @param pStats ,返回数据封装在PackageStats对象中
         * @param succeeded  代表回调成功
         */
        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                throws RemoteException {
            // TODO Auto-generated method stub
            cachesize = pStats.cacheSize; //缓存大小
            datasize = pStats.dataSize;  //数据大小
            codesize = pStats.codeSize;  //应用程序大小
            totalsize = cachesize + datasize + codesize;
            DecimalFormat format = new DecimalFormat("#0.00");

            final float v = totalsize / 1024 / 1024;
            appInfo.setTotalSize("apk总size "+format.format(v) +"M");
            Log.i("PkgSizeObserver", "cachesize--->"+cachesize+" datasize---->"+datasize+ " codeSize---->"+codesize)  ;
        }
    }

    //系统函数，字符串转换 long -String (kb)
    private String formateFileSize(long size) {
        return Formatter.formatFileSize(MainActivity.this, size);
    }


}


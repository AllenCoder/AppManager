package com.allen.kotlinapp

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * 文 件 名: AppManagerAdapter
 * 创 建 人: Allen
 * 创建日期: 2017/6/12 16:57
 * 修改时间：
 * 修改备注：
 */

class AppManagerAdapter : BaseQuickAdapter<AppInfo, AppManagerAdapter.ViewHolder> {
    constructor(layoutResId: Int, data: List<AppInfo>) : super(R.layout.app_item, data) {}

    constructor(data: List<AppInfo>) : super(R.layout.app_item, data) {}


    override fun convert(helper: ViewHolder, item: AppInfo) {
        helper.addOnClickListener(R.id.btCopy)
        helper.imgApp.setImageDrawable(item.appIcon)
        helper.apkName.setText(item.appLabel)
        helper.pkgName.setText(item.pkgName)
        helper.apkSize.setText(item.totalSize)
        helper.apkVersion.setText(item.mVersion)
        helper.sigmd5.setText(item.sigmd5)
    }

    class ViewHolder(var rootView: View) : BaseViewHolder(rootView) {
        var imgApp: ImageView
        var btCopy: Button
        var apkName: TextView
        var pkgName: TextView
        var apkSize: TextView
        var apkVersion: TextView
        var sigmd5: TextView

        init {
            this.imgApp = rootView.findViewById(R.id.imgApp) as ImageView
            this.btCopy = rootView.findViewById(R.id.btCopy) as Button
            this.apkName = rootView.findViewById(R.id.apkName) as TextView
            this.pkgName = rootView.findViewById(R.id.pkgName) as TextView
            this.apkSize = rootView.findViewById(R.id.apkSize) as TextView
            this.apkVersion = rootView.findViewById(R.id.apkVersion) as TextView
            this.sigmd5 = rootView.findViewById(R.id.sigmd5) as TextView
        }

    }
}

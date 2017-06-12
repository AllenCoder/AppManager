package com.allen.kotlinapp

import android.graphics.drawable.Drawable

/**
 * 文 件 名: AppInfo
 * 创 建 人: Allen
 * 创建日期: 2017/6/12 16:51
 * 修改时间：
 * 修改备注：
 */
 class AppInfo {
    var appIcon: Drawable? = null
    var appLabel: String? = null
    var cachesize: String? = null
    var codesize: String? = null
    var datasize: String? = null
    var isSystem: Boolean? = null
    var location: String? = null
    var mVersion: String? = null
    var pkgName: String? = null
    var sigmd5: String? = null
    var totalSize: String? = null
    var totalSizeLong: Long? = null
}

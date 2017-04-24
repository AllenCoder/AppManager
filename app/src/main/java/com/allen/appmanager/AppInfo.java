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

import android.graphics.drawable.Drawable;

public class AppInfo {
    private Drawable appIcon;
    private String appLabel;
    private String cachesize;
    private String codesize;
    private String datasize;
    private Boolean isSystem;
    private String location;
    private String mVersion;
    private String pkgName;
    private String sigmd5;
    private String totalSize;
    private long totalSizeLong;

    public AppInfo() {
    }

    public Drawable getAppIcon() {
        return this.appIcon;
    }

    public String getAppLabel() {
        return this.appLabel;
    }

    public String getCachesize() {
        return this.cachesize;
    }

    public String getCodesize() {
        return this.codesize;
    }

    public String getDatasize() {
        return this.datasize;
    }

    public Boolean getIsSystem() {
        return this.isSystem;
    }

    public String getLocation() {
        return this.location;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public String getSigmd5() {
        return this.sigmd5;
    }

    public String getTotalSize() {
        return this.totalSize;
    }

    public long getTotalSizeLong() {
        return this.totalSizeLong;
    }

    public String getmVersion() {
        return this.mVersion;
    }

    public void setAppIcon(Drawable var1) {
        this.appIcon = var1;
    }

    public void setAppLabel(String var1) {
        this.appLabel = var1;
    }

    public void setCachesize(String var1) {
        this.cachesize = var1;
    }

    public void setCodesize(String var1) {
        this.codesize = var1;
    }

    public void setDatasize(String var1) {
        this.datasize = var1;
    }

    public void setIsSystem(Boolean var1) {
        this.isSystem = var1;
    }

    public void setLocation(String var1) {
        this.location = var1;
    }

    public void setPkgName(String var1) {
        this.pkgName = var1;
    }

    public void setSigmd5(String var1) {
        this.sigmd5 = var1;
    }

    public void setTotalSize(String var1) {
        this.totalSize = var1;
    }

    public void setTotalSizeLong(long var1) {
        this.totalSizeLong = var1;
    }

    public void setmVersion(String var1) {
        this.mVersion = var1;
    }
}

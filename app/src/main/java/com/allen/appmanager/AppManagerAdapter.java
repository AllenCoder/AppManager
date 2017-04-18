package com.allen.appmanager;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * 文 件 名: AppManagerAdapter
 * 创 建 人: Allen
 * 创建日期: 2017/4/17 16:16
 * 修改时间：
 * 修改备注：
 */

public class AppManagerAdapter extends BaseQuickAdapter<AppInfo, AppManagerAdapter.ViewHolder> {
    public AppManagerAdapter(int layoutResId, List<AppInfo> data) {
        super(R.layout.app_item, data);
    }

    public AppManagerAdapter(List<AppInfo> data) {
        super(R.layout.app_item, data);
    }


    @Override
    protected void convert(ViewHolder helper, AppInfo item) {
//        helper.setText()
        helper.addOnClickListener(R.id.btCopy);
        helper.imgApp.setImageDrawable(item.getAppIcon());
        helper.apkName.setText(item.getAppLabel());
        helper.pkgName.setText(item.getPkgName());
        helper.apkSize.setText(item.getTotalSize());
        helper.apkVersion.setText(item.getmVersion());
        helper.sigmd5.setText(item.getSigmd5());
    }

    public static class ViewHolder  extends BaseViewHolder{
        public View rootView;
        public ImageView imgApp;
        public Button btCopy;
        public TextView apkName;
        public TextView pkgName;
        public TextView apkSize;
        public TextView apkVersion;
        public TextView sigmd5;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.imgApp = (ImageView) rootView.findViewById(R.id.imgApp);
            this.btCopy = (Button) rootView.findViewById(R.id.btCopy);
            this.apkName = (TextView) rootView.findViewById(R.id.apkName);
            this.pkgName = (TextView) rootView.findViewById(R.id.pkgName);
            this.apkSize = (TextView) rootView.findViewById(R.id.apkSize);
            this.apkVersion = (TextView) rootView.findViewById(R.id.apkVersion);
            this.sigmd5 = (TextView) rootView.findViewById(R.id.sigmd5);
        }

    }
}

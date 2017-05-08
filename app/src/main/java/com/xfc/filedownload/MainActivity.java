package com.xfc.filedownload;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xiaochen.progressroundbutton.AnimDownloadProgressButton;

import org.wlf.filedownloader.DownloadFileInfo;
import org.wlf.filedownloader.FileDownloader;
import org.wlf.filedownloader.listener.OnDeleteDownloadFileListener;
import org.wlf.filedownloader.listener.OnFileDownloadStatusListener;

import java.io.File;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnFileDownloadStatusListener {
    private TextView tvProgress;
    //    private String url = "http://shouji.360tpcdn.com/170330/4fb59f3a936b412f391971dde543369d/com.tencent.tmgp.sgame_18010702.apk";
    private String url = "http://shouji.360tpcdn.com/170420/c016cadc23334968ba9e312045dd03e3/com.tencent.gamehelper.smoba_17042005.apk";

    private Button btnDownload;
    private File file;
    private File downloadFile;
    private DownloadFileInfo dlInfo;
    private AppBroadcastReceiver mAppBroadcastReceiver;

    private AnimDownloadProgressButton mAnimButton;
    private static final String PACKAGE_NAME = "com.tencent.gamehelper.smoba";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnDownload = (Button) findViewById(R.id.btn_downlaod);
        tvProgress = (TextView) findViewById(R.id.tv_progress);
        mAnimButton = (AnimDownloadProgressButton) findViewById(R.id.anim_btn);
        FileDownloader.registerDownloadStatusListener(this);
        registerMyReceiver();

        dlInfo = FileDownloader.getDownloadFile(url);
        if (dlInfo != null) {
            file = new File(dlInfo.getFilePath());
            long progress = (dlInfo.getDownloadedSizeLong() * 100) / dlInfo.getFileSizeLong();
            mAnimButton.setState(AnimDownloadProgressButton.DOWNLOADING);
            mAnimButton.setTextSize(30);
            mAnimButton.setButtonRadius(20);
            mAnimButton.setProgressText("继续", progress);
            if (file.exists()) {
                Log.e("lzr", "下载apk存在");
                btnDownload.setText("安装");
            } else {
                Log.e("lzr", "apk不存在");
                //显示下载
                btnDownload.setText("下载");
            }
        }
        boolean isInstalled = isAppInstalled(PACKAGE_NAME);
        if (isInstalled) {
            btnDownload.setText("打开");
        }
    }

    private void registerMyReceiver() {
        mAppBroadcastReceiver = new AppBroadcastReceiver(btnDownload);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.registerReceiver(mAppBroadcastReceiver, intentFilter);
    }

    class AppBroadcastReceiver extends BroadcastReceiver {
        private Button btn;

        public AppBroadcastReceiver(Button btn) {
            this.btn = btn;
        }

        private final String ADD_APP = "android.intent.action.PACKAGE_ADDED";
        private final String REMOVE_APP = "android.intent.action.PACKAGE_REMOVED";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ADD_APP.equals(action)) {
                String packageName = intent.getDataString();
                System.out.println("安装了:" + packageName);
                btn.setText("打开");
            }
            if (REMOVE_APP.equals(action)) {
                String packageName = intent.getDataString();
                System.out.println("卸载了:" + packageName);

            }
        }

    }

    private boolean isAppInstalled(String packagename) {
        PackageInfo packageInfo;
        try {
            packageInfo = this.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if (packageInfo == null) {
            //System.out.println("没有安装");
            return false;
        } else {
            //System.out.println("已经安装");
            return true;
        }
    }

    public void download(View v) {
        String str = btnDownload.getText().toString().trim();
        if (str.equals("下载") || str.equals("继续")) {
            FileDownloader.start(url);
        } else if (str.equals("安装")) {
            if (file == null) {
                install(downloadFile);
            } else {
                install(file);
            }
        } else if (str.equals("打开")) {
            startApp(PACKAGE_NAME);
        }
    }

    private void startApp(String packagename) {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = this.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = this.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            startActivity(intent);
        }
    }

    private void install(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity");
        this.startActivity(intent);
    }

    private String getPkgName(String apkPath) {
        PackageManager pm = this.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);//第一个参数是apk文件的路径
        ApplicationInfo appInfo = null;
        String packageName = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            packageName = appInfo.packageName;
        }
        return packageName;
    }

    public void pause(View v) {
        FileDownloader.pause(url);
        btnDownload.setText("继续");
    }

    public void delete(View v) {
        FileDownloader.delete(url, true, new OnDeleteDownloadFileListener() {
            @Override
            public void onDeleteDownloadFilePrepared(DownloadFileInfo downloadFileNeedDelete) {
                Log.e("lzr", "onDeleteDownloadFilePrepared");
            }

            @Override
            public void onDeleteDownloadFileSuccess(DownloadFileInfo downloadFileDeleted) {
                Log.e("lzr", "onDeleteDownloadFileSuccess");
                //删除文件
                //先判断是否安装了
                boolean isInstalled = isAppInstalled(PACKAGE_NAME);
                if (isInstalled) {
                    btnDownload.setText("打开");
                } else {
                    btnDownload.setText("下载");
                }
            }

            @Override
            public void onDeleteDownloadFileFailed(DownloadFileInfo downloadFileInfo, DeleteDownloadFileFailReason failReason) {
                Log.e("lzr", "onDeleteDownloadFileFailed");


            }
        });
    }


    @Override
    public void onFileDownloadStatusWaiting(DownloadFileInfo downloadFileInfo) {
        Log.e("lzr", "onFileDownloadStatusWaiting");
    }

    @Override
    public void onFileDownloadStatusPreparing(DownloadFileInfo downloadFileInfo) {
        Log.e("lzr", "onFileDownloadStatusPreparing");
    }

    @Override
    public void onFileDownloadStatusPrepared(DownloadFileInfo downloadFileInfo) {
        Log.e("lzr", "onFileDownloadStatusPrepared");
    }

    @Override
    public void onFileDownloadStatusDownloading(DownloadFileInfo downloadFileInfo, float downloadSpeed, long remainingTime) {

        Log.e("lzr", "onFileDownloadStatusDownloading");
        tvProgress.setText("apk总大小:" + FileUtil.formatFileSize(downloadFileInfo.getFileSizeLong()) + "\n已经下载了:"
                + FileUtil.formatFileSize(downloadFileInfo.getDownloadedSizeLong()) + "\n下载速度:" + Math.round(downloadSpeed) + "kb/s" +
                "\n剩下时间:" + TimeUtil.seconds2HH_mm_ss(remainingTime) + "秒" + "\n下载路径:" + downloadFileInfo.getFilePath());
        Log.e("lzr", "总大小 " + downloadFileInfo.getFileSizeLong());
        Log.e("lzr", "已下载大小 " + downloadFileInfo.getFileSizeLong());
        long progress = (downloadFileInfo.getDownloadedSizeLong() * 100) / downloadFileInfo.getFileSizeLong();

        Log.e("lzr", "progress " + progress);
        if (progress > 0 && progress < 100) {
            mAnimButton.setTextSize(30);
            mAnimButton.setState(AnimDownloadProgressButton.DOWNLOADING);
            mAnimButton.setProgressText("下载中", progress);
            Log.e("lzr", "showTheButton: " + mAnimButton.getProgress());
        } else if (progress == 100) {
            mAnimButton.setState(AnimDownloadProgressButton.NORMAL);
            mAnimButton.setCurrentText("安装");
        }
    }

    @Override
    public void onFileDownloadStatusPaused(DownloadFileInfo downloadFileInfo) {
        Log.e("lzr", "onFileDownloadStatusPaused");
    }

    @Override
    public void onFileDownloadStatusCompleted(DownloadFileInfo downloadFileInfo) {
        Log.e("lzr", "onFileDownloadStatusCompleted");
        btnDownload.setText("安装");
        Log.e("lzr", "downloadFileInfo==" + downloadFileInfo.getFilePath());
        downloadFile = new File(downloadFileInfo.getFilePath());
    }

    @Override
    public void onFileDownloadStatusFailed(String url, DownloadFileInfo downloadFileInfo, FileDownloadStatusFailReason failReason) {
        Log.e("lzr", "onFileDownloadStatusFailed" + "失败原因===" + failReason.getMessage());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileDownloader.unregisterDownloadStatusListener(this);
        unregisterReceiver(mAppBroadcastReceiver);
    }
}

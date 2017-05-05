package com.xfc.filedownload;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.wlf.filedownloader.DownloadFileInfo;
import org.wlf.filedownloader.FileDownloader;
import org.wlf.filedownloader.listener.OnDeleteDownloadFileListener;
import org.wlf.filedownloader.listener.OnFileDownloadStatusListener;

import java.io.File;

public class MainActivity extends AppCompatActivity implements OnFileDownloadStatusListener {
    private TextView tvProgress;
//    private String url = "http://shouji.360tpcdn.com/170330/4fb59f3a936b412f391971dde543369d/com.tencent.tmgp.sgame_18010702.apk";
    private String url = "http://shouji.360tpcdn.com/170420/c016cadc23334968ba9e312045dd03e3/com.tencent.gamehelper.smoba_17042005.apk";

    private long tempFileSize = 0L;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvProgress = (TextView) findViewById(R.id.tv_progress);
        FileDownloader.registerDownloadStatusListener(this);

        DownloadFileInfo dlInfo = FileDownloader.getDownloadFile(url);
        if (dlInfo != null){
            File file = new File(dlInfo.getFilePath());
            if (file.exists()){
                Log.e("lzr","下载apk存在");
            }else{
                Log.e("lzr","apk不存在");
            }
        }
    }

    public void download(View v) {
        FileDownloader.start(url);
    }

    public void pause(View v) {
        FileDownloader.pause(url);
    }
    public void delete(View v){
        FileDownloader.delete(url, true, new OnDeleteDownloadFileListener() {
            @Override
            public void onDeleteDownloadFilePrepared(DownloadFileInfo downloadFileNeedDelete) {
                Log.e("lzr","onDeleteDownloadFilePrepared");
            }

            @Override
            public void onDeleteDownloadFileSuccess(DownloadFileInfo downloadFileDeleted) {
                Log.e("lzr","onDeleteDownloadFileSuccess");
            }

            @Override
            public void onDeleteDownloadFileFailed(DownloadFileInfo downloadFileInfo, DeleteDownloadFileFailReason failReason) {
                Log.e("lzr","onDeleteDownloadFileFailed");
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
        tvProgress.setText("apk总大小:"+FileUtil.formatFileSize(downloadFileInfo.getFileSizeLong())+"\n已经下载了:"
                + FileUtil.formatFileSize(downloadFileInfo.getDownloadedSizeLong())+ "\n下载速度:" + Math.round(downloadSpeed) + "kb/s"+
                "\n剩下时间:" + TimeUtil.seconds2HH_mm_ss(remainingTime)+"秒"+"\n下载路径:"+downloadFileInfo.getFilePath());
    }

    @Override
    public void onFileDownloadStatusPaused(DownloadFileInfo downloadFileInfo) {
        Log.e("lzr", "onFileDownloadStatusPaused");
    }

    @Override
    public void onFileDownloadStatusCompleted(DownloadFileInfo downloadFileInfo) {
        Log.e("lzr", "onFileDownloadStatusCompleted");
    }

    @Override
    public void onFileDownloadStatusFailed(String url, DownloadFileInfo downloadFileInfo, FileDownloadStatusFailReason failReason) {
        Log.e("lzr", "onFileDownloadStatusFailed" + "失败原因===" + failReason.getMessage());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileDownloader.unregisterDownloadStatusListener(this);
    }
}

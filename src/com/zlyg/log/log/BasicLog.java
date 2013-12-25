
package com.zlyg.log.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zlyg.log.reporter.AbsSendReportsService;

class BasicLog {
    private static final String TAG = BasicLog.class.getSimpleName();
    /**
     * 日志文件扩展名
     */
    private static final String LOG_FILE_EXTENSION = ".log";
    /**
     * 是否启用Log
     */
    static boolean ENABLE_LOGCAT = false;
    /**
     * 是否把Log也输出到文件
     */
    static boolean TO_FILE = false;
    /**
     * 保存Log信息的文件路径
     */
    static String LOG_FILE_PATH = "";
    /**
     * 记录到文件的Log信息的日期前缀
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm:ss ");

    /**
     * 是否把日志输出到Logcat,建议在Application里面设置,安全起见,默认是false.
     * 
     * @param enable 是否启用,出于安全和性能考虑,默认false
     */
    public static void setEnableLogcat(boolean enable) {
        if (enable) {
            if (!ENABLE_LOGCAT) {
                ENABLE_LOGCAT = true;
                Log.i(TAG, "Logcat Enabled");
            } else {
                Log.i(TAG, "Log Already Enabled ");
            }
        } else {
            if (ENABLE_LOGCAT) {
                ENABLE_LOGCAT = false;
                Log.i(TAG, "Now Disabled");
            } else {
                Log.i(TAG, "Log Already Disabled");
            }
        }
    }

    /**
     * 查询当前是否把日志输出到Logcat
     * 
     * @return 是否已启用
     */
    public static boolean isLogcatEnable() {
        return ENABLE_LOGCAT;
    }

    /**
     * 设置是否保存日志到文件,文件所在目录/data/data/packageName/
     * 
     * @param enable 是否启用,出于安全和性能考虑,默认false
     * @param ctx
     */
    public static void setEnableLogToFile(boolean enable, Context ctx) {
        if (enable) {
            if (!TO_FILE) {// 启用保存到文件
                TO_FILE = true;
                LOG_FILE_PATH = "/mnt/sdcard";//ctx.getFilesDir().getAbsolutePath();
                Log.i(TAG, "Will Save Log To File");
            } else {
                Log.i(TAG, "Save To File Already Enable");
            }
        } else {
            if (TO_FILE) {
                TO_FILE = false;
                Log.i(TAG, "Will NOT Save Log To File");
            } else {
                Log.i(TAG, "Save To File Already Disable");
            }
        }
    }

    /**
     * 查询是否有启用保存日志到文件
     * 
     * @return 是否已启用
     */
    public static boolean isSaveToFileEnable() {
        return TO_FILE;
    }

    /**
     * 记录日志到文件
     * 
     * @param msg 需要记录的日志信息
     */
    public static void save(String msg) {
        File file = new File(LOG_FILE_PATH,
                DATE_FORMAT.format(new Date(System.currentTimeMillis())) + LOG_FILE_EXTENSION);
        if (!file.exists()) {
            try {
                new File(LOG_FILE_PATH).mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 时间应当在msg构造时就生成的,但是我们没这么高的精确度要求.
        String time = TIME_FORMAT.format(new Date(System.currentTimeMillis()));
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(file.getAbsolutePath(), true));
            out.append(time);
            out.append(msg);
            out.append("\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 发送日志文件到服务器
    public static void sendReportFiles(Context ctx,
            Class<? extends AbsSendReportsService> sendService) {
        Intent intent = new Intent(ctx, sendService);
        intent.putExtra(AbsSendReportsService.INTENT_DIR, LOG_FILE_PATH);
        intent.putExtra(AbsSendReportsService.INTENT_EXTENSION, LOG_FILE_EXTENSION);
        ctx.startService(intent);
    }
}

package com.zlyg.log.carash;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.zlyg.log.reporter.AbsSendReportsService;

public class CrashHandler implements UncaughtExceptionHandler {
	private static final String TAG = "CrashHandler";

	private static CrashHandler crashHandler;
	private Context mContext;
	private UncaughtExceptionHandler mDefaultHandler;

	/** 使用Properties来保存设备的信息和错误堆栈信息 */
	private Properties mCrashInfo = new Properties();
	private static final String VERSION_NAME = "versionName";
	private static final String VERSION_CODE = "versionCode";
	/** 错误报告文件的扩展名 */
	public static final String CRASH_REPORTER_EXTENSION = ".crash";
	/** 错误报告文件名中的日期格式 */
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd_hh:mm:ss");

	private CrashHandler() {
	}

	private static synchronized void syncInit() {
		if (crashHandler == null) {
			crashHandler = new CrashHandler();
		}
	}

	public static CrashHandler getInstance() {
		if (crashHandler == null) {
			syncInit();
		}

		return crashHandler;

	}

	public void init(Context context,
			Class<? extends AbsSendReportsService> sendService) {
		this.mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		sendLastReport(sendService);
	}

	/**
	 * 发送上次的报告
	 * 
	 * @param sendService
	 */
	private void sendLastReport(
			Class<? extends AbsSendReportsService> sendService) {
		if (sendService == null)
			return;
		Intent intent = new Intent(mContext, sendService);
		intent.putExtra(AbsSendReportsService.INTENT_DIR, mContext
				.getFilesDir().getAbsolutePath());
		intent.putExtra(AbsSendReportsService.INTENT_EXTENSION,
				CRASH_REPORTER_EXTENSION);
		mContext.startService(intent);
	}

	@Override
	public void uncaughtException(Thread thread, final Throwable ex) {
		
		Log.d("CreashHandler","+++++uncaughtException++++");
		if (mDefaultHandler == null && ex == null) {
			Log.d("CreashHandler","++++"+(mDefaultHandler == null)+(ex == null));
			exitCurrentApp();
		} else {
			ex.printStackTrace();
			Log.d("CreashHandler","++++"+(mDefaultHandler == null)+(ex == null));
			new Thread(new Runnable() {

				@Override
				public void run() {
					Looper.prepare();
					AlertDialog dialog = showExceptionDialog();
					collectDeviceInfo(mContext);
					saveCrashInfoToFile(ex);
					dismissExceptionDialog(dialog);

					// 启动消息队列(在队列推出前,后面的代码不会被执行,在这里,后面没有代码了.)
					Looper.loop();

				}
			}).start();
		}

	}

	private void exitCurrentApp() {
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	  private AlertDialog showExceptionDialog() {
	        ProgressBar pb = new ProgressBar(mContext, null,
	                android.R.attr.progressBarStyleInverse);

	        AlertDialog.Builder builder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
	        builder.setView(pb);
	        builder.setCancelable(false);
	        builder.setTitle("程序出错了,即将退出");
	        builder.setMessage("正在收集错误信息...");
	        builder.setIcon(android.R.drawable.ic_dialog_alert);
	        AlertDialog dialog = builder.create();

	        // <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
	        // http://android.35g.tw/?p=191
	        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	        dialog.show();
	        return dialog;
	    }
	  
	  
	   private void dismissExceptionDialog(final AlertDialog dialog) {
	        // 使用postDelayed让用户能有足够时间看清提示信息
	        new Handler().postDelayed(new Runnable() {
	            @Override
	            public void run() {

	                dialog.setMessage("正在退出...");

	                new Handler().postDelayed(new Runnable() {
	                    @Override
	                    public void run() {

	                        dialog.dismiss();
	                        exitCurrentApp();

	                    }
	                }, 2 * 1000);
	            }
	        }, 2 * 1000);
	    }

	    private void collectDeviceInfo(Context ctx) {
	       
	    	PackageHelper packageHelper = new PackageHelper(ctx);
	        mCrashInfo.put(VERSION_NAME, packageHelper.getLocalVersionName());
	        mCrashInfo.put(VERSION_CODE, packageHelper.getLocalVersionCode() + "");
	        // 使用反射来收集设备信息.在Build类中包含各种设备信息,
	        Field[] fields = Build.class.getDeclaredFields();
	        for (Field field : fields) {
	            try {
	                field.setAccessible(true);
	                String fieldStr = "";
	                try {
	                    fieldStr = field.get(null).toString();
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	                mCrashInfo.put(field.getName(), fieldStr);
	            } catch (Exception e) {
	                Log.e(TAG, "Error while collecting device info", e);
	            }
	        }
	    }

	    private void saveCrashInfoToFile(Throwable ex) {
	        Writer info = new StringWriter();
	        PrintWriter printWriter = new PrintWriter(info);

	        printWriter.write("\n=========printStackTrace()==========\n");
	        ex.printStackTrace(printWriter);

	        printWriter.write("\n\n=========getCause()==========\n");
	        Throwable cause = ex.getCause();
	        while (cause != null) {
	            cause.printStackTrace(printWriter);
	            cause = cause.getCause();
	        }

	        String stackTrace = info.toString();
	        printWriter.close();

	        try {
	            String fileName = dateFormat.format(new Date(System.currentTimeMillis()))
	                    + CRASH_REPORTER_EXTENSION;
	            // 保存文件
	            FileOutputStream trace = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
	            mCrashInfo.store(trace, "");
	            trace.write(stackTrace.getBytes());
	            trace.flush();
	            trace.close();
	        } catch (Exception e) {
	            Log.e(TAG, "an error occured while writing report file", e);
	        }
	    }
	
	
}

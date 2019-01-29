package ir.vasfa.iman.edgetoolkit.Extra;

/**
 * Created by vali on 2017-07-17.
 */
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrashHandler implements UncaughtExceptionHandler {
    public static final String TAG = CrashHandler.class.getSimpleName();

    private static CrashHandler instance = null;

    private Context mContext;

    private UncaughtExceptionHandler mDefaultHandler;

    private Map<String, String> infos = new HashMap<String, String>();

    private CrashHandler() {
        String asd="";
    }

    private CrashHandler(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static CrashHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e("error : ", e.getMessage());
            }
            System.exit(0);
        }
    }

    private boolean handleException(final Throwable ex) {


        if (Debug.isDebuggerConnected())
            return false;
        if (ex == null)
            return false;

        collectDeviceInfo(mContext);

        String CrashList="TestBepors_crash_Date_"+getDateTime()+".txt";
        //generateNoteOnSDTEST(CrashList,ex.getMessage().toString());
        saveCrashInfo2File(ex);
        return true;
    }

    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "an error occurred when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Log.e(TAG, "an error occurred when collect crash info", e);
            }
        }
    }

    private void saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        try {
            String fileName = String.format("crash-%s.log", df.format(new Date(System.currentTimeMillis())));
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                String path = "/sdcard/multiThread/log/";
                File dir = new File(path);
                if (!dir.exists())
                    dir.mkdirs();
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
    }


    boolean firstEnter = false;

    public void generateNoteOnSDTEST(String sFileName, String sBody) {
        try {

            String log = "date: " + getDateTime() + "\n" +"error: " +sBody ;

            File root = new File(Environment.getExternalStorageDirectory(), "NotesBepors");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(log);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String newdate = dateFormat.format(date);
        return newdate.replaceAll(" ", "_");

    }
}
package ir.vasfa.iman.edgetoolkit.Extra;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        CrashHandler.getInstance(context);
    }
}

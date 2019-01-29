package ir.vasfa.iman.edgetoolkit;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ir.vasfa.iman.edgetoolkit.models.AppModel;
import ir.vasfa.iman.edgetoolkit.models.ContactModel;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


/**
 * Created by anupamchugh on 01/08/17.
 */

public class FloatingWidgetService extends Service {


    private WindowManager mWindowManager;
    private View mOverlayView;
    int mWidth;
    private ImageView counterFab;
    private boolean activity_background = true;

    //static list for speed up the calling

    public static ArrayList<ContactModel> CONTACT = null;
    public static ArrayList<AppModel> APPS = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        setTheme(R.style.Transparent_setting);
//        if (intent != null) {
//            activity_background = intent.getBooleanExtra("activity_background", true);
//
//        }

        if (mOverlayView == null) {

            mOverlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);

            counterFab = (ImageView) mOverlayView.findViewById(R.id.fabHead);
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);


            //Specify the view position
            params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
            params.x = 0;
            params.y = 100;


            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mOverlayView, params);

            Display display = mWindowManager.getDefaultDisplay();
            final Point size = new Point();
            display.getSize(size);



            final RelativeLayout layout = (RelativeLayout) mOverlayView.findViewById(R.id.layout);
            ViewTreeObserver vto = layout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = layout.getMeasuredWidth();

                    //To get the accurate middle of the screen we subtract the width of the floating widget.
                    mWidth = size.x - width;

                }
            });
            counterFab.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            //remember the initial position.
                            initialX = params.x;
                            initialY = params.y;


                            //get the touch location
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();


                            return true;
                        case MotionEvent.ACTION_UP:

                            /**
                             * back to default_contact_phpto color when stop moving
                             */
                            counterFab.setColorFilter(null);

                            //Only start the activity if the application is in background. Pass the current badge_count to the activity
                            if (activity_background) {

                                float xDiff = event.getRawX() - initialTouchX;
                                float yDiff = event.getRawY() - initialTouchY;

                                if ((Math.abs(xDiff) < 10) && (Math.abs(yDiff) < 10) && !isForeground("ir.vasfa.iman.edgetoolkit")) {

                                    Intent intent = new Intent(FloatingWidgetService.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);


                                    //close the service and remove the fab view
//                                    stopSelf();
                                }

                            }
                            //Logic to auto-position the widget based on where it is positioned currently w.r.t middle of the screen.
                            int middle = mWidth / 2;
                            float nearestXWall = params.x >= middle ? mWidth : 0;
                            params.x = (int) nearestXWall;


                            mWindowManager.updateViewLayout(mOverlayView, params);


                            return true;
                        case MotionEvent.ACTION_MOVE:


                            int xDiff = Math.round(event.getRawX() - initialTouchX);
                            int yDiff = Math.round(event.getRawY() - initialTouchY);


                            /**
                             * change color when move in the screen
                             */
//                            counterFab.setColorFilter(new ColorFilter() , PorterDuff.Mode.MULTIPLY);
                            counterFab.setColorFilter(Color.RED);



                            //Calculate the X and Y coordinates of the view.
                            params.x = initialX + xDiff;
                            params.y = initialY + yDiff;

                            //Update the layout with new X & Y coordinates
                            mWindowManager.updateViewLayout(mOverlayView, params);


                            return true;
                    }
                    return false;
                }

            });


        } else {


        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mOverlayView != null)
//            mWindowManager.removeView(mOverlayView);
    }
    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }
}

package ir.vasfa.iman.edgetoolkit;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.github.vivchar.viewpagerindicator.ViewPagerIndicator;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ir.vasfa.iman.edgetoolkit.Bus.EnableEditMode;
import ir.vasfa.iman.edgetoolkit.Bus.RemoveApp;
import ir.vasfa.iman.edgetoolkit.Bus.RemoveContact;
import ir.vasfa.iman.edgetoolkit.Extra.CustomViewPager;
import ir.vasfa.iman.edgetoolkit.Extra.Setting;
import ir.vasfa.iman.edgetoolkit.Fragments.ContactFragment;
import ir.vasfa.iman.edgetoolkit.Fragments.AppFragment;
import ir.vasfa.iman.edgetoolkit.adapters.AppAdapter;
import ir.vasfa.iman.edgetoolkit.adapters.ContactListAdapter;
import ir.vasfa.iman.edgetoolkit.adapters.ViewPagerAdapter;
import ir.vasfa.iman.edgetoolkit.models.AppModel;
import ir.vasfa.iman.edgetoolkit.models.ContactModel;

public class MainActivity extends AppCompatActivity {


    public static ArrayList<ContactModel> CONTACT = null;
    public static ArrayList<AppModel> APPS = null;

    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;
    private Button button;
    private ImageView background;
    private TextView textView;
    private WindowManager mWindowManager;
    private RecyclerView recyclerView;
    private AppAdapter shortCutAdapter;
    private GridLayoutManager gridLayoutManager;
    private CustomViewPager viewPager;
    public TextView edit;
    private ViewPagerIndicator viewPagerIndicator;
    private ViewPagerAdapter viewPagerAdapter;
    private AppFragment rightSideFragment;
    private ViewPager viewPagerEditMode;
    private ViewPagerAdapter viewPagerAdapterEditMode;
    private TextView done;
    private Activity activity;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor = null;

    private ArrayList<AppModel> tempAppList;
    private ArrayList<ContactModel> tempContactList;


    private SpinKitView spinKitView;

    private RecyclerView rv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
        setContentView(R.layout.activity_main);

        activity = this;

        spinKitView = findViewById(R.id.spin_kit);
        rv2 = findViewById(R.id.RV2);
        recyclerView = findViewById(R.id.RV);
        viewPager = findViewById(R.id.viewPager);
        done = findViewById(R.id.done);

        rv2.setVisibility(View.GONE);

        viewPagerIndicator = findViewById(R.id.view_pager_indicator);
        edit = findViewById(R.id.edit);
        background = findViewById(R.id.background);
        askForSystemOverlayPermission();
        init();
    }

    private void init() {
        done.setVisibility(View.GONE);
        createViewPager();
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (rv2.getVisibility() != View.VISIBLE) {


                    background.setImageResource(R.drawable.white_background);
                    edit.setVisibility(View.GONE);
                    done.setVisibility(View.VISIBLE);
                    if (viewPager.getCurrentItem() == 0) {
                        tempAppList = new ArrayList<>(MainActivity.APPS);
                        appController();
                    }
                    if (viewPager.getCurrentItem() == 1) {
                        tempContactList = new ArrayList<>(MainActivity.CONTACT);
                        contactController();
                    }
                    viewPagerIndicator.setVisibility(View.GONE);
                    viewPager.setPagingEnabled(false);

                    rv2.setVisibility(View.VISIBLE);
                }
            }
        });


        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (viewPager.getCurrentItem() == 0) {
                    tempAppList = MainActivity.APPS;
                    saveAppsToShare();
                    doneHandler();
                    /*
                     * prevention to show old list by edit click perform
                     */
                    if (appAdapter != null) {
                        appAdapter.clear();
                    }
                }
                if (viewPager.getCurrentItem() == 1) {
                    tempContactList = MainActivity.CONTACT;
                    saveContactToShare();
                    doneHandler();
                    /*
                     * prevention to show old list by edit click perform
                     */
                    if (contactListAdapter != null) {
                        contactListAdapter.clear();
                    }
                }
            }
        });
    }


    private void doneHandler() {
        background.setImageResource(R.drawable.layer);
        done.setVisibility(View.GONE);
        viewPager.setPagingEnabled(true);
        edit.setVisibility(View.VISIBLE);
        rv2.setVisibility(View.GONE);
        viewPagerIndicator.setVisibility(View.VISIBLE);
        EnableEditMode enableEditMode = new EnableEditMode();
        enableEditMode.editMode = false;
        EventBus.getDefault().post(enableEditMode);

    }

    @Override
    protected void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }


//    public void editHandler() {
//        background.setImageResource(R.drawable.white_background);
//        //eventBus
//        EnableEditMode enableEditMode = new EnableEditMode();
//        enableEditMode.editMode = true;
//        edit.setVisibility(View.INVISIBLE);
//
//        EventBus.getDefault().post(enableEditMode);
//        //this activity actions
//        viewPagerAdapterEditMode = new ViewPagerAdapter(getSupportFragmentManager());
//
//        int type = viewPager.getCurrentItem();
//        viewPagerEditMode.setAdapter(viewPagerAdapterEditMode);
//        viewPager.setPagingEnabled(false);
//
//    }

    private void createViewPager() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPagerAdapter = new ViewPagerAdapter(fragmentManager);
        rightSideFragment = new AppFragment();
        viewPagerAdapter.addFragment(rightSideFragment, "rs");
        ContactFragment contactFragment = new ContactFragment();
        viewPagerAdapter.addFragment(contactFragment, "cf");
        viewPager.setAdapter(viewPagerAdapter);
        viewPagerIndicator.setupWithViewPager(viewPager);
        viewPager.setPagingEnabled(true);

    }

    private void askForSystemOverlayPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(MainActivity.this)) {
            startService(new Intent(MainActivity.this, FloatingWidgetService.class));
        } else {
            errorToast();
        }
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    //Permission is not available. Display error text.
                    errorToast();
                    finish();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void errorToast() {
        Toast.makeText(this, "Draw over other app permission not available. Can't start the application without the permission.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finisher();
        overridePendingTransition(R.anim.left_to_right_finish, R.anim.right_to_left_finish);

    }

    private void finisher() {
        if (tempAppList != null) {
            MainActivity.APPS = tempAppList;
        }
        if (tempContactList != null) {
            MainActivity.CONTACT = tempContactList;
        }
        finish();
    }


    //------------------------------------- AppController Part -------------------------------------\\

    private AppAdapter appAdapter;
    private GridLayoutManager gridLayoutManagerApps;

    private void appController() {

        EnableEditMode enableEditMode = new EnableEditMode();
        enableEditMode.editMode = true;
        EventBus.getDefault().post(enableEditMode);
        new AppCollectorThread().execute();


    }

    private ArrayList<String> getAppList() {
        ArrayList<String> list = new ArrayList<>();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = activity.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo item : pkgAppsList) {
            list.add((item.activityInfo).packageName);
        }
        return list;
    }

    private ArrayList<AppModel> makeAppListController(ArrayList<String> list) {
        ArrayList<AppModel> arrayList = new ArrayList<>();
        for (String item : list) {
            try {
                AppModel appModel = new AppModel();
                appModel.setPackageName(item);
                try {
                    Drawable icon = activity.getPackageManager().getApplicationIcon(item);
                    appModel.setDrawable(icon);
                } catch (PackageManager.NameNotFoundException e) {
                    continue;
                }
                try {
                    String[] name = item.split("\\.");
                    appModel.setName(name[name.length - 1]);
                } catch (Exception e) {
                    continue;
                }
                arrayList.add(appModel);
            } catch (Exception e) {
                continue;
            }

        }
        return arrayList;
    }

    private class AppCollectorThread extends AsyncTask<Void, Void, ArrayList<AppModel>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinKitView.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<AppModel> doInBackground(Void... voids) {
            return makeAppListController(getAppList());
        }

        @Override
        protected void onPostExecute(ArrayList<AppModel> appModels) {
            super.onPostExecute(appModels);
            appAdapter = new AppAdapter(activity, appModels, AppAdapter.EDIT,
                    false, null);
            gridLayoutManagerApps = new GridLayoutManager(activity, 2);
            rv2.setLayoutManager(gridLayoutManagerApps);
            rv2.setAdapter(appAdapter);
            spinKitView.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onRemoveAppEvent(RemoveApp removeApp) {
        if (appAdapter != null) {
            appAdapter.notifyDataSetChanged();
        }
    }
    @Subscribe
    public void onContactRemoveEvent(RemoveContact removeContact){
        if (contactListAdapter != null){
            contactListAdapter.notifyAdapter();
        }
    }

    private void saveAppsToShare() {
        try {
            if (MainActivity.APPS != null) {
                Gson gson = new Gson();

                /*
                 * it is cleaning all drawable in primary list for reason :
                 * handling too large transaction Exception
                 */
                for (AppModel appModel : MainActivity.APPS) {
                    appModel.setDrawable(null);
                }
                String s = gson.toJson(MainActivity.APPS);
                if (sharedPreferences == null) {
                    sharedPreferences = activity.getSharedPreferences(Setting.SHARE_NAME, Context.MODE_PRIVATE);
                }
                if (editor == null) {
                    editor = sharedPreferences.edit();
                }
                editor.putString(Setting.SHARE_APPS, s);
                editor.commit();
            }
        } catch (Exception e) {
            String s = e.toString();
        }

        /*
         *trying to reload the cleaning drawable
         */
        getDrawable();
    }

    private void getDrawable() {
        for (AppModel item : MainActivity.APPS) {
            try {
                Drawable icon = activity.getPackageManager().getApplicationIcon(item.getPackageName());
                item.setDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    //------------------------------------- AppController Part -------------------------------------\\


    //------------------------------------- ContactController Part -------------------------------------\\

    private ContactListAdapter contactListAdapter;
    private GridLayoutManager layoutManagerContact;
    private boolean permissionAccepted = false;
    private final int REQCODE = 7;
    private ArrayList<ContactModel> contactList = new ArrayList<>();

    private void contactController() {
        EnableEditMode enableEditMode = new EnableEditMode();
        enableEditMode.editMode = true;
        EventBus.getDefault().post(enableEditMode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int res1 = activity.checkSelfPermission(Manifest.permission.WRITE_CONTACTS);
            int res2 = activity.checkSelfPermission(Manifest.permission.READ_CALL_LOG);
            int res3 = activity.checkSelfPermission(Manifest.permission.READ_CONTACTS);
            if (!(res3 == PackageManager.PERMISSION_GRANTED)) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS,
//                        Manifest.permission.READ_CALL_LOG,Manifest.permission.READ_CONTACTS}, REQCODE);
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQCODE);
            } else {
                permissionAccepted = true;
            }

        } else {
            permissionAccepted = true;
        }

        if (permissionAccepted) {
            Toast.makeText(activity, "after granted", Toast.LENGTH_SHORT).show();
            new ContactCollectorThread().execute();
        }

    }

    private void getAllContact() {

        ContentResolver cr = activity.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                ContactModel contactModel = new ContactModel();
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
//                            String photo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                        //---------------------
                        Bitmap photoBitmap = createDefault();
                        try {
                            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(activity.getContentResolver(),
                                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(id)));

                            if (inputStream != null) {
                                photoBitmap = BitmapFactory.decodeStream(inputStream);
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        contactModel.setName(name);
                        contactModel.setNumber(phoneNo);
                        contactModel.setId(id);
                        contactModel.setPhoto(photoBitmap);
                    }
                    pCur.close();
                }
                if (contactModel.getNumber() != null){

                    contactList.add(contactModel);
                }
            }

            if (cur != null) {
                cur.close();
            }
//            prepareRecycler();
        }
    }

    private void prepareRecycler() {
        contactListAdapter = new ContactListAdapter(activity, contactList,
                false, null, ContactListAdapter.EDIT);
        layoutManagerContact = new GridLayoutManager(activity, 2);
        rv2.setLayoutManager(layoutManagerContact);
        rv2.setAdapter(contactListAdapter);
    }

    private Bitmap createDefault() {
        Random random = new Random();
        int ran = random.nextInt(5);
        if (ran == 1) {
            return BitmapFactory.decodeResource(activity.getResources(), R.drawable.c_one);
        } else if (ran == 2) {
            return BitmapFactory.decodeResource(activity.getResources(), R.drawable.c_two);
        } else if (ran == 3) {
            return BitmapFactory.decodeResource(activity.getResources(), R.drawable.c_three);
        } else if (ran == 4) {
            return BitmapFactory.decodeResource(activity.getResources(), R.drawable.c_four);
        } else {
            return BitmapFactory.decodeResource(activity.getResources(), R.drawable.c_five);
        }
    }

    private class ContactCollectorThread extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinKitView.setVisibility(View.VISIBLE);
            Toast.makeText(activity, "onPreExecute", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
//            Toast.makeText(activity, "doInBackground", Toast.LENGTH_SHORT).show();
            getAllContact();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(activity, "onPostExecute", Toast.LENGTH_SHORT).show();
            prepareRecycler();
            Toast.makeText(activity, "all done", Toast.LENGTH_SHORT).show();
            spinKitView.setVisibility(View.GONE);

        }
    }

    private void saveContactToShare() {
        try {
            if (MainActivity.CONTACT != null) {
                /*
                 * it remove all the bitmap in the primary list for reason :
                 * handling LargeTransactionException
                 */
                for (ContactModel item : MainActivity.CONTACT) {
                    item.setPhoto(null);
                }
                Gson gson = new Gson();
                String s = gson.toJson(MainActivity.CONTACT);
                if (sharedPreferences == null) {
                    sharedPreferences = activity.getSharedPreferences(Setting.SHARE_NAME, Context.MODE_PRIVATE);
                }
                if (editor == null) {
                    editor = sharedPreferences.edit();
                }
                editor.putString(Setting.SHARE_CONTACT, s);
                editor.commit();
            }
        } catch (Exception e) {

        }
        if (MainActivity.CONTACT != null && MainActivity.CONTACT.size() != 0) {
            createBitmaps();
        }
    }

    private void createBitmaps() {
        for (ContactModel item : MainActivity.CONTACT) {
            Bitmap bitmap = createDefault();
            InputStream inputStream = null;
            try {
                inputStream = ContactsContract.Contacts.openContactPhotoInputStream(activity.getContentResolver(),
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(item.getId())));
                bitmap = BitmapFactory.decodeStream(inputStream);

            } catch (Exception x) {

            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap == null) {
                bitmap = createDefault();
            }
            item.setPhoto(bitmap);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQCODE){
            boolean t = true;
            for (int  i : grantResults){
                if (i!= PackageManager.PERMISSION_GRANTED){
                    t = false;
                }
            }
            if (t){
                contactController();
            }else{
                Toast.makeText(activity, "دسترسی لازم را به برنامه بدهید !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //------------------------------------- ContactController Part -------------------------------------\\

}

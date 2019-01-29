package ir.vasfa.iman.edgetoolkit.Fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import ir.vasfa.iman.edgetoolkit.Bus.AddApp;
import ir.vasfa.iman.edgetoolkit.Bus.EnableEditMode;
import ir.vasfa.iman.edgetoolkit.Extra.Setting;
import ir.vasfa.iman.edgetoolkit.FloatingWidgetService;
import ir.vasfa.iman.edgetoolkit.MainActivity;
import ir.vasfa.iman.edgetoolkit.R;
import ir.vasfa.iman.edgetoolkit.adapters.AppAdapter;
import ir.vasfa.iman.edgetoolkit.models.AppModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class AppFragment extends android.support.v4.app.Fragment {
    private RecyclerView recyclerView;
    private AppAdapter shortCutAdapter;
    private GridLayoutManager gridLayoutManager;
    private View view;
    private Activity activity;
    private SharedPreferences sharedPreferences = null;
    private SharedPreferences.Editor editor = null;
    public ImageView empty;


    public AppFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_right_side, container, false);
        recyclerView = view.findViewById(R.id.RV);
        empty = view.findViewById(R.id.empty);
        init();
        return view;
    }

    private void init() {
        activity = getActivity();
        if (MainActivity.APPS == null) {
            getAppFromShare();
        }
        createRV();
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activity instanceof MainActivity){
                    ((MainActivity) activity).edit.performClick();
                }
            }
        });
    }

    private void createRV() {
        shortCutAdapter = new AppAdapter(activity, MainActivity.APPS,
                AppAdapter.NORMAL, false, this);
        gridLayoutManager = new GridLayoutManager(activity, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(shortCutAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (MainActivity.APPS == null) {
            getAppFromShare();
        }
    }

    private void getAppFromShare() {
        sharedPreferences = activity.getSharedPreferences(Setting.SHARE_NAME, Context.MODE_PRIVATE);
        try {
            Gson gson = new Gson();
            String share = sharedPreferences.getString(Setting.SHARE_APPS, "");
            MainActivity.APPS = gson.fromJson(share, new TypeToken<ArrayList<AppModel>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ignore null pointer exception !!!
        if (MainActivity.APPS == null) {
            MainActivity.APPS = new ArrayList<>();
        }
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

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
//        save();
    }

    @Subscribe
    public void onEditModeEvent(EnableEditMode enableEditMode) {
        if (shortCutAdapter != null) {
            shortCutAdapter.editMode(enableEditMode.editMode);
        }
    }

    @Subscribe
    public void onAddAppEvent(AddApp add) {
        if (shortCutAdapter != null) {
            shortCutAdapter.notifyDataSetChanged();
        }
    }

    private void save() {
        try {
            if (MainActivity.APPS != null) {
                Gson gson = new Gson();
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
    }

    private ArrayList<String> getAppList() {
        ArrayList<String> list = new ArrayList<>();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> pkgAppsList = getActivity().getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo item : pkgAppsList) {
            list.add((item.activityInfo).packageName);
        }
        return list;
    }

    private ArrayList<AppModel> makeShortcutModel(ArrayList<String> list) {
        ArrayList<AppModel> arrayList = new ArrayList<>();
        for (String item : list) {
            AppModel appModel = new AppModel();
            appModel.setPackageName(item);
            try {
                Drawable icon = activity.getPackageManager().getApplicationIcon(item);
                appModel.setDrawable(icon);//todo
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            try {
                String[] name = item.split("\\.");
                appModel.setName(name[name.length - 1]);
            } catch (Exception e) {
                String s = e.toString();
            }
            arrayList.add(appModel);
        }
        return arrayList;
    }

}

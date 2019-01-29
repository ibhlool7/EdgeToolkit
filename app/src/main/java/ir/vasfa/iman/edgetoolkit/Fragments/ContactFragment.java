package ir.vasfa.iman.edgetoolkit.Fragments;


import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import ir.vasfa.iman.edgetoolkit.Bus.AddContact;
import ir.vasfa.iman.edgetoolkit.Bus.EnableEditMode;
import ir.vasfa.iman.edgetoolkit.Extra.Setting;
import ir.vasfa.iman.edgetoolkit.FloatingWidgetService;
import ir.vasfa.iman.edgetoolkit.MainActivity;
import ir.vasfa.iman.edgetoolkit.R;
import ir.vasfa.iman.edgetoolkit.adapters.ContactListAdapter;
import ir.vasfa.iman.edgetoolkit.models.ContactModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends android.support.v4.app.Fragment {

    private View view;
    private Activity activity;
    private boolean permissionAccepted = false;
    private RecyclerView recyclerView;
    private GridLayoutManager linearLayoutManager;
    private ContactListAdapter adapter;
    private SharedPreferences sharedPreferences = null;
    private SharedPreferences.Editor editor = null;
//    private ArrayList<ContactModel> contactList = new ArrayList<>();
    public ImageView empty;

    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contetnt, container, false);
        activity = getActivity();
        empty = view.findViewById(R.id.empty);
        recyclerView = view.findViewById(R.id.RV);
        init();
        return view;
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser){
//            init();
//        }
//    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
//        save();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (MainActivity.CONTACT == null) {
            getAppFromShare();
        }
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onAddContactEvent(AddContact addContact) {
        adapter.notifyAdapter();

    }

    @Subscribe
    public void onEditModeEvent(EnableEditMode enableEditMode) {
        adapter.editMode(enableEditMode.editMode);
    }

    private void init() {
        if (MainActivity.CONTACT == null) {
            getAppFromShare();
        }
        prepareRecycler();
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activity instanceof MainActivity){
                    ((MainActivity) activity).edit.performClick();
                }
            }
        });
    }

    private void getAppFromShare() {
        sharedPreferences = activity.getSharedPreferences(Setting.SHARE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        try {
            Gson gson = new Gson();
            String share = sharedPreferences.getString(Setting.SHARE_CONTACT, "");
            MainActivity.CONTACT = gson.fromJson(share, new TypeToken<ArrayList<ContactModel>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //ignore null pointer exception !!!
        if (MainActivity.CONTACT == null) {
            MainActivity.CONTACT = new ArrayList<>();
        }
        createBitmaps();
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
            if (bitmap == null){
                bitmap = createDefault();
            }
            item.setPhoto(bitmap);

        }
        String s = "";
    }

    private Bitmap createDefault() {
        Random random = new Random();
        int ran = random.nextInt(5);
//        int ran = 5;
        Bitmap bitmap;
        if (ran == 1) {
            bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.c_one);
        } else if (ran == 2) {
            bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.c_two);
        } else if (ran == 3) {
            bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.c_three);
        } else if (ran == 4) {
            bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.c_four);
        } else {
            bitmap = BitmapFactory.decodeResource(activity.getBaseContext().getResources(), R.drawable.c_five);
        }
        return bitmap;
    }

    private void prepareRecycler() {
        adapter = new ContactListAdapter(activity, MainActivity.CONTACT,
                false, this,ContactListAdapter.NORMAL);
        linearLayoutManager = new GridLayoutManager(activity, 2);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void save() {
        try {
            if (MainActivity.CONTACT != null) {
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
    }

}

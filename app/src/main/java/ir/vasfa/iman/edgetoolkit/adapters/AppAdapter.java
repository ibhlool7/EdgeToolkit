package ir.vasfa.iman.edgetoolkit.adapters;


import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import ir.vasfa.iman.edgetoolkit.Bus.AddApp;
import ir.vasfa.iman.edgetoolkit.Bus.RemoveApp;
import ir.vasfa.iman.edgetoolkit.Fragments.AppFragment;
import ir.vasfa.iman.edgetoolkit.MainActivity;
import ir.vasfa.iman.edgetoolkit.R;
import ir.vasfa.iman.edgetoolkit.models.AppModel;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.LabelHolder> {

    public static int NORMAL = 7;
    public static int EDIT = 85;

    private Activity activity;
    private ArrayList<AppModel> list;
    private LayoutInflater inflater;
    private Boolean showRemoveIcon;
    private Boolean editMode ;
    private int size;
    private Fragment fragment;
    private int which;

    int counter = 0;
    public AppAdapter(Activity activity, ArrayList<AppModel> list, int which ,Boolean showRemoveIcon
            , Fragment fragment) {
        this.activity = activity;
        this.fragment = fragment;
        this.which = which;
        this.list = list;
        inflater = LayoutInflater.from(activity);
        this.showRemoveIcon = showRemoveIcon;
    }

    @NonNull
    @Override
    public LabelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LabelHolder(inflater.inflate(R.layout.row_shortcut, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LabelHolder holder, final int position) {

        if (fragment != null && fragment instanceof AppFragment
                && MainActivity.APPS.size()>0){
            if (((AppFragment) fragment).empty.getVisibility() == View.VISIBLE){
                ((AppFragment) fragment).empty.setVisibility(View.GONE);
            }
        }

        if (showRemoveIcon) {
            holder.remove.setVisibility(View.VISIBLE);
        } else {
            holder.remove.setVisibility(View.GONE);
        }
        try {
            holder.icon.setImageDrawable(list.get(position).getDrawable());
        }catch (Exception e){
            String s = "";
        }
        if (MainActivity.APPS.contains(list.get(position)) && which == EDIT){
            holder.icon.setAlpha(.5f);
        }else{
            holder.icon.setAlpha(1f);
        }
        holder.name.setText(list.get(position).getName());
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (which == AppAdapter.NORMAL && !showRemoveIcon){
                    try {
                        Intent in = activity.getPackageManager().getLaunchIntentForPackage(list.get(position).getPackageName());
                        activity.startActivity(in);
                        activity.finish();
                    }catch (Exception e){
                        String s= e.getMessage();
                    }
                }

                if (which == AppAdapter.EDIT){
                    if (MainActivity.APPS.size() < 10 && !MainActivity.APPS.contains(list.get(position))) {
                        MainActivity.APPS.add(list.get(position));
                        AddApp addApp = new AddApp();
                        addApp.setAdd(true);
                        notifyDataSetChanged();
                        EventBus.getDefault().post(addApp);
                    } else {
                        Toast.makeText(activity, "امکان پذیر نمی باشد .", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (which == NORMAL){
                    MainActivity.APPS.remove(position);
                    RemoveApp removeApp = new RemoveApp();
                    removeApp.setRemove(true);
                    EventBus.getDefault().post(removeApp);
                    notifyDataSetChanged();
                    if (fragment instanceof AppFragment
                            && MainActivity.APPS.size() == 0){
                        ((AppFragment) fragment).empty.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class LabelHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        ImageView remove;
        TextView name;

        LabelHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            remove = itemView.findViewById(R.id.remove);
        }
    }
    public void editMode(Boolean b){
        this.showRemoveIcon = b;
        notifyDataSetChanged();
    }
    public void setNotify(){
        notifyDataSetChanged();
    }
    public void clear(){
        list.clear();
        setNotify();
    }
}




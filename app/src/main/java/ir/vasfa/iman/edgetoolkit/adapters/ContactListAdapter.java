package ir.vasfa.iman.edgetoolkit.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;

import ir.vasfa.iman.edgetoolkit.Bus.AddContact;
import ir.vasfa.iman.edgetoolkit.Bus.RemoveContact;
import ir.vasfa.iman.edgetoolkit.Fragments.ContactFragment;
import ir.vasfa.iman.edgetoolkit.MainActivity;
import ir.vasfa.iman.edgetoolkit.R;
import ir.vasfa.iman.edgetoolkit.models.ContactModel;


public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.LabelHolder> {
    public static int NORMAL = 7;
    public static int EDIT = 85;

    private Activity activity;
    private Fragment fragment;
    private ArrayList<ContactModel> list;
    private LayoutInflater inflater;
    private boolean showRemoveIcon;
    private int which ;

    private ArrayList<Boolean> showDo = new ArrayList<>();

    public ContactListAdapter(Activity activity,
                              ArrayList<ContactModel> list,
                              boolean showRemoveIcon,
                              Fragment fragment,int which) {
        this.activity = activity;
        this.fragment = fragment;
        this.which = which;
        if (which == NORMAL){
            for (ContactModel item:
                 list) {
                showDo.add(false);
            }
        }
        this.showRemoveIcon = showRemoveIcon;
        this.list = list;
        inflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public LabelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LabelHolder(inflater.inflate(R.layout.row_contact, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LabelHolder holder, final int position) {

        if (fragment instanceof ContactFragment && list.size()>0){
            if (((ContactFragment) fragment).empty.getVisibility() == View.VISIBLE){
                ((ContactFragment) fragment).empty.setVisibility(View.GONE);
            }
        }

        if (which == NORMAL && showDo.get(position)){
            holder.rootOfDo.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.GONE);
        }else{
            holder.rootOfDo.setVisibility(View.GONE);
            holder.icon.setVisibility(View.VISIBLE);
        }

        if (showRemoveIcon) {
            holder.remove.setVisibility(View.VISIBLE);
        } else {
            holder.remove.setVisibility(View.GONE);
        }

        if (MainActivity.CONTACT.contains(list.get(position)) && which == EDIT){
            holder.icon.setAlpha(.5f);
        }else{
            holder.icon.setAlpha(1f);
        }

        try{
            holder.icon.setImageBitmap(list.get(position).getPhoto());
        }catch(Exception e){
            String s = e.toString();
        }
        holder.name.setText(list.get(position).getName());
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (which == NORMAL && !showRemoveIcon) {
                    Iterator iterator = showDo.iterator();
                    int i = 0;
                    while(i < showDo.size()){
                        showDo.set(i,false);
                        i++;
                    }
                    showDo.set(position,true);

                    notifyDataSetChanged();
                }
                if (which == EDIT) {
                    if (MainActivity.CONTACT.size() < 10 && !MainActivity.CONTACT.contains(list.get(position))) {
                        MainActivity.CONTACT.add(list.get(position));
                        AddContact addContact = new AddContact();
                        addContact.addContact = true;
                        EventBus.getDefault().post(addContact);
                        notifyAdapter();
                    } else {
                        Toast.makeText(activity, "لیست پر می باشد .", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (which == NORMAL){
                    MainActivity.CONTACT.remove(position);
                    RemoveContact removeContact = new RemoveContact(true);
                    EventBus.getDefault().post(removeContact);
                    notifyAdapter();
                    if (list.size() == 0){
                        ((ContactFragment) fragment).empty.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        holder.sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",
                        list.get(position).getNumber(), null)));
                activity.finish();
            }
        });
        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+list.get(position).getNumber()));
                activity.startActivity(intent);
                activity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void editMode(Boolean b) {

        showRemoveIcon = b;
        if (b){
            showDo.clear();
            for (ContactModel item:
                    list) {
                showDo.add(false);
            }
        }
        notifyDataSetChanged();

    }

    class LabelHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        ImageView remove;
        TextView name;
        ImageView call,sms;
        LinearLayout rootOfDo;

        LabelHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            remove = itemView.findViewById(R.id.remove);
            rootOfDo = itemView.findViewById(R.id.rootOfDo);
            sms = itemView.findViewById(R.id.sms);
            call = itemView.findViewById(R.id.call);
        }
    }
    public void notifyAdapter(){
        if (which == NORMAL){
            showDo.clear();
            for (ContactModel item:
                    list) {
                showDo.add(false);
            }
        }
        notifyDataSetChanged();
    }

    public void clear(){
        list.clear();
        notifyAdapter();
    }


}


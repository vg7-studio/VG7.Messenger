package com.vg7.messenger.adapter;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vg7.messenger.R;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;

import java.util.List;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {

    private Context context;
    private List<UserModel> adminList;

    public AdminAdapter(Context context, List<UserModel> adminList) {
        this.context = context;
        this.adminList = adminList;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_recycler_row, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        UserModel admin = adminList.get(position);
        holder.adminName.setText(admin.getUsername());

        if (!admin.getHideNumberValue()) {
            holder.adminPhoneNumber.setText(admin.getPhone());
        } else {
            holder.adminPhoneNumber.setText("*************");
        }

        FirebaseUtil.getOtherProfilePicStorageRef(admin.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful() && t.getResult() != null) {
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.adminProfilePic);
                    } else {
                        Log.e("RecentChatRecyclerAdapter", "Failed to get profile picture URL");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }

    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        ImageView adminProfilePic;
        TextView adminName;
        TextView adminPhoneNumber;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            adminProfilePic = itemView.findViewById(R.id.profile_pic_image_view);
            adminName = itemView.findViewById(R.id.user_name_text);
            adminPhoneNumber = itemView.findViewById(R.id.phone_text);
        }
    }
}

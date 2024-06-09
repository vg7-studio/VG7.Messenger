package com.vg7.messenger.adapter;

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

import com.bumptech.glide.Glide;
import com.vg7.messenger.GroupDialog;
import com.vg7.messenger.R;
import com.vg7.messenger.model.UserModel;
import com.vg7.messenger.utils.AndroidUtil;
import com.vg7.messenger.utils.FirebaseUtil;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    Context context;
    List<UserModel> memberList;
    GroupDialog dialog;

    public MemberAdapter(Context context, List<UserModel> memberList) {
        this.context = context;
        this.memberList = memberList;
    }

    public MemberAdapter(Context context, GroupDialog dialog, List<UserModel> memberList) {
        this.context = context;
        this.dialog = dialog;
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_recycler_row, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        UserModel member = memberList.get(position);
        holder.memberName.setText(member.getUsername());

        if (!member.getHideNumberValue()) {
            holder.memberPhoneNumber.setText(member.getPhone());
        } else {
            holder.memberPhoneNumber.setText("*************");
        }

        holder.itemView.setOnClickListener(v -> {
            dialog.openUserProfile(member);
        });

        FirebaseUtil.getOtherProfilePicStorageRef(member.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful() && t.getResult() != null) {
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(context, uri, holder.memberProfilePic);
                    } else {
                        Log.e("RecentChatRecyclerAdapter", "Failed to get profile picture URL");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView memberProfilePic;
        TextView memberName, memberPhoneNumber;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberProfilePic = itemView.findViewById(R.id.profile_pic_image_view);
            memberName = itemView.findViewById(R.id.user_name_text);
            memberPhoneNumber = itemView.findViewById(R.id.phone_text);
        }
    }
}

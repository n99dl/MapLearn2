package com.n99dl.maplearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.n99dl.maplearn.R;
import com.n99dl.maplearn.Model.Quest;

import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> {
    private Context mContext;
    private List<Quest> mQuest;

    public QuestAdapter(Context mContext, List<Quest> mQuest) {
        this.mContext = mContext;
        this.mQuest = mQuest;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.quest_item, parent, false);
        return new QuestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Quest quest = mQuest.get(position);
        holder.tv_quest_name.setText(quest.getName());
        if (quest.getImageURL().equals("default")) {
            holder.quest_image.setImageResource(R.mipmap.ic_profile_default);
        } else {
            Glide.with(mContext).load(quest.getImageURL()).into(holder.quest_image);
        }

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(mContext, ProfileActivity.class);
//                intent.putExtra("userid", user.getId());
//                intent.putExtra("profileType", "other");
//                mContext.startActivity(intent);
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return mQuest.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_quest_name;
        public ImageView quest_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_quest_name = itemView.findViewById(R.id.tv_quest_name);
            quest_image = itemView.findViewById(R.id.quest_image);
        }
    }
}

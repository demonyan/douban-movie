package com.demon.doubanmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.demon.doubanmovies.R;
import com.demon.doubanmovies.db.bean.CelebrityEntity;
import com.demon.doubanmovies.db.bean.ImagesEntity;
import com.demon.doubanmovies.db.bean.SimpleActorBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ActorAdapter extends BaseAdapter<ActorAdapter.ViewHolder> {

    private Context mContext;
    private List<SimpleActorBean> mData = new ArrayList<>();

    public ActorAdapter(Context context) {
        mContext = context;
    }

    public void setOnItemClickListener(OnItemClickListener callback) {
        mCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).
                inflate(R.layout.item_simple_actor_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.update();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void update(List<SimpleActorBean> mActorData) {
        mData.clear();
        notifyDataSetChanged();
        for (int i = 0; i < mActorData.size(); i++) {
            mData.add(mActorData.get(i));
            notifyItemInserted(i);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.iv_item_simple_actor_image)
        ImageView imageMovie;
        @Bind(R.id.tv_item_simple_actor_text)
        TextView textTitle;
        @Bind(R.id.tv_item_simple_director_text)
        TextView textDirector;

        SimpleActorBean cardBean;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void update() {
            cardBean = mData.get(getLayoutPosition());
            if (cardBean == null) return;

            CelebrityEntity entity = cardBean.entity;
            if (entity == null) return;

            ImagesEntity imagesEntity = entity.avatars;
            if (imagesEntity != null) {
                // 如果有大图，使用大图
                String url = imagesEntity.large;
                // 如果没有大图，使用中图
                if (url == null) url = imagesEntity.medium;
                // 如果没有中图，使用小图
                if (url == null) url = imagesEntity.small;
                if (url != null)
                    imageLoader.displayImage(url, imageMovie, options);
            }

            textTitle.setText(entity.name);

            if (cardBean.type == 1) {
                textDirector.setText(mContext.getString(R.string.directors));
            } else {
                textDirector.setText("");
            }
        }

        @Override
        public void onClick(View view) {
            int pos = getLayoutPosition();
            if (mCallback != null) {
                mCallback.onItemClick(mData.get(pos).entity.id,
                        mData.get(pos).entity.avatars.large, false);
            }
        }
    }
}
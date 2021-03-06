package com.demon.doubanmovies.adapter.base;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;


public class BaseAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T> {
    protected OnItemClickListener mCallback;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mCallback = listener;
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(T holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public interface OnItemClickListener {
        void onItemClick(String id, String imageUrl, Boolean isFilm);
    }
}

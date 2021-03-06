package com.demon.doubanmovies.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.demon.doubanmovies.R;
import com.demon.doubanmovies.adapter.base.BaseRecyclerAdapter;
import com.demon.doubanmovies.adapter.base.BaseRecyclerHolder;
import com.demon.doubanmovies.model.bean.CelebrityEntity;
import com.demon.doubanmovies.model.bean.SimpleSubjectBean;
import com.demon.doubanmovies.utils.ImageUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SearchAdapter extends BaseRecyclerAdapter<SimpleSubjectBean> {
    private OnItemClickListener mCallback;

    public SearchAdapter(RecyclerView view, Collection<SimpleSubjectBean> datas) {
        super(view, datas, R.layout.item_search_layout);
        setOnItemClickListener((View v, Object data, int position) -> {
            if (mCallback != null) {
                SimpleSubjectBean bean = (SimpleSubjectBean) data;
                String url = ImageUtil.getDisplayImage(mContext, bean.images);
                mCallback.onItemClick(bean.id, url, false);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mCallback = listener;
    }

    @Override
    public void convert(BaseRecyclerHolder holder, SimpleSubjectBean item, int position, boolean isScrolling) {
        // set rating bar
        holder.setRatingBar(R.id.rb_item_search_rating, ((float) item.rating.average) / 2);
        holder.setText(R.id.tv_item_search_rating, String.format("%s ", item.rating.average));

        // set rating count
        StringBuilder comment = new StringBuilder();
        comment.append(mContext.getString(R.string.left_brackets));
        comment.append(String.format(Locale.getDefault(), "%d", item.collect_count));
        comment.append(mContext.getString(R.string.count));
        holder.setText(R.id.tv_item_search_favorite_count, comment.toString());

        // display image
        holder.setRoundImageFromEntity(R.id.iv_item_search_images, item.images);

        holder.setText(R.id.tv_item_search_title, item.title);
        holder.setText(R.id.tv_item_search_content, getBaseInformation(item));
    }

    private String getBaseInformation(SimpleSubjectBean item) {
        StringBuilder infor = new StringBuilder();

        List<String> entries = new ArrayList<>();
        // director
        if (item.directors.size() > 0) {
            infor.append(item.directors.get(0).name);
            infor.append(mContext.getString(R.string.director));
        }

        // actor
        for (CelebrityEntity cast : item.casts) {
            entries.add(cast.name);
        }

        // movie genre
        for (String genre : item.genres) {
            entries.add(genre);
        }

        // movie year
        if (item.year.length() > 0) {
            entries.add(item.year);
        }

        if (entries.size() > 0) {
            String sep = "/";
            for (String entry : entries) {
                infor.append(sep).append(entry);
            }
        }

        return infor.toString();
    }

    public interface OnItemClickListener {
        void onItemClick(String id, String imageUrl, Boolean isFilm);
    }
}

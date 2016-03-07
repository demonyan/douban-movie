package com.demon.doubanmovies.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.demon.doubanmovies.MovieApplication;
import com.demon.doubanmovies.R;
import com.demon.doubanmovies.adapter.ActorAdapter;
import com.demon.doubanmovies.adapter.RecommendMovieAdapter;
import com.demon.doubanmovies.douban.DataManager;
import com.demon.doubanmovies.model.bean.CelebrityEntity;
import com.demon.doubanmovies.model.bean.SimpleActorBean;
import com.demon.doubanmovies.model.bean.SimpleSubjectBean;
import com.demon.doubanmovies.model.bean.SubjectBean;
import com.demon.doubanmovies.model.realm.SimpleSubject;
import com.demon.doubanmovies.utils.BitmapUtil;
import com.demon.doubanmovies.utils.Constant;
import com.demon.doubanmovies.utils.DensityUtil;
import com.demon.doubanmovies.utils.RealmUtil;
import com.demon.doubanmovies.utils.StringUtil;
import com.demon.doubanmovies.widget.ColoredSnackbar;
import com.demon.doubanmovies.widget.RoundedBackgroundSpan;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import rx.Subscriber;

import static android.app.ActivityOptions.makeSceneTransitionAnimation;

public class SubjectActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener,
        AppBarLayout.OnOffsetChangedListener,
        View.OnClickListener {

    // intent中subjectId的key用于查询数据
    private static final String KEY_SUBJECT_ID = "subject_id";
    private static final String KEY_IMAGE_URL = "image_url";
    private static final String URI_FOR_IMAGE = ".png";
    private static final String TAG = "SubjectActivity";


    @Bind(R.id.cl_container)
    CoordinatorLayout mContainer;
    @Bind(R.id.refresh_subject)
    SwipeRefreshLayout mRefresh;
    @Bind(R.id.btn_subject_skip)
    FloatingActionButton mFloatingButton;
    @Bind(R.id.nested_scroll_view)
    NestedScrollView scrollView;
    //movie header
    @Bind(R.id.header_container_subject)
    AppBarLayout mHeaderContainer;
    @Bind(R.id.toolbar_container_subject)
    CollapsingToolbarLayout mToolbarContainer;
    @Bind(R.id.iv_header_subject)
    ImageView mToolbarImage;
    @Bind(R.id.toolbar_subject)
    Toolbar mToolbar;
    @Bind(R.id.rb_subject_rating)
    RatingBar mRatingBar;
    @Bind(R.id.tv_subject_rating)
    TextView mRating;
    @Bind(R.id.tv_subject_favorite_count)
    TextView mCollect;
    @Bind(R.id.tv_subject_title)
    TextView mTitle;
    @Bind(R.id.tv_subject_genres)
    TextView mGenres;
    @Bind(R.id.tv_subject_countries)
    TextView mCountries;
    @Bind(R.id.movie_container_subject)
    LinearLayout mMovieContainer;
    // movie summary
    @Bind(R.id.tv_summary_tip)
    TextView mSummaryTip;
    @Bind(R.id.tv_subject_summary)
    TextView mSummaryText;

    // movie actor
    @Bind(R.id.tv_subject_actor_tip)
    TextView mActorTip;
    @Bind(R.id.re_subject_actor)
    RecyclerView mActorView;

    // movie recommend
    @Bind(R.id.tv_subject_recommend_tip)
    TextView mRecommendTip;
    @Bind(R.id.re_subject_recommend)
    RecyclerView mRecommendView;

    // movie subject
    private String mId;
    private SubjectBean mSubject;

    private ActorAdapter mActorAdapter;

    private String mRecommendTags;
    private RecommendMovieAdapter mRecommendMovieAdapter;
    private boolean isSummaryShow = false;

    private File mFile;
    private boolean isCollect = false;

    private float titleDy = Float.MAX_VALUE;

    public static void toActivity(Activity activity, String id, String imageUrl) {
        Intent intent = new Intent(activity, SubjectActivity.class);
        intent.putExtra(KEY_SUBJECT_ID, id);
        intent.putExtra(KEY_IMAGE_URL, imageUrl);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            activity.startActivity(intent,
                    makeSceneTransitionAnimation(activity).toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    /**
     * activity转场动画
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Transition makeTransition() {
        TransitionSet transition = new TransitionSet();
        transition.addTransition(new Explode());
        transition.addTransition(new Fade());
        transition.setDuration(400);
        return transition;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(makeTransition());
        }

        mId = getIntent().getStringExtra(KEY_SUBJECT_ID);

        initView();
        initEvent();
        initData();
    }

    private void initView() {
        // 设置圆形刷新球的偏移量
        mRefresh.setProgressViewOffset(false,
                -DensityUtil.dp2px(getApplication(), 16f),
                DensityUtil.dp2px(getApplication(), 48f));
        mRefresh.setColorSchemeResources(R.color.green_500);

        // 设置Toolbar
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        mActorView.setLayoutManager(new LinearLayoutManager(SubjectActivity.this,
                LinearLayoutManager.HORIZONTAL, false));
        mActorAdapter = new ActorAdapter(mActorView, null);
        mActorView.setAdapter(mActorAdapter);

        mRecommendView.setLayoutManager(new LinearLayoutManager(
                SubjectActivity.this, LinearLayoutManager.HORIZONTAL, false));
        mRecommendMovieAdapter = new RecommendMovieAdapter(mRecommendView, null);
        mRecommendView.setAdapter(mRecommendMovieAdapter);

        mFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                mId + URI_FOR_IMAGE);
        // 判断从缓存加载图片还是从网络加载图片
        String imageUri = (mFile.exists() ? mFile.getPath() :
                getIntent().getStringExtra(KEY_IMAGE_URL));

        Glide.with(this)
                .load(imageUri)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Bitmap blurBitmap = BitmapUtil.fastBlur(resource, 20);
                        BitmapDrawable drawable = new BitmapDrawable(getResources(), blurBitmap);
                        // 设置 alpha 值降低亮度
                        drawable.setAlpha(192);
                        // 设置 toolbar 模糊背景
                        mToolbarContainer.setBackground(drawable);
                        return false;
                    }
                }).into(mToolbarImage);
    }


    private void initEvent() {
        scrollView.setOnScrollChangeListener((NestedScrollView v, int scrollX,
                                              int scrollY, int oldScrollX, int oldScrollY) -> {
            if (titleDy == Float.MAX_VALUE) {
                /** 计算显示标题需要滑动的距离 */
                titleDy = mTitle.getY() + mTitle.getHeight();
            }

            if (scrollY >= titleDy) {
                if (mSubject.title != null) {
                    mToolbarContainer.setTitle(mSubject.title);
                }
            } else {
                mToolbarContainer.setTitle("");
            }
        });

        mRefresh.setOnRefreshListener(this);
        mFloatingButton.setOnClickListener(this);
        mRecommendMovieAdapter.setOnItemClickListener((String id, String imageUrl, Boolean isFilm) -> {
            if (id == null) {
                Snackbar snackbar = Snackbar.make(mContainer, R.string.no_detail_info, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null);
                // 改变 snack bar 默认颜色
                ColoredSnackbar.alert(snackbar).show();
            }

            if (isFilm) {
                SubjectActivity.toActivity(this, id, imageUrl);
            } else {
                CelebrityActivity.toActivity(this, id);
            }
        });
        mRecommendTip.setOnClickListener(this);
        mRecommendTip.setClickable(false);

        mActorAdapter.setOnItemClickListener((String id, String imageUrl, Boolean isFilm) -> {
            if (id == null) {
                Snackbar snackbar = Snackbar.make(mContainer, R.string.no_detail_info, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null);
                // 改变 SnackBar 默认颜色
                ColoredSnackbar.alert(snackbar).show();
            }

            if (isFilm) {
                SubjectActivity.toActivity(this, id, imageUrl);
            } else {
                CelebrityActivity.toActivity(this, id);
            }
        });

        // 利用 appBarLayout 的回调接口禁止或启用 swipeRefreshLayout
        mHeaderContainer.addOnOffsetChangedListener(this);
    }

    private void initData() {
        SimpleSubject subject = Realm.getDefaultInstance().where(SimpleSubject.class)
                .equalTo(Constant.SIMPLE_SUBJECT_ID, mId).findFirst();
        if (subject != null) {
            subject.getJsonStr();
            mSubject = MovieApplication.gson.fromJson(subject.getJsonStr(), Constant.subType);
        }

        if (mSubject != null) {
            isCollect = true;
        }

        loadSubjectData();
    }

    private void loadSubjectData() {
        mRefresh.setRefreshing(true);
        DataManager.getInstance().getSubjectData(mId)
                .subscribe(new Subscriber<SubjectBean>() {
                    @Override
                    public void onCompleted() {
                        mRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(SubjectActivity.this, e.toString(),
                                Toast.LENGTH_SHORT).show();
                        mRefresh.setRefreshing(false);
                    }

                    @Override
                    public void onNext(SubjectBean subjectBean) {
                        // 如果movie已经收藏,更新数据
                        if (isCollect) saveMovie();
                        mSubject = subjectBean;
                        initAfterGetData();
                    }
                });
    }

    /**
     * 得到网络返回数据初始化界面
     */
    private void initAfterGetData() {
        if (mSubject == null) return;

        if (mSubject.rating != null) {
            float rate = (float) (mSubject.rating.average / 2);
            mRatingBar.setRating(rate);
            mRating.setText(String.format("%s", rate * 2));
        }

        mCollect.setText(getString(R.string.left_brackets));
        mCollect.append(String.format("%s", mSubject.collect_count));
        mCollect.append(getString(R.string.count));
        mTitle.setText(String.format("%s   ", mSubject.title));
        mTitle.append(StringUtil.getSpannableString1(
                String.format("  %s  ", mSubject.year),
                new ForegroundColorSpan(Color.WHITE),
                new RoundedBackgroundSpan(this),
                new RelativeSizeSpan(0.87f)));

        mGenres.setText(StringUtil.getListString(mSubject.genres, '/'));
        mCountries.setText(StringUtil.getSpannableString(
                getString(R.string.countries), ContextCompat.getColor(this, R.color.gray_black_1000)));
        mCountries.append(StringUtil.getListString(mSubject.countries, '/'));

        mSummaryText.setText(StringUtil.getSpannableString(
                getString(R.string.summary), ContextCompat.getColor(this, R.color.gray_500)));
        mSummaryText.append(System.getProperty("line.separator"));
        mSummaryText.append(mSubject.summary);
        mSummaryText.setEllipsize(TextUtils.TruncateAt.END);
        mSummaryText.setOnClickListener(this);
        mSummaryTip.setOnClickListener(this);

        // 获得导演和演员数据列表
        getActorData();

        // 显示View并配上动画
        mMovieContainer.setAlpha(0f);
        mMovieContainer.setVisibility(View.VISIBLE);
        mMovieContainer.animate().alpha(1f).setDuration(800);

        //加载推荐
        mRecommendTip.setText(getString(R.string.recommend_loading));
        StringBuilder tag = new StringBuilder();
        for (int i = 0; i < mSubject.genres.size(); i++) {
            tag.append(mSubject.genres.get(i));
            if (i == 1) break;
        }
        mRecommendTags = tag.toString();
        loadRecommendData();
    }

    /**
     * 获得导演演员的数据
     */
    private void getActorData() {
        mActorTip.setText(getString(R.string.actor_list));
        List<SimpleActorBean> actorData = new ArrayList<>();

        for (CelebrityEntity entity : mSubject.directors) {
            if (entity != null)
                actorData.add(new SimpleActorBean(entity, 1));
        }

        for (CelebrityEntity entity : mSubject.casts) {
            if (entity.id != null)
                actorData.add(new SimpleActorBean(entity, 3));
        }

        mActorAdapter.update(actorData);
        mActorView.setVisibility(View.VISIBLE);
    }

    private void loadRecommendData() {
        DataManager.getInstance().getRecommendData(mRecommendTags)
                .subscribe(new Subscriber<List<SimpleSubjectBean>>() {
                    @Override
                    public void onCompleted() {
                        mRecommendView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError: " + e.toString());
                        mRecommendTip.setText(getString(R.string.recommend_load_fail));
                        mRecommendTip.setClickable(true);
                    }

                    @Override
                    public void onNext(List<SimpleSubjectBean> simpleSubjectBeans) {
                        mRecommendTip.setText(getString(R.string.recommend_list));
                        mRecommendTip.setClickable(false);

                        mRecommendMovieAdapter.update(simpleSubjectBeans);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sub, menu);
        MenuItem favorite = menu.findItem(R.id.action_sub_favorite);
        if (isCollect) {
            favorite.setIcon(R.drawable.ic_action_collected);
        } else {
            favorite.setIcon(R.drawable.ic_action_uncollected);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    this.finishAfterTransition();
                } else {
                    this.finish();
                }
                break;
            case R.id.action_sub_favorite:
                favoriteAndSaveMovie();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 点击收藏后将subject存入数据库中,并将图片存入文件
     */
    private void favoriteAndSaveMovie() {
        if (mSubject == null) return;
        if (isCollect) {
            Snackbar snackbar = Snackbar.make(mContainer, getString(R.string.favorite_cancel), Snackbar.LENGTH_LONG)
                    .setAction("Action", null);
            ColoredSnackbar.info(snackbar).show();

            deleteMovie();
            isCollect = false;
        } else {
            Snackbar snackbar = Snackbar.make(mContainer, getString(R.string.favorite_completed), Snackbar.LENGTH_LONG)
                    .setAction("Action", null);
            ColoredSnackbar.info(snackbar).show();

            saveMovie();
            isCollect = true;
        }
        supportInvalidateOptionsMenu();
    }

    /**
     * 用于保存content和image
     */
    private void saveMovie() {
        if (mFile.exists()) {
            mFile.delete();
        }
        try {
            final FileOutputStream out = new FileOutputStream(mFile);

            new Thread(() -> {
                Bitmap bitmap = null;
                try {
                    bitmap = Glide.with(this)
                            .load(mSubject.images.large)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(-1, -1)       // get full size
                            .get();

                    // 将image保存到缓存
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 将电影信息存入到数据库中
        mSubject.localImageFile = mFile.getPath();
        String content = MovieApplication.gson.toJson(mSubject, Constant.subType);

        RealmUtil.saveRecord(mId, content, Constant.SAVE_FAVORITE);
    }

    private void deleteMovie() {
        // 将数据从数据库中删除
        RealmUtil.deleteRecord(Constant.SIMPLE_SUBJECT_ID, mId);
        // 将保存的海报图片删除
        if (mFile.exists()) {
            mFile.delete();
        }
    }


    /**
     * SwipeRefreshLayout onRefreshListener
     */
    @Override
    public void onRefresh() {
        loadSubjectData();
    }

    /**
     * AppBarLayout onOffsetChangeListener
     */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        // 利用AppBarLayout的回调接口启用或者关闭下拉刷新
        mRefresh.setEnabled(i == 0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_subject_summary:
            case R.id.tv_summary_tip:
                if (isSummaryShow) {
                    isSummaryShow = false;
                    mSummaryText.setEllipsize(TextUtils.TruncateAt.END);
                    // 需要根据实际字数来设置函数
                    mSummaryText.setLines(5);
                    mSummaryTip.setText(getString(R.string.more_info));
                } else {
                    isSummaryShow = true;
                    mSummaryText.setEllipsize(null);
                    mSummaryText.setSingleLine(false);
                    mSummaryTip.setText(getString(R.string.put_away));
                }
                break;
            case R.id.btn_subject_skip://跳往豆瓣电影的移动版网页
                if (mSubject == null)
                    break;
                WebActivity.toWebActivity(this,
                        mSubject.mobile_url, mSubject.title);
                break;
            case R.id.tv_subject_recommend_tip:
                loadRecommendData();
                break;
        }
    }
}

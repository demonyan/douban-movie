package com.demon.doubanmovies.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.demon.doubanmovies.R;

import java.lang.reflect.Method;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SearchMovieView extends LinearLayout {
    static final AutoCompleteTextViewReflector HIDDEN_METHOD_INVOKER = new AutoCompleteTextViewReflector();
    private static final String TAG = "SearchMovieView";
    @Bind(R.id.view_search_src_text)
    SearchAutoComplete mQueryTextView;
    @Bind(R.id.view_search_close_btn)
    ImageView mClearTextButton;
    private boolean mClearingFocus;
    private OnQueryTextListener mOnQueryChangeListener;
    private OnClearButtonListener mOnClearButtonListener;
    private Runnable mShowImeRunnable = () -> {
        InputMethodManager imm = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            HIDDEN_METHOD_INVOKER.showSoftInputUnchecked(imm, SearchMovieView.this, 0);
        }
    };

    private Runnable mUpdateDrawableStateRunnable = () -> {
        updateFocusedState();
    };

    public SearchMovieView(Context context) {
        this(context, null);
    }

    public SearchMovieView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_movie_search, this, true);
        ButterKnife.bind(this);

        mQueryTextView.setSearchView(this);
        mClearTextButton.setOnClickListener((View v) -> {
            if (!TextUtils.isEmpty(mQueryTextView.getText())) {
                mQueryTextView.setText("");
                mQueryTextView.requestFocus();
                if (mOnClearButtonListener != null)
                    mOnClearButtonListener.onClearButtonClick();
                setImeVisibility(true);
            }
        });

        mQueryTextView.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int before, int after) {
                Log.i(TAG, "beforeTextChanged: ");
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int after) {
                Log.i(TAG, "onTextChanged: ");
            }

            public void afterTextChanged(Editable s) {
                Log.i(TAG, "afterTextChanged: ");
                updateCloseButton();
            }
        });

        mQueryTextView.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            onSubmitQuery();
            return true;
        });


        setFocusable(true);
        updateCloseButton();
    }

    static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (mClearingFocus) return false;
        if (!isFocusable()) return false;

        boolean result = mQueryTextView.requestFocus(direction, previouslyFocusedRect);
        if (result) {
            updateCloseButton();
        }

        return result;
    }

    @Override
    public void clearFocus() {
        mClearingFocus = true;
        setImeVisibility(false);
        super.clearFocus();
        mQueryTextView.clearFocus();
        mClearingFocus = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        postUpdateFocusedState();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                width = Math.min(getPreferredWidth(), width);
                break;
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.UNSPECIFIED:
                width = getPreferredWidth();
                break;
        }
        widthMode = MeasureSpec.EXACTLY;
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, widthMode), heightMeasureSpec);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(mUpdateDrawableStateRunnable);
        super.onDetachedFromWindow();
    }

    private int getPreferredWidth() {
        return (int) (320 * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryChangeListener = listener;
    }

    public void setOnQueryTextListener(OnClearButtonListener listener) {
        mOnClearButtonListener = listener;
    }

    private void forceSuggestionQuery() {
        HIDDEN_METHOD_INVOKER.doBeforeTextChanged(mQueryTextView);
        HIDDEN_METHOD_INVOKER.doAfterTextChanged(mQueryTextView);
    }

    void onTextFocusChanged() {
        updateCloseButton();
        postUpdateFocusedState();
        if (mQueryTextView.hasFocus()) {
            forceSuggestionQuery();
        }
    }

    private void updateCloseButton() {
        final boolean hasText = !TextUtils.isEmpty(mQueryTextView.getText());
        mClearTextButton.setVisibility(hasText ? VISIBLE : GONE);
        mClearTextButton.getDrawable().setState(hasText ? ENABLED_STATE_SET : EMPTY_STATE_SET);
    }

    private void postUpdateFocusedState() {
        post(mUpdateDrawableStateRunnable);
    }

    private void updateFocusedState() {
        mQueryTextView.hasFocus();
        invalidate();
    }

    private void setImeVisibility(final boolean visible) {
        if (visible) {
            post(mShowImeRunnable);
        } else {
            removeCallbacks(mShowImeRunnable);
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    public void setQueryHint(CharSequence hint) {
        if (hint != null) {
            mQueryTextView.setHint(getDecoratedHint(hint));
        } else {
            mQueryTextView.setHint(getDecoratedHint(""));
        }
    }

    public void setQueryText(String text) {
        if (text != null) {
            mQueryTextView.setText(text);
            // 点击tag,直接开始搜索
            onSubmitQuery();
        }
    }

    private CharSequence getDecoratedHint(CharSequence hintText) {
        Spannable spannable = new SpannableString(hintText);
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue_200)),
                0, hintText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private void onSubmitQuery() {
        CharSequence query = mQueryTextView.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (mOnQueryChangeListener != null) {
                mOnQueryChangeListener.onQueryTextSubmit(query.toString());
            }
        }
    }

    public interface OnQueryTextListener {
        boolean onQueryTextSubmit(String query);
    }

    public interface OnClearButtonListener {
        boolean onClearButtonClick();
    }

    public static class SearchAutoComplete extends AutoCompleteTextView {
        private int mThreshold;
        private SearchMovieView mSearchView;

        public SearchAutoComplete(Context context, AttributeSet attrs) {
            super(context, attrs);
            mThreshold = getThreshold();
        }

        void setSearchView(SearchMovieView searchView) {
            mSearchView = searchView;
        }

        @Override
        public void setThreshold(int threshold) {
            super.setThreshold(threshold);
            mThreshold = threshold;
        }

        @Override
        protected void replaceText(CharSequence text) {
        }

        @Override
        public void performCompletion() {
        }

        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);

            if (hasWindowFocus && mSearchView.hasFocus() && getVisibility() == VISIBLE) {
                InputMethodManager inputManager = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(this, 0);
                if (isLandscapeMode(getContext())) {
                    HIDDEN_METHOD_INVOKER.ensureImeVisible(this, true);
                }
            }
        }

        @Override
        protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            mSearchView.onTextFocusChanged();
        }

        @Override
        public boolean enoughToFilter() {
            return mThreshold <= 0 || super.enoughToFilter();
        }

        @Override
        public boolean onKeyPreIme(int keyCode, @SuppressWarnings("NullableProblems") KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.startTracking(event, this);
                    }
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.handleUpEvent(event);
                    }
                    if (event.isTracking() && !event.isCanceled()) {
                        mSearchView.clearFocus();
                        mSearchView.setImeVisibility(false);
                        return true;
                    }
                }
            }
            return super.onKeyPreIme(keyCode, event);
        }
    }

    private static class AutoCompleteTextViewReflector {
        private Method doBeforeTextChanged, doAfterTextChanged;
        private Method ensureImeVisible;
        private Method showSoftInputUnchecked;

        AutoCompleteTextViewReflector() {
            try {
                doBeforeTextChanged = AutoCompleteTextView.class
                        .getDeclaredMethod("doBeforeTextChanged");
                doBeforeTextChanged.setAccessible(true);
            } catch (NoSuchMethodException ignored) {

            }
            try {
                doAfterTextChanged = AutoCompleteTextView.class
                        .getDeclaredMethod("doAfterTextChanged");
                doAfterTextChanged.setAccessible(true);
            } catch (NoSuchMethodException ignored) {

            }
            try {
                ensureImeVisible = AutoCompleteTextView.class
                        .getMethod("ensureImeVisible", boolean.class);
                ensureImeVisible.setAccessible(true);
            } catch (NoSuchMethodException ignored) {

            }
            try {
                showSoftInputUnchecked = InputMethodManager.class.getMethod(
                        "showSoftInputUnchecked", int.class, ResultReceiver.class);
                showSoftInputUnchecked.setAccessible(true);
            } catch (NoSuchMethodException ignored) {

            }
        }

        void doBeforeTextChanged(AutoCompleteTextView view) {
            if (doBeforeTextChanged != null) {
                try {
                    doBeforeTextChanged.invoke(view);
                } catch (Exception ignored) {

                }
            }
        }

        void doAfterTextChanged(AutoCompleteTextView view) {
            if (doAfterTextChanged != null) {
                try {
                    doAfterTextChanged.invoke(view);
                } catch (Exception ignored) {

                }
            }
        }

        void ensureImeVisible(AutoCompleteTextView view, boolean visible) {
            if (ensureImeVisible != null) {
                try {
                    ensureImeVisible.invoke(view, visible);
                } catch (Exception ignored) {

                }
            }
        }

        void showSoftInputUnchecked(InputMethodManager imm, View view, int flags) {
            if (showSoftInputUnchecked != null) {
                try {
                    showSoftInputUnchecked.invoke(imm, flags, null);
                    return;
                } catch (Exception ignored) {

                }
            }

            imm.showSoftInput(view, flags);
        }
    }
}

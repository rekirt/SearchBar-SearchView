package com.lapism.searchview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.Size;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


// @RestrictTo(LIBRARY_GROUP)
// @CoordinatorLayout.DefaultBehavior(SearchBehavior.class)
public class SearchView extends FrameLayout implements View.OnClickListener {

    public static final String TAG = SearchView.class.getName();

    private int mTextStyle = Typeface.NORMAL;
    private Typeface mTextFont = Typeface.DEFAULT;


    @Search.Logo
    private int mLogo;

    @Search.Shape
    private int mShape;

    @Search.Theme
    private int mTheme;

    @Search.Version
    private int mVersion;

    @Search.VersionMargins
    private int mVersionMargins;







    @FloatRange(from = SearchArrowDrawable.STATE_HAMBURGER, to = SearchArrowDrawable.STATE_ARROW)
    private float mIsSearchArrowState = SearchArrowDrawable.STATE_HAMBURGER;

    private boolean mGoogle = false;

    private long mAnimationDuration;

    private Context mContext;

    private OnQueryTextListener mOnQueryTextListener;
    private OnOpenCloseListener mOnOpenCloseListener;
    private OnLogoClickListener mOnLogoClickListener;
    private OnMicClickListener mOnMicClickListener;

    private ImageView mImageViewLogo;
    private ImageView mImageViewMic;
    private SearchArrowDrawable mSearchArrowDrawable;
    private View mViewShadow;
    private View mViewDivider;
    private CardView mCardView;
    private LinearLayout mLinearLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerViewAdapter;
    private FlexboxLayout mFlexboxLayout;
    private SearchEditText mSearchEditText;
    private List<Boolean> mSearchFiltersStates;
    private List<SearchFilter> mSearchFilters;

    // ---------------------------------------------------------------------------------------------
    public SearchView(@NonNull Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public SearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public SearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SearchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    // init + kotlin 1.2.1 + 4.4.1 + glide 4.4.0
    // ---------------------------------------------------------------------------------------------
    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        mAnimationDuration = mContext.getResources().getInteger(R.integer.search_animation_duration);

        mSearchArrowDrawable = new SearchArrowDrawable(mContext);

        final TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.SearchView, defStyleAttr, defStyleRes);
        final int layoutResId = a.getResourceId(R.styleable.SearchView_layout_view, R.layout.search_view);

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(layoutResId, this, true);

        mCardView = findViewById(R.id.search_cardView);

        mViewShadow = findViewById(R.id.search_view_shadow);
        mViewShadow.setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_shadow));
        mViewShadow.setOnClickListener(this);

        mLinearLayout = findViewById(R.id.search_linearLayout);

        mImageViewLogo = findViewById(R.id.search_imageView_logo);
        mImageViewLogo.setOnClickListener(this);

        mImageViewMic = findViewById(R.id.search_imageView_mic);
        mImageViewMic.setOnClickListener(this);

        mSearchEditText = findViewById(R.id.search_searchEditText);
        mSearchEditText.setSearchView(this);

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SearchView.this.onTextChanged(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onSubmitQuery();
                return true;
            }
        });
        mSearchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    addFocus();
                } else {
                    removeFocus();
                }
            }
        });

        mViewDivider = findViewById(R.id.search_view_divider);
        mViewDivider.setVisibility(View.GONE);

        mFlexboxLayout = findViewById(R.id.search_flexboxLayout);
        mFlexboxLayout.setVisibility(View.GONE);

        mRecyclerView = findViewById(R.id.search_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        // todo null check+ init
        a.recycle();

        setLogo(Search.Logo.GOOGLE);
        setShape(Search.Shape.DEFAULT);
        setTheme(Search.Theme.COLOR);
        setVersion(Search.Version.TOOLBAR);
        setVersionMargins(Search.VersionMargins.TOOLBAR_SMALL);
    }

    // ---------------------------------------------------------------------------------------------
    @Search.Logo
    public int getLogo() {
        return mLogo;
    }

    public void setLogo(@Search.Logo int logo) {
        mLogo = logo;

        int left, top, right, bottom;

        switch (mLogo) {
            case Search.Logo.GOOGLE:
                left = getResources().getDimensionPixelSize(R.dimen.search_logo_padding_left);
                top = getContext().getResources().getDimensionPixelSize(R.dimen.search_logo_padding_top);
                right = mContext.getResources().getDimensionPixelSize(R.dimen.search_logo_padding_right);
                bottom = 0;

                mImageViewLogo.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_google_color));
                mImageViewLogo.setPadding(left, top, right, bottom);
                break;
            case Search.Logo.G:
                top = 0;
                leftRightG = mContext.getResources().getDimensionPixelSize(R.dimen.search_logo_padding_left_right_view);
                bottom = 0;

                mImageViewLogo.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_g_color_24dp));
                mImageViewLogo.setPadding(left, top, right, bottom);
                break;
            case Search.Logo.HAMBURGER:
                left = mContext.getResources().getDimensionPixelSize(R.dimen.search_logo_padding_left_right_view);
                top = 0;
                right = mContext.getResources().getDimensionPixelSize(R.dimen.search_logo_padding_left_right_view);
                bottom = 0;

                mSearchArrowDrawable = new SearchArrowDrawable(mContext);
                mImageViewLogo.setImageDrawable(mSearchArrowDrawable);
                mImageViewLogo.setPadding(left, top, right, bottom);
                break;
                break;
        }
    }

    @Search.Shape
    public int getShape() {
        return mShape;
    }

    public void setShape(@Search.Shape int shape) {
        mShape = shape;

        switch (mShape) {
            case Search.Shape.DEFAULT:
                mCardView.setRadius(getResources().getDimensionPixelSize(R.dimen.search_shape_default));
                break;
            case Search.Shape.ROUNDED_TOP:
                mCardView.setRadius(getResources().getDimensionPixelSize(R.dimen.search_shape_rounded));
                break;
            case Search.Shape.ROUNDED:
                mCardView.setRadius(getResources().getDimensionPixelSize(R.dimen.search_shape_rounded));
                break;
            case Search.Shape.OVAL:
                mCardView.setRadius(getResources().getDimensionPixelSize(R.dimen.search_shape_oval));
                break;
        }
    }

    @Search.Theme
    public int getTheme() {
        return mTheme;
    }

    public void setTheme(@Search.Theme int theme) {
        setTheme(theme, true);
    }

    public void setTheme(@Search.Theme int theme, boolean tint) {
        mTheme = theme;

        switch (mTheme) {
            case Search.Theme.COLOR:
                break;
            case Search.Theme.LIGHT:
                setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_view_light_background));
                if (tint) {
                    setIconColor(ContextCompat.getColor(mContext, R.color.search_view_light_icon));
                    setHintColor(ContextCompat.getColor(mContext, R.color.search_view_light_hint));
                    setTextColor(ContextCompat.getColor(mContext, R.color.search_view_light_text));
                    setTextHighlightColor(ContextCompat.getColor(mContext, R.color.search_view_light_text_highlight));
                }
                break;
            case Search.Theme.DARK:
                setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_view_dark_background));
                if (tint) {
                    setIconColor(ContextCompat.getColor(mContext, R.color.search_view_dark_icon));
                    setHintColor(ContextCompat.getColor(mContext, R.color.search_view_dark_hint));
                    setTextColor(ContextCompat.getColor(mContext, R.color.search_view_dark_text));
                    setTextHighlightColor(ContextCompat.getColor(mContext, R.color.search_view_dark_text_highlight));
                }
                break;
        }
    }

    @Search.Version
    public int getVersion() {
        return mVersion;
    }

    public void setVersion(@Search.Version int version) {
        mVersion = version;

        if (mVersion == Search.Version.MENU_ITEM) {
            setVisibility(View.GONE);
        }
    }

    @Search.VersionMargins
    public int getVersionMargins() {
        return mVersionMargins;
    }

    public void setVersionMargins(@Search.VersionMargins int versionMargins) {
        mVersionMargins = versionMargins;

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        int left, top, right, bottom;

        switch (mVersionMargins) {
            case Search.VersionMargins.TOOLBAR_SMALL:
                top = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_top);
                left = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_small_left_right);
                right = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_small_left_right);
                bottom = 0;

                params.setMargins(left, top, right, bottom);

                mCardView.setLayoutParams(params);
                break;
            case Search.VersionMargins.TOOLBAR_BIG:
                int topBig = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_top);
                int leftRightBig = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_big_left_right);
                int bottomBig = 0;

                params.setMargins(left, top, right, bottom);

                mCardView.setLayoutParams(params);
                break;
            case Search.VersionMargins.MENU_ITEM:
                int top = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin);
                int leftRightMenu = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin_left_right);
                int bottomMenu = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin);

                params.setMargins(left, top, right, bottom);

                mCardView.setLayoutParams(params);
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------
    /**
     * new SearchDivider(Context)
     * new DividerItemDecoration(Context, DividerItemDecoration.VERTICAL)
     */
    public void addDivider(RecyclerView.ItemDecoration itemDecoration) {
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    /**
     * new SearchDivider(Context)
     * new DividerItemDecoration(Context, DividerItemDecoration.VERTICAL)
     */
    public void removeDivider(RecyclerView.ItemDecoration itemDecoration) {
        mRecyclerView.removeItemDecoration(itemDecoration);
    }

    // ---------------------------------------------------------------------------------------------
    public void showKeyboard() {
        if (!isInEditMode()) {
            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(mSearchEditText, 0); // todo this or edittext
                inputMethodManager.showSoftInput(this, 0);
            }
        }
    }

    public void hideKeyboard() {
        if (!isInEditMode()) {
            InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }













    // ---------------------------------------------------------------------------------------------



    @Size
    public int getCustomHeight() {
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        return params.height;
    }

    public void setCustomHeight(@Size int height) {
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        params.height = height;
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        mLinearLayout.setLayoutParams(params);
    }

    public void setIconColor(@ColorInt int color) {
        // ColorFilter colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);

        mImageViewLogo.setColorFilter(color);
        mImageViewMic.setColorFilter(color);
    }

    public void setHintColor(@ColorInt int color) {
        mSearchEditText.setHintTextColor(color);
    }

    public Editable getText() {
        return mSearchEditText.getText();
    }

    public void setText(@StringRes int text) {
        mSearchEditText.setText(text);
    }

    public void setText(CharSequence text) {
        mSearchEditText.setText(text);
    }

    public void setTextColor(@ColorInt int color) {
        // todo adapter
        mSearchEditText.setTextColor(color);
    }

    public void setTextHighlightColor(@ColorInt int color) {
        // todo adapter
    }

    public void setHint(CharSequence hint) {
        mSearchEditText.setHint(hint);
    }

    public void setHint(@StringRes int hint) {
        mSearchEditText.setHint(hint);
    }

    public void setImeOptions(int imeOptions) {
        mSearchEditText.setImeOptions(imeOptions);
    }

    public void setInputType(int inputType) {
        mSearchEditText.setInputType(inputType);
    }

    public void setLogoIcon(@DrawableRes int resource) {
        mImageViewLogo.setImageResource(resource);
    }

    public void setLogoIcon(@Nullable Drawable drawable) {
        mImageViewLogo.setImageDrawable(drawable);
    }

    /**
     * Typeface.NORMAL
     * Typeface.BOLD
     * Typeface.ITALIC
     * Typeface.BOLD_ITALIC
     */
    public void setTextStyle(int textStyle) {
        // todo adapter
        mSearchEditText.setTypeface((Typeface.create(mTextFont, mTextStyle)));
    }

    /**
     * Typeface.DEFAULT
     * Typeface.DEFAULT_BOLD
     * Typeface.MONOSPACE
     * Typeface.SANS_SERIF
     * Typeface.SERIF
     */
    public void setTextFont(Typeface font) {
        // todo adapter
        mSearchEditText.setTypeface((Typeface.create(mTextFont, mTextStyle)));
    }

    public void setMicIcon(@DrawableRes int resource) {
        mImageViewMic.setImageResource(resource);
    }

    public void setMicIcon(@Nullable Drawable drawable) {
        mImageViewMic.setImageDrawable(drawable);
    }

    public void setSearchItemAnimator(RecyclerView.ItemAnimator itemAnimator) {
        mRecyclerView.setItemAnimator(itemAnimator);
    }

    public RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerViewAdapter = adapter;
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    public void setAnimationDuration(long animationDuration) {
        mAnimationDuration = animationDuration;
    }

    public void setShadow(boolean shadow) {
        if (shadow) {
            mViewShadow.setVisibility(View.VISIBLE);
        } else {
            mViewShadow.setVisibility(View.GONE);
        }
    }

    public void setShadowColor(@ColorInt int color) {
        mViewShadow.setBackgroundColor(color);
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryTextListener = listener;
    }

    public void setOnOpenCloseListener(OnOpenCloseListener listener) {
        mOnOpenCloseListener = listener;
    }

    public void setOnLogoClickListener(OnLogoClickListener listener) {
        mOnLogoClickListener = listener;
    }

    public void setOnMicClickListener(OnMicClickListener listener) {
        mOnMicClickListener = listener;
    }

    @Override
    public void setElevation(float elevation) {
        mCardView.setMaxCardElevation(elevation);
        mCardView.setCardElevation(elevation);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        mCardView.setCardBackgroundColor(color);
    }

    public void setRadius(float radius) {
        mCardView.setRadius(radius);
    }



    public boolean isOpen() {
        return getVisibility() == View.VISIBLE;
    }

    @Override
    public void onClick(View v) {
        if (v == mImageViewLogo) {
            if (mSearchArrowDrawable != null && mIsSearchArrowState == SearchArrowDrawable.STATE_ARROW) {
                // close(true);
            } else {
                if (mOnLogoClickListener != null) {
                    mOnLogoClickListener.onLogoClick(mIsSearchArrowState);
                }
            }
        }

        if (v == mImageViewMic) {
            if (mOnMicClickListener != null) {
                mOnMicClickListener.onMicClick();
                if (mIsSearchArrowState == SearchArrowDrawable.STATE_ARROW) {

                } else {
                    if (mSearchEditText.length() > 0) {
                        mSearchEditText.getText().clear();
                    }
                }
            }
        }

        if (v == mViewShadow) {
            //close true
        }
    }

    // ---------------------------------------------------------------------------------------------
    public interface OnQueryTextListener {
        boolean onQueryTextChange(String newText);

        boolean onQueryTextSubmit(String query);
    }

    public interface OnOpenCloseListener {
        boolean onClose();

        boolean onOpen();
    }

    public interface OnLogoClickListener {
        void onLogoClick(@FloatRange(from = SearchArrowDrawable.STATE_HAMBURGER, to = SearchArrowDrawable.STATE_ARROW) float state);
    }

    public interface OnMicClickListener {
        void onMicClick();
    }

























    private View mMenuItemView;
    private int mMenuItemCx = -1;
    private float mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
    private CharSequence mQuery = "";



    public void setQuery(CharSequence query, boolean submit) {
        mQuery = query;
        mSearchEditText.setText(query);
        mSearchEditText.setSelection(mSearchEditText.length());

        if (!TextUtils.isEmpty(mQuery)) {
            mImageViewMic.setVisibility(View.VISIBLE);
        } else {
            mImageViewMic.setVisibility(View.GONE);
        }

        if (submit) {
            onSubmitQuery();
        }
    }


    public void open(boolean animate) {
        open(animate, null);
    }

    public void open(boolean animate, MenuItem menuItem) {
        if (mVersion == Search.Version.MENU_ITEM) {
            setVisibility(View.VISIBLE);

            if (animate) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (menuItem != null) {
                        getMenuItemPosition(menuItem.getItemId());
                    }
                    mCardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                mCardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                SearchAnimator.revealOpen(
                                        mCardView,
                                        mMenuItemCx,
                                        mAnimationDuration,
                                        mContext,
                                        mSearchEditText,
                                        mOnOpenCloseListener);
                            }
                        }
                    });
                } else {
                    SearchAnimator.fadeOpen(
                            mCardView,
                            mAnimationDuration,
                            mSearchEditText,
                            mOnOpenCloseListener);
                }
            } else {
                mCardView.setVisibility(View.VISIBLE);
                if (mOnOpenCloseListener != null) {
                    mOnOpenCloseListener.onOpen();
                }
                if (mSearchEditText.length() > 0) {
                    mSearchEditText.getText().clear();
                }
                mSearchEditText.requestFocus();
            }
        }

        if (mVersion == Search.Version.TOOLBAR) {
            if (mSearchEditText.length() > 0) {
                mSearchEditText.getText().clear();
            }
            mSearchEditText.requestFocus();
        }
    }

    public void open(boolean animate) {
        open(animate, null);
    }

    public void close(boolean animate) {
        close(animate, null);
    }

    public void close(boolean animate, MenuItem menuItem) {
        switch (mVersion) {
            case Search.Version.MENU_ITEM:
                if (animate) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (menuItem != null) {
                            getMenuItemPosition(menuItem.getItemId());
                        }
                        SearchAnimator.revealClose(
                                mCardView,
                                mMenuItemCx,
                                mAnimationDuration,
                                mContext,
                                mSearchEditText,
                                this,
                                mOnOpenCloseListener);
                    } else {
                        SearchAnimator.fadeClose(
                                mCardView,
                                mAnimationDuration,
                                mSearchEditText,
                                this,
                                mOnOpenCloseListener);
                    }
                } else {
                    if (mSearchEditText.length() > 0) {
                        mSearchEditText.getText().clear();
                    }
                    mSearchEditText.clearFocus();
                    mCardView.setVisibility(View.GONE);
                    setVisibility(View.GONE);
                    if (mOnOpenCloseListener != null) {
                        mOnOpenCloseListener.onClose();
                    }
                }
                break;
            case Search.Version.TOOLBAR:
                if (mSearchEditText.length() > 0) {
                    mSearchEditText.getText().clear();
                }
                mSearchEditText.clearFocus();
                break;
        }
    }

    public void setFilters(@Nullable List<SearchFilter> filters) {
        mSearchFilters = filters;
        mFlexboxLayout.removeAllViews();
        if (filters == null) {
            mSearchFiltersStates = null;
            mFlexboxLayout.setVisibility(View.GONE);
        } else {
            mSearchFiltersStates = new ArrayList<>();
            for (SearchFilter filter : filters) {
                AppCompatCheckBox checkBox = new AppCompatCheckBox(mContext);
                checkBox.setText(filter.getTitle());
                checkBox.setTextSize(12);
                checkBox.setTextColor(mTextColor);
                checkBox.setChecked(filter.isChecked());

                FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(getResources().getDimensionPixelSize(R.dimen.search_filter_margin_start), getResources().getDimensionPixelSize(R.dimen.search_filter_margin_top), getResources().getDimensionPixelSize(R.dimen.search_filter_margin_top), getResources().getDimensionPixelSize(R.dimen.search_filter_margin_top));

                checkBox.setLayoutParams(lp);
                checkBox.setTag(filter.getTagId());
                mFlexboxLayout.addView(checkBox);
                mSearchFiltersStates.add(filter.isChecked());
            }
        }
    }

    public List<SearchFilter> getSearchFilters() {
        if (mSearchFilters == null) {
            return new ArrayList<>();
        }

        dispatchFilters();

        List<SearchFilter> searchFilters = new ArrayList<>();
        for (SearchFilter filter : mSearchFilters) {
            searchFilters.add(new SearchFilter(filter.getTitle(), filter.isChecked(), filter.getTagId()));
        }

        return searchFilters;
    }

    public List<Boolean> getFiltersStates() {
        return mSearchFiltersStates;
    }

    public void addFocus() {
        if (mArrow) {
            mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_ARROW;
        } else {
            if (mSearchArrowDrawable != null) {
                mSearchArrowDrawable.setVerticalMirror(false);
                mSearchArrowDrawable.animate(SearchArrowDrawable.STATE_ARROW, mAnimationDuration);
                mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_ARROW;
            }
        }

        if (mViewShadow.getVisibility() == View.VISIBLE) { // TODO: 28.12.17
            SearchAnimator.fadeIn(mViewShadow, mAnimationDuration);
        }

        if (!TextUtils.isEmpty(mQuery)) {
            mImageViewMic.setVisibility(View.VISIBLE);
        }

        showKeyboard();
        showSuggestions();

        if (mVersion == Search.Version.TOOLBAR) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mOnOpenCloseListener != null) {
                        mOnOpenCloseListener.onOpen();
                    }
                }
            }, mAnimationDuration);
        }
    }

    public void removeFocus() {
        if (mArrow) {
            mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
        } else {
            if (mSearchArrowDrawable != null) {
                mSearchArrowDrawable.setVerticalMirror(true);
                mSearchArrowDrawable.animate(SearchArrowDrawable.STATE_HAMBURGER, mAnimationDuration);
                mIsSearchArrowHamburgerState = SearchArrowDrawable.STATE_HAMBURGER;
            }
        }

        if (mShadow) {
            SearchAnimator.fadeOut(mViewShadow, mAnimationDuration);
        }

        if (TextUtils.isEmpty(mQuery)) {
            mImageViewMic.setVisibility(View.GONE);
            mImageViewMic.setVisibility(View.VISIBLE);
        }

        hideKeyboard();
        hideSuggestions();

        if (mVersion == Search.Version.TOOLBAR) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mOnOpenCloseListener != null) {
                        mOnOpenCloseListener.onClose();
                    }
                }
            }, mAnimationDuration);
        }
    }

    public void showSuggestions() {
        if (mFlexboxLayout.getChildCount() > 0 && mFlexboxLayout.getVisibility() == View.GONE) {
            mViewDivider.setVisibility(View.VISIBLE);
            mFlexboxLayout.setVisibility(View.VISIBLE);
        }

        if (mRecyclerViewAdapter != null && mRecyclerViewAdapter.getItemCount() > 0) {
            mViewDivider.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            SearchAnimator.fadeIn(mRecyclerView, mAnimationDuration);
        }
    }

    public void hideSuggestions() {
        if (mFlexboxLayout.getVisibility() == View.VISIBLE) {
            mViewDivider.setVisibility(View.GONE);
            mFlexboxLayout.setVisibility(View.GONE);
        }

        if (mRecyclerViewAdapter != null) {
            mViewDivider.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            SearchAnimator.fadeOut(mRecyclerView, mAnimationDuration);
        }
    }

    public void setTextSize(float size) {
        mSearchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }


    public void setNavigationIconAnimation(boolean animate) {
        if (!animate) {
            mSearchArrowDrawable.setProgress(SearchArrowDrawable.STATE_ARROW);
        }
    }


    public void setSuggestionsList(List<SearchItem> suggestionsList) {
        if (mRecyclerViewAdapter instanceof SearchAdapter) {
            ((SearchAdapter) mRecyclerViewAdapter).setSuggestionsList(suggestionsList);
        }
    }

    //krishkapil filter  listener

    private void restoreFiltersState(List<Boolean> states) {
        mSearchFiltersStates = states;
        for (int i = 0, j = 0, n = mFlexboxLayout.getChildCount(); i < n; i++) {
            View view = mFlexboxLayout.getChildAt(i);
            if (view instanceof AppCompatCheckBox) {
                ((AppCompatCheckBox) view).setChecked(mSearchFiltersStates.get(j++));
            }
        }
    }

    private void getMenuItemPosition(int menuItemId) {
        if (mMenuItemView != null) {
            mMenuItemCx = getCenterX(mMenuItemView);
        }
        ViewParent viewParent = getParent();
        while (viewParent != null && viewParent instanceof View) {
            View parent = (View) viewParent;
            View view = parent.findViewById(menuItemId);
            if (view != null) {
                mMenuItemView = view;
                mMenuItemCx = getCenterX(mMenuItemView);
                break;
            }
            viewParent = viewParent.getParent();
        }
    }

    private int getCenterX(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[0] + view.getWidth() / 2;
    }

    private void isVoiceAvailable() {
        if (isInEditMode()) {
            return;//break continue + krisnha filter listener
        }
    }

    private void onTextChanged(CharSequence s) {
        mQuery = s;


        CharSequence query = "";
        if (!TextUtils.isEmpty(query)) {
            //onSubmitQuery();
        }

        if (mRecyclerViewAdapter != null && mRecyclerViewAdapter instanceof Filterable) {
            final CharSequence mFilterKey = s.toString().toLowerCase(Locale.getDefault());
            ((SearchAdapter) mRecyclerViewAdapter).getFilter().filter(s, new Filter.FilterListener() {
                @Override
                public void onFilterComplete(int i) {
                    if (mFilterKey.equals(((SearchAdapter) mRecyclerViewAdapter).getKey())) {
                        if (i > 0) {
                            if (mRecyclerView.getVisibility() == View.GONE) {
                                mViewDivider.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.VISIBLE);
                                SearchAnimator.fadeIn(mRecyclerView, mAnimationDuration);
                            }
                        } else {
                            if (mRecyclerView.getVisibility() == View.VISIBLE) {
                                mViewDivider.setVisibility(View.GONE);
                                mRecyclerView.setVisibility(View.GONE);
                                SearchAnimator.fadeOut(mRecyclerView, mAnimationDuration);
                            }
                        }
                    }
                }
            });
        }

        if (!TextUtils.isEmpty(s)) {
            mImageViewMic.setImageResource(R.drawable.ic_clear_black_24dp);
        } else {
            if (mGoogle) {
                mImageViewMic.setImageResource(R.drawable.ic_mic_color_24dp);
            } else {
                mImageViewMic.setImageResource(R.drawable.ic_mic_black_24dp);
                // TODO BARVY
            }
        }

        if (mOnQueryTextListener != null) {
            dispatchFilters();
            mOnQueryTextListener.onQueryTextChange(s.toString());
        }
    }

    private void onSubmitQuery() {
        CharSequence query = mSearchEditText.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            dispatchFilters();
            if (mOnQueryTextListener == null || !mOnQueryTextListener.onQueryTextSubmit(query.toString())) {
                mSearchEditText.setText(query);
            }
        }
    }

    private void dispatchFilters() {
        if (mSearchFiltersStates != null) {
            for (int i = 0, j = 0, n = mFlexboxLayout.getChildCount(); i < n; i++) {
                View view = mFlexboxLayout.getChildAt(i);
                if (view instanceof AppCompatCheckBox) {
                    boolean isChecked = ((AppCompatCheckBox) view).isChecked();
                    mSearchFiltersStates.set(j, isChecked);
                    mSearchFilters.get(j).setChecked(isChecked);
                    j++;
                }
            }
        }
    }





    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());

        bundle.putCharSequence("query", mQuery);
        bundle.putBoolean("isSearchOpen", getVisibility() == View.VISIBLE);

        dispatchFilters();
        ArrayList<Integer> searchFiltersStatesInt = null;
        if (mSearchFiltersStates != null) {
            searchFiltersStatesInt = new ArrayList<>();
            for (Boolean bool : mSearchFiltersStates) {
                searchFiltersStatesInt.add((bool) ? 1 : 0);
            }
        }
        bundle.putIntegerArrayList("searchFiltersStates", searchFiltersStatesInt);

        ArrayList<SearchFilter> searchFilters = null;
        if (mSearchFilters != null) {
            searchFilters = new ArrayList<>();
            searchFilters.addAll(mSearchFilters);
        }
        bundle.putParcelableArrayList("searchFilters", searchFilters);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            mQuery = bundle.getCharSequence("query");
            if (bundle.getBoolean("isSearchOpen")) {
                open(true);
                setQuery(mQuery, false);
                mSearchEditText.requestFocus();
            }

            ArrayList<Integer> searchFiltersStatesInt = bundle.getIntegerArrayList("searchFiltersStates");
            ArrayList<Boolean> searchFiltersStatesBool = null;
            if (searchFiltersStatesInt != null) {
                searchFiltersStatesBool = new ArrayList<>();
                for (Integer value : searchFiltersStatesInt) {
                    searchFiltersStatesBool.add(value == 1);
                }
            }
            restoreFiltersState(searchFiltersStatesBool);

            mSearchFilters = bundle.getParcelableArrayList("searchFilters");

            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }


          /*  for (int i = 0, n = mFlexboxLayout.getChildCount(); i < n; i++) {
            View child = mFlexboxLayout.getChildAt(i);
            if (child instanceof AppCompatCheckBox) {
                ((AppCompatCheckBox) child).setTextColor(mTextColor);
            }
        }*/

    // todo doplnit hodnoty
    private void initStyle(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.SearchView, defStyleAttr, defStyleRes);
        if (a != null) {/// todo
            if (a.hasValue(R.styleable.SearchView_search_custom_height)) {
                setCustomHeight(a.getDimensionPixelSize(R.styleable.SearchView_search_custom_height, mContext.getResources().getDimensionPixelSize(R.dimen.search_height)));
            }
            if (a.hasValue(R.styleable.SearchView_search_version)) {
                setVersion(a.getInt(R.styleable.SearchView_search_version, Version.TOOLBAR));
            }
            if (a.hasValue(R.styleable.SearchView_search_version_margins)) {
                setVersionMargins(a.getInt(R.styleable.SearchView_search_version_margins, VersionMargins.TOOLBAR_SMALL));
            }
            if (a.hasValue(R.styleable.SearchView_search_theme)) {
                setTheme(a.getInt(R.styleable.SearchView_search_theme, Theme.LIGHT));
            }
            if (a.hasValue(R.styleable.SearchView_search_navigation_icon)) {
                setNavigationIcon(a.getResourceId(R.styleable.SearchView_search_navigation_icon, 0));
            }
            if (a.hasValue(R.styleable.SearchView_search_icon_color)) {
                setIconColor(a.getColor(R.styleable.SearchView_search_icon_color, Color.BLACK));
            }
            if (a.hasValue(R.styleable.SearchView_search_background_color)) {
                setBackgroundColor(a.getColor(R.styleable.SearchView_search_background_color, Color.WHITE));
            }
            if (a.hasValue(R.styleable.SearchView_search_text_color)) {
                setTextColor(a.getColor(R.styleable.SearchView_search_text_color, Color.BLACK));
            }
            if (a.hasValue(R.styleable.SearchView_search_text_highlight_color)) {
                setTextHighlightColor(a.getColor(R.styleable.SearchView_search_text_highlight_color, Color.GRAY));
            }
            if (a.hasValue(R.styleable.SearchView_search_text_size)) {
                setTextSize(a.getDimension(R.styleable.SearchView_search_text_size, mContext.getResources().getDimension(R.dimen.search_text_16)));
            }
            if (a.hasValue(R.styleable.SearchView_search_text_style)) {
                setTextStyle(agetInt(R.styleable.SearchView_search_text_style, Typeface.NORMAL));
            }
            if (a.hasValue(R.styleable.SearchView_search_hint)) {
                setHint(a.getString(R.styleable.SearchView_search_hint));
            }
            if (a.hasValue(R.styleable.SearchView_search_hint_color)) {
                setHintColor(a.getColor(R.styleable.SearchView_search_hint_color, Color.BLACK));
            }
            if (a.hasValue(R.styleable.SearchView_search_animation_duration)) {
                setAnimationDuration(a.getInteger(R.styleable.SearchView_search_animation_duration, mAnimationDuration));
            }
            if (a.hasValue(R.styleable.SearchView_search_shadow)) {
                setShadow(a.getBoolean(R.styleable.SearchView_search_shadow, true));
            }
            if (a.hasValue(R.styleable.SearchView_search_shadow_color)) {
                setShadowColor(a.getColor(R.styleable.SearchView_search_shadow_color, mContext.getResources().getColor(R.color.search_shadow)));
            }
            if (a.hasValue(R.styleable.SearchView_search_elevation)) {
                setElevation(a.getDimensionPixelSize(R.styleable.SearchView_search_elevation, mContext.getResources().getDimensionPixelSize(R.dimen.search_elevation));
            }
            if (a.hasValue(R.styleable.SearchView_search_google_icons) {
                setGoogleIcons(a.getBoolean(R.styleable.SearchView_search_google_icons, true));
            }
            a.recycle();
        }
    }



}


// Kotlinize + NULLABLE
/*
todo
or a onFilterClickListener method is fine
*/// int id = view.getId();
// this(context, null);



// aj




// ---------------------------------------------------------------------------------------------
    /*@ColorInt
    //@Contract(pure = true)
    public static int getIconColor() {
        return mIconColor;
    }*/

// ontrola anotaci
// https://stackoverflow.com/questions/35625247/android-is-it-ok-to-put-intdef-values-inside-interface
// https://developer.android.com/reference/android/support/annotation/FloatRange.html
// ViewCompat.setBackground
// mSearchButton.setImageDrawable(a.getDrawable(R.styleable.SearchView_searchIcon));

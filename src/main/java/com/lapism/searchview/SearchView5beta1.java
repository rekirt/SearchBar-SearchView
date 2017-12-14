package com.lapism.searchview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import org.jetbrains.annotations.Contract;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;


// @RestrictTo(LIBRARY_GROUP)
// @CoordinatorLayout.DefaultBehavior(SearchBehavior.class)
public class SearchView5beta1 extends FrameLayout implements View.OnClickListener {

    public static final String TAG = SearchView5beta1.class.getName();

    private static int mIconColor = Color.DKGRAY;
    private static int mTextColor = Color.BLACK;
    private static int mTextHighlightColor = Color.GRAY;
    private static int mTextStyle = Typeface.NORMAL;
    private static Typeface mTextFont = Typeface.DEFAULT;

    @Version
    private int mVersion = Version.TOOLBAR;

    @VersionMargins
    private int mVersionMargins = VersionMargins.TOOLBAR_SMALL;

    @Theme
    private int mTheme = Theme.LIGHT;

    @FloatRange(from = SearchArrowDrawable.STATE_HAMBURGER, to = SearchArrowDrawable.STATE_ARROW)
    private float mIsSearchArrowState = SearchArrowDrawable.STATE_HAMBURGER;

    private boolean mGoogle = false;

    private long mAnimationDuration;

    private OnQueryTextListener mOnQueryTextListener;
    private OnOpenCloseListener mOnOpenCloseListener;
    private OnNavigationClickListener mOnNavigationClickListener;
    private OnMicClickListener mOnMicClickListener;
    private OnMenuClickListener mOnMenuClickListener;

    private ImageView mImageViewNavigation;
    private ImageView mImageViewMic;
    private ImageView mImageViewMenu;
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

    private Context mContext;

    // https://stackoverflow.com/questions/35625247/android-is-it-ok-to-put-intdef-values-inside-interface
    // https://developer.android.com/reference/android/support/annotation/FloatRange.html
    // ---------------------------------------------------------------------------------------------
    public SearchView5beta1(@NonNull Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public SearchView5beta1(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public SearchView5beta1(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SearchView5beta1(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    // ---------------------------------------------------------------------------------------------
    @ColorInt
    @Contract(pure = true)
    public static int getIconColor() {
        return mIconColor;
    }

    @ColorInt
    @Contract(pure = true)
    public static int getTextColor() {
        return mTextColor;
    }

    @ColorInt
    @Contract(pure = true)
    public static int getTextHighlightColor() {
        return mTextHighlightColor;
    }

    @Contract(pure = true)
    public static int getTextStyle() {
        return mTextStyle;
    }

    /**
     * Typeface.NORMAL
     * Typeface.BOLD
     * Typeface.ITALIC
     * Typeface.BOLD_ITALIC
     */
    public void setTextStyle(int textStyle) {
        mTextStyle = textStyle;
        mSearchEditText.setTypeface((Typeface.create(mTextFont, mTextStyle)));
    }

    @Contract(pure = true)
    public static Typeface getTextFont() {
        return mTextFont;
    }

    /**
     * Typeface.DEFAULT
     * Typeface.DEFAULT_BOLD
     * Typeface.MONOSPACE
     * Typeface.SANS_SERIF
     * Typeface.SERIF
     */
    public void setTextFont(Typeface font) {
        mTextFont = font;
        mSearchEditText.setTypeface((Typeface.create(mTextFont, mTextStyle)));
    }

    // ViewCompat.setBackground
    // mSearchButton.setImageDrawable(a.getDrawable(R.styleable.SearchView_searchIcon));
    // ---------------------------------------------------------------------------------------------
    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        mAnimationDuration = mContext.getResources().getInteger(R.integer.search_animation_duration);

        mSearchArrowDrawable = new SearchArrowDrawable(mContext);

        final TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.SearchView, defStyleAttr, defStyleRes);
        final int layoutResId = a.getResourceId(R.styleable.SearchView_layout, R.layout.search_view);

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(layoutResId, this, true);

        mViewShadow = findViewById(R.id.search_view_shadow);
        mViewShadow.setBackgroundColor(ContextCompat.getColor(mContext, R.color.search_shadow));
        mViewShadow.setOnClickListener(this);

        mCardView = findViewById(R.id.search_cardView);
        mLinearLayout = findViewById(R.id.search_linearLayout);

        mImageViewNavigation = findViewById(R.id.search_imageView_navigation);
        mImageViewNavigation.setOnClickListener(this);

        mImageViewMic = findViewById(R.id.search_imageView_mic);
        mImageViewMic.setOnClickListener(this);

        mImageViewMenu = findViewById(R.id.search_imageView_menu);
        mImageViewMenu.setVisibility(View.GONE);

        mSearchEditText = findViewById(R.id.search_searchEditText);
        // mSearchEditText.setSearchView(this);

        // init + kotlin 1.2.1 + 4.4 + glide
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SearchView5beta1.this.onTextChanged(s);
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
        // null check
        a.recycle();
    }

    // ---------------------------------------------------------------------------------------------
    public void onTextChanged(CharSequence s){

    }

    public void onSubmitQuery() {
        CharSequence query = "";
        if (!TextUtils.isEmpty(query)) {
            //onSubmitQuery();
            // kontrola vsech listeneru podle nazvu
        }
    }

    public void addFocus() {

    }

    public void removeFocus() {

    }

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
    @Version
    public int getVersion() {
        return mVersion;
    }

    @VersionMargins
    public int getVersionMargins() {
        return mVersionMargins;
    }

    public void setVersionMargins(@VersionMargins int versionMargins) {
        mVersionMargins = versionMargins;

        switch (mVersionMargins) {
            case VersionMargins.TOOLBAR_SMALL:
                int top_small = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_top);
                int leftRight_small = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_small_left_right);
                int bottom_small = 0;

                LayoutParams params_small = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params_small.setMargins(leftRight_small, top_small, leftRight_small, bottom_small);

                mCardView.setLayoutParams(params_small);
                break;
            case VersionMargins.TOOLBAR_BIG:
                int top_big = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_top);
                int leftRight_big = mContext.getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin_big_left_right);
                int bottom_big = 0;

                LayoutParams params_big = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params_big.setMargins(leftRight_big, top_big, leftRight_big, bottom_big);

                mCardView.setLayoutParams(params_big);
                break;
            case VersionMargins.MENU_ITEM:
                int top_menu = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin);
                int leftRight_menu = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin_left_right);
                int bottom_menu = mContext.getResources().getDimensionPixelSize(R.dimen.search_menu_item_margin);

                LayoutParams params_menu = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params_menu.setMargins(leftRight_menu, top_menu, leftRight_menu, bottom_menu);

                mCardView.setLayoutParams(params_menu);
                break;
        }
    }

    @Theme
    public int getTheme() {
        return mTheme;
    }

    public int getCustomHeight() {
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        return params.height;
    }

    public void setCustomHeight(int height) {
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        params.height = height;
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        mLinearLayout.setLayoutParams(params);
    }

    public void setHintColor(@ColorInt int color) {
        mSearchEditText.setHintTextColor(color);
    }

    public Editable getText() {
        return mSearchEditText.getText();
    }

    public void setText(CharSequence text) {
        mSearchEditText.setText(text);
    }

    public void setText(@StringRes int text) {
        mSearchEditText.setText(text);
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

    public void setNavigationIcon(@DrawableRes int resource) {
        mImageViewNavigation.setImageResource(resource);
    }

    public void setNavigationIcon(@Nullable Drawable drawable) {
        mImageViewNavigation.setImageDrawable(drawable);
    }

    public void setMenuIcon(@DrawableRes int resource) {
        mImageViewMenu.setImageResource(resource);
    }

    public void setMenuIcon(@Nullable Drawable drawable) {
        mImageViewMenu.setImageDrawable(drawable);
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

    public void setOnNavigationClickListener(OnNavigationClickListener listener) {
        mOnNavigationClickListener = listener;
    }

    public void setOnMicClickListener(OnMicClickListener listener) {
        mOnMicClickListener = listener;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        if (listener != null) {
            mImageViewMenu.setImageResource(R.drawable.ic_menu_black_24dp);
            mImageViewMenu.setOnClickListener(this);
            mImageViewMenu.setVisibility(View.VISIBLE);
        }

        mOnMenuClickListener = listener;
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

    public boolean isOpen() {
        return getVisibility() == View.VISIBLE;
    }

    /*
     tried card_view:cardUseCompatPadding="true" but no avail. Didn't work!

Then I discovered from a stackoverflow post this card_view:cardPreventCornerOverlap="false"
    */

    @Override
    public void onClick(View v) {
        if (v == mImageViewNavigation) {
            if (mSearchArrowDrawable != null && mIsSearchArrowState == SearchArrowDrawable.STATE_ARROW) {
                // close(true);
            } else {
                if (mOnNavigationClickListener != null) {
                    mOnNavigationClickListener.onNavigationClick(mIsSearchArrowState);
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

        if (v == mImageViewMenu) {
            if (mOnMenuClickListener != null) {
                mOnMenuClickListener.onMenuClick();
            }
        }

        if (v == mViewShadow) {
            //close true
        }
    }

    // ---------------------------------------------------------------------------------------------
    @IntDef({Version.TOOLBAR, Version.MENU_ITEM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Version {
        int TOOLBAR = 1000;
        int MENU_ITEM = 1001;
    }

    @IntDef({VersionMargins.TOOLBAR_SMALL, VersionMargins.TOOLBAR_BIG, VersionMargins.MENU_ITEM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface VersionMargins {
        int TOOLBAR_SMALL = 2000;
        int TOOLBAR_BIG = 2001;
        int MENU_ITEM = 2002;
    }

    @IntDef({Theme.LIGHT, Theme.DARK, Theme.PLAY_STORE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Theme {
        int LIGHT = 3000;
        int DARK = 3001;
        int PLAY_STORE = 3002;
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

    public interface OnNavigationClickListener {
        void onNavigationClick(@FloatRange(from = SearchArrowDrawable.STATE_HAMBURGER, to = SearchArrowDrawable.STATE_ARROW) float state);
    }

    public interface OnMicClickListener {
        void onMicClick();
    }

    public interface OnMenuClickListener {
        void onMenuClick();
    }

}

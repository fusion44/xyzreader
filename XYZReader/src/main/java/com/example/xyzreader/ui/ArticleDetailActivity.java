package com.example.xyzreader.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.Transition;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_ITEM_ID = "ARTICLE_ITEM_URI";
    public static final String EXTRA_ITEM_IMG_URL = "ARTICLE_ITEM_IMG_URL";
    public static final String EXTRA_CURSOR_POSITION = "ARTICLE_ITEM_POSITION";
    private static final String TAG = ArticleDetailActivity.class.getSimpleName();

    private Cursor mCursor;
    private long mStartId;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private ImageView mPhotoView;
    private boolean mIsReturning;
    private String mCurrentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        setupTransitions();

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        if (t != null) {
            setSupportActionBar(t);
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    mCurrentUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
                    updateAppBarImage();
                }
            }
        });

        mPhotoView = (ImageView) this.findViewById(R.id.article_backdrop);

        if (savedInstanceState == null) {
            if (getIntent() != null) {
                mStartId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);

                mCurrentUrl = getIntent().getStringExtra(EXTRA_ITEM_IMG_URL);
                if (!mCurrentUrl.equals("")) {
                    updateAppBarImage();
                }
            }
        }
    }

    private void setupTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterSharedElementCallback(new SharedElementCallback() {
                @SuppressLint("NewApi") @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (mIsReturning) {
                        names.clear();
                        sharedElements.clear();
                        names.add(mPhotoView.getTransitionName());
                        sharedElements.put(mPhotoView.getTransitionName(), mPhotoView);
                    }
                }

                @SuppressLint("NewApi") @Override
                public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements,
                                                 List<View> sharedElementSnapshots) {
                    if (!mIsReturning) {
                        getWindow().setEnterTransition(makeEnterTransition());
                    }
                }

                @SuppressLint("NewApi") @Override
                public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements,
                                               List<View> sharedElementSnapshots) {
                    if (mIsReturning) {
                        getWindow().setReturnTransition(makeReturnTransition());
                    }
                }
            });
        }
    }

    @SuppressLint("NewApi") private Transition makeReturnTransition() {
        View rootView = mPagerAdapter.getCurrentDetailsFragment().getView();
        assert rootView != null;

        findViewById(R.id.article_appbar).setVisibility(View.INVISIBLE);

        // slide card out of the screen
        Transition slideBottom = new Slide(Gravity.BOTTOM);
        slideBottom.addTarget(rootView.findViewById(R.id.article_card));
        slideBottom.addTarget(findViewById(R.id.share_fab));
        slideBottom.setDuration(getResources().getInteger(R.integer.transition_duration));
        return slideBottom;
    }

    @SuppressLint("NewApi") private Transition makeEnterTransition() {
        View rootView = mPagerAdapter.getCurrentDetailsFragment().getView();
        assert rootView != null;

        // slide card in to the screen
        Transition cardSlide = new Slide(Gravity.BOTTOM);
        cardSlide.addTarget(rootView.findViewById(R.id.article_card));
        cardSlide.setDuration(getResources().getInteger(R.integer.transition_duration));
        return cardSlide;

    }

    private void updateAppBarImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoView.setTransitionName(mCurrentUrl);
        }

        mPhotoView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Picasso.with(getApplicationContext())
                        .load(mCurrentUrl)
                        .resize(mPhotoView.getMeasuredWidth(), mPhotoView.getMeasuredHeight())
                        .centerCrop()
                        .into(mPhotoView);

                precacheThumbnail();
                return true;
            }
        });
    }

    private void precacheThumbnail() {
        Picasso.with(this)
                .load(mCurrentUrl)
                .fetch();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
    }

    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) @Override
    public void finishAfterTransition() {
        // report cursor position back
        // RecyclerView has to scroll to last viewed article
        Intent i = new Intent();
        i.putExtra(ArticleDetailActivity.EXTRA_CURSOR_POSITION, mCursor.getPosition());
        setResult(RESULT_OK, i);
        mIsReturning = true;
        getWindow().setReturnTransition(makeReturnTransition());
        super.finishAfterTransition();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        private ArticleDetailFragment mCurrentFragment;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }

        @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurrentFragment = (ArticleDetailFragment) object;
        }

        public ArticleDetailFragment getCurrentDetailsFragment() {
            return mCurrentFragment;
        }
    }
}

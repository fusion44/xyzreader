package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

import java.util.ArrayList;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private boolean mIsRefreshing = false;
    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setTitle(getString(R.string.app_name));

        if (savedInstanceState == null) {
            refresh();
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private void updateRefreshingUI() {
        //mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        final int columnCount = getResources().getInteger(R.integer.list_column_count);

        cursor.moveToFirst();
        ArrayList<ArticleViewModel> viewModels = new ArrayList<>(cursor.getCount());

        int currentColumn = 0;
        while (!cursor.isAfterLast()) {
            String subtitle = DateUtils.getRelativeTimeSpanString(
                    cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                    System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL).toString()
                    + " by "
                    + cursor.getString(ArticleLoader.Query.AUTHOR);

            int spanSize = 1;

            if (columnCount == 2 && currentColumn == 0) {
                // if we are in two column mode, check whether this item is displayed in first column
                // if yes, check whether it will be displayed with two columns
                int rand = (int) (Math.random() * 4);
                if (rand == 2) {
                    spanSize = 2;

                    // count one up as one additional column is used up.
                    // This basically leads to resetting at the end of this for loop on case of two columns
                    currentColumn++;
                }
            } else if (columnCount == 3) {
                if (currentColumn == 0 || currentColumn == 1) {
                    int rand = (int) (Math.random() * 3);
                    if (rand == 2) {
                        spanSize = 2;
                        currentColumn++; // count one up as one additional column is used up
                    }
                }
            }

            ArticleViewModel avm = new ArticleViewModel(
                    cursor.getLong(ArticleLoader.Query._ID),
                    cursor.getString(ArticleLoader.Query.TITLE),
                    subtitle,
                    cursor.getString(ArticleLoader.Query.THUMB_URL),
                    spanSize
            );
            viewModels.add(avm);
            cursor.moveToNext();

            currentColumn++;
            // reset column counter.
            if (currentColumn >= columnCount) {
                currentColumn = 0;
            }
        }

        ArticleAdapter adapter = new ArticleAdapter();
        adapter.replaceModels(viewModels);
        mRecyclerView.setAdapter(adapter);
        GridLayoutManager glm = new GridLayoutManager(
                getApplicationContext(), columnCount, LinearLayoutManager.VERTICAL, false);
        glm.setSpanSizeLookup(adapter.getSpanSizeLookup());
        mRecyclerView.setLayoutManager(glm);
        glm.requestLayout();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ArticleViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
    }

    private class ArticleAdapter extends BaseAdapter<ArticleViewModel, ArticleViewHolder> {

        public ArticleAdapter() {
        }

        @Override
        public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ArticleViewHolder vh = new ArticleViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ArticleViewHolder holder, int position) {
            ArticleViewModel model = mViewModels.get(position);
            holder.titleView.setText(String.valueOf(position) + " " + model.getTitle());
            holder.subtitleView.setText(model.getSubtitle());
            Glide.with(getApplicationContext())
                    .load(model.getThumbnailUrl())
                    .centerCrop()
                    .into(holder.thumbnailView);

            if (model.spanCount == 2) {
                final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                    StaggeredGridLayoutManager.LayoutParams sglp =
                            (StaggeredGridLayoutManager.LayoutParams) lp;
                    sglp.setFullSpan(true);
                    holder.itemView.setLayoutParams(sglp);
                }
            }
            runAnimation(holder, position, defaultItemAnimationDuration, getAnimationDirection());
        }
    }
}

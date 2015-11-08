package com.example.xyzreader.ui;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

// https://github.com/bduncavage/recyclerViewToTheRescue
// https://www.youtube.com/watch?v=Cfb3RAyQg4w
// modified by me :-)
public abstract class BaseAdapter<T extends BaseViewModel, S extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<S> {
    protected static final int ANIMATION_DELAY_INTERVAL = 50;
    protected final Handler handler = new Handler(Looper.getMainLooper());
    protected final SpanSizeLookup spanSizeLookup = new SpanSizeLookup();
    protected ArrayList<T> mViewModels;
    protected ClickListener mOutsideClickListener;
    protected int lastAnimatedPosition = -1;
    protected long nextAnimationStartTime;
    protected boolean animateItemsOnScroll = true;
    protected int defaultItemAnimationDuration = 0;

    public BaseAdapter() {
        super();
        mViewModels = new ArrayList<>();

        if (defaultItemAnimationDuration == 0) {
            defaultItemAnimationDuration =
                    Resources.getSystem().getInteger(android.R.integer.config_mediumAnimTime);
        }
    }

    public void setAnimateItemsOnScroll(boolean animate) {
        animateItemsOnScroll = animate;
    }

    protected AnimationDirection getAnimationDirection() {
        return AnimationDirection.UpFromBottom;
    }

    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return spanSizeLookup;
    }

    public void setOnItemClickListener(ClickListener outsideClickListener) {
        mOutsideClickListener = outsideClickListener;
    }

    public void add(int position, T artist) {
        mViewModels.add(position, artist);
        notifyItemInserted(position);
    }

    public void remove(T artist) {
        int position = mViewModels.indexOf(artist);
        mViewModels.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        mViewModels.clear();
    }

    public void replaceModels(ArrayList<T> models) {
        mViewModels.clear();
        mViewModels = models;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mViewModels.size();
    }

    public ArrayList<T> getModels() {
        return mViewModels;
    }

    @Override
    public int getItemViewType(int position) {
        return mViewModels.get(position).layout;
    }

    protected void runAnimation(final RecyclerView.ViewHolder targetViewHolder,
                                final int position,
                                final int duration,
                                final AnimationDirection animationDirection) {
        if (animateItemsOnScroll) {
            final float maxAlpha = 1f;
            final View targetView = targetViewHolder.itemView;

            // Don't actually run the animation right a way. This gives a nice effect
            // when adding a large batch of items.
            if (position > lastAnimatedPosition) {
                int delay = 0;
                long currTime = System.currentTimeMillis();
                if (currTime < nextAnimationStartTime + ANIMATION_DELAY_INTERVAL) {
                    delay = (int) ((nextAnimationStartTime + ANIMATION_DELAY_INTERVAL) - currTime);
                }
                nextAnimationStartTime = currTime + delay;

                targetView.setAlpha(0);
                switch (animationDirection) {
                    case UpFromBottom:
                        targetView.setTranslationY(500.0f);
                        break;
                    case DownFromTop:
                        targetView.setTranslationY(-500.0f);
                        break;
                    case InFromLeft:
                        targetView.setTranslationX(500.0f);
                        break;
                    case InFromRight:
                        targetView.setTranslationX(-500.0f);
                        break;
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switch (animationDirection) {
                            case DownFromTop:
                            case UpFromBottom:
                                targetView.animate().alpha(maxAlpha).translationY(0).setDuration(duration);
                                break;
                            case InFromRight:
                            case InFromLeft:
                                targetView.animate().alpha(maxAlpha).translationX(0).setDuration(duration);
                                break;
                        }
                        targetView.animate().setInterpolator(new LinearOutSlowInInterpolator());
                        targetView.animate().start();
                    }
                }, delay);
                lastAnimatedPosition = position;
            }
        }
    }

    public enum AnimationDirection {
        UpFromBottom,
        DownFromTop,
        InFromLeft,
        InFromRight,
    }

    public interface ClickListener {
        void onItemClick(BaseViewModel model);
    }

    protected class SpanSizeLookup extends GridLayoutManager.SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            return mViewModels.get(position).spanCount;
        }
    }
}

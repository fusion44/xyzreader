package com.example.xyzreader.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

// Solutions for item click listeners in recyclerviews:
// http://stackoverflow.com/questions/24471109/recyclerview-onclick
// Implementation is actually from this awesome demo app:
// https://github.com/bduncavage/recyclerViewToTheRescue

public class RecyclerViewClickListener implements RecyclerView.OnItemTouchListener {
    GestureDetector mGestureDetector;
    private OnItemGestureListener mListener;
    private View viewUnderTouch;
    private RecyclerView recyclerView;

    public RecyclerViewClickListener(Context context, OnItemGestureListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override public void onLongPress(MotionEvent e) {
                int position = recyclerView.getChildAdapterPosition(viewUnderTouch);
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemLongClick(viewUnderTouch, position);
                }
            }
        });
        mGestureDetector.setIsLongpressEnabled(true);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());

        /* Return false early if we know that the click has been made on a subview
         * that has an onClick listener set on it. This will allow propagating the
         * click event to child views.
         */
        if (ViewUtil.isTouchInsideViewWithClickListener(childView, e)) {
            return false;
        }

        /* Else we consider the click event for the entire recycler view item */
        viewUnderTouch = childView;
        recyclerView = view;
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }

    @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemGestureListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }
}


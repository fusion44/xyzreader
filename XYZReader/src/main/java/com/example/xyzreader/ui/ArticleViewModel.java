package com.example.xyzreader.ui;

import com.example.xyzreader.R;

class ArticleViewModel extends BaseViewModel {
    public long id;
    public String title;
    public String subtitle;
    public String thumbnailUrl;
    public String photoUrl;

    ArticleViewModel(long id, String title, String subtitle, String photoUrl, String thumbUrl, int spanCount) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.thumbnailUrl = thumbUrl;
        this.photoUrl = photoUrl;
        this.layout = R.layout.list_item_article;
        this.spanCount = spanCount;
    }
}

package com.example.xyzreader.ui;

import com.example.xyzreader.R;

class ArticleViewModel extends BaseViewModel {
    private long id;
    private String title;
    private String subtitle;
    private String thumbnailUrl;

    ArticleViewModel(long id, String title, String subtitle, String imgUrl, int spanCount) {
        this.setId(id);
        this.setTitle(title);
        this.setSubtitle(subtitle);
        this.setThumbnailUrl(imgUrl);
        this.layout = R.layout.list_item_article;
        this.spanCount = spanCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

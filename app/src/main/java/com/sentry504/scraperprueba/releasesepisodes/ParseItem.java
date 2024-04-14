package com.sentry504.scraperprueba.releasesepisodes;

import java.io.Serializable;

public class ParseItem {
    private String imgUrl;
    private String title;
    private String episode;
    private String episodeUrl;

    public ParseItem(String imgUrl, String title, String episode,String episodeUrl) {
        this.imgUrl = imgUrl;
        this.title = title;
        this.episode = episode;
        this.episodeUrl = episodeUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEpisode() {
        return episode;
    }

    public void setepisode(String episode) {
        this.episode = episode;
    }

    public String getEpisodeUrl() {
        return episodeUrl;
    }

    public void setepisodeUrl(String episodeUrl) {
        this.episodeUrl = episodeUrl;
    }
}

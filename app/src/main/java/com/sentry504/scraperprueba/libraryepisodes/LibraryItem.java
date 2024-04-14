package com.sentry504.scraperprueba.libraryepisodes;

public class LibraryItem {
    private final String imgUrl;
    private final String title;
    private final String categoria;
    private final String episodeUrl;

    public LibraryItem(String imgUrl, String title, String categoria, String episodeUrl) {
        this.imgUrl = imgUrl;
        this.title = title;
        this.categoria = categoria;
        this.episodeUrl = episodeUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }
    public String getTitle() {
        return title;
    }
    public String getCategoria() {
        return categoria;
    }
    public String getEpisodeUrl() {
        return episodeUrl;
    }
}

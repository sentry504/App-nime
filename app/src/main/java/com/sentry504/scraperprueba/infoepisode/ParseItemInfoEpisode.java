package com.sentry504.scraperprueba.infoepisode;

public class ParseItemInfoEpisode {
    private final String episodeUrl;
    private final String nombre;
    private final Boolean visto;

    public ParseItemInfoEpisode(String episodeUrl, String nombre, Boolean visto) {
        this.episodeUrl = episodeUrl;
        this.nombre = nombre;
        this.visto = visto;
    }

    public String getEpisodeUrl() {
        return episodeUrl;
    }
    public String getNombre() {
        return nombre;
    }
    public Boolean getVisto() { return visto; }
}

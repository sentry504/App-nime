package com.sentry504.scraperprueba.milista;

public class SeriesModel {
    private String titular, servidor, url, img;
    private int posicion, episodes;

    public SeriesModel(String titular, String servidor, String url, String img, int posicion, int episodes){
        this.titular = titular;
        this.servidor = servidor;
        this.url = url;
        this.img = img;
        this.posicion = posicion;
        this.episodes = episodes;
    }

    public int getEpisodes() {return episodes;}
    public String getImg() {return img;}
    public int getPosicion() {return posicion;}
    public String getServidor() {return servidor;}
    public String getTitular() {return titular;}
    public String getUrl() {return url;}

    public void setUrl(String url) {this.url = url;}
    public void setEpisodes(int episodes) {this.episodes = episodes;}
    public void setImg(String img) {this.img = img;}
    public void setPosicion(int posicion) {this.posicion = posicion;}
    public void setServidor(String servidor) {this.servidor = servidor;}
    public void setTitular(String titular) {this.titular = titular;}
}

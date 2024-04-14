package com.sentry504.scraperprueba.infoepisode;

import com.sentry504.scraperprueba.common.LevenshteinDistance;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class InfoScraping {
    private final ArrayList<ParseItemInfoEpisode> parseItemInfoEpisodes = new ArrayList<>();
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    public String urImagen, title, sinopsis, state;



    public ArrayList<ParseItemInfoEpisode> serverContenido(String servidor, String urlRaiz, int episodios){
        levenshteinDistance.setWords(servidor, "JKanime");
        if (levenshteinDistance.getAfinidad()==1.0) {
            return jkanimeContenido(urlRaiz, episodios);
        }
        levenshteinDistance.setWords(servidor, "monoschinos");
        if (levenshteinDistance.getAfinidad()==1.0) {
            return animeFlvContenido(urlRaiz, episodios);
        }else {
            return animeFlvContenido(urlRaiz, episodios);
        }
    }
    /*--------------------------------------------------------------------------------------------*/

    public ArrayList<ParseItemInfoEpisode> animeFlvContenido(String urlRaiz, int episodios){
        Document doc;
        String uri = "https://m.animeflv.net";
        try {
            doc = Jsoup.connect(urlRaiz)
                    .userAgent("Mozilla") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .header("Accept", "text/html")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            urImagen =uri + doc.select("figure.Image").select("img").attr("src");
            title= doc.select("h1.Title").text();
            state = doc.select("strong.Anm-On").text();
            sinopsis =doc.select("article.Anime.Single.Bglg").select("p").eq(1).text().replace("Sinopsis: ","") ;

            //@DrawableRes int prueba = R.drawable.baseline_remove_red_eye_24;
            //levenshteinDistance.setWords(title, listadoCapitulosVisto.item);

            Elements data = doc.select("li.Episode");
            String episodio, nombre;
            boolean visto;

            for (int i=0; i<data.size(); i++){
                episodio = uri + data.select("a").eq(i).attr("href");
                nombre = data.select("a").eq(i).text();
                visto = i == episodios;
                parseItemInfoEpisodes.add(new ParseItemInfoEpisode(episodio, nombre, visto));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseItemInfoEpisodes;
    }
    /*--------------------------------------------------------------------------------------------*/

    public ArrayList<ParseItemInfoEpisode> jkanimeContenido(String urlRaiz, int episodios){
        Document doc;
        try {
            doc = Jsoup.connect(urlRaiz)
                    .userAgent("Mozilla") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .header("Accept", "text/html")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            urImagen = doc.select("div.anime__details__pic.set-bg").attr("data-setbg");
            title= doc.select("div.anime__details__title").select("h3").text();
            state = doc.select("span.enemision.finished").text();

            if (!state.contains("Concluido")){state = "En emision";}

            sinopsis =doc.select("div.anime__details__text").select("p").text() ;

            Elements paginado = doc.select("a.numbers");
            String episodio, nombre;
            boolean visto;

            String episodiosTotal = paginado.get(paginado.size()-1).text().split(" ")[2];
            for (int j = 0; j<Integer.parseInt(episodiosTotal); j++){
                episodio = urlRaiz + (j+1) + "/";
                nombre = title + " - " + (j+1);
                visto = j < episodios;
                parseItemInfoEpisodes.add(new ParseItemInfoEpisode(episodio, nombre, visto));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseItemInfoEpisodes;
    }
    /*--------------------------------------------------------------------------------------------*/
}
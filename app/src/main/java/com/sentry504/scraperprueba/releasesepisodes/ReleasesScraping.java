package com.sentry504.scraperprueba.releasesepisodes;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.sentry504.scraperprueba.common.LevenshteinDistance;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ReleasesScraping {
    private final ArrayList<ParseItem> parseItems = new ArrayList<>();
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    private Context context;

    public ReleasesScraping(Context context){
        this.context = context;
    }

    public ArrayList<ParseItem> server(String servidor){
        levenshteinDistance.setWords(servidor, "JKanime");
        if (levenshteinDistance.getAfinidad()==1.0) {
            return jkanime();
        }
        levenshteinDistance.setWords(servidor, "monoschinos");
        if (levenshteinDistance.getAfinidad()==1.0) {
            return monoschinos();
        }else {
            return animeFlv();
        }
    }

    public ArrayList<ParseItem> animeFlv(){
        try {
            String url = "https://m.animeflv.net";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .header("Accept", "text/html")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            Elements data = doc.select("li.Episode");

            int size = data.size();
            for (int i = 0; i < size-1; i++) {
                String imgUrl = data.select("a")
                        .select("img")
                        .eq(i)
                        .attr("src");

                String title = data.select("a")
                        .select("h2")
                        .eq(i)
                        .text();

                String episode = data.select("p")
                        .select("span")
                        .eq(i)
                        .text();

                String episodeUrl = data.select("li.episode")
                        .select("a")
                        .eq(i)
                        .attr("href");

                parseItems.add(new ParseItem(url+imgUrl, title, episode, url + episodeUrl));
            }
        } catch (IOException e) {
        }
        return parseItems;
    }

    public ArrayList<ParseItem> jkanime(){
        try {
            String url = "https://jkanime.net";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .header("Accept", "text/html")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            Elements data = doc.select("a.bloqq");

            int size = data.size();
            for (int i = 0; i < size-1; i++) {
                String imgUrl = data.select("div.anime__sidebar__comment__item__pic.listadohome")
                        .select("img")
                        .eq(i)
                        .attr("src");

                String title = data.select("div.anime__sidebar__comment__item__text")
                        .select("h5")
                        .eq(i)
                        .text();

                String episode = data.select("div.anime__sidebar__comment__item__text")
                        .select("h6")
                        .eq(i)
                        .text();

                String episodeUrl = data.eq(i).attr("href");

                parseItems.add(new ParseItem(imgUrl, title, episode.replace("Episodio ",""), episodeUrl));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseItems;
    }

    public ArrayList<ParseItem> monoschinos(){
        try {
            String url = "https://monoschinos2.com";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .header("Accept", "text/html")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            Elements data = doc.select("div.col.col-md-6.col-lg-2.col-6");

            int size = data.size();
            for (int i = 0; i < size-1; i++) {
                String imgUrl = data.select("div.animeimgdiv")
                        .select("img")
                        .eq(i)
                        .attr("data-src");

                String title = data.select("div.animes")
                        .select("h2.animetitles")
                        .eq(i)
                        .text();

                String episode = data.select("p")
                        .eq(i)
                        .text();

                String episodeUrl = data.select("a")
                        .eq(i)
                        .attr("href");

                parseItems.add(new ParseItem(imgUrl, title, episode, episodeUrl));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseItems;
    }
}
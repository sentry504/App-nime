package com.sentry504.scraperprueba.libraryepisodes;

import com.sentry504.scraperprueba.common.LevenshteinDistance;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class LibraryScraping {
    private final ArrayList<LibraryItem> libraryItems = new ArrayList<>();
    private final ArrayList<LibraryItemPagination> libraryItemsPagination = new ArrayList<>();
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

    public ArrayList<LibraryItem> serverContenido(String servidor, String urlRaiz){
        libraryItems.clear();
        levenshteinDistance.setWords(servidor, "JKanime");
        if (levenshteinDistance.getAfinidad()==1.0) {
            return jkanimeContenido(urlRaiz);
        }
        levenshteinDistance.setWords(servidor, "monoschinos");
        if (levenshteinDistance.getAfinidad()==1.0) {
            return monoschinosContenido(urlRaiz);
        }else {
            return animeFlvContenido(urlRaiz);
        }
    }
    public ArrayList<LibraryItemPagination> serverPaginado(String servidor, String urlRaiz){
        libraryItemsPagination.clear();
        levenshteinDistance.setWords(servidor, "JKanime");
        if (levenshteinDistance.getAfinidad()==1.0) {
            return jkanimePaginado(urlRaiz);
        }
        levenshteinDistance.setWords(servidor, "monoschinos");
        if (levenshteinDistance.getAfinidad()==1.0) {
            return monoschinosPaginado(urlRaiz);
        }else {
            return animeFlvPaginado(urlRaiz);
        }
    }
    /*--------------------------------------------------------------------------------------------*/

    public ArrayList<LibraryItem> animeFlvContenido(String urlRaiz){
        try {
            Document doc = Jsoup.connect(urlRaiz)
                    .userAgent("Mozilla") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .header("Accept", "text/html")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            Elements data = doc.select("li.Anime");
            for (int i = 0; i < data.size(); i++) {
                String imgUrl = data.select("img").eq(i).attr("src");

                String title = data.select("h2").eq(i).text();

                String categoria = data.select("span").eq(i).text();

                String animeUrl = data.select("a")
                        .eq(i)
                        .attr("href");

                libraryItems.add(new LibraryItem(
                        urlRaiz.replace(urlRaiz,"https://m.animeflv.net")+imgUrl,
                        title,
                        categoria,
                        animeUrl
                ));
            }
        } catch (IOException e) {
        }
        return libraryItems;
    }

    public ArrayList<LibraryItemPagination> animeFlvPaginado(String urlRaiz){
        try {
            Document doc = Jsoup.connect(urlRaiz)
                    .userAgent("Mozilla") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .header("Accept", "text/html")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            Elements paginado = doc.select("ul.pagination").select("li");
            for (int i=0; i< paginado.size()-1; i++){
                String pagina = paginado.select("a").eq(i).attr("href");
                if(pagina.equals("#")){
                    pagina = "/browse?page=1";
                }
                String texto = paginado.select("a").eq(i).text();
                libraryItemsPagination.add(new LibraryItemPagination(
                        texto,
                        pagina,
                        false)
                );
            }
        } catch (IOException e) {
        }
        return libraryItemsPagination;
    }
    /*--------------------------------------------------------------------------------------------*/

    public ArrayList<LibraryItem> jkanimeContenido(String urlRaiz){
        try {
            Document doc = Jsoup.connect(urlRaiz)
                    .userAgent("Mozilla") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .header("Accept", "text/html")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            Elements data;
            String imgUrl, title, categoria, animeUrl;

            if (urlRaiz.contains("https://jkanime.net/directorio")){
                data = doc.select("div.card.mb-3.custom_item2");
                for (int i = 0; i < data.size(); i++) {
                    imgUrl = data.select("div.col-md-5.custom_thumb2").select("img").eq(i).attr("src");

                    title = data.select("h5.card-title").select("a").eq(i).text();

                    categoria = data.select("div.card-info").select("p.card-txt").eq(i).text();

                    animeUrl = data.select("h5.card-title").select("a").eq(i).attr("href");

                    libraryItems.add(new LibraryItem(imgUrl, title, categoria, animeUrl));
                }
            }else{
                data = doc.select("div.col-lg-2.col-md-6.col-sm-6");
                for (int i = 0; i < data.size(); i++) {
                    imgUrl = data.select("div.anime__item__pic.set-bg").eq(i).attr("data-setbg");

                    title = data.select("div.anime__item__text").select("a").eq(i).text();

                    categoria = data.select("li.anime").eq(i).text();

                    animeUrl = data.select("div.anime__item__text").select("a").eq(i).attr("href");

                    libraryItems.add(new LibraryItem(imgUrl, title, categoria, animeUrl));
                }
            }

        } catch (IOException e) {
        }
        return libraryItems;
    }

    public ArrayList<LibraryItemPagination> jkanimePaginado(String urlRaiz){
        try {
            Document doc = Jsoup.connect(urlRaiz)
                    .userAgent("Mozilla") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .header("Accept", "text/html")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            Elements paginado = doc.select("div.navigation").select("a");
            for (int i=0; i< paginado.size(); i++){
                String pagina = paginado.eq(i).attr("href");
                String texto = paginado.eq(i).text();

                libraryItemsPagination.add(new LibraryItemPagination(
                        texto,
                        pagina,false));
            }
        } catch (IOException e) {
        }
        return libraryItemsPagination;
    }

    public ArrayList<LibraryItem> monoschinosContenido(String urlRaiz){
        try {
            Document doc = Jsoup.connect(urlRaiz)
                    .userAgent("Mozilla/5.0") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .timeout(30000)
                    .header("Accept", "text/html")
                    .header("Cookie", "ZvcurrentVolume=100; zvAuth=1; zvLang=0; ZvcurrentVolume=100; notice=11")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            Elements data = doc.select("div.col-md-4.col-lg-2.col-6");
            for (int i = 0; i < data.size(); i++) {
                String imgUrl = data.select("div.seriesimg").select("img").eq(i).attr("src");

                String title = data.select("div.seriesdetails").select("h3.seristitles").eq(i).text();

                String categoria = data.select("div.seriesdetails").select("span.seriesinfo").eq(i).text();

                String animeUrl = data.select("a").eq(i).attr("href");

                libraryItems.add(new LibraryItem(imgUrl, title, categoria, animeUrl));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return libraryItems;
    }

    public ArrayList<LibraryItemPagination> monoschinosPaginado(String urlRaiz){
        try {
            Document doc = Jsoup.connect(urlRaiz)
                    .userAgent("Mozilla") // /5.0 Chrome/110.0.5481.63 Mobile Safari/537.36")
                    .header("Accept", "text/html")
                    //.header("Accept-Encoding", "gzip,deflate")
                    .header("Accept-Language", "it-IT,en;q=0.8,en-US;q=0.6,de;q=0.4,it;q=0.2,es;q=0.2")
                    .header("Connection", "keep-alive")
                    .ignoreContentType(true)
                    .get();

            Elements paginado = doc.select("ul.navigation").select("li");
            for (int i=0; i< paginado.size()-1; i++){
                String pagina = paginado.select("a").eq(i).attr("href");
                if(pagina.equals("")){
                    pagina = urlRaiz;
                }
                String texto = paginado.select("a").eq(i).text();

                libraryItemsPagination.add(new LibraryItemPagination(texto, pagina, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return libraryItemsPagination;
    }
}
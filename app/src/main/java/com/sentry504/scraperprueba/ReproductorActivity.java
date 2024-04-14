package com.sentry504.scraperprueba;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sentry504.scraperprueba.common.LevenshteinDistance;
import com.sentry504.scraperprueba.common.UrlsBloqueadas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Objects;

public class ReproductorActivity extends AppCompatActivity {
    String urlRaiz = "", servidor="", title, imagen;
    int posicion=0, episodios=0;
    ImageButton previo, siguiente;
    private static final String FILE_NAME = "listadoAnimes.txt";
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    JSONArray json = new JSONArray();
    SwipeRefreshLayout mySwipeRefreshLayout;
    WebView myWebView;
    public AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproductor);
        this.setTitle("Servidor");

        MobileAds.initialize(this, initializationStatus -> {});
        AdView adViewTop = findViewById(R.id.adViewTop);
        AdView adViewBottom = findViewById(R.id.adViewBottom);

        adRequest = new AdRequest.Builder().build();
        adViewTop.loadAd(adRequest);
        adViewBottom.loadAd(adRequest);

        Intent i = getIntent();
        title = i.getStringExtra("title");
        imagen = i.getStringExtra("img");
        urlRaiz = i.getStringExtra("url");
        servidor = i.getStringExtra("server");
        posicion = i.getIntExtra("position",0);
        episodios = i.getIntExtra("episodios",0);

        myWebView = findViewById(R.id.webView);
        WebSettings webSettings= myWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAllowFileAccess(false);
        webSettings.setSaveFormData(true);

        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        myWebView.setVerticalScrollBarEnabled(false);
        myWebView.clearCache(true);

        //Creación de los clientes del WebView
        myWebView.setWebViewClient(new MyWebViewClient());
        myWebView.setWebChromeClient(new MyWebChromeClient());

        myWebView.loadUrl(urlRaiz);

        mySwipeRefreshLayout = findViewById(R.id.swipeContainer);
        mySwipeRefreshLayout.setOnRefreshListener(() -> {
            myWebView.reload();
            mySwipeRefreshLayout.setRefreshing(false);
        });

        previo = findViewById(R.id.imageButtonPrevio);
        siguiente = findViewById(R.id.imageButtonSiguiente);

        previo.setOnClickListener(v -> {
            if (posicion>1 && posicion <= episodios){
                if (servidor.equals("JKanime")){
                    urlRaiz = urlRaiz.replace("/"+posicion+"/","/"+(posicion-1)+"/");
                }else{
                    urlRaiz = urlRaiz.replace("-"+posicion, "-"+(posicion-1));
                }
                posicion -=1;
                myWebView.loadUrl(urlRaiz);
            }
        });

        siguiente.setOnClickListener(v -> {
            if (posicion>0 && posicion < episodios){
                if (servidor.equals("JKanime")){
                    urlRaiz = urlRaiz.replace("/"+posicion+"/","/"+(posicion+1)+"/");
                }else{
                    urlRaiz = urlRaiz.replace("-"+posicion, "-"+(posicion+1));
                }
                posicion +=1;
                myWebView.loadUrl(urlRaiz);
            }
        });
    }

    public void guardadoEpisodio(){
        json = readListFile(FILE_NAME);
        JSONObject obj = new JSONObject();
        String patron = "[,#\\[\\]]";
        String tituloFormateado = title.replaceAll(patron, " ");
        try {
            obj.put("titular",tituloFormateado);
            obj.put("servidor", servidor);
            obj.put("url", urlRaiz);
            obj.put("img", imagen);
            obj.put("posicion", posicion);
            obj.put("episodes",episodios);

            int foundIn= -1;
            if (!json.isNull(0)){
                for (int i=0; i<json.length(); i++){
                    levenshteinDistance.setWords(json.getJSONObject(i).getString("titular"), title);
                    if( levenshteinDistance.getAfinidad()>0.9){
                        foundIn = i;
                    }
                }
            }
            if( foundIn>-1){
                json.remove(foundIn);
                json.put(obj);
            }else{
                json.put(obj);
            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(FILE_NAME,MODE_PRIVATE));
            outputStreamWriter.write(json.toString());
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        // Obtener una referencia al nodo 'myList' del usuario actual
        DatabaseReference myListRef = FirebaseDatabase.getInstance().getReference("users").child("user").child("myList");

        // Buscar si la serie ya existe en 'myList'
        myListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean found = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String titular = snapshot.getKey();
                    String episodes = (String) Objects.requireNonNull(snapshot.child("episodes").getValue()).toString();
                    //Hacemos una comparativa de similitud entre los titulos
                    levenshteinDistance.setWords(titular, tituloFormateado);
                    //si la similitud es mayor al 90% entonces asumimos que es la misma serie
                    if( levenshteinDistance.getAfinidad()>0.9){
                        // Actualizar el registro si la serie ya existe
                        myListRef.child(titular).child("servidor").setValue(servidor);
                        myListRef.child(titular).child("posicion").setValue(posicion);
                        myListRef.child(titular).child("episodes").setValue(Integer.valueOf(episodes));
                        myListRef.child(titular).child("url").setValue(urlRaiz);
                        found = true;
                        break;
                    }
                }
                // Agregar la serie si no existe
                if (!found) {
                    try {
                        myListRef.child(tituloFormateado).child("servidor").setValue(obj.get("servidor"));
                        myListRef.child(tituloFormateado).child("url").setValue(obj.get("url"));
                        myListRef.child(tituloFormateado).child("img").setValue(obj.get("img"));
                        myListRef.child(tituloFormateado).child("posicion").setValue(Integer.valueOf(obj.get("posicion").toString()));
                        myListRef.child(tituloFormateado).child("episodes").setValue(Integer.valueOf(obj.get("episodes").toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ERROR", "onCancelled: ".concat(databaseError.toString()));
            }
        });
    }

    public JSONArray readListFile(String file_name) {
        JSONArray retorno = new JSONArray();
        try {
            //Lectura del archivo, procesamiento para conversion a texto
            InputStreamReader inputStreamReader = new InputStreamReader(openFileInput(file_name));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String linea = bufferedReader.readLine();
            StringBuilder contenido = new StringBuilder();
            while (linea != null){
                contenido.append(linea).append("\n");
                linea = bufferedReader.readLine();
            }
            bufferedReader.close();
            inputStreamReader.close();

            //Conversion del contenido del archivo settings.txt a objeto json
            retorno = new JSONArray(contenido.toString());

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return retorno;
    }

    private class MyWebViewClient extends WebViewClient {
        UrlsBloqueadas urlsBloqueadas = new UrlsBloqueadas();
        @Override
        public void onReceivedHttpError (WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            //Toast.makeText(ReproductorActivity.this, "ReceivedHttpError: "+errorResponse.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            List<String> lista = urlsBloqueadas.urls();
            for (int i=0; i<lista.size(); i++){
                if (url.contains(lista.get(i))){
                    myWebView.stopLoading();
                }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url){
            super.onPageFinished(view, url);
            guardadoEpisodio();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (!(view.getUrl().equals(urlRaiz))){
                myWebView.stopLoading();
                return false;
            }else{
                return true;
            }
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        //Declaración de Instancias
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallBack;
        private int mOriginalSystemVisibility;

        //Constructor
        MyWebChromeClient(){
        }
        //Creación de fondo de color default
        @Nullable
        public Bitmap getDefaultVideoPoster(){
            if (mCustomView ==null){
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(),999999999);
        }

        //Se hace referencia de la instancia de mCustomView y se cierra la vista.
        public void onHideCustomView(){
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemVisibility);
            this.mCustomViewCallBack.onCustomViewHidden();
            this.mCustomViewCallBack = null;
        }

        //Se hace referencia de la instancia de mCustomView y se muestra la vista.
        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallBack){
            if (this.mCustomView != null){
                onHideCustomView();
                return;
            }

            this.mCustomView = paramView;
            this.mOriginalSystemVisibility = getWindow().getDecorView().getWindowSystemUiVisibility();
            this.mCustomViewCallBack = paramCustomViewCallBack;

            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1,-1));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}

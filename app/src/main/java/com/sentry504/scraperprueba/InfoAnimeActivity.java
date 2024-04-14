package com.sentry504.scraperprueba;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.sentry504.scraperprueba.common.LevenshteinDistance;
import com.sentry504.scraperprueba.common.NoconexionFragment;
import com.sentry504.scraperprueba.infoepisode.InfoScraping;
import com.sentry504.scraperprueba.infoepisode.ParseAdapterInfoEpisode;
import com.sentry504.scraperprueba.infoepisode.ParseItemInfoEpisode;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class InfoAnimeActivity extends Fragment implements ParseAdapterInfoEpisode.OnItemClickListener {
    String urlRaiz = "", servidor, urImagen, title, titular, sinopsis, urlActualLibrary, posicion;
    private static final String FILE_NAME = "listadoAnimes.txt";
    JSONArray json = new JSONArray();
    ImageView imageViewInfo;
    TextView textViewTitle, textViewEstado, textViewSinopsis;
    CardView cardViewInfo;
    LinearLayout ly_info;
    RecyclerView recyclerView;
    private ParseAdapterInfoEpisode adapter;
    private final ArrayList<ParseItemInfoEpisode> parseItemInfoEpisodes = new ArrayList<>();
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    private ProgressBar progressBar;
    private InterstitialAd interstitialAd;
    public AdRequest adRequest;
    private static final String ID_INTERSTICIAL_PRUEBAS = "ca-app-pub-3940256099942544/1033173712";
    private static final String ID_INTERSTICIAL_NAVIGATION_MENU = "ca-app-pub-1883323185290636/1765003037";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Bundle args = new Bundle();
                args.putString("servidor", servidor);
                args.putString("url", urlActualLibrary);
                args.putString("posicion", posicion);

                LibraryActivity libraryActivity1 = new LibraryActivity();
                libraryActivity1.setArguments(args);
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, libraryActivity1).commit();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        MobileAds.initialize(requireContext(), initializationStatus -> {});
        adRequest = new AdRequest.Builder().build();

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.info_anime_fragment, container, false);

        if (getArguments() != null) {
            urlRaiz= getArguments().getString("url");
            servidor = getArguments().getString("servidor");
            titular = getArguments().getString("title");
            urlActualLibrary = getArguments().getString("urlActualLibrary");
            posicion = getArguments().getString("posicion");
        }

        progressBar = view.findViewById(R.id.progressBar);
        imageViewInfo = view.findViewById(R.id.imageViewInfo);
        textViewTitle = view.findViewById(R.id.textViewInfoTitle);
        textViewEstado = view.findViewById(R.id.textViewInfoEstado);
        textViewSinopsis = view.findViewById(R.id.textViewInfoSinopsis);
        textViewSinopsis.setMovementMethod(new ScrollingMovementMethod());
        cardViewInfo = view.findViewById(R.id.cardViewInfo);
        ly_info = view.findViewById(R.id.ly_Info);

        recyclerView = view.findViewById(R.id.recyclerViewEpisodios);
        adapter = new ParseAdapterInfoEpisode(parseItemInfoEpisodes, this.getContext());
        adapter.setOnItemCLickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);

        NetworkInfo networkInfo;
        ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if ((networkInfo == null)) {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,new NoconexionFragment()).commit();
            Toast.makeText(requireActivity(), "Parece que no tienes conexion a internet", Toast.LENGTH_LONG).show();
        }else{
            Content content = new Content();
            content.execute();
        }

        return view;
    }
    @Override
    public void OnItemClick(View view, int position) {
        loadAd(ID_INTERSTICIAL_PRUEBAS);
        String capitulo;
        Intent i = new Intent(getActivity(), ReproductorActivity.class);

        levenshteinDistance.setWords(servidor, "AnimeFLV");
        if (levenshteinDistance.getAfinidad() ==1.0){
            capitulo = urlRaiz.replace("anime/", "ver/");;
            capitulo = parseItemInfoEpisodes.get(position).getEpisodeUrl().replace(capitulo+"-","");
            i.putExtra("position", Integer.parseInt(capitulo));
        }else{
            i.putExtra("position", position+1);
        }

        i.putExtra("title", title);
        i.putExtra("img", urImagen);
        i.putExtra("url", parseItemInfoEpisodes.get(position).getEpisodeUrl());
        i.putExtra("server", servidor);
        i.putExtra("episodios", parseItemInfoEpisodes.size());

        startActivity(i);
    }

    public int encontrarVistos(String title) throws JSONException {
        File archivo = new File(requireActivity().getFilesDir(),"listadoAnimes.txt");
        if (archivo.exists() && !archivo.isDirectory()) {
            json = readListFile(FILE_NAME);
        }
        int valor = -1;
        if (!json.isNull(0)){
            for (int i=0; i<json.length(); i++){
                levenshteinDistance.setWords(json.getJSONObject(i).getString("titular"), title);
                if( levenshteinDistance.getAfinidad()>0.9){
                    valor = Integer.parseInt(json.getJSONObject(i).getString("posicion"));
                }
            }
        }
        return valor;
    }

    public void loadAd(String id) {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(requireContext(),id, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd _interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                interstitialAd = _interstitialAd;
                interstitialAd.show(requireActivity());
                interstitialAd.setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                interstitialAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                // Called when fullscreen content failed to show.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                interstitialAd = null;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                            }
                        });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                interstitialAd = null;
            }
        });
    }

    public class Content extends AsyncTask<Void,Void,Void> {
        String state;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ly_info.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            progressBar.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
            Picasso.get().load(urImagen).into(imageViewInfo);
            textViewTitle.setText(title);
            if(!Objects.equals(state, "")){
                textViewEstado.setText(state);
                textViewEstado.setTextColor(Color.GREEN);
            }else{
                textViewEstado.setText("Finalized");
                textViewEstado.setTextColor(Color.RED);
            }

            ly_info.setVisibility(View.VISIBLE);
            textViewSinopsis.setText(sinopsis);
            try {
                int scrollTo = encontrarVistos(titular);
                if(scrollTo != -1){
                    recyclerView.scrollToPosition(scrollTo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            adapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            InfoScraping infoScraping = new InfoScraping();
            parseItemInfoEpisodes.clear();
            try {
                parseItemInfoEpisodes.addAll(infoScraping.serverContenido(servidor, urlRaiz, (encontrarVistos(titular)+1)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            urImagen = infoScraping.urImagen;
            title= infoScraping.title;
            state = infoScraping.state;
            sinopsis = infoScraping.sinopsis;
            return null;
        }
    }

    public JSONArray readListFile(String file_name) {
        JSONArray retorno = new JSONArray();
        try {
            //Lectura del archivo, procesamiento para conversion a texto
            InputStreamReader inputStreamReader = new InputStreamReader(requireActivity().openFileInput(file_name));
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
}
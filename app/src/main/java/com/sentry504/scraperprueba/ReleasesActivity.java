package com.sentry504.scraperprueba;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sentry504.scraperprueba.common.LevenshteinDistance;
import com.sentry504.scraperprueba.releasesepisodes.ParseAdapter;
import com.sentry504.scraperprueba.releasesepisodes.ParseItem;
import com.sentry504.scraperprueba.releasesepisodes.ReleasesScraping;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReleasesActivity extends Fragment implements ParseAdapter.OnItemClickListener {
    RecyclerView recyclerView;
    private String servidor = "AnimeFLV";
    private ParseAdapter adapter;
    private final ArrayList<ParseItem> parseItems = new ArrayList<>();
    private final ReleasesScraping releasesScraping = new ReleasesScraping(getContext());
    private ProgressBar progressBar;
    private LinearLayout lyInfoConnection;
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    JSONArray json = new JSONArray();
    private InterstitialAd interstitialAd;
    public AdRequest adRequest;
    private static final String ID_INTERSTICIAL_PRUEBAS = "ca-app-pub-3940256099942544/1033173712";
    private static final String ID_INTERSTICIAL_NAVIGATION_MENU = "ca-app-pub-1883323185290636/5483018084";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.releases_fragment, container, false);

        if (getArguments() != null) {
            servidor= getArguments().getString("servidor");
        }

        MobileAds.initialize(requireContext(), initializationStatus -> {});
        adRequest = new AdRequest.Builder().build();
        requireActivity().setTitle("Recientes");
        progressBar = view.findViewById(R.id.progressBar);
        recyclerView = view.findViewById(R.id.recyclerViewEpisodios);
        lyInfoConnection = view.findViewById(R.id.lyInfoConnection);

        try {
            Content content = new Content(this);
            content.execute();
        }catch (Exception ex){
            Toast.makeText(requireContext(), "No se pudo realizar la operacion", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void OnItemClick(View view, int position) {
        loadAd(ID_INTERSTICIAL_PRUEBAS);
        Intent i = new Intent(getActivity(), ReproductorActivity.class);
        i.putExtra("title", parseItems.get(position).getTitle());
        i.putExtra("img", parseItems.get(position).getImgUrl());
        i.putExtra("url", parseItems.get(position).getEpisodeUrl());
        i.putExtra("server", servidor);
        i.putExtra("position", Integer.parseInt(parseItems.get(position).getEpisode()));
        i.putExtra("episodios",Integer.parseInt( parseItems.get(position).getEpisode()));
        startActivity(i);
    }

    public void loadAd(String id) {
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

    public class Content extends AsyncTask<Void,Void,ArrayList<ParseItem>> {
        private ReleasesActivity context;
        public Content(ReleasesActivity context){
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<ParseItem> doInBackground(Void... voids) {
            parseItems.addAll(releasesScraping.server(servidor));

            return parseItems;
        }

        @Override
        protected void onPostExecute(ArrayList<ParseItem> aVoid) {
            super.onPostExecute(aVoid);

            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            if(aVoid.isEmpty()){
                lyInfoConnection.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "No se obtuvieron resultados", Toast.LENGTH_SHORT).show();
            }else{
                // Obtén una referencia a la lista de series vistas en Firebase
                DatabaseReference myListRef = FirebaseDatabase.getInstance().getReference("users").child("user").child("myList");

                HashMap<String, Integer> titulosEpisodiosMap = new HashMap<>();

                for (ParseItem capitulo : aVoid) {
                    String patron = "[\\.,#\\$\\[\\]]";
                    String tituloFormateado = capitulo.getTitle().replaceAll(patron, " ");

                    int episodio = Integer.parseInt(capitulo.getEpisode());
                    if (!titulosEpisodiosMap.containsKey(tituloFormateado) || episodio > titulosEpisodiosMap.get(tituloFormateado)) {
                        // Si el título no está en el mapa o el episodio es más alto, actualizar el mapa
                        titulosEpisodiosMap.put(tituloFormateado, episodio);
                    }
                }

                for (Map.Entry<String, Integer> entry : titulosEpisodiosMap.entrySet()) {
                    String titulo = entry.getKey();
                    int episodio = entry.getValue();

                    // Verifica si la serie está en tu lista de series vistas
                    myListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String titular = snapshot.getKey();
                                //Hacemos una comparativa de similitud entre los titulos
                                levenshteinDistance.setWords(titular, titulo);
                                //si la similitud es mayor al 90% entonces asumimos que es la misma serie
                                if( levenshteinDistance.getAfinidad()>0.9){
                                    // Actualizar el registro si la serie ya existe
                                    myListRef.child(titular).child("episodes").setValue(episodio);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("ERROR", "onCancelled - Error al leer los datos de Firebase: ".concat(databaseError.toString()));
                        }
                    });
                }

                lyInfoConnection.setVisibility(View.GONE);
                adapter = new ParseAdapter(parseItems);
                adapter.setOnItemCLickListener(ReleasesActivity.this);
                recyclerView.setAdapter(adapter);
            }
            progressBar.setVisibility(View.GONE);
        }
    }
}
package com.sentry504.scraperprueba;

import static android.content.Context.MODE_PRIVATE;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.sentry504.scraperprueba.milista.SeriesModel;
import com.sentry504.scraperprueba.milista.adapterMyListaRecycler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyListaActivity extends Fragment implements adapterMyListaRecycler.OnItemClickListener {
    private static final String FILE_NAME = "listadoAnimes.txt";
    JSONArray json = new JSONArray();
    RecyclerView rv_item;

    // ArrayList para almacenar las series vistas
    private List<SeriesModel> seriesVistas = new ArrayList<>();
    private ValueEventListener valueEventListener;
    private DatabaseReference myListRef;
    private adapterMyListaRecycler adapter;
    private InterstitialAd interstitialAd;
    public AdRequest adRequest;
    private static final String ID_INTERSTICIAL_PRUEBAS = "ca-app-pub-3940256099942544/1033173712";
    private static final String ID_INTERSTICIAL_NAVIGATION_MENU = "ca-app-pub-1883323185290636/7691438519";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_lista_fragment, container, false);
        MobileAds.initialize(requireContext(), initializationStatus -> {});
        adRequest = new AdRequest.Builder().build();

        requireActivity().setTitle("Mi lista");

        rv_item = view.findViewById(R.id.rv_item);

        llenarRecycler();

        return view;
    }

    private void llenarRecycler() {
        Context context = getContext();
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (context != null) { // Verificar si el contexto es nulo
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setMessage("¿Deseas eliminar este registro?");
                    builder.setTitle("Eliminar");
                    builder.setCancelable(true);
                    builder.setNegativeButton("No", (dialog, which) -> {
                        adapter.notifyDataSetChanged();
                    });
                    builder.setPositiveButton("Sí", (dialog, which) -> {
                        int position = viewHolder.getAdapterPosition();

                        // Eliminar el elemento de Firebase
                        String titular = seriesVistas.get(position).getTitular();
                        myListRef.child(titular).removeValue();
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        };

        ItemTouchHelper itemDeleteTouchHelper = new ItemTouchHelper(simpleCallback);
        itemDeleteTouchHelper.attachToRecyclerView(rv_item);

        myListRef = FirebaseDatabase.getInstance().getReference("users").child("user").child("myList");

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                seriesVistas.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String titular = snapshot.getKey();
                    String servidor = (String) snapshot.child("servidor").getValue();
                    String url = (String) snapshot.child("url").getValue();
                    String img = (String) snapshot.child("img").getValue();
                    String posicion = String.valueOf(snapshot.child("posicion").getValue() != null?snapshot.child("posicion").getValue():0);
                    String episodes = String.valueOf(snapshot.child("episodes").getValue() != null?snapshot.child("episodes").getValue():0);
                    SeriesModel serie = new SeriesModel(
                            titular,
                            servidor,
                            url,
                            img,
                            Integer.parseInt(posicion),
                            Integer.parseInt(episodes)
                    );
                    seriesVistas.add(serie);
                }
                adapter = new adapterMyListaRecycler(seriesVistas);
                adapter.setOnItemCLickListener(MyListaActivity.this);
                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                rv_item.setLayoutManager(layoutManager);
                rv_item.addItemDecoration(new DividerItemDecoration(rv_item.getContext(), DividerItemDecoration.VERTICAL));
                rv_item.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ERROR", "onCancelled: ".concat(databaseError.toString()));
            }
        };

        myListRef.addValueEventListener(valueEventListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (valueEventListener != null) {
            myListRef.addValueEventListener(valueEventListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (valueEventListener != null) {
            myListRef.removeEventListener(valueEventListener);
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

    @Override
    public void OnItemClick(View view, int position) {
        File archivo = new File(requireActivity().getFilesDir(),"listadoAnimes.txt");
        if (archivo.exists() && !archivo.isDirectory()) {
            json = readListFile(FILE_NAME);
        }
        loadAd(ID_INTERSTICIAL_PRUEBAS);
        Intent i = new Intent(getActivity(), ReproductorActivity.class);
        i.putExtra("title", seriesVistas.get(position).getTitular());
        i.putExtra("img", seriesVistas.get(position).getImg());
        i.putExtra("url", seriesVistas.get(position).getUrl());
        i.putExtra("server", seriesVistas.get(position).getServidor());
        i.putExtra("position", seriesVistas.get(position).getPosicion());
        i.putExtra("episodios", seriesVistas.get(position).getEpisodes());

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
}
package com.sentry504.scraperprueba;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyListaActivity extends Fragment implements adapterMyListaRecycler.OnItemClickListener {
    RecyclerView rv_item;

    // ArrayList para almacenar las series vistas
    private static List<SeriesModel> seriesVistas = new ArrayList<>();
    private static List<SeriesModel> seriesVistasOriginal = new ArrayList<>();
    private ValueEventListener valueEventListener;
    private DatabaseReference myListRef;
    private static adapterMyListaRecycler adapter;
    private InterstitialAd interstitialAd;
    public AdRequest adRequest;
    private static final String ID_INTERSTICIAL_PRUEBAS = "ca-app-pub-3940256099942544/1033173712";
    private static final String ID_INTERSTICIAL_NAVIGATION_MENU = "ca-app-pub-1883323185290636/7691438519";

    private SearchView searchViewMyList;
    private ImageView imgBtnFiltersShow;
    private LinearLayout lyFilters;
    private ImageButton imgBtnFilterName;

    //filters
    private SharedPreferences sharedPreferences;
    private static boolean filterName = false;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_lista_fragment, container, false);
        MobileAds.initialize(requireContext(), initializationStatus -> {});
        adRequest = new AdRequest.Builder().build();

        requireActivity().setTitle("Mi lista");

        rv_item = view.findViewById(R.id.rv_item);
        searchViewMyList = view.findViewById(R.id.searchViewMyList);
        lyFilters = view.findViewById(R.id.lyFilters);
        imgBtnFiltersShow = view.findViewById(R.id.imgBtnFiltersShow);
        imgBtnFilterName = view.findViewById(R.id.imgBtnFilterName);

        llenarRecycler();

        sharedPreferences = requireActivity().getSharedPreferences("Filters",MODE_PRIVATE);


        searchViewMyList.setOnClickListener(v -> searchViewMyList.setIconified(false));
        searchViewMyList.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrarPorTitular(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarPorTitular(newText);
                return false;
            }
        });

        imgBtnFiltersShow.setOnClickListener(v->{
            if(lyFilters.getVisibility() == View.VISIBLE){
                lyFilters.setVisibility(View.GONE);
            }else{
                lyFilters.setVisibility(View.VISIBLE);
            }
        });

        imgBtnFilterName.setOnClickListener(v->{
            filterName = sharedPreferences.getBoolean("filterName", false);
            ordenarPorTitular(adapter.myList, filterName);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("filterName", !filterName);
            editor.apply();
            adapter.notifyDataSetChanged();
        });

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public static void filtrarPorTitular(String textoBusqueda) {
        // Si el texto de búsqueda está vacío, regresar la lista original
        int longitude = textoBusqueda.length();

        adapter.myList.clear();

        if (longitude == 0) {
            adapter.myList.addAll(seriesVistasOriginal);
        } else {
            String txtBuscarLowerCase = textoBusqueda.toLowerCase();

            for (SeriesModel c : seriesVistasOriginal) {
                if (c.getTitular().concat(c.getServidor()).toLowerCase().contains(txtBuscarLowerCase)) {
                    adapter.myList.add(c);
                }
            }
        }
        ordenarPorTitular(adapter.myList,filterName);
        adapter.notifyDataSetChanged();
    }

    public static void ordenarPorTitular(List<SeriesModel> lista, boolean ascendente) {
        // Definir el comparador para ordenar por titular
        Comparator<SeriesModel> comparador = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            comparador = Comparator.comparing(SeriesModel::getTitular);
        }

        // Si el orden es descendente, invertir el comparador
        if (!ascendente) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                comparador = comparador.reversed();
            }
        }

        // Ordenar la lista usando el comparador
        Collections.sort(lista, comparador);
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
                        adapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
                    });
                    builder.setPositiveButton("Sí", (dialog, which) -> {
                        int position = viewHolder.getBindingAdapterPosition();

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
                seriesVistasOriginal.clear();
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
                seriesVistasOriginal.addAll(seriesVistas);
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

    @Override
    public void OnItemClick(View view, int position) {
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
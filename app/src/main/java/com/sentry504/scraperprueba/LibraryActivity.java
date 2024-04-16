package com.sentry504.scraperprueba;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sentry504.scraperprueba.common.LevenshteinDistance;
import com.sentry504.scraperprueba.common.NoconexionFragment;
import com.sentry504.scraperprueba.libraryepisodes.LibraryAdapter;
import com.sentry504.scraperprueba.libraryepisodes.LibraryAdapterPagination;
import com.sentry504.scraperprueba.libraryepisodes.LibraryItem;
import com.sentry504.scraperprueba.libraryepisodes.LibraryItemPagination;
import com.sentry504.scraperprueba.libraryepisodes.LibraryScraping;

import java.util.ArrayList;

public class LibraryActivity extends Fragment implements
        LibraryAdapter.OnItemClickListener,
        LibraryAdapterPagination.OnPaginationClickListener {
    private String servidor = "";
    private String urlRaiz = "";
    private String itemPaginationSelected = "";
    private int columnas = 0;
    private LinearLayout lyInfoConnection;
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    RecyclerView recyclerViewContenido;
    RecyclerView recyclerViewPagination;
    SearchView searchViewLibrary;
    private LibraryAdapter adapter;
    private LibraryAdapterPagination adapterPagination;
    private final ArrayList<LibraryItem> libraryItems = new ArrayList<>();
    private final LibraryScraping libraryScraping = new LibraryScraping();
    private final ArrayList<LibraryItemPagination> libraryItemsPagination = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            servidor= getArguments().getString("servidor");
        }
        {
            levenshteinDistance.setWords(servidor, "JKanime");
            if (levenshteinDistance.getAfinidad()==1.0) {
                urlRaiz = "https://jkanime.net/directorio/1/";
            }
            levenshteinDistance.setWords(servidor, "monoschinos");
            if (levenshteinDistance.getAfinidad()==1.0) {
                urlRaiz = "https://monoschinos2.com/buscar?q=a";
                //urlRaiz = "https://m.animeflv.net/browse";
            }
            levenshteinDistance.setWords(servidor, "AnimeFLV");
            if (levenshteinDistance.getAfinidad()==1.0) {
                urlRaiz = "https://m.animeflv.net/browse";
            }
        }
        if(getArguments()  != null){
            if(getArguments().getString("url") != null){
                urlRaiz = getArguments().getString("url");
                itemPaginationSelected = getArguments().getString("posicion");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.library_fragment, container, false);

        requireActivity().setTitle("Libreria");

        progressBar = view.findViewById(R.id.progressBar);
        recyclerViewContenido = view.findViewById(R.id.recyclerViewEpisodios);
        recyclerViewPagination = view.findViewById(R.id.recyclerViewPagination);
        lyInfoConnection = view.findViewById(R.id.lyInfoConnection);
        searchViewLibrary = view.findViewById(R.id.searchVeiwLibrary);
        searchViewLibrary.setOnClickListener(v-> searchViewLibrary.setIconified(false));
        searchViewLibrary.clearFocus();
        searchViewLibrary.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                levenshteinDistance.setWords(servidor, "JKanime");
                if (levenshteinDistance.getAfinidad()==1.0) {
                    urlRaiz = "https://jkanime.net/buscar/"+query+"/1/";
                }
                levenshteinDistance.setWords(servidor, "monoschinos");
                if (levenshteinDistance.getAfinidad()==1.0) {
                    //urlRaiz = "https://monoschinos2.com/buscar?q=" + query;
                    urlRaiz = "https://m.animeflv.net/browse?q=" + query;
                }
                levenshteinDistance.setWords(servidor, "AnimeFLV");
                if (levenshteinDistance.getAfinidad()==1.0) {
                    urlRaiz = "https://m.animeflv.net/browse?q=" + query;
                }
                libraryItems.clear();

                adapter.notifyDataSetChanged();

                Content content = new Content();
                content.execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        recyclerViewContenido.setLayoutManager(new GridLayoutManager(
                this.getContext(),
                numberColumns(),
                GridLayoutManager.VERTICAL,
                false
        ));
        recyclerViewPagination.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));

        adapter = new LibraryAdapter(libraryItems, this.getContext());
        adapterPagination = new LibraryAdapterPagination(libraryItemsPagination, this.getContext(), servidor);

        Content content = new Content();
        content.execute();

        adapter.setOnItemCLickListener(this);
        adapterPagination.setOnPaginationCLickListener(this);

        recyclerViewContenido.setAdapter(adapter);
        recyclerViewPagination.setAdapter(adapterPagination);

        return view;
    }

    public int numberColumns() {
        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){ return 2;}
        return 1;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration myConfig) {
        super.onConfigurationChanged(myConfig);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            columnas = 2;
        } else {
            columnas = 1;
        }
        recyclerViewContenido.setLayoutManager(new GridLayoutManager(
                this.getContext(),
                columnas,
                GridLayoutManager.VERTICAL,
                false
        ));
        adapter = new LibraryAdapter(libraryItems, this.getContext());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void OnItemClick(View view, int position) {
        Bundle args = new Bundle();
        args.putString("servidor", servidor) ;
        args.putString("title", libraryItems.get(position).getTitle());
        args.putString("urlActualLibrary", urlRaiz);
        args.putString("posicion", itemPaginationSelected);

        if (view.getParent()!= recyclerViewPagination){
            InfoAnimeActivity infoAnimeActivity = new InfoAnimeActivity();
            infoAnimeActivity.setArguments(args);

            levenshteinDistance.setWords(servidor, "AnimeFLV");
            if (levenshteinDistance.getAfinidad()==1.0) {
                args.putString(
                        "url",
                        "https://m.animeflv.net" + libraryItems.get(position).getEpisodeUrl()
                );
            }
            levenshteinDistance.setWords(servidor, "monoschinos");
            if (levenshteinDistance.getAfinidad()==1.0) {
                args.putString(
                        "url",
                        "https://m.animeflv.net" + libraryItems.get(position).getEpisodeUrl()
                );
            }
            levenshteinDistance.setWords(servidor, "JKanime");
            if (levenshteinDistance.getAfinidad()==1.0) {
                args.putString("url", libraryItems.get(position).getEpisodeUrl()) ;
            }

            NetworkInfo networkInfo;
            ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(CONNECTIVITY_SERVICE);
            networkInfo = connectivityManager.getActiveNetworkInfo();

            if ((networkInfo == null)) {
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,new NoconexionFragment()).commit();
                Toast.makeText(requireActivity(), "Parece que no tienes conexion a internet", Toast.LENGTH_LONG).show();
            }else{
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, infoAnimeActivity).commit();
            }
        }else{
            levenshteinDistance.setWords(servidor, "JKanime");
            if (levenshteinDistance.getAfinidad()==1.0) {
                urlRaiz = libraryItemsPagination.get(position).getUrlPaginado();
            }
            levenshteinDistance.setWords(servidor, "monoschinos");
            if (levenshteinDistance.getAfinidad()==1.0) {
                //urlRaiz = libraryItemsPagination.get(position).getUrlPaginado();
                urlRaiz = "https://m.animeflv.net" + libraryItemsPagination.get(position).getUrlPaginado();
            }
            levenshteinDistance.setWords(servidor, "AnimeFLV");
            if (levenshteinDistance.getAfinidad()==1.0) {
                urlRaiz = "https://m.animeflv.net" + libraryItemsPagination.get(position).getUrlPaginado();
            }
            itemPaginationSelected = libraryItemsPagination.get(position).getButtonText();
            libraryItems.clear();
            libraryItemsPagination.clear();

            Content content = new Content();
            content.execute();
        }
    }

    private class Content extends AsyncTask<Void,Void,ArrayList<LibraryItem>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            //progressBar.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        }

        @Override
        protected ArrayList<LibraryItem> doInBackground(Void... voids) {
            libraryItems.addAll(libraryScraping.serverContenido(servidor, urlRaiz));
            libraryItemsPagination.addAll(libraryScraping.serverPaginado(servidor, urlRaiz));
            return libraryItems;
        }

        @Override
        protected void onPostExecute(ArrayList<LibraryItem> aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            if(aVoid.isEmpty()){
                lyInfoConnection.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "No se obtuvieron resultados", Toast.LENGTH_SHORT).show();
            }else {
                for (int i = 0; i < libraryItemsPagination.size(); i++) {
                    libraryItemsPagination.get(i).setButtonSelected(libraryItemsPagination.get(i).getButtonText().equals(itemPaginationSelected));
                }
                //progressBar.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
                adapter.notifyDataSetChanged();
                adapterPagination.notifyDataSetChanged();
            }
        }
    }
}
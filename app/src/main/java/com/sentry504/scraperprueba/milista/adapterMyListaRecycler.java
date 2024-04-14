package com.sentry504.scraperprueba.milista;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sentry504.scraperprueba.R;
import com.sentry504.scraperprueba.infoepisode.ParseAdapterInfoEpisode;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class adapterMyListaRecycler extends RecyclerView.Adapter<adapterMyListaRecycler.ViewHolder> {
    private static adapterMyListaRecycler.OnItemClickListener mListener;
    public interface OnItemClickListener{
        void OnItemClick(View view, int position);
    }
    public List<SeriesModel> myList = new ArrayList<>();
    public adapterMyListaRecycler(List<SeriesModel> myList){
        this.myList = myList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textViewTitleMyList;
        TextView textViewServidor;
        TextView textViewEpisodeMyList;
        TextView textViewEpisodesMyList;
        ImageView imageViewMyListPlay;
        ImageView imageViewMyList;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> mListener.OnItemClick(v, getAdapterPosition()));

            textViewTitleMyList = itemView.findViewById(R.id.textViewTitleMyList);
            textViewServidor = itemView.findViewById(R.id.textViewServidor);
            textViewEpisodeMyList = itemView.findViewById(R.id.textViewEpisodeMyList);
            textViewEpisodesMyList = itemView.findViewById(R.id.textViewEpisodesMyList);
            imageViewMyListPlay = itemView.findViewById(R.id.imageViewMyListPlay);
            imageViewMyList = itemView.findViewById(R.id.imageViewMyList);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemActivity = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_my_lista_item, parent, false);
        return new ViewHolder(itemActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull adapterMyListaRecycler.ViewHolder holder, int position) {
        holder.textViewTitleMyList.setText(myList.get(position).getTitular());
        holder.textViewServidor.setText(myList.get(position).getServidor());
        holder.textViewEpisodeMyList.setText(String.valueOf(myList.get(position).getPosicion()));
        holder.textViewEpisodesMyList.setText(String.valueOf(myList.get(position).getEpisodes()));
        if(myList.get(position).getPosicion() == myList.get(position).getEpisodes()){
            holder.imageViewMyListPlay.setVisibility(View.INVISIBLE);
        }else{
            holder.imageViewMyListPlay.setVisibility(View.VISIBLE);
        }
        Picasso.get().load(myList.get(position).getImg()).into(holder.imageViewMyList);
    }

    @Override
    public int getItemCount() {
        return myList.size();
    }

    public void setOnItemCLickListener(adapterMyListaRecycler.OnItemClickListener OnItemClick){
        mListener = OnItemClick;
    }
}
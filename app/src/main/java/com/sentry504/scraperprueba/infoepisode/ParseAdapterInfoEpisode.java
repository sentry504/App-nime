package com.sentry504.scraperprueba.infoepisode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sentry504.scraperprueba.R;

import java.util.ArrayList;

public class ParseAdapterInfoEpisode extends RecyclerView.Adapter<ParseAdapterInfoEpisode.ViewHolder> {
    private static OnItemClickListener mListener;
    public interface OnItemClickListener{
        void OnItemClick(View view, int position);
    }

    private final ArrayList<ParseItemInfoEpisode> parseItemInfoEpisodes;
    private final Context context;

    public ParseAdapterInfoEpisode(ArrayList<ParseItemInfoEpisode> parseItemInfoEpisodes, Context context) {
        this.parseItemInfoEpisodes = parseItemInfoEpisodes;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textViewEpisodio;
        ImageView icono;
        public ViewHolder(@NonNull View view) {
            super(view);
            view.setOnClickListener(v -> mListener.OnItemClick(v, getAbsoluteAdapterPosition()));
            textViewEpisodio = view.findViewById(R.id.textViewInfoEpisode);
            icono = view.findViewById(R.id.imageView2);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_info_anime_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParseItemInfoEpisode parseItemInfoEpisode = parseItemInfoEpisodes.get(position);
        holder.textViewEpisodio.setText(parseItemInfoEpisode.getNombre());
        /*
        if (parseItemInfoEpisode.getVisto()){
            holder.icono.setImageResource(R.drawable.baseline_remove_red_eye_24);
        }else{
            holder.icono.setImageResource(R.drawable.baseline_play_circle_24);
        }
         */

    }
    @Override
    public int getItemCount() {
        return parseItemInfoEpisodes.size();
    }

    public void setOnItemCLickListener(OnItemClickListener OnItemClick){
        mListener = OnItemClick;
    }
}

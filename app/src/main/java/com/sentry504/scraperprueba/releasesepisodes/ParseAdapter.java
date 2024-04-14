package com.sentry504.scraperprueba.releasesepisodes;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sentry504.scraperprueba.R;
import com.sentry504.scraperprueba.ReleasesActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ParseAdapter  extends RecyclerView.Adapter<ParseAdapter.ViewHolder> {
    private static OnItemClickListener mListener;

    public interface OnItemClickListener{
        void OnItemClick(View view, int position);
    }
    private final ArrayList<ParseItem> parseItems;
    public ParseAdapter(ArrayList<ParseItem> parseItems) {
        this.parseItems = parseItems;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textViewTitle;
        TextView textViewEpisode;

        public ViewHolder(@NonNull View view) {
            super(view);

            view.setOnClickListener(v -> mListener.OnItemClick(v, getAbsoluteAdapterPosition()));

            imageView = view.findViewById(R.id.imageViewMyList);
            textViewTitle = view.findViewById(R.id.textViewTitleMyList);
            textViewEpisode = view.findViewById(R.id.textViewEpisodeMyList);
        }
    }

    @NonNull
    @Override
    public ParseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_season, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ParseAdapter.ViewHolder holder, int position) {
        ParseItem parseItem = parseItems.get(position);
        holder.textViewTitle.setText(parseItem.getTitle());
        holder.textViewEpisode.setText(("Episodio " + parseItem.getEpisode()));
        Picasso.get().load(parseItem.getImgUrl()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return parseItems.size();
    }

    public void setOnItemCLickListener(OnItemClickListener OnItemClick){
        mListener = OnItemClick;
    }
}

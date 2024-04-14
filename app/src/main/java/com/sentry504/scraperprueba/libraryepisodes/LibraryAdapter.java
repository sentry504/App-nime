package com.sentry504.scraperprueba.libraryepisodes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sentry504.scraperprueba.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {
    private static OnItemClickListener mListener;

    public interface OnItemClickListener{
        void OnItemClick(View view, int position);
    }

    private final ArrayList<LibraryItem> libraryItems;
    private final Context context;

    public LibraryAdapter(ArrayList<LibraryItem> LibraryItems, Context context) {
        this.libraryItems = LibraryItems;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textViewTitle;
        TextView textViewCategoria;

        public ViewHolder(@NonNull View view) {
            super(view);

            view.setOnClickListener(v -> mListener.OnItemClick(v, getAbsoluteAdapterPosition()));

            imageView = view.findViewById(R.id.imageViewMyList);
            textViewTitle = view.findViewById(R.id.textViewTitle);
            textViewCategoria = view.findViewById(R.id.textViewCategoria);
        }

    }

    @NonNull
    @Override
    public LibraryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_library_item_season, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryAdapter.ViewHolder holder, int position) {
        LibraryItem Item = libraryItems.get(position);
        holder.textViewTitle.setText(Item.getTitle());
        holder.textViewCategoria.setText(Item.getCategoria());
        Picasso.get().load(Item.getImgUrl()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return libraryItems.size();
    }

    public void setOnItemCLickListener(OnItemClickListener OnItemClick){
        mListener = OnItemClick;
    }
}

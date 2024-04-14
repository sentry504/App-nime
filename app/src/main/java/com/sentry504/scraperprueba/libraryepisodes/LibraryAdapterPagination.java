package com.sentry504.scraperprueba.libraryepisodes;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sentry504.scraperprueba.R;
import com.sentry504.scraperprueba.common.LevenshteinDistance;

import java.util.ArrayList;

public class LibraryAdapterPagination extends RecyclerView.Adapter<LibraryAdapterPagination.ViewHolder> {
    private static OnPaginationClickListener mListener;

    public interface OnPaginationClickListener{
        void OnItemClick(View view, int position);
    }

    private final ArrayList<LibraryItemPagination> libraryItems;
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    private final Context context;
    private final String servidor;

    public LibraryAdapterPagination(ArrayList<LibraryItemPagination> LibraryItems, Context context, String servidor) {
        this.libraryItems = LibraryItems;
        this.context = context;
        this.servidor = servidor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView textViewPagination;
        public ViewHolder(@NonNull View view) {
            super(view);

            view.setOnClickListener(v -> mListener.OnItemClick(v, getAbsoluteAdapterPosition()));

            textViewPagination = view.findViewById(R.id.textViewTitleMyList);
        }

    }

    @NonNull
    @Override
    public LibraryAdapterPagination.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_library_pagination_season, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryAdapterPagination.ViewHolder holder, int position) {
        LibraryItemPagination Item = libraryItems.get(position);
        holder.textViewPagination.setText(Item.getButtonText());
        holder.textViewPagination.setBackgroundColor(Item.getButtonSelected()? Color.GREEN:Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return libraryItems.size();
    }

    public void setOnPaginationCLickListener(OnPaginationClickListener OnItemClick){
        mListener = OnItemClick;
    }
}

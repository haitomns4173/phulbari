package com.haitomns.phulbari;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FlowerAdapter extends RecyclerView.Adapter<FlowerAdapter.FlowerViewHolder> {

    private List<Flower> flowerList;
    private Context context;

    public FlowerAdapter(List<Flower> flowerList) {
        this.flowerList = flowerList;
    }

    @NonNull
    @Override
    public FlowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_flower, parent, false);
        return new FlowerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlowerViewHolder holder, int position) {
        Flower flower = flowerList.get(position);
        holder.flowerNameTextView.setText(flower.getName());

        // Load image from assets
        try {
            InputStream inputStream = holder.itemView.getContext().getAssets().open("flowers/" + flower.getImageName());
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            holder.flowerImageView.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set click listener to open FlowerDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FlowerDetailActivity.class);
            intent.putExtra("flower_name", flower.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return flowerList.size();
    }

    public static class FlowerViewHolder extends RecyclerView.ViewHolder {
        TextView flowerNameTextView;
        ImageView flowerImageView;

        public FlowerViewHolder(View itemView) {
            super(itemView);
            flowerNameTextView = itemView.findViewById(R.id.flower_name);
            flowerImageView = itemView.findViewById(R.id.flower_image);
        }
    }
}

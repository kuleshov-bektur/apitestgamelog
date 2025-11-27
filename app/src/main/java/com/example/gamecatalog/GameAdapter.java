package com.example.gamecatalog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private final List<Game> games;

    public GameAdapter(List<Game> games) {
        this.games = games;
    }

    @Override
    public GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GameViewHolder holder, int position) {
        Game game = games.get(position);

        holder.tvTitle.setText(game.getTitle());

        StringBuilder info = new StringBuilder(game.getPlatform() + " • " + game.getYear());
        if (game.getDeveloper() != null && !game.getDeveloper().trim().isEmpty()) {
            info.append(" • ").append(game.getDeveloper());
        }
        holder.tvPlatform.setText(info.toString());

        holder.tvGenre.setText(game.getGenre());
        holder.tvDescription.setText(game.getDescription());

        // Разворачиваем описание по клику
        holder.tvDescription.setOnClickListener(v -> {
            if (holder.tvDescription.getMaxLines() == 3) {
                holder.tvDescription.setMaxLines(200);
            } else {
                holder.tvDescription.setMaxLines(3);
            }
        });

        // Только placeholder — без error()
        Glide.with(holder.itemView.getContext())
                .load(game.getCover())
                .placeholder(R.drawable.placeholder_game)
                .into(holder.ivCover);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPlatform, tvGenre, tvDescription;
        ImageView ivCover;

        GameViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPlatform = itemView.findViewById(R.id.tvPlatform);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivCover = itemView.findViewById(R.id.ivCover);
        }
    }
}
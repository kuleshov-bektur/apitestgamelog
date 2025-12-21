package com.example.gamecatalog.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gamecatalog.R;
import com.example.gamecatalog.model.Game;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private List<Game> games = new ArrayList<>();

    private OnItemClickListener itemClickListener;
    private OnFavoriteClickListener favoriteClickListener;
    private OnEditClickListener editClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(Game game);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Game game);
    }

    public interface OnEditClickListener {
        void onEditClick(Game game);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Game game);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteClickListener = listener;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public void setGames(List<Game> newGames) {
        this.games.clear();
        if (newGames != null) {
            this.games.addAll(newGames);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);

        // Название
        holder.tvTitle.setText(game.getTitle());

        // Год и разработчик
        StringBuilder info = new StringBuilder()
                .append(game.getYear());

        if (game.getDeveloper() != null && !game.getDeveloper().trim().isEmpty()) {
            info.append(" • ").append(game.getDeveloper());
        }
        holder.tvInfo.setText(info.toString());

        // Жанр
        holder.tvGenre.setText(game.getGenre());

        // Иконка платформы
        String platformUrl = getPlatformIconUrl(game.getPlatform());
        Glide.with(holder.itemView.getContext())
                .load(platformUrl)
                .placeholder(R.drawable.placeholder_platform)
                .error(R.drawable.ic_error_platform)
                .into(holder.ivPlatform);

        // Иконка избранного
        holder.ivFavorite.setImageResource(
                game.isFavorite()
                        ? android.R.drawable.btn_star_big_on
                        : android.R.drawable.btn_star_big_off
        );

        // Обложка игры
        Glide.with(holder.itemView.getContext())
                .load(game.getCover())
                .placeholder(R.drawable.placeholder_game)
                .into(holder.ivCover);

        // Пользовательский рейтинг (только в избранном)
        if (game.isFavorite() && game.getRating() > 0) {
            holder.tvUserRating.setVisibility(View.VISIBLE);
            holder.tvUserRating.setText(String.format("★ %.1f", game.getRating()));
        } else {
            holder.tvUserRating.setVisibility(View.GONE);
        }

        // Клик по карточке — детали
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(game);
            }
        });

        // Клик по звезде — избранное
        holder.ivFavorite.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                boolean newFavorite = !game.isFavorite();
                favoriteClickListener.onFavoriteClick(game);

                String toastText = newFavorite
                        ? "«" + game.getTitle() + "» добавлена в избранное"
                        : "«" + game.getTitle() + "» убрана из избранного";

                Toast.makeText(v.getContext(), toastText, Toast.LENGTH_SHORT).show();
            }
        });

        // Долгое нажатие — редактирование или удаление
        holder.itemView.setOnLongClickListener(v -> {
            final CharSequence[] options = {"Редактировать", "Удалить", "Отмена"};

            new AlertDialog.Builder(v.getContext())
                    .setTitle(game.getTitle())
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) { // Редактировать
                            if (editClickListener != null) {
                                editClickListener.onEditClick(game);
                            }
                        } else if (which == 1) { // Удалить
                            new AlertDialog.Builder(v.getContext())
                                    .setTitle("Удалить игру?")
                                    .setMessage("«" + game.getTitle() + "» будет безвозвратно удалена")
                                    .setPositiveButton("Удалить", (d, w) -> {
                                        if (deleteClickListener != null) {
                                            deleteClickListener.onDeleteClick(game);
                                        }
                                        Toast.makeText(v.getContext(), "Игра удалена", Toast.LENGTH_SHORT).show();
                                    })
                                    .setNegativeButton("Отмена", null)
                                    .show();
                        }
                        // Отмена — ничего
                    })
                    .show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInfo, tvGenre, tvUserRating;
        ImageView ivCover, ivPlatform, ivFavorite;

        GameViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvUserRating = itemView.findViewById(R.id.tvUserRating);
            ivCover = itemView.findViewById(R.id.ivCover);
            ivPlatform = itemView.findViewById(R.id.ivPlatform);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }

    private String getPlatformIconUrl(String platform) {
        if (platform == null) {
            return "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a4/No_image_available.svg/640px-No_image_available.svg.png";
        }

        switch (platform.trim()) {
            case "NES":
                return "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/NES_logo.svg/2560px-NES_logo.svg.png";
            case "SNES":
                return "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/SNES_logo.svg/1280px-SNES_logo.svg.png";
            case "Nintendo 64":
                return "https://upload.wikimedia.org/wikipedia/en/thumb/2/2d/Nintendo_64_%28logo%29.svg/1200px-Nintendo_64_%28logo%29.svg.png";
            case "GameCube":
                return "https://upload.wikimedia.org/wikipedia/commons/thumb/7/79/GC_Logo.svg/1200px-GC_Logo.svg.png";
            case "Wii":
                return "https://upload.wikimedia.org/wikipedia/commons/thumb/b/bc/Wii.svg/2560px-Wii.svg.png";
            case "Wii U":
                return "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/WiiU.svg/1024px-WiiU.svg.png?20240809170226";
            case "Nintendo Switch":
                return "https://upload.wikimedia.org/wikipedia/commons/3/38/Nintendo_switch_logo.png";
            default:
                return "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a4/No_image_available.svg/640px-No_image_available.svg.png";
        }
    }
}
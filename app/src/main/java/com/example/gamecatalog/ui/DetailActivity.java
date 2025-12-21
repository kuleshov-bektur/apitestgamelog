package com.example.gamecatalog.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.gamecatalog.model.Game;
import com.example.gamecatalog.R;
import com.google.android.material.textfield.TextInputEditText;

public class DetailActivity extends AppCompatActivity {

    private Game game;
    private GameListViewModel viewModel;

    private ImageView ivCover;
    private TextView tvTitle, tvInfo, tvGenre, tvDescription;
    private RatingBar ratingBar;
    private TextInputEditText editComment;
    private Button btnFavorite, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Получаем игру из Intent
        game = (Game) getIntent().getSerializableExtra("game");
        if (game == null) {
            finish();
            return;
        }

        // ViewModel (та же, что и в MainActivity)
        viewModel = new ViewModelProvider(this).get(GameListViewModel.class);

        initViews();
        displayGameData();
        setupButtons();
    }

    private void initViews() {
        ivCover = findViewById(R.id.ivCover);
        tvTitle = findViewById(R.id.tvTitle);
        tvInfo = findViewById(R.id.tvInfo);
        tvGenre = findViewById(R.id.tvGenre);
        tvDescription = findViewById(R.id.tvDescription);
        ratingBar = findViewById(R.id.ratingBar);
        editComment = findViewById(R.id.editComment);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnSave = findViewById(R.id.btnSave);
    }

    private void displayGameData() {
        Glide.with(this)
                .load(game.getCover())
                .placeholder(R.drawable.placeholder_game)
                .into(ivCover);

        tvTitle.setText(game.getTitle());

        StringBuilder info = new StringBuilder()
                .append(game.getPlatform())
                .append(" • ")
                .append(game.getYear());
        if (game.getDeveloper() != null && !game.getDeveloper().isEmpty()) {
            info.append(" • ").append(game.getDeveloper());
        }
        tvInfo.setText(info.toString());

        tvGenre.setText("Жанр: " + game.getGenre());
        tvDescription.setText(game.getDescription());

        //Cохранённые пользователем данные
        ratingBar.setRating(game.getRating());
        if (game.getComment() != null) {
            editComment.setText(game.getComment());
        }

        updateFavoriteButton();
    }

    private void setupButtons() {
        //Кнопка избранного
        btnFavorite.setOnClickListener(v -> {
            game.setFavorite(!game.isFavorite());
            viewModel.setFavorite(game.getId(), game.isFavorite());
            updateFavoriteButton();

            String toastText = game.isFavorite() ?
                    "Добавлено в избранное" :
                    "Убрано из избранного";
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        });

        //Кнопка сохранить (рейтинг + комментарий)
        btnSave.setOnClickListener(v -> {
            game.setRating(ratingBar.getRating());
            game.setComment(editComment.getText().toString());
            viewModel.editGame(game);

            Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateFavoriteButton() {
        if (game.isFavorite()) {
            btnFavorite.setText("Убрать из избранного");
        } else {
            btnFavorite.setText("Добавить в избранное");
        }
    }
}
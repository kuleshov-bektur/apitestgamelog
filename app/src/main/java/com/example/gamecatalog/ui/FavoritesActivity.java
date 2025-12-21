package com.example.gamecatalog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamecatalog.R;
import com.example.gamecatalog.model.Game;

public class FavoritesActivity extends AppCompatActivity {

    private GameListViewModel viewModel;
    private GameAdapter adapter;

    private RecyclerView recyclerView;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Избранное");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerFavorites);
        textEmpty = findViewById(R.id.textEmpty);

        // Адаптер
        adapter = new GameAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(GameListViewModel.class);

        // Наблюдение за избранными играми
        viewModel.getFavoriteGames().observe(this, games -> {
            if (games == null || games.isEmpty()) {
                textEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setGames(games);
            }
        });

        // Клик по карточке — детали
        adapter.setOnItemClickListener(game -> {
            Intent intent = new Intent(FavoritesActivity.this, DetailActivity.class);
            intent.putExtra("game", game);
            startActivity(intent);
        });

        // Клик по звезде — удаление из избранного (только из избранного)
        adapter.setOnFavoriteClickListener(game -> {
            viewModel.setFavorite(game.getId(), false);
            Toast.makeText(this, "«" + game.getTitle() + "» убрана из избранного", Toast.LENGTH_SHORT).show();
        });

        // Редактирование по long click — через отдельный DialogFragment
        adapter.setOnEditClickListener(game -> {
            GameEditDialogFragment.newInstance(game)
                    .show(getSupportFragmentManager(), "edit_game");
        });

        // Удаление по long click — полное удаление из каталога
        adapter.setOnDeleteClickListener(game -> {
            viewModel.deleteGame(game.getId());
            Toast.makeText(this, "«" + game.getTitle() + "» удалена из каталога", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
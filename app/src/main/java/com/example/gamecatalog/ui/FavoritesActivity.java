package com.example.gamecatalog.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;

import android.content.Intent;

import com.example.gamecatalog.R;

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

        // Тот же адаптер, что и в MainActivity
        adapter = new GameAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Та же ViewModel
        viewModel = new ViewModelProvider(this).get(GameListViewModel.class);

        //Только за избранными играми
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

        //Клик по карточке — переход в детали
        adapter.setOnItemClickListener(game -> {
            Intent intent = new Intent(FavoritesActivity.this, DetailActivity.class);
            intent.putExtra("game", game);
            startActivity(intent);
        });

        //Клик по звезде — удаление из избранного
        adapter.setOnFavoriteClickListener(game -> {
            viewModel.setFavorite(game.getId(), false);
            // LiveData автоматически обновит список
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
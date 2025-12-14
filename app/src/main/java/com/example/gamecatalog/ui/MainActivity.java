package com.example.gamecatalog.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamecatalog.R;
import com.example.gamecatalog.model.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private GameListViewModel viewModel;
    private GameAdapter adapter;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textEmpty;

    private final List<Game> fullList = new ArrayList<>();
    private final List<Game> currentList = new ArrayList<>();
    private String currentQuery = "";
    private SortMode sortMode = SortMode.NONE;

    // Фильтры
    private final Set<String> selectedPlatforms = new HashSet<>();
    private final Set<String> selectedGenres = new HashSet<>();

    private static final String[] PLATFORMS = {
            "NES", "SNES", "Nintendo 64", "GameCube", "Wii", "Wii U", "Nintendo Switch"
    };

    private static final String[] GENRES = {
            "2D-платформер", "3D-платформер", "Экшен-приключение", "RPG",
            "Шутер", "Файтинг", "Гонки", "Симулятор жизни", "Стратегия в реальном времени"
    };

    private enum SortMode {
        NONE, TITLE_ASC, YEAR_DESC, PLATFORM_ASC
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Каталог видеоигр");
        }

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        textEmpty = findViewById(R.id.textEmpty);

        adapter = new GameAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(game -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("game", game);
            startActivity(intent);
        });

        adapter.setOnFavoriteClickListener(game -> {
            viewModel.setFavorite(game.getId(), !game.isFavorite());
            applyFiltersAndSort();
        });

        viewModel = new ViewModelProvider(this).get(GameListViewModel.class);

        viewModel.getAllGames().observe(this, games -> {
            fullList.clear();
            if (games != null) {
                fullList.addAll(games);
            }
            progressBar.setVisibility(View.GONE);
            applyFiltersAndSort();
        });

        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Поиск по названию");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return true; }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText.trim().toLowerCase();
                applyFiltersAndSort();
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) { return true; }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                currentQuery = "";
                applyFiltersAndSort();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            return true;
        }

        if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        }

        if (id == R.id.action_filter) {
            showFilterDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] options = {
                "По названию (А → Я)",
                "По году (новые сначала)",
                "По платформе (А → Я)"
        };

        // Определяем текущий выбранный пункт (0 — по названию, 1 — по году, 2 — по платформе)
        int checkedItem;
        switch (sortMode) {
            case TITLE_ASC:
                checkedItem = 0;
                break;
            case YEAR_DESC:
                checkedItem = 1;
                break;
            case PLATFORM_ASC:
                checkedItem = 2;
                break;
            default:
                checkedItem = -1; // ничего не выбрано (радиокнопки будут пустыми)
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle("Сортировка")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sortMode = SortMode.TITLE_ASC;
                            break;
                        case 1:
                            sortMode = SortMode.YEAR_DESC;
                            break;
                        case 2:
                            sortMode = SortMode.PLATFORM_ASC;
                            break;
                    }
                })
                .setPositiveButton("Применить", (dialog, which) -> applyFiltersAndSort())
                .setNegativeButton("Отмена", (dialog, which) -> {
                    // При отмене ничего не меняем — сортировка остаётся прежней
                })
                .setNeutralButton("Сбросить", (dialog, which) -> {
                    sortMode = SortMode.NONE;
                    applyFiltersAndSort();
                })
                .show();
    }

    private void showFilterDialog() {
        // Кастомный view для диалога
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 24, 32, 24);

        // Заголовок платформ
        TextView tvPlatforms = new TextView(this);
        tvPlatforms.setText("Платформы");
        tvPlatforms.setTextSize(18);
        tvPlatforms.setPadding(0, 0, 0, 16);
        layout.addView(tvPlatforms);

        CheckBox[] platformCheckBoxes = new CheckBox[PLATFORMS.length];
        for (int i = 0; i < PLATFORMS.length; i++) {
            CheckBox cb = new CheckBox(this);
            cb.setText(PLATFORMS[i]);
            cb.setChecked(selectedPlatforms.contains(PLATFORMS[i]));
            platformCheckBoxes[i] = cb;
            layout.addView(cb);
        }

        // Заголовок жанров
        TextView tvGenres = new TextView(this);
        tvGenres.setText("Жанры");
        tvGenres.setTextSize(18);
        tvGenres.setPadding(0, 32, 0, 16);
        layout.addView(tvGenres);

        CheckBox[] genreCheckBoxes = new CheckBox[GENRES.length];
        for (int i = 0; i < GENRES.length; i++) {
            CheckBox cb = new CheckBox(this);
            cb.setText(GENRES[i]);
            cb.setChecked(selectedGenres.contains(GENRES[i]));
            genreCheckBoxes[i] = cb;
            layout.addView(cb);
        }

        scrollView.addView(layout);

        new AlertDialog.Builder(this)
                .setTitle("Фильтр")
                .setView(scrollView)
                .setPositiveButton("Применить", (dialog, which) -> {
                    // Сохраняем платформы
                    selectedPlatforms.clear();
                    for (int i = 0; i < platformCheckBoxes.length; i++) {
                        if (platformCheckBoxes[i].isChecked()) {
                            selectedPlatforms.add(PLATFORMS[i]);
                        }
                    }

                    // Сохраняем жанры
                    selectedGenres.clear();
                    for (int i = 0; i < genreCheckBoxes.length; i++) {
                        if (genreCheckBoxes[i].isChecked()) {
                            selectedGenres.add(GENRES[i]);
                        }
                    }

                    applyFiltersAndSort();
                })
                .setNeutralButton("Сбросить", (dialog, which) -> {
                    selectedPlatforms.clear();
                    selectedGenres.clear();
                    applyFiltersAndSort();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void applyFiltersAndSort() {
        currentList.clear();
        currentList.addAll(fullList);

        // Поиск
        if (!currentQuery.isEmpty()) {
            currentList.removeIf(game ->
                    game.getTitle() == null || !game.getTitle().toLowerCase().contains(currentQuery));
        }

        // Фильтр по платформам
        if (!selectedPlatforms.isEmpty()) {
            currentList.removeIf(game -> !selectedPlatforms.contains(game.getPlatform()));
        }

        // Фильтр по жанрам
        if (!selectedGenres.isEmpty()) {
            currentList.removeIf(game -> !selectedGenres.contains(game.getGenre()));
        }

        // Сортировка
        switch (sortMode) {
            case TITLE_ASC:
                Collections.sort(currentList, (g1, g2) ->
                        safeString(g1.getTitle()).compareToIgnoreCase(safeString(g2.getTitle())));
                break;
            case YEAR_DESC:
                Collections.sort(currentList, (g1, g2) -> Integer.compare(g2.getYear(), g1.getYear()));
                break;
            case PLATFORM_ASC:
                Collections.sort(currentList, (g1, g2) ->
                        safeString(g1.getPlatform()).compareToIgnoreCase(safeString(g2.getPlatform())));
                break;
            case NONE:
            default:
                break;
        }

        adapter.setGames(currentList);

        if (currentList.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private String safeString(String s) {
        return s == null ? "" : s;
    }
}
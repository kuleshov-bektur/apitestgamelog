package com.example.gamecatalog.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.gamecatalog.model.Game;
import com.example.gamecatalog.data.AppDatabase;
import com.example.gamecatalog.data.GameDao;
import com.example.gamecatalog.data.GameRepository;

import java.util.List;

public class GameListViewModel extends AndroidViewModel {

    private final GameRepository repository;

    // LiveData для наблюдения в Activity
    private final LiveData<List<Game>> allGames;
    private final LiveData<List<Game>> favoriteGames;

    public GameListViewModel(@NonNull Application application) {
        super(application);

        // Получение DAO и создание репозитория
        GameDao gameDao = AppDatabase.getInstance(application).gameDao();
        repository = new GameRepository(gameDao);

        // Подписка на данные из Room
        allGames = repository.getAllGames();
        favoriteGames = repository.getFavoriteGames();

        // При создании ViewModel сразу загружаем данные с сервера (один раз)
        repository.refreshGames();
    }

    // Методы для получения LiveData
    public LiveData<List<Game>> getAllGames() {
        return allGames;
    }

    public LiveData<List<Game>> getFavoriteGames() {
        return favoriteGames;
    }

    public LiveData<List<Game>> searchByTitle(String query) {
        return repository.searchByTitle(query);
    }

    // Переключение избранного
    public void setFavorite(int gameId, boolean isFavorite) {
        repository.setFavorite(gameId, isFavorite);
    }

    // Добавление новой игры
    public void addGame(Game game) {
        repository.addGame(game);
    }

    // Редактирование существующей игры
    public void editGame(Game game) {
        repository.editGame(game);
    }

    // Удаление игры по ID
    public void deleteGame(int id) {
        repository.deleteGame(id);
    }
}
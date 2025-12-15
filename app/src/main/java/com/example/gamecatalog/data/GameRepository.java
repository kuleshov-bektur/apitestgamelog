package com.example.gamecatalog.data;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.gamecatalog.model.Game;
import com.example.gamecatalog.network.ApiService;
import com.example.gamecatalog.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameRepository {

    private static final String TAG = "GameRepository";

    private final GameDao gameDao;
    private final ApiService apiService;

    public GameRepository(GameDao gameDao) {
        this.gameDao = gameDao;
        this.apiService = RetrofitClient.getApiService();
    }

    public LiveData<List<Game>> getAllGames() {
        return gameDao.getAllGames();
    }

    public LiveData<List<Game>> getFavoriteGames() {
        return gameDao.getFavoriteGames();
    }

    public LiveData<List<Game>> searchByTitle(String query) {
        return gameDao.searchByTitle(query);
    }

    //Загрузка данных из сети и сохранение в Room
    public void refreshGames() {
        apiService.getGames().enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Ошибка загрузки: " + response.code());
                    return;
                }

                List<Game> newList = response.body();

                new Thread(() -> {
                    //Вызов текущих данных из Room (с избранным, рейтингом, комментарием)
                    List<Game> oldList = gameDao.getAllSync();

                    //Перенос локальных полей в новые объекты
                    for (Game newGame : newList) {
                        for (Game oldGame : oldList) {
                            if (newGame.getId() == oldGame.getId()) {
                                newGame.setFavorite(oldGame.isFavorite());
                                newGame.setRating(oldGame.getRating());
                                newGame.setComment(oldGame.getComment());
                                break;
                            }
                        }
                    }

                    gameDao.insertAll(newList);
                }).start();
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e(TAG, "Сетевая ошибка: " + t.getMessage());
            }
        });
    }

    // Быстрое переключение избранного
    public void setFavorite(int gameId, boolean isFavorite) {
        new Thread(() -> gameDao.setFavorite(gameId, isFavorite)).start();
    }

    //Обновление игры (после изменения рейтинга или комментария)
    public void updateGame(Game game) {
        new Thread(() -> gameDao.update(game)).start();
    }
}
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

    // Получение списков
    public LiveData<List<Game>> getAllGames() {
        return gameDao.getAllGames();
    }

    public LiveData<List<Game>> getFavoriteGames() {
        return gameDao.getFavoriteGames();
    }

    public LiveData<List<Game>> searchByTitle(String query) {
        return gameDao.searchByTitle(query);
    }

    // Загрузка и синхронизация с сервера
    public void refreshGames() {
        apiService.getGames().enqueue(new Callback<List<Game>>() {
            @Override
            public void onResponse(Call<List<Game>> call, Response<List<Game>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Ошибка загрузки игр: " + response.code());
                    return;
                }

                List<Game> newList = response.body();

                new Thread(() -> {
                    List<Game> oldList = gameDao.getAllSync();

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
                    Log.d(TAG, "Данные успешно обновлены из сети (" + newList.size() + " игр)");
                }).start();
            }

            @Override
            public void onFailure(Call<List<Game>> call, Throwable t) {
                Log.e(TAG, "Сетевая ошибка при загрузке: " + t.getMessage());
            }
        });
    }

    // Изменение избранного
    public void setFavorite(int gameId, boolean isFavorite) {
        new Thread(() -> {
            gameDao.setFavorite(gameId, isFavorite);
            Log.d(TAG, "Избранное изменено для ID " + gameId + ": " + isFavorite);
        }).start();
    }

    // Добавление новой игры
    public void addGame(Game game) {
        apiService.addGame(game).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Игра успешно отправлена на сервер (ID: " + game.getId() + ")");
                } else {
                    Log.w(TAG, "Сервер вернул ошибку при добавлении, но сохраняем локально");
                }
                // В любом случае — сохраняем в Room
                new Thread(() -> gameDao.insert(game)).start();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.w(TAG, "Ошибка сети при добавлении, сохраняем локально: " + t.getMessage());
                new Thread(() -> gameDao.insert(game)).start();
            }
        });
    }

    // Редактирование игры
    public void editGame(Game game) {
        apiService.updateGame(game.getId(), game).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Игра успешно обновлена на сервере (ID: " + game.getId() + ")");
                } else {
                    Log.w(TAG, "Сервер вернул ошибку при обновлении, но обновляем локально");
                }
                new Thread(() -> gameDao.update(game)).start();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.w(TAG, "Ошибка сети при обновлении, обновляем локально: " + t.getMessage());
                new Thread(() -> gameDao.update(game)).start();
            }
        });
    }

    // Удаление игры
    public void deleteGame(int id) {
        apiService.deleteGame(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Игра успешно удалена на сервере (ID: " + id + ")");
                } else {
                    Log.w(TAG, "Сервер вернул ошибку при удалении, но удаляем локально");
                }
                new Thread(() -> gameDao.deleteById(id)).start();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.w(TAG, "Ошибка сети при удалении, удаляем локально: " + t.getMessage());
                new Thread(() -> gameDao.deleteById(id)).start();
            }
        });
    }
}
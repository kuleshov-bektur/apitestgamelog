package com.example.gamecatalog.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gamecatalog.model.Game;

import java.util.List;

@Dao
public interface GameDao {

    // Получить все игры
    @Query("SELECT * FROM games ORDER BY title ASC")
    LiveData<List<Game>> getAllGames();

    // Получить только избранные игры
    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY title ASC")
    LiveData<List<Game>> getFavoriteGames();

    // Поиск по названию (без учета регистра)
    @Query("SELECT * FROM games WHERE title LIKE '%' || :query || '%' ORDER BY title ASC")
    LiveData<List<Game>> searchByTitle(String query);

    // Вставить/обновить весь список игр из сети
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Game> games);

    // Обновить одну игру (после изменения рейтинга или комментария)
    @Update
    void update(Game game);

    // Быстрое изменение статуса избранного
    @Query("UPDATE games SET isFavorite = :isFavorite WHERE id = :id")
    void setFavorite(int id, boolean isFavorite);

    @Query("SELECT * FROM games")
    List<Game> getAllSync();

    @Query("DELETE FROM games WHERE id = :id")
    void deleteById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Game game);
}
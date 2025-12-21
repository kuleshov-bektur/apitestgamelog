package com.example.gamecatalog.network;

import com.example.gamecatalog.model.Game;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.List;

public interface ApiService {

    // Получение всех игр
    @GET("allgames")
    Call<List<Game>> getGames();

    // Добавление новой игры
    @POST("games")
    Call<Void> addGame(@Body Game game);

    // Редактирование существующей игры
    @PUT("games/{id}")
    Call<Void> updateGame(@Path("id") int id, @Body Game game);

    // Удаление игры по ID
    @DELETE("games/{id}")
    Call<Void> deleteGame(@Path("id") int id);
}
package com.example.gamecatalog.network;

import com.example.gamecatalog.model.Game;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;

public interface ApiService {
    @GET("allgames")
    Call<List<Game>> getGames();
}
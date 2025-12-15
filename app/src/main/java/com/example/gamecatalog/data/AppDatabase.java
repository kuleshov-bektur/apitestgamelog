package com.example.gamecatalog.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gamecatalog.model.Game;

@Database(entities = {Game.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // DAO
    public abstract GameDao gameDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "game_catalog_db"  // имя файла базы данных
                            )
                            .fallbackToDestructiveMigration()  // при обновлении версии очистит базу
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
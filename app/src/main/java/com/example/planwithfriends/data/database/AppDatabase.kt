package com.example.planwithfriends.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.planwithfriends.data.database.dao.EventDao
import com.example.planwithfriends.data.database.dao.GroupDao
import com.example.planwithfriends.data.database.dao.UserDao
import com.example.planwithfriends.data.database.entity.EventEntity
import com.example.planwithfriends.data.database.entity.GroupEntity
import com.example.planwithfriends.data.database.entity.UserEntity
import com.example.planwithfriends.data.database.entity.UserGroupCrossRef
import androidx.room.TypeConverters // Importul este aici

@Database(
    entities = [
        EventEntity::class,
        GroupEntity::class,
        UserEntity::class,
        UserGroupCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun groupDao(): GroupDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "plan_with_friends_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
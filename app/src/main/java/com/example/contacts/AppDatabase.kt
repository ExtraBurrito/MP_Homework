package com.example.contacts

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import  androidx.room.*

@Database(entities = [Contact::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    companion object{
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context) : AppDatabase =
            INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "contacts.db"
                    ).build().also { INSTANCE = it }
            }

    }

}
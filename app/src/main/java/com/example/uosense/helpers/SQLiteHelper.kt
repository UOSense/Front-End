package com.example.uosense.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, "users.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                nickname TEXT NOT NULL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun insertUser(email: String, password: String, nickname: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("email", email)
            put("password", password) // 실제로는 암호화된 비밀번호 저장 필요
            put("nickname", nickname)
        }
        val result = db.insert("users", null, values)
        return result != -1L
    }

    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM users WHERE email = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists


    }
    fun isNicknameExists(nickname: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM users WHERE nickname = ?"
        val cursor = db.rawQuery(query, arrayOf(nickname))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists


    }
    fun validateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM users WHERE email = ? AND password = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val isValid = cursor.moveToFirst() // 데이터가 있으면 true, 없으면 false
        cursor.close()
        return isValid
    }

}

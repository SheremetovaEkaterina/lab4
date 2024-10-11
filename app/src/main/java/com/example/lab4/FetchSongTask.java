package com.example.lab4;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchSongTask extends AsyncTask<Void, Void, String> {
    private final Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;
    MainActivity MainActivity;
   // private MainActivity activity;


    @Override
    protected String doInBackground(Void... voids) {
        try {
            // URL запроса
            URL url = new URL("https://www.loveradio.ru/backend/api/v1/love-radio/player/online?filter[musicStreamIds][]=28");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            // Чтение ответа
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public FetchSongTask(Context context) {
        this.context = context;
    }


    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                // Парсим JSON-ответ
                JSONObject jsonObject = new JSONObject(result);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray historyArray = data.getJSONArray("playerHistory");
                if (historyArray.length() > 0) {
                    JSONObject firstSong = historyArray.getJSONObject(0);
                    String track = firstSong.getString("title");
                    if(!isLastSongInDatabase(track)) {
                        dbHelper = new DBHelper(context);
                        db = dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("TrackTitle", track);
                        db.insert("songs", null, values);
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).loadSongsFromDatabase();
                        }
                    }// Обновляем список песен

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }





    private boolean isLastSongInDatabase(String track) {
        dbHelper = new DBHelper(context);
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM songs ORDER BY ID DESC LIMIT 1", null);

        boolean exists = false;
        if (cursor.moveToFirst()) {
            String lastTrack = cursor.getString(1);
            exists = lastTrack.equals(track);
        }

        cursor.close();
        db.close();
        return exists; // Возвращаем true, если такая песня уже есть
    }
}


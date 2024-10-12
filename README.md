# Лабораторная работа №4. Взаимодействие с сервером
- _Выполнила:_ Шереметова
- _Язык программирования:_ Java
- _API:_ GET https://www.loveradio.ru/backend/api/v1/love-radio/player/online?filter[musicStreamIds][]=28

## Что делает приложение?
Приложение состоит из 1 экрана, на котором выводится история прослушивания треков на love радио.
<p align="center">
    <img src="https://github.com/user-attachments/assets/f312e8b0-8061-41ec-94ae-37e6d8505ddd" width="350"> 
</p> 

---
## Как приложение выполняет свою функцию?
### Проверка подключения к интернету 📶 
Отправка запросов серверу возможна только при наличии интернета, поэтому при открытии приложения требуется сразу проверять наличие подключения к сети:
- если интернет есть, то мы можем получать данные от сервера и обновлять БД;
- если интернета нет, то мы можем только отобразить те данные, которые есть в БД, но без добавления новых => отображается тост "Запуск в автономном режиме".
<p align="center">
    <img src="https://github.com/user-attachments/assets/99d67f10-d6f4-4615-9cca-397459bfa5c8" width="350"> 
</p> 

---
### Взаимодействие с сервером
#### Периодичность отправки запросов
Для поддержания истории в актуальном состоянии нужно отправлять запросы с определенной периодичностью, мною было выбрано значение 3 минуты (чаще делать нет смысла, т.к. данные так быстро не изменятся, а реже - можно пропустить некоторые песни).
```java
private void startFetchingSongs() {
        final Runnable fetchTask = new Runnable() {
            @Override
            public void run() {
                new FetchSongTask(MainActivity.this).execute(); 
                handler.postDelayed(this, 180000); // задание периодичности отправки запроса
            }
        };
        handler.post(fetchTask);
    }
```


#### Внутренняя работа с запросом
##### 1. Подключение к серверу
Подключение к серверу осуществялется при помощи метода `doInBackgroung`, в котором указывается путь для отправки запроса и указание, что нужно получить ответ.
```java
URL url = new URL("https://www.loveradio.ru/backend/api/v1/love-radio/player/online?filter[musicStreamIds][]=28"); // путь для отправки запроса
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); // метод, по которому происходит подключение к серверу
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
```

##### 2. Обработка ответа
- Ответ от сервера приходит в формате JSON, а чтобы получить необходимые данные нужно распарсить полученные значения. В моем случае нужный параметр `title` хранится внтури элемента массива `historyProgram`, который в свою очередь вложен в объект `data`.
```java
// Парсим JSON-ответ
                JSONObject jsonObject = new JSONObject(result); // result переменная, которая хранит весь полученный json
                JSONObject data = jsonObject.getJSONObject("data"); // проваливаемся в объект data
                JSONArray historyArray = data.getJSONArray("playerHistory"); // проваливаемся в массив historyProgram
                if (historyArray.length() > 0) {
                    JSONObject firstSong = historyArray.getJSONObject(0); // проваливаемся в 1й элемент массива historyProgram
                    String track = firstSong.getString("title"); // извлечение значения параметра title
```

- Нужное значение извлекли, поэтому следующий шаг - добавление этого в БД. 
```java
dbHelper = new DBHelper(context);
                        db = dbHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("TrackTitle", track);
                        db.insert("songs", null, values);
```

- После добавления требуется отобразить обновленную БД.
```java
void loadSongsFromDatabase() {
        Cursor cursor = db.rawQuery("SELECT * FROM songs", null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String track = cursor.getString(cursor.getColumnIndex("TrackTitle"));
                songList.add(track);
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }
```
<p align="center">
  <b>УРА-УРА приложение, которое взаимодействует с сервером, готово 🎉🎊</b>
</p>


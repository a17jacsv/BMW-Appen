package com.example.jacobsvensson.bmw_appen;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.example.jacobsvensson.bmw_appen.R.id.list_view;


public class MainActivity extends AppCompatActivity {

    private boolean ischecked = false;

    protected ArrayList<Models> bmwlist = new ArrayList<>();
    ListView myListView;

    private ArrayAdapter adapter;
    BMWReaderDbHelper mDbHelper;


    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mDbHelper = new BMWReaderDbHelper(this);

        // Gets the data repository in write mode
        db = mDbHelper.getWritableDatabase();

        adapter = new ArrayAdapter(getApplicationContext(), R.layout.list_item_textview, R.id.my_item_textview, bmwlist);

        myListView = (ListView)findViewById(list_view);
        myListView.setAdapter(adapter);

        Brorsan getJson = new Brorsan();
        getJson.execute();

        ListView myListView = (ListView) findViewById(list_view);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Toast nedan:
                Toast.makeText(getApplicationContext(), bmwlist.get(position).utmatare(), Toast.LENGTH_SHORT).show();
            }
        });


        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Intent intent = new Intent(getApplicationContext(), ModelActivity.class);
                Bundle extras = new Bundle();

                String name = bmwlist.get(position).getName();
                String location = bmwlist.get(position).getLocation();
                String horsepower = Integer.toString(bmwlist.get(position).getHorsepower());
                String url = bmwlist.get(position).getImage();

                extras.putString("MODEL_NAME", name);
                extras.putString("MODEL_LOCATION", location);
                extras.putString("MODEL_HORSEPOWER", horsepower);
                extras.putString("MODEL_IMAGE", url);

                intent.putExtras(extras);
                getApplicationContext().startActivity(intent);


            }
        });
    }

    public void testmethod() {
        SQLiteDatabase db1 = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                BMWReaderContract.BMWEntry.COLUMN_NAME_NAME,
                BMWReaderContract.BMWEntry.COLUMN_NAME_LOCATION,
                BMWReaderContract.BMWEntry.COLUMN_NAME_HORSEPOWER,
                BMWReaderContract.BMWEntry.COLUMN_NAME_IMAGEURL,
                BMWReaderContract.BMWEntry.COLUMN_NAME_WIKIURL
        };

        // Filter results WHERE "title" = 'My Title'
        //String selection = BMWReaderContract.BMWEntry.COLUMN_NAME_NAME + " = ?";
        //String[] selectionArgs = { "K2" };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = BMWReaderContract.BMWEntry.COLUMN_NAME_NAME  + " ASC";
        if (ischecked) {
            sortOrder = BMWReaderContract.BMWEntry.COLUMN_NAME_NAME  + " DESC";
        }

        Log.d("hej", "test" + ischecked);

        Cursor cursor = db1.query(
                BMWReaderContract.BMWEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order

        );

        Log.d("testkebe", cursor.toString());

        adapter.clear();

        while(cursor.moveToNext()) {
            cursor.getColumnIndexOrThrow(BMWReaderContract.BMWEntry._ID);
            String name = cursor.getString(cursor.getColumnIndexOrThrow(BMWReaderContract.BMWEntry.COLUMN_NAME_NAME));
            int horsepower = cursor.getInt(cursor.getColumnIndexOrThrow(BMWReaderContract.BMWEntry.COLUMN_NAME_HORSEPOWER));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(BMWReaderContract.BMWEntry.COLUMN_NAME_LOCATION));
            String url = cursor.getString(cursor.getColumnIndexOrThrow(BMWReaderContract.BMWEntry.COLUMN_NAME_IMAGEURL));

            Models bilar = new Models(name, horsepower, location, url);

            Log.d("hejhej", name + ":" + horsepower);

            adapter.add(bilar);
        }
        cursor.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_malgrupp) {
            bmwlist.clear();
            new Brorsan().execute();
            Toast audience = Toast.makeText(this, "The target audience for this application is car interested people who loves BMW.", Toast.LENGTH_LONG);
            audience.show();

            return true;
        }
        if (id == R.id.action_sortera1) {
            ischecked = true;
            testmethod();

            Toast refreshed = Toast.makeText(this, "List has been sorted!", Toast.LENGTH_SHORT);
            refreshed.show();

            return true;
        }
        if (id == R.id.action_sortera2) {
            ischecked = false;
            testmethod();

            Toast refreshed = Toast.makeText(this, "List has been sorted!", Toast.LENGTH_SHORT);
            refreshed.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class Brorsan extends AsyncTask {

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            String s = new String(o.toString());
            Log.d("Jacob","DataFetched"+s);

            try {
                JSONArray modeldata = new JSONArray(s);

                for(int i = 0; i < modeldata.length(); i++){
                    JSONObject model = modeldata.getJSONObject(i);

                    String name = model.getString("name");
                    String location = model.getString("location");
                    int horsepower = model.getInt("size");

                    String auxdata = model.getString("auxdata");
                    JSONObject aux = new JSONObject(auxdata);
                    String url = aux.getString("img");
                    Models m = new Models(name, horsepower, location, url);
                    bmwlist.add(m);

                    ContentValues values = new ContentValues();
                    values.put(BMWReaderContract.BMWEntry.COLUMN_NAME_NAME, name);
                    values.put(BMWReaderContract.BMWEntry.COLUMN_NAME_LOCATION, location);
                    values.put(BMWReaderContract.BMWEntry.COLUMN_NAME_HORSEPOWER, horsepower);
                    values.put(BMWReaderContract.BMWEntry.COLUMN_NAME_WIKIURL, url);
                    values.put(BMWReaderContract.BMWEntry.COLUMN_NAME_IMAGEURL, auxdata);

                    db.insert(BMWReaderContract.BMWEntry.TABLE_NAME, null, values);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            adapter = new ArrayAdapter(getApplicationContext(), R.layout.list_item_textview, R.id.my_item_textview, bmwlist);

            myListView = (ListView)findViewById(list_view);
            myListView.setAdapter(adapter);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonStr = null;

            try {
                // Construct the URL for the php-service
                URL url = new URL("https://wwwlab.iit.his.se/brom/kurser/mobilprog/dbservice/admin/getdataasjson.php?type=a17jacsv");

                // Create the request to the PHP-service, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
                return jsonStr;

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Network error", "Error closing stream", e);
                    }
                }
            }
        }
    }
}
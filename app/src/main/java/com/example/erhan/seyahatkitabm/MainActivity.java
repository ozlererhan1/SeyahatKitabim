package com.example.erhan.seyahatkitabm;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
 static  ArrayList<String>names=new ArrayList<String>();
  static   ArrayList<LatLng>locations=new ArrayList<LatLng>();
  static ArrayAdapter arrayAdapter;

// Menümüzü Bağlıyoruz
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.add_place,menu);
        return super.onCreateOptionsMenu(menu);

    }
// Menümüze Tıklandığında Ne yapılacağını yazıyoruz
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item.getItemId()==R.id.add_place){

            Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView=(ListView)findViewById(R.id.listview);

        try {
            MapsActivity.database=this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            Cursor cursor=MapsActivity.database.rawQuery("SELECT * FROM places",null);
            int nameıx=cursor.getColumnIndex("name");
            int latitude=cursor.getColumnIndex("latitude");
            int longitude=cursor.getColumnIndex("longitude");

            while (cursor.moveToNext()){
                String nameFromDatabase=cursor.getString(nameıx);
                String latitudeDatabase=cursor.getString(latitude);
                String longitudedatabase=cursor.getString(longitude);
                System.out.println("name"+nameFromDatabase);

                names.add(nameFromDatabase);
                Double l1=Double.parseDouble(latitudeDatabase);
                Double l2=Double.parseDouble(longitudedatabase);

                LatLng locationfromdatabase=new LatLng(l1,l2);
                locations.add(locationfromdatabase);


            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,names);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("position",position);
                startActivity(intent);


            }
        });

    }

}

package yama_chi.n.shiftworkcalendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class EntryActivity extends AppCompatActivity {

    private Button mAddButton;
    private ListView mListView;
    private ArrayList<String> mTitleList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        SharedPreferences pref = getSharedPreferences("preset", MODE_PRIVATE);
        Gson gson = new Gson();
        mTitleList = gson.fromJson(pref.getString("title", ""), new TypeToken<ArrayList<String>>() {
        }.getType());

        mListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mTitleList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                Intent intent = new Intent(getApplicationContext(), EntryAddActivity.class);
                intent.putExtra("newCase", 0);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        mAddButton = findViewById(R.id.addButton);
        mAddButton.setText("追加");
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTitleList.size() > 8) {
                    Toast.makeText(EntryActivity.this, "すでに最大まで登録されています。", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), EntryAddActivity.class);
                    intent.putExtra("newCase", 1);
                    startActivity(intent);

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("root","root!");

        SharedPreferences pref = getSharedPreferences("preset", MODE_PRIVATE);
        Gson gson = new Gson();
        mTitleList = gson.fromJson(pref.getString("title", ""), new TypeToken<ArrayList<String>>() {
        }.getType());

        mListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mTitleList);
        mListView.setAdapter(adapter);

    }
}

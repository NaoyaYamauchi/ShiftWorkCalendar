package yama_chi.n.shiftworkcalendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;

public class EntryActivity extends AppCompatActivity {

    private Button mAddButton;
    private ListView mListView;
    private ArrayList<String> titleList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        SharedPreferences pref = getSharedPreferences("preset", MODE_PRIVATE);
        Gson gson = new Gson();
        titleList = gson.fromJson(pref.getString("title", ""), new TypeToken<ArrayList<String>>(){}.getType());

        mListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,titleList);
        mListView.setAdapter(adapter);

        mAddButton=findViewById(R.id.addButton);
        mAddButton.setText("追加");
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),EntryAddActivity.class);
                startActivity(intent);
            }
        });
    }

    private void SharedPreferensesLoad(){

        }

}

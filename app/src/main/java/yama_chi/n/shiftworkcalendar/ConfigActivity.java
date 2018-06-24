package yama_chi.n.shiftworkcalendar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ConfigActivity extends AppCompatActivity {
    private ListView mListView;
    private final String[] mMenuList = {"アカウント名","登録する通知の時間","リマインド方法"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        mListView = findViewById(R.id.listView);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mMenuList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        //アカウント名、登録する通知の時間（24h1h,30minなど）、リマインド方法（メールか通知、あるいは両方）


    }
}

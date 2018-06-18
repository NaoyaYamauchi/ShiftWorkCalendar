package yama_chi.n.shiftworkcalendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class EntryActivity extends AppCompatActivity {

    private Button mAddButton;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        mListView = (ListView) findViewById(R.id.listView);



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

}

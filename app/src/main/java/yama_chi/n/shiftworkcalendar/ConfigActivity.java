package yama_chi.n.shiftworkcalendar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigActivity extends AppCompatActivity {
    private final String[] mMenuList = {"アカウント"};
    private ListView mListView;
    private ArrayList<String> mSubList = new ArrayList<String>();
    private String mAccountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Intent intent = getIntent();
        String account = intent.getStringExtra("account");

        mListView = findViewById(R.id.listView);

        mSubList.add(account);
        //mSubList.add("30分");
        //mSubList.add("メール");

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (int i = 0; i < mMenuList.length; i++) {
            Map<String, String> item = new HashMap<String, String>();
            item.put("Main", mMenuList[i]);
            item.put("Sub", mSubList.get(i));
            data.add(item);
        }


        SimpleAdapter adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2
                , new String[]{"Main", "Sub"}, new int[]{android.R.id.text1, android.R.id.text2});
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                System.out.println(position);
                switch (position) {
                    case 0:
                        SharedPreferences sharedPreferences = getSharedPreferences("accountName", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String str = sharedPreferences.getString("accountName",null);
                        System.out.println(str);

                        new AlertDialog.Builder(view.getContext())
                                .setMessage("現在設定されているアカウントと連携を解除しますか？")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new AlertDialog.Builder(view.getContext())
                                                .setMessage("連携を解除しました。予定登録画面に戻って再度選択してください")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // OK button pressed
                                                    }
                                                })
                                                .show();
                                        SharedPreferences sharedPreferences = getSharedPreferences("accountName", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("accountName",null);
                                        editor.apply();
                                        String str = sharedPreferences.getString("accountName",null);
                                        System.out.println(str);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();


                        break;
                    case 1:
                        String[] timeString = {"開始時", "5分前", "10分前", "15分前", "30分前", "45分前", "1時間前", "1時間30分前", "2時間前", "3時間前", "12時間前", "1日前"};
                        int defaultTime = 0; // デフォルトでチェックされているアイテム
                        final List<Integer> checkedTime = new ArrayList<>();
                        checkedTime.add(defaultTime);
                        new AlertDialog.Builder(view.getContext())
                                .setTitle("通知タイミング")
                                .setSingleChoiceItems(timeString, defaultTime, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        checkedTime.clear();
                                        checkedTime.add(which);
                                    }
                                })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!checkedTime.isEmpty()) {
                                            Log.d("checkedItem:", "" + checkedTime.get(0));

                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        break;
                    case 2:
                        final String[] items = {"プッシュ通知", "メール", "両方"};
                        int defaultItem = 0; // デフォルトでチェックされているアイテム
                        final List<Integer> checkedItems = new ArrayList<>();
                        checkedItems.add(defaultItem);
                        new AlertDialog.Builder(view.getContext())
                                .setTitle("通知方法")
                                .setSingleChoiceItems(items, defaultItem, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        checkedItems.clear();
                                        checkedItems.add(which);
                                    }
                                })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!checkedItems.isEmpty()) {
                                            Log.d("checkedItem:", "" + checkedItems.get(0));

                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                        break;
                }
            }
        });
    }
}

package yama_chi.n.shiftworkcalendar;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int x = displayMetrics.widthPixels;
        int y = displayMetrics.heightPixels;

        int a = displayMetrics.densityDpi;
        float b = displayMetrics.density;

        System.out.println(x+":"+y);

        System.out.println(a+":"+b);

        TextView textView1 =findViewById(R.id.textView1);
        textView1.setText("「シフトパターン登録ツール」をお使いいただき、ありがとうございます。\n" +
                "このアプリはGoogleカレンダーまでデータを登録することに特化したツールとなります。\n\n" +
                "カレンダーの日付をタップしてシフト名を押すと、黄色く塗られている日付に予定が入ります。\n\n" +
                "予定を入れたら、黄色い「登録」ボタンを押すことでGoogleカレンダーの「WorkShift」に予定が登録されます。\n\n" +
                "新たに予定を追加したり名前や時間を変更する場合は「パターン登録」を押してください。\n\n" +
                "不具合など見つけましたら下記メールアドレスまでお伝えいただけますと幸いです。\n\n" +
                "yamachienu@gmail.com");


    }
}

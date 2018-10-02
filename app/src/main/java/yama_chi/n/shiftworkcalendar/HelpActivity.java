package yama_chi.n.shiftworkcalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        System.out.println(x + ":" + y);

        System.out.println(a + ":" + b);

        TextView textView0 = findViewById(R.id.textView0);
        textView0.setText("「シフトパターン登録ツール」をお使いいただき、ありがとうございます。\n\n" +
                "このアプリはGoogleカレンダーまでデータを登録することに特化したツールとなります。\n");


        TextView textView1 = findViewById(R.id.textView1);
        textView1.setText("アプリの使い方\n");

        TextView textView2 = findViewById(R.id.textView2);
        textView2.setText("カレンダーの日付をタップしてシフト名が書かれたボタンを押すと、タップした日付に予定が入ります。");

        TextView textView3 = findViewById(R.id.textView3);
        textView3.setText("ボタンを押すと、次の日付が自動的に選択されます。ここでも同様にボタンを押すとシフトが登録されて次の日付が選択されます。\n\n" +
                "もし押すボタンを間違えた場合は、その日付をタップしてから改めてボタンを押すと反映されます。\n\n" +
                "最終日に到達すると、下の画像のようなダイアログが表示されます。\n\n");

        TextView textView4 = findViewById(R.id.textView4);
        textView4.setText("ここで「OK」を押すことで、現在表示されているシフトがGoogleカレンダーに登録されます。\n" +
                "最終日まで到達していない場合でも左下の「登録」を押すことでGoogleカレンダーに登録されます。\n\n");

        TextView textView5 = findViewById(R.id.textView5);
        textView5.setText("パターンの追加方法\n");

        TextView textView6 = findViewById(R.id.textView6);
        textView6.setText("パターンの追加や登録方法は「パターン登録」ボタンから行えます。\n");
        TextView textView7 = findViewById(R.id.textView7);
        textView7.setText("シフトを変更する場合はシフト名をタップしてください。新たに追加する場合は「追加」を押してください。\n");


        TextView textView8 = findViewById(R.id.textView8);
        textView8.setText("シフト名を登録・変更画面が表示されますので\n\n" +
                "・タイトル\n" +
                "・勤務時間\n" +
                "・休みの設定\n\n" +
                "を設定して「OK」を押してください。");

        TextView textView9 = findViewById(R.id.textView9);
        textView9.setText("終了時間を開始時間より前の時間にした場合は、日付を跨いで予定が登録されます。夜勤の方でもお使いいただけます。\n" +
                "「休み」をオンにしている場合は登録時に時間帯が空になります。ボタンを押しても登録されません。\n" +
                "登録可能なシフトパターンは8個までとなります。\n\n" +
                "以上がこのアプリの使い方となります。不具合を見つけましたら、下記メールアドレスまで報告いただけますと幸いです。\n\n" +
                "enuyamachi@gmail.com");


    }
}

package yama_chi.n.shiftworkcalendar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView textView1 =findViewById(R.id.textView1);
        textView1.setText("「シフトパターン登録ツール」をお使いいただき、ありがとうございます。\n" +
                "このアプリはGoogleカレンダーまでデータを登録するツールとなります。\n\n" +
                "カレンダーの日付をタップしてシフト名を押すと、黄色く塗られている日付に予定が入ります。\n\n" +
                "予定を入れたら、黄色い「登録」ボタンを押すことでGoogleカレンダーの「WorkShift」に予定が登録されます。\n\n" +
                "新たに予定を追加したり名前や時間を変更する場合は「パターン登録」を押してください。");


    }
}

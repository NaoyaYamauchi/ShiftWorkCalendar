package yama_chi.n.shiftworkcalendar;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class EntryAddActivity extends AppCompatActivity {
    private String mTitle;
    private int mStartHour, mStartMinute, mEndHour, mEndMinute;
    private boolean mStartTimePicked, mEndTimePicked, mHoliday, mNotice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_add);

        final EditText titleEditText = findViewById(R.id.titleEditText);
        final TextView startTime = findViewById(R.id.startTime);
        final TextView endTime = findViewById(R.id.endTime);
        final TextView cautionText = findViewById(R.id.cautionText);
        Switch holidaySwitch = findViewById(R.id.switch_holiday);
        Switch noticeSwitch = findViewById(R.id.switch_notice);
        Button cancelButton = findViewById(R.id.cancel_button);
        Button OKButton = findViewById(R.id.ok_button);


        //開始時間
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(EntryAddActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mStartHour = hourOfDay;
                        mStartMinute = minute;
                        String timeString = String.format("%02d", mStartHour) + ":" + String.format("%02d", mStartMinute);
                        startTime.setText(timeString);

                        if (!mEndTimePicked) {
                            mEndHour = hourOfDay + 9;
                            if (mEndHour >= 24) {
                                mEndHour -= 24;
                            }
                            mEndMinute = minute;
                            String timeStringEnd = String.format("%02d", mEndHour) + ":" + String.format("%02d", mEndMinute);
                            endTime.setText(timeStringEnd);

                        }
                        mStartTimePicked = true;
                        if (mStartHour > mEndHour) {
                            cautionText.setText("日付をまたいでいます");
                        } else {
                            cautionText.setText("");

                        }
                    }
                }, mStartHour, mStartMinute, false);
                timePickerDialog.show();


            }
        });
        //終了時間
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(EntryAddActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mEndHour = hourOfDay;
                        mEndMinute = minute;
                        String timeString = String.format("%02d", mEndHour) + ":" + String.format("%02d", mEndMinute);
                        endTime.setText(timeString);
                        if (!mStartTimePicked) {
                            mStartHour = hourOfDay - 9;
                            if (mStartHour <= 0) {
                                mStartHour += 24;
                            }
                            mStartMinute = minute;
                            String timeStringStart = String.format("%02d", mStartHour) + ":" + String.format("%02d", mStartMinute);
                            startTime.setText(timeStringStart);
                        }
                        if (mStartHour > mEndHour) {
                            cautionText.setText("日付をまたいでいます");
                        } else {
                            cautionText.setText("");

                        }
                        mEndTimePicked = true;

                    }
                }, mEndHour, mEndMinute, false);
                timePickerDialog.show();
            }
        });
        //休日
        holidaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String timeStringEnd = "--:--";
                    endTime.setText(timeStringEnd);
                    String timeStringStart = "--:--";
                    startTime.setText(timeStringStart);
                    mHoliday = true;

                } else {
                    String timeString = String.format("%02d", mStartHour) + ":" + String.format("%02d", mStartMinute);
                    startTime.setText(timeString);
                    String timeStringEnd = String.format("%02d", mEndHour) + ":" + String.format("%02d", mEndMinute);
                    endTime.setText(timeStringEnd);
                    mHoliday = false;
                }
                Toast.makeText(
                        EntryAddActivity.this,
                        isChecked ? "「休み」にしました" : "通常のシフトにしました",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
        //通知切り替え
        noticeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mNotice = true;
                } else {
                    mNotice = false;
                }
                Toast.makeText(
                        EntryAddActivity.this,
                        isChecked ? "通知をONにしました" : "通知をOFFにしました",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Log.d("root","cancel");
            }
        });
        OKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //タイトルの取得
                mTitle = titleEditText.getText().toString();

                System.out.println(mTitle);
                System.out.println(mStartHour+":"+mStartMinute);
                System.out.println(mEndHour+":"+mEndMinute);
                System.out.println("HOLIDAY:"+mHoliday);
                System.out.println("NOTICE:"+mHoliday);
                Log.d("root","OK");

            }
        });

    }
}

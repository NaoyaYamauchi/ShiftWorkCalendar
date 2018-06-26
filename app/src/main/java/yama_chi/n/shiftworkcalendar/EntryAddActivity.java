package yama_chi.n.shiftworkcalendar;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class EntryAddActivity extends AppCompatActivity {
    private String mTitle;
    private int mStartHour, mStartMinute, mEndHour, mEndMinute;
    private boolean mStartTimePicked, mEndTimePicked, mHoliday, mNotice;
    private ArrayList<String> mPatternTitle = new ArrayList<String>();
    private ArrayList<String> mPatternStartTime = new ArrayList<String>();
    private ArrayList<String> mPatternEndTime = new ArrayList<String>();
    private ArrayList<Boolean> mPatternHoliday = new ArrayList<Boolean>();
    private ArrayList<Boolean> mPatternNotice = new ArrayList<Boolean>();

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
        final Button delButton = findViewById(R.id.delete_button);

        //通知実装できないので隠しておく
        noticeSwitch.setVisibility(View.INVISIBLE);

        //初期値
        mStartHour =9;
        mStartMinute=0;
        mEndHour =18;
        mEndMinute=0;

        Intent intent = getIntent();
        final int oldNewCase = intent.getIntExtra("newCase",1);
        final int position = intent.getIntExtra("position",0);

        SharedPreferences pref = getSharedPreferences("preset", MODE_PRIVATE);
        final Gson gson = new Gson();
        mPatternTitle = gson.fromJson(pref.getString("title", ""), new TypeToken<ArrayList<String>>(){}.getType());
        mPatternStartTime = gson.fromJson(pref.getString("startTime", ""), new TypeToken<ArrayList<String>>(){}.getType());
        mPatternEndTime= gson.fromJson(pref.getString("endTime", ""), new TypeToken<ArrayList<String>>(){}.getType());
        mPatternHoliday = gson.fromJson(pref.getString("holiday", ""), new TypeToken<ArrayList<Boolean>>(){}.getType());
        mPatternNotice = gson.fromJson(pref.getString("notice", ""), new TypeToken<ArrayList<Boolean>>(){}.getType());

        if(oldNewCase ==0){

            mTitle = mPatternTitle.get(position);
            mStartHour=Integer.parseInt(mPatternStartTime.get(position).substring(0,mPatternStartTime.get(position).indexOf(":")));
            mStartMinute=Integer.parseInt(mPatternStartTime.get(position).substring(mPatternStartTime.get(position).indexOf(":")+1));
            mEndHour=Integer.parseInt(mPatternEndTime.get(position).substring(0,mPatternEndTime.get(position).indexOf(":")));
            mEndMinute=Integer.parseInt(mPatternEndTime.get(position).substring(mPatternEndTime.get(position).indexOf(":")+1));
            mHoliday=mPatternHoliday.get(position);
            mNotice=mPatternNotice.get(position);

            titleEditText.setText(mTitle);

            String timeString = String.format("%02d", mStartHour) + ":" + String.format("%02d", mStartMinute);
            startTime.setText(timeString);
            String timeStringEnd = String.format("%02d", mEndHour) + ":" + String.format("%02d", mEndMinute);
            endTime.setText(timeStringEnd);

            if (mHoliday) {
                timeStringEnd = "--:--";
                endTime.setText(timeStringEnd);
                timeString = "--:--";
                startTime.setText(timeString);
                holidaySwitch.setChecked(true);
            }

        }
        else{
            String timeString = String.format("%02d", mStartHour) + ":" + String.format("%02d", mStartMinute);
            startTime.setText(timeString);
            String timeStringEnd = String.format("%02d", mEndHour) + ":" + String.format("%02d", mEndMinute);
            endTime.setText(timeStringEnd);
            delButton.setVisibility(View.INVISIBLE);
        }

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
                            cautionText.setText("日付をまたいで登録されます");
                        }else if(mStartHour == mEndHour&&mStartMinute==mEndMinute){
                            cautionText.setText("開始時間と終了時間が同じです");
                        }
                        else {
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
                    if(mStartHour == mEndHour&&mStartMinute==mEndMinute){
                        cautionText.setText("");
                    }

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
            }
        });
        OKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(titleEditText.getText().toString()==null||titleEditText.getText().toString().isEmpty()){
                    Toast.makeText(EntryAddActivity.this,"タイトルが未設定です",Toast.LENGTH_LONG).show();
                }
                else if(mStartHour == mEndHour&&mStartMinute==mEndMinute&&mHoliday==false){
                    Toast.makeText(EntryAddActivity.this,"開始時間と終了時間が同じです",Toast.LENGTH_LONG).show();
                }
                else{

                    Gson gson = new Gson();
                    SharedPreferences sharedPreferences = getSharedPreferences("preset", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    if(oldNewCase ==1){
                        mTitle=titleEditText.getText().toString();
                        mPatternTitle.add(mTitle);
                        mPatternStartTime.add(String.format("%02d", mStartHour) + ":" + String.format("%02d", mStartMinute));
                        mPatternEndTime.add(String.format("%02d", mEndHour) + ":" + String.format("%02d", mEndMinute));
                        mPatternHoliday.add(mHoliday);
                        mPatternNotice.add(mNotice);


                        editor.putString("title", gson.toJson(mPatternTitle));
                        editor.putString("startTime", gson.toJson(mPatternStartTime));
                        editor.putString("endTime", gson.toJson(mPatternEndTime));
                        editor.putString("holiday", gson.toJson(mPatternHoliday));
                        editor.putString("notice", gson.toJson(mPatternNotice));
                        editor.apply();
                    }
                    else{
                        mTitle=titleEditText.getText().toString();
                        mPatternTitle.set(position,mTitle);
                        mPatternStartTime.set(position,String.format("%02d", mStartHour) + ":" + String.format("%02d", mStartMinute));
                        mPatternEndTime.set(position,String.format("%02d", mEndHour) + ":" + String.format("%02d", mEndMinute));
                        mPatternHoliday.set(position,mHoliday);
                        mPatternNotice.set(position,mNotice);

                        editor.putString("title", gson.toJson(mPatternTitle));
                        editor.putString("startTime", gson.toJson(mPatternStartTime));
                        editor.putString("endTime", gson.toJson(mPatternEndTime));
                        editor.putString("holiday", gson.toJson(mPatternHoliday));
                        editor.putString("notice", gson.toJson(mPatternNotice));
                        editor.apply();
                    }


                    finish();

                }
            }
        });
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(EntryAddActivity.this)
                        .setMessage("削除します。本当によいのですか？")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPatternTitle.remove(position);
                                mPatternStartTime.remove(position);
                                mPatternEndTime.remove(position);
                                mPatternHoliday.remove(position);
                                mPatternNotice.remove(position);

                                Gson gson = new Gson();
                                SharedPreferences sharedPreferences = getSharedPreferences("preset", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("title", gson.toJson(mPatternTitle));
                                editor.putString("startTime", gson.toJson(mPatternStartTime));
                                editor.putString("endTime", gson.toJson(mPatternEndTime));
                                editor.putString("holiday", gson.toJson(mPatternHoliday));
                                editor.putString("notice", gson.toJson(mPatternNotice));
                                editor.apply();

                                finish();

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

    }
}

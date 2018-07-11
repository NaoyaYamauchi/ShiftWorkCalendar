package yama_chi.n.shiftworkcalendar;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    //初回起動かどうかの判定
    public static final int PREFERENCE_INIT = 0;
    public static final int PREFERENCE_BOOTED = 1;
    //Googleカレンダーとの連携のためのメンバ変数
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String ENTRY_BUTTON_TEXT = "登録";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    ProgressDialog mProgressDialog;
    GoogleAccountCredential mCredential;
    Date mDate;
    int mOldGridColor;
    private String mAccountName;
    private Button mEntryButton;
    private com.google.api.services.calendar.Calendar mService = null;
    private Exception mLastError = null;
    private String mCalendarId;
    private Boolean mReminder;
    private int mReminderMethod;
    private int mSelectPosition = -1;
    private View oldView = null;
    private int mLastSelectPosition = -101;
    private boolean mFirstSelect = true;
    //アプリ内のカレンダーのメンバ変数
    private List<Date> mDateArray;
    private TextView mTitleText;
    private Button mPrevButton;
    private Button mNextButton;
    private CalendarAdapter mCalendarAdapter;
    private GridView mCalendarGridView;
    private String mDateStartString;
    private String mDateEndString;

    //登録する日時を配列で持つ
    private ArrayList<String> mTitleList = new ArrayList<String>();
    private ArrayList<String> mStartList = new ArrayList<String>();
    private ArrayList<String> mEndList = new ArrayList<String>();
    private ArrayList<Boolean> mHolidayList = new ArrayList<Boolean>();
    //保存しておくシフトパターン
    private ArrayList<String> mPatternTitle = new ArrayList<String>();
    private ArrayList<String> mPatternStartTime = new ArrayList<String>();
    private ArrayList<String> mPatternEndTime = new ArrayList<String>();
    private ArrayList<Boolean> mPatternHoliday = new ArrayList<Boolean>();
    private ArrayList<Boolean> mPatternNotice = new ArrayList<Boolean>();

    //シフト登録のボタン
    private Button[] mShiftEntryButton = new Button[8];

    //画面下のオプションボタン群
    private Button mPatternButton;
    private Button mConfigButton;
    private Button mHelpButton;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int x = displayMetrics.widthPixels;
        int y = displayMetrics.heightPixels;

        // プログレスダイアログのメッセージを設定します
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("予定を登録しています…");

        mAdView = findViewById(R.id.adView);
        //mAdView.setAdSize(AdSize.BANNER);
//        mAdView.setAdUnitId("ca-app-pub-7578219808125514~7546525171");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mTitleText = findViewById(R.id.titleText);
        mPrevButton = findViewById(R.id.prevButton);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarAdapter.prevMonth();
                mTitleText.setText(mCalendarAdapter.getTitle());
            }
        });
        mNextButton = findViewById(R.id.nextButton);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarAdapter.nextMonth();
                mTitleText.setText(mCalendarAdapter.getTitle());
            }
        });

        //登録用Activity呼び出し
        mPatternButton = findViewById(R.id.pattern_setting);
        mPatternButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EntryActivity.class);
                startActivity(intent);
            }
        });

        //設定Activity呼び出し
        mConfigButton = findViewById(R.id.config);
        mConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                intent.putExtra("account", mAccountName);
                startActivity(intent);


            }

        });
        mHelpButton = findViewById(R.id.help);
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
            }

        });

        //GridViewの処理
        mCalendarGridView = findViewById(R.id.calendarGridView);
        mCalendarAdapter = new CalendarAdapter(this);
        mCalendarGridView.setAdapter(mCalendarAdapter);

        mCalendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mFirstSelect = true;
                if (oldView != null) {
                    oldView.setBackgroundColor(mOldGridColor);
                }
                if (mLastSelectPosition != -101) {
                    Date dateSelect = mCalendarAdapter.getDate(mLastSelectPosition);
                    String dateString = new SimpleDateFormat("MM").format(dateSelect);
                    Date nowDateSelect = mCalendarAdapter.getDate(position);
                    String nowDateString = new SimpleDateFormat("MM").format(nowDateSelect);

                    View ButtonTapAfterView = mCalendarGridView.getChildAt(mLastSelectPosition);

                    //最後にタップしたのが当月なら白にする
                    if (dateString.equals(mCalendarAdapter.getTitle().substring(5, 7))) {
                        ButtonTapAfterView.setBackgroundColor(-1);
                    }
                    //違うなら灰色
                    else {
                        ButtonTapAfterView.setBackgroundColor(-3355444);
                    }
                    mLastSelectPosition = -101;
                }


                //ButtonTapAfterView.setBackgroundColor();
                //if(mDateString.substring())
                //日付を取得する
                mDate = mCalendarAdapter.getDate(position);
                ColorDrawable colorDrawable = (ColorDrawable) view.getBackground();
                mOldGridColor = colorDrawable.getColor();
                oldView = view;
                view.setBackgroundColor(Color.parseColor("#FFFF00"));
                mSelectPosition = position;

            }
        });

        mTitleText.setText(mCalendarAdapter.getTitle());

        //Googleカレンダー
        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        //カレンダーとの連携
        mShiftEntryButton[0] = findViewById(R.id.enter1_button);
        mShiftEntryButton[1] = findViewById(R.id.enter2_button);
        mShiftEntryButton[2] = findViewById(R.id.enter3_button);
        mShiftEntryButton[3] = findViewById(R.id.enter4_button);
        mShiftEntryButton[4] = findViewById(R.id.enter5_button);
        mShiftEntryButton[5] = findViewById(R.id.enter6_button);
        mShiftEntryButton[6] = findViewById(R.id.enter7_button);
        mShiftEntryButton[7] = findViewById(R.id.enter8_button);

        mShiftEntryButton[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = mPatternStartTime.get(0);
                String end = mPatternEndTime.get(0);
                String title = mPatternTitle.get(0);
                boolean holiday = mPatternHoliday.get(0);

                enterShift(title, start, end, holiday);
            }
        });
        mShiftEntryButton[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mPatternTitle.get(1);
                String start = mPatternStartTime.get(1);
                String end = mPatternEndTime.get(1);
                boolean holiday = mPatternHoliday.get(1);

                enterShift(title, start, end, holiday);
            }
        });
        mShiftEntryButton[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mPatternTitle.get(2);
                String start = mPatternStartTime.get(2);
                String end = mPatternEndTime.get(2);
                boolean holiday = mPatternHoliday.get(2);

                enterShift(title, start, end, holiday);
            }
        });
        mShiftEntryButton[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mPatternTitle.get(3);
                String start = mPatternStartTime.get(3);
                String end = mPatternEndTime.get(3);
                boolean holiday = mPatternHoliday.get(3);

                enterShift(title, start, end, holiday);
            }
        });
        mShiftEntryButton[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mPatternTitle.get(4);
                String start = mPatternStartTime.get(4);
                String end = mPatternEndTime.get(4);
                boolean holiday = mPatternHoliday.get(4);

                enterShift(title, start, end, holiday);
            }
        });
        mShiftEntryButton[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mPatternTitle.get(5);
                String start = mPatternStartTime.get(5);
                String end = mPatternEndTime.get(5);
                boolean holiday = mPatternHoliday.get(5);

                enterShift(title, start, end, holiday);
            }
        });
        mShiftEntryButton[6].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mPatternTitle.get(6);
                String start = mPatternStartTime.get(6);
                String end = mPatternEndTime.get(6);
                boolean holiday = mPatternHoliday.get(6);

                enterShift(title, start, end, holiday);
            }
        });
        mShiftEntryButton[7].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mPatternTitle.get(7);
                String start = mPatternStartTime.get(7);
                String end = mPatternEndTime.get(7);
                boolean holiday = mPatternHoliday.get(7);

                enterShift(title, start, end, holiday);
            }
        });

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        getResultsFromApi();

        //登録ボタンの処理
        mEntryButton = findViewById(R.id.enter_button);
        mEntryButton.setText(ENTRY_BUTTON_TEXT);
        mEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.show();
                getResultsFromApi();
            }
        });

        getResultsFromApi();
        getState();
    }

    protected void enterShift(final String title, String start, String end, boolean holiday) {
        Log.d("position", String.valueOf(mSelectPosition));
         if (mSelectPosition >= 0) {
            View targetViewOld = mCalendarGridView.getChildAt(mSelectPosition);
            targetViewOld.setBackgroundColor(mOldGridColor);
            mSelectPosition++;
            final View[] targetView = {mCalendarGridView.getChildAt(mSelectPosition)};

            // getViewで対象のViewを更新
             try {
                 mCalendarGridView.getAdapter().getView(mSelectPosition, targetView[0], mCalendarGridView);
             }catch (IndexOutOfBoundsException e){

             }
            if (mDate != null) {
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.setTime(mDate);

                if (mFirstSelect) {
                    mFirstSelect = false;
                } else {
                    calendar.add(java.util.Calendar.DATE, 1);
                }

                int starthour = Integer.parseInt(start.substring(0, 2));
                int endhour = Integer.parseInt(end.substring(0, 2));

                mDate = calendar.getTime();
                mDateStartString = new SimpleDateFormat("yyyy-MM-dd").format(mDate);

                if (starthour > endhour) {
                    calendar.add(java.util.Calendar.DATE, 1);
                    mDate = calendar.getTime();
                }
                mDateEndString = new SimpleDateFormat("yyyy-MM-dd").format(mDate);
                System.out.println(mDateStartString);
                if (starthour > endhour) {
                    calendar.add(java.util.Calendar.DATE, -1);
                    mDate = calendar.getTime();
                }
                calendar.add(java.util.Calendar.DATE, 1);

                String nextMonth = new SimpleDateFormat("MM").format(calendar.getTime());
                calendar.add(java.util.Calendar.DATE, -1);


                mTitleList.add(title);
                mHolidayList.add(holiday);
                mStartList.add(mDateStartString + "T" + start + ":00.000+09:00");
                mEndList.add(mDateEndString + "T" + end + ":00.000+09:00");
                //System.out.println();
                //同じ日付で追加されていないか比較
                Log.d("root", mDateStartString);
                int daburiCheck = 0;
                int daburiBefore = 0;
                boolean daburiBool = false;
                for (int i = 0; i < mStartList.size(); i++) {
                    if (mDateStartString.equals(mStartList.get(i).substring(0, 10))) {
                        daburiCheck++;
                        if (daburiCheck == 1 && daburiBool == false) {
                            daburiBefore = i;
                            daburiBool = true;
                        }
                        Log.d("daburi", i + "番目");
                        if (daburiCheck == 2) {
                            mTitleList.remove(daburiBefore);
                            mHolidayList.remove(daburiBefore);
                            mStartList.remove(daburiBefore);
                            mEndList.remove(daburiBefore);
                            Log.d("daburi", "ダブった！");
                            Log.d("daburi", i + "番目");
                            daburiBool = false;

                        }
                    }
                }

                if(mCalendarAdapter.getTitle().substring(5, 7).equals(nextMonth)){

                    mCalendarAdapter.setMessage(mSelectPosition, title);

                    mCalendarAdapter.notifyDataSetChanged();
                }

                //Log.d("gettitle",mDateStartString);
                //日付が来月になる。もしくはGridViewの数が35を超えると来月（NEXTボタン）の処理に
                if (!mCalendarAdapter.getTitle().substring(5, 7).equals(nextMonth)) {
                    int prev = Integer.parseInt(mCalendarAdapter.getTitle().substring(5, 7));
                    int next = Integer.parseInt(nextMonth);
                    Log.d("mDate", String.valueOf(mSelectPosition));
                    Log.d("mDate", String.valueOf(mDate));

                    /*
                    次月が前月より大きい場合
                    次月が1月の場合
                     */
                    if (prev < next || next ==1) {
                        if((prev !=1 || next !=12)&&(prev !=2 || next !=1)){
                            mCalendarAdapter.nextMonth();
                            mSelectPosition = mSelectPosition - 28;
                            if(mSelectPosition >7) {
                                mSelectPosition -= 7;
                            }
                        }


                        final Handler handle = new Handler();
                        final Runnable runnabl = new Runnable() {

                            @Override
                            public void run() {
                                handle.postDelayed(this, 0050);
                                View targetViewNextMonth = mCalendarGridView.getChildAt(mSelectPosition);

                                targetViewNextMonth = mCalendarGridView.getChildAt(mSelectPosition);
                                targetViewNextMonth.setBackgroundColor(Color.parseColor("#FFFF00"));
                                mLastSelectPosition = mSelectPosition;

                                return;
                            }
                        };
                        handle.post(runnabl);

                        Log.d("root", String.valueOf(mSelectPosition));

                        mTitleText.setText(mCalendarAdapter.getTitle());
                        mCalendarAdapter.setMessage(mSelectPosition, title);
                        mCalendarAdapter.notifyDataSetChanged();


                    } else {

                        final Handler handle = new Handler();
                        final Runnable runnabl = new Runnable() {

                            @Override
                            public void run() {
                                handle.postDelayed(this, 0050);
                                targetView[0] = mCalendarGridView.getChildAt(mSelectPosition);
                                targetView[0].setBackgroundColor(Color.parseColor("#FFFF00"));

                                return;
                            }
                        };
                        handle.post(runnabl);

                        mCalendarAdapter.setMessage(mSelectPosition, title);
                        mCalendarAdapter.notifyDataSetChanged();

                    }
                } else {
                    final Handler handler = new Handler();
                    final Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            handler.postDelayed(this, 0050);
                            ColorDrawable colorDrawable = (ColorDrawable) targetView[0].getBackground();
                            targetView[0] = mCalendarGridView.getChildAt(mSelectPosition);
                            targetView[0].setBackgroundColor(Color.parseColor("#FFFF00"));

                            return;
                        }
                    };
                    handler.post(runnable);

                }
                mLastSelectPosition = mSelectPosition;
                oldView = null;

            }
        } else {
            Toast.makeText(MainActivity.this, "先に日付をタップしてください", Toast.LENGTH_LONG).show();
        }
    }

    protected void calendarEntry() throws IOException {
        for (int i = 0; i < mStartList.size(); i++) {

            //休みだったら登録しない。
            if (!mHolidayList.get(i)) {
                //シフト登録のテスト
                Event event = new Event()
                        .setSummary(mTitleList.get(i));//タイトル
                //開始時間
                DateTime startDateTime = new DateTime(mStartList.get(i));
                EventDateTime start = new EventDateTime()
                        .setDateTime(startDateTime)
                        .setTimeZone("Asia/Tokyo");
                event.setStart(start);

                //終了時間
                DateTime endDateTime = new DateTime(mEndList.get(i));
                EventDateTime end = new EventDateTime()
                        .setDateTime(endDateTime)
                        .setTimeZone("Asia/Tokyo");
                event.setEnd(end);


                //リマインダ　"email"はメール、"popup"は通知。どちらも分単位で指定
                //EventReminder内に宣言しないとリマインダOFFになる
                EventReminder[] reminderOverrides = new EventReminder[]{
                        //new EventReminder().setMethod("email").setMinutes(24*60),
                        //new EventReminder().setMethod("popup").setMinutes(1),

                };

                Event.Reminders reminders = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminderOverrides));
                event.setReminders(reminders);

                event = mService.events().insert(mCalendarId, event).execute();
                System.out.println(event.getHtmlLink());
            }

        }
        mStartList.clear();
        mEndList.clear();

    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (!isDeviceOnline()) {
            Toast.makeText(MainActivity.this, "端末のネットワークが使えません。\n"
                    + "オフラインになっていないか確認してください。", Toast.LENGTH_LONG).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    //プリセットのシフト
    private void getPreSetShift() {
        //休み、日勤、早番、遅番、

        mPatternTitle.add("休み");
        mPatternTitle.add("日勤");
        mPatternTitle.add("早番");
        mPatternTitle.add("遅番");

        mPatternStartTime.add("09:00");
        mPatternStartTime.add("09:00");
        mPatternStartTime.add("08:00");
        mPatternStartTime.add("10:00");

        mPatternEndTime.add("18:00");
        mPatternEndTime.add("18:00");
        mPatternEndTime.add("17:00");
        mPatternEndTime.add("19:00");

        mPatternHoliday.add(true);
        mPatternHoliday.add(false);
        mPatternHoliday.add(false);
        mPatternHoliday.add(false);

        mPatternNotice.add(false);
        mPatternNotice.add(false);
        mPatternNotice.add(false);
        mPatternNotice.add(false);

        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getSharedPreferences("preset", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("title", gson.toJson(mPatternTitle));
        editor.putString("startTime", gson.toJson(mPatternStartTime));
        editor.putString("endTime", gson.toJson(mPatternEndTime));
        editor.putString("holiday", gson.toJson(mPatternHoliday));
        editor.putString("notice", gson.toJson(mPatternNotice));
        editor.apply();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getSharedPreferences("accountName", Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            mAccountName = accountName;
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "このアプリはGoogleアカウントが必要です",
                    REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(MainActivity.this, "このアプリケーションはGoogle Play開発者サービスを使います。\n"
                            + "このデバイスにインストールしてから改めて起動してください", Toast.LENGTH_LONG).show();
                } else {
                    getResultsFromApi();
                }
                break;

            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {

                        SharedPreferences settings = getSharedPreferences("accountName", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();

                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (requestCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        //何も書かない
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        //何も書かない
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    //データ読み込み
    private int getState() {
        int state;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        state = sharedPreferences.getInt("InitState", PREFERENCE_INIT);
        return state;
    }

    //データ保存
    private void setState(int state) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putInt("InitState", state).commit();

    }

    @Override
    public void onStart() {
        super.onStart();

        chooseAccount();
        //初回表示内容
        setState(PREFERENCE_BOOTED);
        if (mCredential.getSelectedAccountName() == null) {

            getPreSetShift();
        }
        System.out.println(mCredential.getSelectedAccountName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getSharedPreferences("preset", MODE_PRIVATE);
        Gson gson = new Gson();
        mPatternTitle = gson.fromJson(pref.getString("title", ""), new TypeToken<ArrayList<String>>() {
        }.getType());
        mPatternStartTime = gson.fromJson(pref.getString("startTime", ""), new TypeToken<ArrayList<String>>() {
        }.getType());
        mPatternEndTime = gson.fromJson(pref.getString("endTime", ""), new TypeToken<ArrayList<String>>() {
        }.getType());
        mPatternHoliday = gson.fromJson(pref.getString("holiday", ""), new TypeToken<ArrayList<Boolean>>() {
        }.getType());
        mPatternNotice = gson.fromJson(pref.getString("notice", ""), new TypeToken<ArrayList<Boolean>>() {
        }.getType());

        for (int i = 0; i < 8; i++) {
            try {
                mShiftEntryButton[i].setText(mPatternTitle.get(i).toString());
                mShiftEntryButton[i].setVisibility(View.VISIBLE);
            } catch (IndexOutOfBoundsException e) {
                for (int j = 0; 8 - i > j; j++) {
                    mShiftEntryButton[7 - j].setVisibility(View.INVISIBLE);
                }
            }

        }
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, String> {

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential)
                    .setApplicationName("シフト登録")
                    .build();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return createCalendar();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private String createCalendar() throws IOException {
            String pageToken = null;
            boolean calendarBool = false;

            SharedPreferences sharedPreferences = getSharedPreferences("Id", Context.MODE_PRIVATE);
            mCalendarId = sharedPreferences.getString("ShiftCalendarId", "null");

            do {
                CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();
                //「WorkShift」という名前のカレンダーがあるかどうか確認。なければ作成する
                for (CalendarListEntry calendarListEntry : items) {
                    if (calendarListEntry.getSummary().equals("WorkShift")) {
                        if (!calendarListEntry.getId().equals(mCalendarId)) {

                            mCalendarId = String.valueOf(calendarListEntry.getId());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("ShiftCalendarId", mCalendarId);
                            editor.apply();

                        }
                        mCalendarId = calendarListEntry.getId();
                        calendarBool = true;
                        break;
                    }
                }
                pageToken = calendarList.getNextPageToken();
            } while (pageToken != null);
            // 新規にカレンダーを作成する
            com.google.api.services.calendar.model.Calendar calendar = new Calendar();

            if (!calendarBool) {
                calendar.setSummary("WorkShift");
                calendar.setTimeZone("Asia/Tokyo");
                Calendar createdCalendar = mService.calendars().insert(calendar).execute();
                mCalendarId = createdCalendar.getId();
                CalendarListEntry calendarListEntry = mService.calendarList().get(mCalendarId).execute();

                //追加するカレンダーの色。変更できるようにする？
                calendarListEntry.setBackgroundColor("#ff0000");
                CalendarListEntry updatedCalendarListEntry =
                        mService.calendarList()
                                .update(calendarListEntry.getId(), calendarListEntry)
                                .setColorRgbFormat(true)
                                .execute();

                //カレンダーのIDを保存しておく
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("ShiftCalendarId", mCalendarId);
                editor.apply();
                // 新規に作成したカレンダーのIDを返却する
                return mCalendarId;
            } else {
                calendarEntry();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String output) {
            mProgressDialog.hide();
            if (output == null || output.isEmpty()) {
            } else {
                Toast.makeText(MainActivity.this, "カレンダーを生成しました。", Toast.LENGTH_LONG).show();

            }

        }

        @Override
        protected void onCancelled() {

            mProgressDialog.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else if (mLastError instanceof NumberFormatException) {
                    //なにもしない
                }
            } else {
                Toast.makeText(MainActivity.this, "投稿がキャンセルされました。", Toast.LENGTH_LONG).show();
            }
        }
    }
}
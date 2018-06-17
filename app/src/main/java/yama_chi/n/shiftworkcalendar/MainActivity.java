package yama_chi.n.shiftworkcalendar;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    //Googleカレンダーとの連携のためのメンバ変数

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String API_BUTTON_TEXT = "APIを呼び出す";
    private static final String ENTRY_BUTTON_TEXT = "登録";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    GoogleAccountCredential mCredential;
    Date mDate;
    int mOldGridColor;
    private Button mCallApiButton;
    private Button mEntryButton;
    private com.google.api.services.calendar.Calendar mService = null;
    private Exception mLastError = null;
    private String mCalendarId;
    private int mSelectPosition;
    private View oldView = null;
    private int mLastSelectPosition=-101;
    private boolean mFirstSelect = true;
    //アプリ内のカレンダーのメンバ変数
    private List<Date> mDateArray;
    private TextView mTitleText;
    private Button mPrevButton;
    private Button mNextButton;
    private CalendarAdapter mCalendarAdapter;
    private GridView mCalendarGridView;
    private String mDateString;
    //登録する日時を配列で持つ
    private ArrayList<String> mStartList = new ArrayList<String>();
    private ArrayList<String> mEndList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        //GridViewの処理
        mCalendarGridView = findViewById(R.id.calendarGridView);
        mCalendarAdapter = new CalendarAdapter(this);
        mCalendarGridView.setAdapter(mCalendarAdapter);

        mCalendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mFirstSelect =true;
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
                    if(dateString.equals(mCalendarAdapter.getTitle().substring(5,7))){
                        ButtonTapAfterView.setBackgroundColor(-1);
                    }
                    //違うなら灰色
                    else{
                        ButtonTapAfterView.setBackgroundColor(-3355444);
                    }
                    mLastSelectPosition=-101;
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
        mCallApiButton = findViewById(R.id.button);
        mCallApiButton.setText(API_BUTTON_TEXT);
        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View targetViewOld = mCalendarGridView.getChildAt(mSelectPosition);
                targetViewOld.setBackgroundColor(mOldGridColor);
                System.out.println(mOldGridColor);
                mSelectPosition++;
                View targetView = mCalendarGridView.getChildAt(mSelectPosition);

                // getViewで対象のViewを更新
                mCalendarGridView.getAdapter().getView(mSelectPosition, targetView, mCalendarGridView);
                if (mDate != null) {
                    if (mFirstSelect) {
                        mDateString = new SimpleDateFormat("yyyy-MM-dd").format(mDate);
                        mFirstSelect = false;
                    } else {
                        java.util.Calendar calendar = java.util.Calendar.getInstance();
                        calendar.setTime(mDate);
                        calendar.add(java.util.Calendar.DATE, 1);
                        mDate = calendar.getTime();
                        mDateString = new SimpleDateFormat("yyyy-MM-dd").format(mDate);
                    }
                    System.out.println(mDateString);
                    mStartList.add(mDateString + "T17:00:00.000+09:00");
                    mEndList.add(mDateString + "T19:00:00.000+09:00");

                    ColorDrawable colorDrawable = (ColorDrawable) targetView.getBackground();
                    mOldGridColor = colorDrawable.getColor();
                    targetView.setBackgroundColor(Color.parseColor("#FFFF00"));
                    mLastSelectPosition = mSelectPosition;
                    System.out.println(mLastSelectPosition);
                    oldView =null;
                }
            }
        });

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mCallApiButton.setEnabled(false);
        getResultsFromApi();
        mCallApiButton.setEnabled(true);

        //登録ボタンの処理
        mEntryButton = findViewById(R.id.enter_button);
        mEntryButton.setText(ENTRY_BUTTON_TEXT);
        mEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getResultsFromApi();
            }
        });
        getResultsFromApi();
    }

    protected void calendarEntry() throws IOException {
        for (int i = 0; i < mStartList.size(); i++) {
            //シフト登録のテスト
            Event event = new Event()
                    .setSummary("ShiftWorkText");//タイトル
            //.setLocation("Sapporo")//場所
            //.setDescription("説明");//概要
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

            //RRULE:FREQ=DAILY;COUNT=4　だと、4日繰り返す
            //String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=4"};
            //event.setRecurrence(Arrays.asList(recurrence));

            //追加するゲストのアカウント
            //EventAttendee[] attendess = new EventAttendee[]{
            //        new EventAttendee().setEmail("doya@example.com"),
            //        new EventAttendee().setEmail("wrightism@gmail.com"),
            //};
            //event.setAttendees(Arrays.asList(attendess));

            //リマインダ　"email"はメール、"popup"は通知。どちらも分単位で指定
            //EventReminder内に宣言しないとリマインダOFFになる
            EventReminder[] reminderOverrides = new EventReminder[]{
                    // new EventReminder().setMethod("email").setMinutes(24*60),
                    //   new EventReminder().setMethod("popup").setMinutes(1),
            };

            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);


            event = mService.events().insert(mCalendarId, event).execute();
            System.out.println(event.getHtmlLink());

        }
        mStartList.clear();
        mEndList.clear();
        System.out.println(mStartList.size() + ":" + mEndList.size());


    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(MainActivity.this, "端末のネットワークが使えません。\n"
                    + "オフラインになっていないか確認してください。", Toast.LENGTH_LONG).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
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
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
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
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String output) {
            if (output == null || output.isEmpty()) {
            } else {
                Toast.makeText(MainActivity.this, "カレンダーを生成しました。", Toast.LENGTH_LONG).show();

            }

        }

        @Override
        protected void onCancelled() {
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
                } else {
                    Toast.makeText(MainActivity.this, "何やらエラーが発生して登録ができませんでした", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "投稿がキャンセルされました。", Toast.LENGTH_LONG).show();
            }
        }
    }
}

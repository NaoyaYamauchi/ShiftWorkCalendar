package yama_chi.n.shiftworkcalendar;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
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
    private static final String BUTTON_TEXT = "APIを呼び出す";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    private TextView mOutputText;
    private Button mCallApiButton;
    private com.google.api.services.calendar.Calendar mService = null;
    private Exception mLastError = null;

    //アプリ内のカレンダーのメンバ変数
    private List<Date> mDateArray;
    private TextView mTitleText;
    private Button mPrevButton;
    private Button mNextButton;
    private CalendarAdapter mCalendarAdapter;
    private GridView mCalendarGridView;


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
                //日付を取得する
                Date date = mCalendarAdapter.getDate(position);
                Log.d("root", String.valueOf(date));
                Toast.makeText(MainActivity.this, "" + String.valueOf(date), Toast.LENGTH_LONG).show();

            }
        });

        mTitleText.setText(mCalendarAdapter.getTitle());
        //

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
        mCallApiButton.setText(BUTTON_TEXT);
        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallApiButton.setEnabled(false);
                mOutputText.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });
        // activityLayout.addView(mCallApiButton);

        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(" \'" + BUTTON_TEXT + "\' をタップしてAPIを呼び出してください");
        activityLayout.addView(mOutputText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Google CalendarのAPIの呼び出し中…");

        //setContentView(activityLayout);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            mOutputText.setText("ネットワークが使えません");
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
                    mOutputText.setText(
                            "このアプリケーションはGoogle Play開発者サービスを使います。" +
                                    "このデバイスにインストールしてから改めて起動してください");
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
                    .setApplicationName("Google Calendar API Android QuickStart")
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
            String calendarId = sharedPreferences.getString("ShiftCalendarId", "null");

            do {
                CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();


                for (CalendarListEntry calendarListEntry : items) {
                    if (calendarListEntry.getSummary().equals("WorkShift")) {
                        if (!calendarListEntry.getId().equals(calendarId)) {

                            calendarId = String.valueOf(calendarListEntry.getId());

                            Log.d("root", calendarId);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("ShiftCalendarId", calendarId);
                            editor.apply();


                            //showAlertDialog();
                        }
                        calendarId = calendarListEntry.getId();
                        calendarBool = true;
                        break;
                    }
                }
                pageToken = calendarList.getNextPageToken();
            } while (pageToken != null);
            // 新規にカレンダーを作成する
            com.google.api.services.calendar.model.Calendar calendar = new Calendar();

            if (!calendarBool) {
                // カレンダーにタイトルを設定する
                calendar.setSummary("WorkShift");
                // カレンダーにタイムゾーンを設定する
                calendar.setTimeZone("Asia/Tokyo");
                // 作成したカレンダーをGoogleカレンダーに追加する
                Calendar createdCalendar = mService.calendars().insert(calendar).execute();

                calendarId = createdCalendar.getId();

                // カレンダー一覧から新規に作成したカレンダーのエントリを取得する
                CalendarListEntry calendarListEntry = mService.calendarList().get(calendarId).execute();

                // カレンダーのデフォルトの背景色を設定する
                calendarListEntry.setBackgroundColor("#ff0000");

                // カレンダーのデフォルトの背景色をGoogleカレンダーに反映させる
                CalendarListEntry updatedCalendarListEntry =
                        mService.calendarList()
                                .update(calendarListEntry.getId(), calendarListEntry)
                                .setColorRgbFormat(true)
                                .execute();

                System.out.println(calendarId);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("ShiftCalendarId", calendarId);
                editor.apply();
                // 新規に作成したカレンダーのIDを返却する
                return calendarId;
            } else {
                //シフト登録のテスト
                Event event = new Event()
                        .setSummary("ShiftWorkText")//タイトル
                        .setLocation("Sapporo")//場所
                        .setDescription("説明");//概要
                //開始時間
                DateTime startDateTime = new DateTime("2018-06-13T17:00:00.000+09:00");
                EventDateTime start = new EventDateTime()
                        .setDateTime(startDateTime)
                        .setTimeZone("Asia/Tokyo");
                event.setStart(start);

                //終了時間
                DateTime endDateTime = new DateTime("2018-06-13T19:00:00.000+09:00");
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

                event = mService.events().insert(calendarId, event).execute();
                System.out.printf("Event created:%s\n", event.getHtmlLink());

                return null;
            }
        }



        /*
        //カレンダーのデータ取得
        private List<String> getDataFromApi() throws IOException {
            DateTime now = new DateTime(System.currentTimeMillis());
            String year = now.toString().substring(0, 4);
            String month = now.toString().substring(5, 7);
            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(DateTime.parseRfc3339("2011-06-03T00:00:00.000+09:00"))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                if (event.getSummary() != null) {
                    eventStrings.add(String.format(event.getSummary()));
                }
            }
            return eventStrings;
        }
        */


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(String output) {
            mProgress.hide();
            if (output == null || output.isEmpty()) {
                mOutputText.setText("No results returned.");
            } else {
                mOutputText.setText("カレンダーを作成しました。 " + output);
            }

        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("次のエラーが発生：" + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled");
            }
        }


    }
    /*
    private void showAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("既にGoogleカレンダーに「WorkShift」が登録されています。既存のカレンダーを使用しますか？");

        alertDialogBuilder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPreferences = getSharedPreferences("Id", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("ShiftCalendarId", mCalendarId);
                editor.apply();
            }
        });
        alertDialogBuilder.setNegativeButton("新規に作成する", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    mService.calendarList().delete(mCalendarId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCalendarId="update";
            }
        });
    }
     */
}

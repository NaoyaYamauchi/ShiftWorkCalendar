package yama_chi.n.shiftworkcalendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateManager {
    Calendar mCalender;

    public DateManager() {
        mCalender = Calendar.getInstance();
    }

    public List<Date> getDays() {
        //現在の状態の保持
        Date startDate = mCalender.getTime();

        //GridViewに表示するます計算
        int count = 6* 7;

        //カレンダーに表示される前月分の日数計算
        mCalender.set(Calendar.DATE, 1);//引数は何か見ておく
        int dayOfWeek = mCalender.get(Calendar.DAY_OF_WEEK)-1;
        mCalender.add(Calendar.DATE, -dayOfWeek);

        List<Date> days = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            days.add(mCalender.getTime());
            mCalender.add(Calendar.DATE, 1);
        }

        //状態の復元
        mCalender.setTime(startDate);

        return days;
    }

    //当日かどうかの確認
    public boolean inCurrentMonth(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM", Locale.US);
        //元のコードはLocale.US

        String currentMonth = format.format(mCalender.getTime());
        if (currentMonth.equals(format.format(date))) {
            return true;
        } else {
            return false;
        }
    }

    //1週間を取得
    public int getWeeks() {
        return mCalender.getActualMaximum(Calendar.WEEK_OF_MONTH);
    }

    //曜日取得
    public int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    //翌月
    public void nextMonth() {
        mCalender.add(Calendar.MONTH, 1);
    }

    //前月
    public void prevMonth() {
        mCalender.add(Calendar.MONTH, -1);
    }
}

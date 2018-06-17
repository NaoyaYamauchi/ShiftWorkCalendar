package yama_chi.n.shiftworkcalendar;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends BaseAdapter {
    private List<Date> mDateArray;
    private Context mContext;
    private DateManager mDateManager;
    private LayoutInflater mLayoutInflater;

    public CalendarAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mDateManager = new DateManager();
        mDateArray = mDateManager.getDays();
    }


    //カスタムセルの定義
    private static class ViewHolder {
        public TextView dateText;
    }

    @Override
    public int getCount() {
        return mDateArray.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }
    public Date getDate(int position){
        return mDateArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.calendar_cell, null);
            holder = new ViewHolder();
            holder.dateText = convertView.findViewById(R.id.dateText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //セルのサイズ指定
        float dp = mContext.getResources().getDisplayMetrics().density;
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(parent.getWidth()
                / 7 - (int) dp, (parent.getHeight() / 9));
        convertView.setLayoutParams(params);

        //日付の表示
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.US);
        //これも元は[Locale.US]
        holder.dateText.setText(dateFormat.format(mDateArray.get(position)));
        //holder.dateText.setText(dateFormat.format(mDateArray.get(position\n"シフト名"));
        //

        //当月以外のセルをグレーアウト
        if (mDateManager.inCurrentMonth(mDateArray.get(position))) {
            convertView.setBackgroundColor(Color.WHITE);
        } else {
            convertView.setBackgroundColor(Color.LTGRAY);
        }

        //土日を青と赤にする
        int colorId;
        switch (mDateManager.getDayOfWeek(mDateArray.get(position))) {
            case 1:
                colorId = Color.RED;
                break;
            case 7:
                colorId = Color.BLUE;
                break;
            default:
                colorId = Color.BLACK;
                break;
        }
        holder.dateText.setTextColor(colorId);

        return convertView;
    }

    //表示月の取得
    public String getTitle(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM",Locale.US);
        return format.format(mDateManager.mCalender.getTime());
    }

    //翌月表示
    public void nextMonth(){
        mDateManager.nextMonth();
        mDateArray = mDateManager.getDays();
        this.notifyDataSetChanged();
    }
    //前月表示
    public void prevMonth(){
        mDateManager.prevMonth();
        mDateArray= mDateManager.getDays();
        this.notifyDataSetChanged();
    }

}

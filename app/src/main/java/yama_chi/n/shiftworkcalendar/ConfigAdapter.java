package yama_chi.n.shiftworkcalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ConfigAdapter  extends BaseAdapter{
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<Config> config;

    public ConfigAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setConfigList(ArrayList<Config> configList) {
        this.config = config;
    }

    @Override
    public int getCount() {
        return config.size();
    }

    @Override
    public Object getItem(int position) {
        return config.get(position);
    }

    @Override
    public long getItemId(int position){
        return  config.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        convertView =layoutInflater.inflate(R.layout.config,parent,false);

        ((TextView)convertView.findViewById(R.id.main)).setText(config.get(position).getMain());
        ((TextView)convertView.findViewById(R.id.sub)).setText(config.get(position).getSub());

        return convertView;
    }

}

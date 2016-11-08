package com.thalmic.android.sample.helloworld;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyActivity extends Activity implements WearableListView.ClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        List<String> labels = new ArrayList<String>();
        labels.add("DataItem");
        labels.add("Message");
        WearableListView listView = (WearableListView)findViewById(R.id.list);
        listView.setAdapter(new Adapter(this, labels));
        listView.setClickListener(this);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int i = (Integer)viewHolder.itemView.getTag();
        Log.d("TAG", "index: " + i);
        if (i == 0) {
            Intent intent = new Intent(this, DataItemSampleActivity.class);
            startActivity(intent);
        } else if (i == 1) {
            Intent intent = new Intent(this, MessageSampleActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

    private static class Adapter extends WearableListView.Adapter {

        private Context context;
        private LayoutInflater layoutInflater;
        private List<String> labels;

        public Adapter(Context context, List<String> labels) {
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
            this.labels = labels;
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new WearableListView.ViewHolder(
                    layoutInflater.inflate(R.layout.list_item, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int i) {
            TextView textView = (TextView)viewHolder.itemView.findViewById(R.id.label);
            textView.setText(labels.get(i));
            viewHolder.itemView.setTag(i);
        }

        @Override
        public int getItemCount() {
            return labels.size();
        }
    }
}

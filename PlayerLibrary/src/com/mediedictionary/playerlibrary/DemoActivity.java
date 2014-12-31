package com.mediedictionary.playerlibrary;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class DemoActivity extends ListActivity implements OnItemClickListener {

	List<String> items;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_demo);

		items = new ArrayList<String>();
		items.add("http://img1.peiyinxiu.com/2014121211339c64b7fb09742e2c.mp4");
		items.add("rtmp://183.129.244.168/weipai/s1");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		startActivity(new Intent(this, PlayerActivity.class).putExtra("url", items.get(position)));
	}
}

package com.video.player;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public abstract class VideoBaseActivity extends Activity implements OnClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initListener();
		initData();
	}
	
	protected abstract void initView();
	protected abstract void initListener();
	protected abstract void initData();
	
	/**
	 * 可以处理共同点击事件的按钮
	 * @param v
	 */
	protected abstract void processClick(View v);
	
	@Override
	public void onClick(View v) {
		processClick(v);
	}
}

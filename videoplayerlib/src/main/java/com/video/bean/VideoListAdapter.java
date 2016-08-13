package com.video.bean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import player.video.com.videoplayerlib.R;

public class VideoListAdapter extends BaseAdapter {
	private LayoutInflater mInflater = null;
	private List<ViedoDatas.ChapterListBean> mVideoList;

	public VideoListAdapter(Context context,
			List<ViedoDatas.ChapterListBean> videoList) {
		this.mInflater = LayoutInflater.from(context);
		this.mVideoList = videoList;
	}

	@Override
	public int getCount() {
		return mVideoList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mVideoList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		ViedoDatas.ChapterListBean item =mVideoList.get(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.video_list_item, null);
			holder.title = (TextView) convertView.findViewById(R.id.tv_title);
			holder.img_item = (ImageView) convertView.findViewById(R.id.img_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();

		}
		holder.title.setText(item.getLE_NAME());
		if(item.isPlay()){
			holder.img_item.setImageResource(R.drawable.list_item_play);
		}else {
			holder.img_item.setImageResource(R.drawable.list_item_puase);
		}
		return convertView;

	}

	public void setPlayStatus(){
		notifyDataSetChanged();
	}
	static class ViewHolder {
		private TextView title;
		private ImageView img_item;
	}

}

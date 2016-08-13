package com.example.testvideoplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class TestActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.activity_main);
		  String res ="{\"chapterList\":[{\"id\":\"3aa5c966fa62444ead1a9f0faed3fb48\",\"le_id\":\"91370fbdac544002886b73702008be11\",\"le_name\":\"标杆管理课程讲解\",\"enter_url\":\"650.mp4\",\"lindex\":0,\"dqurl\":\"2\"},{\"id\":\"a21a8180d08a4ad4afe5349df440094c\",\"le_id\":\"91370fbdac544002886b73702008be11\",\"le_name\":\"标杆管理案例解析\",\"enter_url\":\"651.mp4\",\"lindex\":1,\"dqurl\":\"2\"}]}";

	//	String res ="{\"chapterList\":[{\"ID\":\"3aa5c966fa62444ead1a9f0faed3fb48\",\"LE_ID\":\"91370fbdac544002886b73702008be11\",\"LE_NAME\":\"标杆管理课程讲解\",\"ENTER_URL\":\"650.mp4\",\"LINDEX\":0,\"DQURL\":\"2\"},{\"ID\":\"a21a8180d08a4ad4afe5349df440094c\",\"LE_ID\":\"91370fbdac544002886b73702008be11\",\"LE_NAME\":\"标杆管理案例解析\",\"ENTER_URL\":\"651.mp4\",\"LINDEX\":1,\"DQURL\":\"2\"}]}";
		Intent intent = new Intent(this, VideoPlayerActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("currentIndex", 2);
		bundle.putString("chapterList", res);
		bundle.putString("userId", "2222");
		intent.putExtra("chapterListData", bundle);
		startActivity(intent);

//        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String res = "{\"chapterList\":[{\"id\":\"3aa5c966fa62444ead1a9f0faed3fb48\",\"le_id\":\"91370fbdac544002886b73702008be11\",\"le_name\":\"标杆管理课程讲解\",\"enter_url\":\"650.mp4\",\"lindex\":0,\"dqurl\":\"2\"},{\"id\":\"a21a8180d08a4ad4afe5349df440094c\",\"le_id\":\"91370fbdac544002886b73702008be11\",\"le_name\":\"标杆管理案例解析\",\"enter_url\":\"651.mp4\",\"lindex\":1,\"dqurl\":\"2\"}]}";
//
//                Intent intent = new Intent(TestActivity.this, VideoPlayerActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putInt("currentIndex", 2);
//                bundle.putString("chapterList", res);
//                bundle.putString("url", "http://172.31.36.159/www/MP4/A_1.flv");
//                intent.putExtra("chapterListData", bundle);
//
//
//
//                startActivity(intent);
//            }
//        });
//
//        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String res = "{\"chapterList\":[{\"id\":\"3aa5c966fa62444ead1a9f0faed3fb48\",\"le_id\":\"91370fbdac544002886b73702008be11\",\"le_name\":\"标杆管理课程讲解\",\"enter_url\":\"650.mp4\",\"lindex\":0,\"dqurl\":\"2\"},{\"id\":\"a21a8180d08a4ad4afe5349df440094c\",\"le_id\":\"91370fbdac544002886b73702008be11\",\"le_name\":\"标杆管理案例解析\",\"enter_url\":\"651.mp4\",\"lindex\":1,\"dqurl\":\"2\"}]}";
//
//                Intent intent = new Intent(TestActivity.this, VideoPlayerActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putInt("currentIndex", 2);
//                bundle.putString("chapterList", res);
//                bundle.putString("url", "http://172.31.36.159/www/MP4/B_1.flv");
//                intent.putExtra("chapterListData", bundle);
//
//
//
//                startActivity(intent);
//            }
//        });
    }

//	
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		getMenuInflater().inflate(R.layout.activity_main, menu);
//		return true;
//	}

}

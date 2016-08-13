package com.video.bean;

import java.util.List;

/**
 * Created by Administrator on 2016/6/3.
 */

public class ViedoDatas {

    /**
     * chapterList : [{"NAME":"标杆管理课程讲解","ID":"3aa5c966fa62444ead1a9f0faed3fb48","ENTER_URL":"650.mp4","LINDEX":0},{"NAME":"标杆管理案例解析","ID":"a21a8180d08a4ad4afe5349df440094c","ENTER_URL":"651.mp4","LINDEX":1}]
     * success : true
     */

  
    /**
     * NAME : 标杆管理课程讲解
     * ID : 3aa5c966fa62444ead1a9f0faed3fb48
     * ENTER_URL : 650.mp4
     * LINDEX : 0
     */

    private List<ChapterListBean> chapterList;

   
    public List<ChapterListBean> getChapterList() {
        return chapterList;
    }

    public void setChapterList(List<ChapterListBean> chapterList) {
        this.chapterList = chapterList;
    }

    public  class ChapterListBean {
        private String LE_ID;
        private String ID;
        private String LE_NAME;
        private String ENTER_URL;
        private int LINDEX;
        private int DQURL;
        private boolean isPlay;
		public boolean isPlay() {
			return isPlay;
		}
		public void setPlay(boolean isPlay) {
			this.isPlay = isPlay;
		}
		public String getLE_ID() {
			return LE_ID;
		}
		public void setLE_ID(String lE_ID) {
			LE_ID = lE_ID;
		}
		public String getID() {
			return ID;
		}
		public void setID(String iD) {
			ID = iD;
		}
		public String getLE_NAME() {
			return LE_NAME;
		}
		public void setLE_NAME(String lE_NAME) {
			LE_NAME = lE_NAME;
		}
		public String getENTER_URL() {
			return ENTER_URL;
		}
		public void setENTER_URL(String eNTER_URL) {
			ENTER_URL = eNTER_URL;
		}
		public int getLINDEX() {
			return LINDEX;
		}
		public void setLINDEX(int lINDEX) {
			LINDEX = lINDEX;
		}
		public int getDQURL() {
			return DQURL;
		}
		public void setDQURL(int dQURL) {
			DQURL = dQURL;
		}
        
        

       
    }
}

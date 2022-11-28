package com.example.speaktext.util;

import android.content.Context;

import com.example.speaktext.models.UserSessionData;

public class GlobalData {
    private static GlobalData instance = new GlobalData();

    // Getter-Setters
    public static GlobalData getInstance() {
        return instance;
    }

    public static void setInstance(GlobalData instance) {
        GlobalData.instance = instance;
    }
    private GlobalData() {

    }
    private UserSessionData _user;
    private boolean _isTablet;
    private double _fontSize = 13;
    /// Loading a json configuration file with the given info into the current app config.
    /// read config from storage
    public void loadFromAsset(Context context) {
        _user = Common.getStoredUser(context);
        if(_user == null) {
            _user = new UserSessionData();
            _user.setSentence(-1);
            _user.setChapter(-1);
        }
        _isTablet = false;
    }
    public UserSessionData getUser(){
        return _user;
    }
    ///get is tablet device
    public boolean getTablet(){
        return _isTablet;
    }
    ///is tablet device
    public void setIsTablet(boolean value){
        _isTablet = value;
        if(value == true)
            _fontSize = 22;
    }
    public double getFontSize(){
        return _fontSize;
    }
    public int getSentence(){
        return (_user != null ? _user.sentence : -1);
    }
    public void setSentence(int pSentence){
        if (_user != null)
            _user.sentence = pSentence;
    }
    public int getChapter(){
        return (_user != null ? _user.chapter : -1);
    }
    public void setChapter(int pChapter){
        if (_user != null)
            _user.chapter = pChapter;
    }
    public String getUrl(){
        return (_user != null ? (_user.url == null ? "" : _user.url) : "");
    }
    public void setUrl(String psUrl){
        if (_user != null)
            _user.url = psUrl;
    }
    public String getSearchContent(){
        return (_user != null ? (_user.searchContent == null ? "" : _user.searchContent) : "");
    }
    public void setSearchContent(String psSearchContent){
        if (_user != null)
            _user.searchContent = psSearchContent;
    }

    public String getSearchChapter(){
        return (_user != null ? (_user.searchChapter == null ? "" : _user.searchChapter) : "");
    }
    public void setSearchChapter(String psSearchChapter){
        if (_user != null)
            _user.searchChapter = psSearchChapter;
    }
}

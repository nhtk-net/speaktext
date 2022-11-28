package com.example.speaktext.models;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
public class UserSessionData
{
    public UserSessionData(){}
    public String username;
    public int sentence;
    public int chapter;
    public String url, searchContent, searchChapter;
    /*public static UserSessionData fromJson(String s) {
        return new Gson().fromJson(s, UserSessionData.class);
    }
    public String toString() {
        return new Gson().toJson(this);
    }*/

    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public int getSentence() {
        return sentence;
    }
    public void setSentence(int sentence) {
        this.sentence = sentence;
    }
    public int getChapter() {
        return chapter;
    }
    public void setChapter(int chapter) {
        this.chapter = chapter;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getSearchContent() {
        return searchContent;
    }
    public void setSearchContent(String searchContent) {
        this.searchContent = searchContent;
    }
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
package com.example.speaktext;

public interface PostTaskListener<String> {
    // K is the type of the result object of the async task
    void onPostTask(String result);
    void onMsgTask(String message);
}
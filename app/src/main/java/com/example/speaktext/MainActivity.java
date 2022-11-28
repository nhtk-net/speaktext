package com.example.speaktext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.speaktext.models.UserSessionData;
import com.example.speaktext.util.Common;
import com.example.speaktext.util.GlobalData;
import com.example.speaktext.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
enum TtsState { none, playing, pause, stopped }
public class MainActivity extends AppCompatActivity implements PostTaskListener {
    TextToSpeech tts;
    EditText text;
    EditText pageNumber;
    TextView txtMsg, txtDataRow;
    ImageButton btnPlay, btnPause, btnStop, btnNext, btnPrevious, btnPrevSentence, btnNextSentence;
    SeekBar sbVolume;

    HashMap<String, String> map;
    GlobalData cfg;
    boolean _isTablet, _isStopAction = false, _hasLanguageVi = false, _hasTtsEngine = false;//StopAction using in event done
    double _fontSize = 13;
    TtsState ttsState;
    List<String> arrSentence = new ArrayList<String>();
    int mIndex = -1, _chapter = -1;
    private ProgressBar spinner;
    AudioManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkTTSEngine();
        initControl();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Util.alertMsg("Already Installed", getApplicationContext());
            } else {
                try{
                    Intent installIntent = new Intent();
                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                    Util.alertMsg("Installed Now", getApplicationContext());
                } catch (ActivityNotFoundException e) {
                    Log.e("Error", "[onActivityResult] " + e.fillInStackTrace());
                    Util.alertMsg("[onActivityResult] " + e.getMessage(), getApplicationContext());
                }
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
    void checkTTSEngine(){//mở giao diện thêm tts nếu chưa có
        // Fire off an intent to check if a TTS engine is installed
        /*Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);*/

        try {
            Intent intent = new Intent();
            intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(intent, 0);
        } catch (Exception e) {
            Log.e("Error", "[checkTTSEngine] " + e.getMessage());
        }
    }
    void initControl(){
        String vsUrl;
        int vMaxVolume;
        try {
            cfg = GlobalData.getInstance();
            map = new HashMap<String, String>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SpeakText");
            initTts();
            //cfg = GlobalData.getInstance();
            text = findViewById(R.id.txtUrl);
            pageNumber = findViewById(R.id.pageNumber);
            txtMsg = findViewById(R.id.txtMsg);
            txtDataRow = findViewById(R.id.txtDataRow);
            btnPlay = findViewById(R.id.btnPlay);
            btnPause = findViewById(R.id.btnPause);
            btnStop = findViewById(R.id.btnStop);
            spinner = findViewById(R.id.progressBar);
            am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            sbVolume = (SeekBar)findViewById(R.id.sbVolume);
            setCurrentSeek();
            // perform seek bar change listener event used for getting the progress value
            sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progressChangedValue = 0;

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progressChangedValue = progress;
                    setVolume(progress);
                }

                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                public void onStopTrackingTouch(SeekBar seekBar) {
                    Util.alertMsg("Seek bar progress is :" + progressChangedValue, getApplicationContext());
                }
            });

            setTtsState(TtsState.stopped);
            cfg.loadFromAsset(getApplicationContext());
            vsUrl = cfg.getUrl();
            if (vsUrl.isEmpty())
                vsUrl = "https://truyenfull.vn/hom-nay-cong-tu-hac-hoa-chua/chuong-1/";
            text.setText(vsUrl);
            txtDataRow.setMovementMethod(new ScrollingMovementMethod());
        }catch (Exception ex){
            Log.e("Error", "[initControl] " + ex.getMessage());
            Util.alertMsg("[initControl] " + ex.getMessage(), getApplicationContext());
        }
    }
    void hideKeyboard(){
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    int getMaxVolume(){
        int sb2value = 0;
        try{
            sb2value = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }catch (Exception ex){
            Log.i("TAG", "{getMaxVolume} error: " + ex.getMessage());
        }
        return sb2value;
    }
    void setCurrentSeek(){
        int vVolume, vMaxVolume;
        try{
            vMaxVolume = getMaxVolume();
            sbVolume.setMax(vMaxVolume);
            vVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            setVolume(vVolume);
        }catch (Exception ex){
            Log.i("TAG", "{setVolume} error: " + ex.getMessage());
        }
    }
    void setVolume(int pVolume){
        try{
            //int sb2value = sbVolume.getProgress();
            am.setStreamVolume(AudioManager.STREAM_MUSIC, pVolume, 0);
        }catch (Exception ex){
            Log.i("TAG", "{setVolume} error: " + ex.getMessage());
        }
    }
    void initTts(){
        boolean vbExistTtsEngine = false;
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Locale lang;
            if (status == TextToSpeech.ERROR) {
                Log.e("Error", "TTS init failed");
               Util.alertMsg("TextToSpeech.ERROR", getApplicationContext());
            } else if (status == TextToSpeech.SUCCESS) {
                _hasTtsEngine = true;
                Log.i("TAG", "Init TSUCCESS.");
                lang = new Locale("vi", "VN", "");
                //maxVolume();
                _hasLanguageVi = (tts.isLanguageAvailable(lang) > 0);//tts.setAudioAttributes()
                if(_hasLanguageVi)
                    tts.setLanguage(lang);
                else {
                    setMsg("Không có ngôn ngữ tiếng Việt.", false);
                    Util.alertMsg("Không có ngôn ngữ tiếng Việt.", getApplicationContext());
                }
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onDone(String utteranceId) {
                        if(_isStopAction) {
                            _isStopAction = false;
                            return;
                        }else if(ttsState == TtsState.pause)
                            return;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            int vCount;
                            vCount = arrSentence.size();
                            if(vCount > 0 && mIndex >= vCount)
                                setTtsState(TtsState.stopped);
                            speakNext();
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        //Log.e("onError", "TTS error " + utteranceId);
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Stuff that updates the UI
                                speakCurrent();
                            }
                        });*/
                    }

                    @Override
                    public void onStart(String utteranceId) {
                        //Log.e("onStart", "Index = " + mIndex);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Stuff that updates the UI
                                setTtsState(TtsState.playing);
                            }
                        });
                    }
                });
            }
            }
        });
        if(tts != null) {
            for (TextToSpeech.EngineInfo engines : tts.getEngines()) {
                Log.d("**Engine Info ", engines.toString());
                vbExistTtsEngine = true;
            }
        }
        if(!vbExistTtsEngine) {
            Util.alertMsg("Thiết bị chưa cài đặt text to speech engine.", getApplicationContext());
        }
    }
    @Override
    public void onPostTask(Object result) {
        showHideIndicator(false);
        if(result != null && !result.toString().isEmpty())
            speakMultiline(result.toString());
        else
            Util.alertMsg("Fail read", getApplicationContext());
    }

    @Override
    public void onMsgTask(Object message) {
        showNote(message.toString());
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Stuff that updates the UI
                //showNote(message.toString());
            }
        });*/
    }

    public void previousSentence(View view) {
        _isStopAction = true;
        if(ttsState == TtsState.playing)
            tts.stop();
        speakPrevious();
        _isStopAction = false;
    }

    public void nextSentence(View view) {
        _isStopAction = true;
        if(ttsState == TtsState.playing)
            tts.stop();
        speakNext();
        _isStopAction = false;
    }
    public void nextMedia(View view) {
        _isStopAction = true;
        stopSpeak();
        nextChapter();
    }
    public void previousMedia(View view) {
        _isStopAction = true;
        stopSpeak();
        previousChapter();
    }
    public void resetMedia(View view) {
        reset();
    }
    void addSentence(String psMsg){
        arrSentence.add(psMsg);
    }
    ///get chapter number from url
    int getChapter(boolean pbSetChapter){
        int vPos, vLenFind, vChapter = -1;
        String vsUrl, vsChapter, vsFind;
        vsFind = cfg.getSearchChapter();
        if(vsFind.isEmpty() || vsFind == null)
            vsFind = "chuong-";
        vLenFind = vsFind.length();
        vsUrl = text.getText().toString().trim();
        vPos = vsUrl.lastIndexOf(vsFind);
        vsChapter = vsUrl.substring(vPos + vLenFind);
        if(vsChapter.lastIndexOf("/") >= 0)
            vsChapter = vsChapter.substring(0, vsChapter.length() - 1);
        if(Util.isNumeric(vsChapter)) {
            vChapter = Integer.parseInt(vsChapter);
            if(pbSetChapter && vChapter > 0)
                setChapter(vChapter);
        }
        return vChapter;
    }
    boolean getNextChapter(boolean pbNextChapter){
        boolean vbRes = false;
        int vPos, vLenFind;
        String vsUrl, vsFind;
        try {
            vsFind = cfg.getSearchChapter();
            if(vsFind.isEmpty() || vsFind == null)
                vsFind = "chuong-";
            vLenFind = vsFind.length();
            vsUrl = text.getText().toString().trim();
            if (vsUrl.toLowerCase().startsWith("http:") || vsUrl.toLowerCase().startsWith("https:")) {
                if(_chapter < 0)
                    getChapter(true);
                if(_chapter > 0)
                    vbRes = true;
            }
            if (vbRes) {
                vPos = vsUrl.lastIndexOf(vsFind);
                vsUrl = vsUrl.substring(0, vPos + vLenFind) + String.valueOf(_chapter + (pbNextChapter ? 1 : -1));
                text.setText(vsUrl);
            }
        }catch (Exception ex){
            showNote("[getNextChapter] " + ex.getMessage());
        }
        return vbRes;
    }
    void nextChapter(){
        boolean vbNext;
        pageNumber.setText("");
        resetSentence();
        vbNext = getNextChapter(true);
        if(vbNext){
            _isStopAction = true;
            setMsg("Tiếp chương " + (_chapter + 1), true);
            speakData();
        }
    }
    void previousChapter(){
        boolean vbPrev;
        pageNumber.setText("");
        resetSentence();
        vbPrev = getNextChapter(false);
        if(vbPrev){
            _isStopAction = true;
            setMsg("Xem lại chương " + (_chapter - 1), true);
            speakData();
        }
    }
    ///pause playing media
    public void pauseMedia(View view){
        _isStopAction = true;
        setTtsState(TtsState.pause);
        tts.stop();
        if(mIndex >= 0)
            mIndex--;
        updateConfig();
        _isStopAction = false;
        pageNumber.setText("");
    }
    int getPageNumber(){
        int pageIdx = -1;
        String vsData;
        vsData = pageNumber.getText().toString().trim();
        if(Util.isNumeric(vsData)) {
            pageIdx = Integer.parseInt(vsData);
            if(pageIdx > 0)
                pageIdx--;
        }
        return pageIdx;
    }
    void setPageNumber(){
        int pageIdx;
        pageIdx = getPageNumber();
        if(pageIdx > 0)
            mIndex = pageIdx;
    }
    public void playMedia(View view){
        int vCount, pageIdx, vPageUrl;
        hideKeyboard();
        vCount = arrSentence.size();
        if(vCount > 0) {
            vPageUrl = getChapter(false);
            if(_chapter > 0 && vPageUrl > 0 && vPageUrl != _chapter){
                speakData();
            }else {
                setPageNumber();
                if (mIndex < 0)
                    mIndex = 0;
                speakCurrent();
            }
        }else
            speakData();
        if(!_hasTtsEngine)
            setMsg("Lưu ý: Thiết bị không có TextToSpeech Engine.", false);
    }
    void reset(){
        text.setText("");
        pageNumber.setText("");
        tts.stop();
        resetSentence();
        updateConfig();
    }
    void resetSentence(){
        mIndex = -1;
        arrSentence.clear();
    }
    void showHideIndicator(boolean pbShow){
        try {
            if (pbShow)
                spinner.setVisibility(View.VISIBLE);
            else
                spinner.setVisibility(View.GONE);
        }catch (Exception ex){
            Log.e("[showHideIndicator]", ex.getMessage());
        }
    }
    void setChapter(int pChapter){
        _chapter = pChapter;
    }

    void setMsg(String psMsg, boolean pSpeech){
        txtMsg.setText(psMsg);
        if(pSpeech && psMsg != null && !psMsg.isEmpty())
            speakText(psMsg);
        if(!pSpeech)
            Log.d("** setMsg ", psMsg);
    }
    void setSentence(String psMsg){
        txtDataRow.setText(psMsg);
    }

    void showNote(String psMsg){
        setMsg(psMsg, false);
    }
    void setTtsState(TtsState pState){
        try {
            if(ttsState == pState)
                return;
            ttsState = pState;
            switch (pState){
                case pause:
                    btnPause.setBackgroundColor(Color.parseColor("#03A9F4"));
                    btnPlay.setBackgroundColor(Color.LTGRAY);
                    btnStop.setBackgroundColor(Color.LTGRAY);
                    break;
                case playing:
                    btnPlay.setBackgroundColor(Color.parseColor("#03A9F4"));
                    btnPause.setBackgroundColor(Color.LTGRAY);
                    btnStop.setBackgroundColor(Color.LTGRAY);
                    break;
                case stopped:
                    btnStop.setBackgroundColor(Color.parseColor("#03A9F4"));
                    btnPlay.setBackgroundColor(Color.LTGRAY);
                    btnPause.setBackgroundColor(Color.LTGRAY);
            }
        }catch (Exception ex){
            Log.d("[setState]", ex.getMessage());
        }
    }
    void setTabletInfo(){
        double vWidth, vHeight;
        String vsUrl;

        vWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        vHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        if(vWidth > vHeight)
            vWidth = vHeight;
        if(vWidth > 600)
            _isTablet = true;
        else
            _isTablet = false;

    }
    void speakData(){
        String vsData;
        try {
            vsData = text.getText().toString().trim();
            if (vsData.isEmpty()) {
                Util.alertMsg("Please input url or content", getApplicationContext());
                return;
            }
            if (vsData.toLowerCase().startsWith("http:") || vsData.toLowerCase().startsWith("https:"))
                excuteTask(vsData);
            else
                speakMultiline(vsData);
        }catch (Exception ex){
            showNote("[speakData] " + ex.getMessage());
        }
    }
    void excuteTask(String psUrl){
        RequestTask req;
        try {
            showHideIndicator(true);
            //send para this class has implement PostTaskListener
            //will call func postTaskListener.onPostTask(result) after download url success
            req = new RequestTask(this);
            req.execute(psUrl);
            req = null;
        }catch (Exception ex){
            showNote("[excuteTask] " + ex.getMessage());
        }
    }
    void speakCurrent(){
        String vsData;
        int vCount;
        try {
            vCount = arrSentence.size();
            if (mIndex >= vCount) {
                setIndex(-1);
                setTtsState(TtsState.stopped);
                nextChapter();
                return;
            } else {
                setTtsState(TtsState.playing);
                showIndex();
            }
            vsData = arrSentence.get(mIndex);
            speakText(vsData);
        }catch (Exception ex){
            showNote("[speakCurrent] " + ex.getMessage());
        }
    }
    void speakNext(){
        String vsData;
        int vCount;
        setIndex(mIndex + 1);
        vCount = arrSentence.size();
        if(vCount > 0) {
            if (mIndex >= vCount) {
                setIndex(-1);
                setTtsState(TtsState.stopped);
                nextChapter();
                return;
            }
        }else
            return;
        vsData = arrSentence.get(mIndex);
        speakText(vsData);
    }
    void speakPrevious(){
        String vsData;
        setIndex(mIndex - 1);
        if(mIndex <= 0){
            setIndex(-1);
            setTtsState(TtsState.stopped);
            return;
        }
        vsData = arrSentence.get(mIndex);
        speakText(vsData);
    }

    void speakText(String psMsg) {
        if(psMsg != null && !psMsg.isEmpty()){
            tts.speak(psMsg, TextToSpeech.QUEUE_FLUSH, map);
            setSentence(psMsg);
        }
        //tts.isSpeaking();
    }
    int countWord(String psData){
        String[] arrWord;
        int vCountWord = 0;
        arrWord = psData.split("[,\"\\s+]");
        vCountWord = arrWord.length;
        if(vCountWord == 1 && arrWord[0].isEmpty())
            vCountWord = 0;
        return vCountWord;
    }
    void speakMultiline(String psData) {
        int i, vCount, vCountWord, vIdxSentence = 0, vChapterPrev, vIdxSentencePrev;
        String[] arrMsg, arrWord;
        String vsMsg, vsConfigSearch;
        try {
            vsConfigSearch = cfg.getSearchContent();
            if(vsConfigSearch.isEmpty() || vsConfigSearch == null)
                vsConfigSearch = "class=\"chapter-c\"";
            //tìm content truyện bắt đầu từ:
            psData = Util.parserContent(psData, vsConfigSearch);
            arrMsg = psData.split("\\.");
            vCount = arrMsg.length;
            resetSentence();
            if (vCount == 0) {
                addSentence(psData);
            } else {
                for (i = 0; i < vCount; i++) {
                    vsMsg = arrMsg[i].trim();
                    vCountWord = countWord(vsMsg);
                    if (vCountWord == 0)
                        continue;
                    addSentence(vsMsg);
                }
            }
            vCount = arrSentence.size();
            if (vCount > 0) {
                getChapter(true);
                vChapterPrev = cfg.getChapter();
                vIdxSentence = getPageNumber();
                if (vIdxSentence < 0 && vChapterPrev == _chapter) {
                    vIdxSentencePrev = cfg.getSentence();
                    if (vIdxSentencePrev >= 0 && vIdxSentencePrev < vCount)
                        vIdxSentence = vIdxSentencePrev;
                }
                if(vIdxSentence < 0)
                    vIdxSentence = 0;
                setIndex(vIdxSentence);
                updateConfig();
                speakCurrent();
            }
        }catch (Exception ex){
            showNote("[speakMultiline] " + ex.getMessage());
        }
    }
    void setIndex(int pIdx){
        mIndex = pIdx;
        showIndex();
    }
    void showIndex(){
        if(mIndex >= 0)
            setMsg("Paragraph " + (mIndex + 1) + "/" + arrSentence.size(), false);
        else
            setMsg("", false);
    }
    public void stopMedia(View view){
        _isStopAction = true;
        stopSpeak();
        updateConfig();
        _isStopAction = false;
    }
    void stopSpeak(){
        setIndex(-1);
        pageNumber.setText("");
        if(ttsState != TtsState.stopped) {
            setTtsState(TtsState.stopped);
            _isStopAction = true;
            tts.stop();
            _isStopAction = false;
        }
    }
    public void loadConfig(View view){
        Intent intent = new Intent(getBaseContext(), ConfigActivity.class);
        String vSearchContent = cfg.getSearchContent();
        intent.putExtra("SearchContent", vSearchContent);
        startActivity(intent);
    }
    //write cache for reload
    void updateConfig(){
        String vsUrl;
        UserSessionData vUser;
        int vChapter, vIdxSentence;
        try {
            vsUrl = text.getText().toString().trim();
            if(!vsUrl.toLowerCase().startsWith("http:") && !vsUrl.toLowerCase().startsWith("https:")){
                vsUrl = "";
                vChapter = -1;
                vIdxSentence = -1;
            }else {
                vChapter = _chapter;
                vIdxSentence = mIndex;
            }
            cfg.setUrl(vsUrl);
            cfg.setChapter(vChapter);
            cfg.setSentence(vIdxSentence);
            vUser = cfg.getUser();
            Common.setStoredUser(getApplicationContext(), vUser);
        }catch (Exception ex){
            showNote("[updateConfig] " + ex.getMessage());
        }
    }
}

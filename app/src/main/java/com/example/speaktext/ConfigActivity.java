package com.example.speaktext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.speaktext.util.GlobalData;
import com.example.speaktext.util.Util;

public class ConfigActivity extends AppCompatActivity {
    EditText text, txtChapter;
    ImageButton btnSave;
    GlobalData cfg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_config);
        initControl();
    }
    void initControl(){
        String vsConfigSearch, vsCfgChapter;
        try {
            cfg = GlobalData.getInstance();
            text = findViewById(R.id.txtConfig);
            txtChapter = findViewById(R.id.txtConfigChapter);
            btnSave = findViewById(R.id.btnPlay);
            vsConfigSearch = cfg.getSearchContent();
            vsCfgChapter = cfg.getSearchChapter();
            // get the text from MainActivity
            /*Intent intent = getIntent();
            vsConfigSearch = intent.getStringExtra("SearchContent");*/
            if(vsConfigSearch.isEmpty() || vsConfigSearch == null)
                vsConfigSearch = "class=\"chapter-c\"";
            text.setText(vsConfigSearch);
            if(vsCfgChapter.isEmpty() || vsCfgChapter == null)
                vsCfgChapter = "chuong-";
            txtChapter.setText(vsCfgChapter);
        }catch (Exception ex){
            Util.alertMsg("[initControl] " + ex.getMessage(), getApplicationContext());
        }
    }
    public void closeConfig(View view){
        finish();
    }
    public void saveConfig(View view){
        String vsConfigSearch, vsCfgChapter;
        vsConfigSearch = text.getText().toString().trim();
        vsCfgChapter = txtChapter.getText().toString().trim();
        if(vsConfigSearch.isEmpty()) {
            Util.alertMsg("Hãy nhập chuỗi giá trị cần tìm.", getApplicationContext());
            return;
        }
        if(vsCfgChapter.isEmpty()) {
            Util.alertMsg("Hãy nhập chuỗi phân trang cần tìm.", getApplicationContext());
            return;
        }
        cfg.setSearchContent(vsConfigSearch);
        cfg.setSearchChapter(vsConfigSearch);
        finish();
    }
}

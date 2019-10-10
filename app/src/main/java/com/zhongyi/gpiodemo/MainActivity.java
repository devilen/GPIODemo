package com.zhongyi.gpiodemo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zhongyi.gpiolibrary.CommandExec;
import com.zhongyi.gpiolibrary.GpioManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //    static {
//        System.loadLibrary("native-lib");
//    }
    ///其中1-绿灯 11-白灯 13-红灯
    static int[] GPIO_ID = {1, 8, 11, 12, 13, 14, 164, 165, 170, 204, 220, 230, 231, 237, 251, 257};
    //1 8 11 12 13 14 164 165 170 204 220 230 231 237 251 257
    static int LED_GREEN = 1;
    static int LED_WHITE = 11;
    static int LED_RED = 13;
    static int flag_green = 0;
    static int flag_white = 0;
    static int flag_red = 0;

    /**
     * 存放了自由组合灯的策略
     */
    private HashMap<String, Integer[]> freeMap = new HashMap<>();
    private Iterator<Map.Entry<String, Integer[]>> iterator;

    /**
     * 存放三个独立led策略
     */
    private Map<View, Integer> map = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        execLed();
        map.put(findViewById(R.id.btn_write), LED_WHITE);
        map.put(findViewById(R.id.btn_red), LED_RED);
        map.put(findViewById(R.id.btn_green), LED_GREEN);
        initFreeMap();

        findViewById(R.id.btn_write).setOnClickListener(this);
        findViewById(R.id.btn_red).setOnClickListener(this);
        findViewById(R.id.btn_green).setOnClickListener(this);
        findViewById(R.id.btn_free).setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        releaseLed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeAll(); //关闭独立的三个LED灯
        closeFreeLed(); //关闭自由组合的等
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseLed();
    }

    /**
     * 让led可执行
     */
    private void execLed() {
        CommandExec.execLed(LED_WHITE);
        CommandExec.execLed(LED_RED);
        CommandExec.execLed(LED_GREEN);
    }

    /**
     * 释放led灯
     */
    private void releaseLed() {
        CommandExec.releaseLed(LED_WHITE);
        CommandExec.releaseLed(LED_RED);
        CommandExec.releaseLed(LED_GREEN);
    }

    private void initFreeMap() {
        freeMap.put("白+红灯亮", new Integer[]{LED_WHITE, LED_RED});
        freeMap.put("白+绿灯亮", new Integer[]{LED_WHITE, LED_GREEN});
        freeMap.put("绿+红灯亮", new Integer[]{LED_GREEN, LED_RED});
        freeMap.put("白+红+绿灯亮", new Integer[]{LED_WHITE, LED_RED, LED_GREEN});
        iterator = freeMap.entrySet().iterator();
    }

    private void resetFreeLedView() {
        TextView mText = findViewById(R.id.btn_free);
        if (mText.getText().toString().trim().contains("亮")) {
            mText.setText("自由组合灯灭");
        }
    }

    private void resetSingleLedView() {
        for (Map.Entry<View, Integer> flag : map.entrySet()) {
            TextView mText = (TextView) flag.getKey();
            if (mText.getText().toString().trim().contains("亮")) {
                String replace = mText.getText().toString().replace("亮", "灭");
                mText.setText(replace);
            }
        }
    }

    /**
     * 关闭三个独立的led等
     */
    private void closeAll() {
        for (Map.Entry<View, Integer> flag : map.entrySet()) {
            TextView mText = (TextView) flag.getKey();
            if (mText.getText().toString().trim().contains("亮")) {
                GpioManager.writeGpioStatus(flag.getValue(), 0);
            }
        }
    }

    /**
     * 关闭组合灯
     */
    private void closeFreeLed() {
        TextView mTextView = findViewById(R.id.btn_free);
        String text = mTextView.getText().toString().trim();
        for (Map.Entry<String, Integer[]> entries : freeMap.entrySet()) {
            if (entries.getKey().equals(text)) {
                Integer[] value = entries.getValue();
                for (int led : value) {
                    GpioManager.writeGpioStatus(led, 0);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        closeAll();
        closeFreeLed();
        resetSingleLedView();
        if (v.getId() == R.id.btn_red || v.getId() == R.id.btn_green || v.getId() == R.id.btn_write) {
            resetFreeLedView();
        }
        switch (v.getId()) {
            case R.id.btn_write:
                flag_white = (flag_white == 0 ? 1 : 0);
                GpioManager.writeGpioStatus(LED_WHITE, flag_white);
                ((TextView) v).setText(flag_white == 1 ? "白灯亮" : "白灯灭");
                flag_green = 0;
                flag_red = 0;
                break;
            case R.id.btn_green:
                flag_green = (flag_green == 0 ? 1 : 0);
                GpioManager.writeGpioStatus(LED_GREEN, flag_green);
                ((TextView) v).setText(flag_green == 1 ? "绿灯亮" : "绿灯灭");
                flag_white = 0;
                flag_red = 0;
                break;
            case R.id.btn_red:
                flag_red = (flag_red == 0 ? 1 : 0);
                GpioManager.writeGpioStatus(LED_RED, flag_red);
                ((TextView) v).setText(flag_red == 1 ? "红灯亮" : "红灯灭");
                flag_white = 0;
                flag_green = 0;
                break;
            case R.id.btn_free:
                flag_red = 0;
                flag_white = 0;
                flag_green = 0;
                if (!iterator.hasNext()) { //数据没有了重新恢复
                    iterator = freeMap.entrySet().iterator();
                    ((TextView) v).setText("自由组合灯灭");
                    return;
                }
                Map.Entry<String, Integer[]> next = iterator.next();
                ((TextView) v).setText(next.getKey());
                Integer[] value = next.getValue();
                for (int led : value) {
                    GpioManager.writeGpioStatus(led, 1);
                }
                break;
        }
    }

}

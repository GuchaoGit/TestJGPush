package com.guc.testjgpush;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * logt 快速输入
 * Lambda 表达式用法
 */
public class SecondActivity extends AppCompatActivity {
    private static final String TAG = "SecondActivity";
    private static final int REQUEST_CODE_SETTING = 2;
    @BindView(R.id.btn_lambda)
    Button mBtnLambda;
    @BindView(R.id.progress_percent)
    TextView mProgressPercent;
    @BindView(R.id.pb_progressbar)
    ProgressBar mPbProgressbar;
    @BindView(R.id.btn_start)
    Button mBtnStart;
    private float scrollDistance;
    private int width;
    private int currentStatue;
    private float currentPosition;
    private int tvWidth;
    private boolean isRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_second);
        ButterKnife.bind(this);
        startSetting();
        testLambda();
        initView();
    }

    private void initView() {
        // 得到progressBar控件的宽度
        ViewTreeObserver vto2 = mPbProgressbar.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPbProgressbar.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                width = mPbProgressbar.getWidth();
            }
        });
    }

    /**
     * 基本语法:
     * (parameters) -> expression
     * 或
     * (parameters) ->{ statements; }
     */
    private void testLambda() {
        Log.d(TAG, "testLambda: Lambda表达式写的点击事件");
        mBtnLambda.setOnClickListener(event -> Toast.makeText(this, "Lambda表达式生效", Toast.LENGTH_LONG).show());
        mBtnStart.setOnClickListener(event -> {
            if (isRunning) return;
            currentStatue = 0;
            currentPosition = 0;
            mPbProgressbar.setProgress(0);
            startProgress();
        });
    }

    private void startProgress() {
        //开启分线程
        new Thread(() -> {
            isRunning = true;
            //每一段要移动的距离
            scrollDistance = (float) ((1.0 / mPbProgressbar.getMax()) * width);
            for (int i = 1; i <= 100; i++) {
                runOnUiThread(() -> {
                    currentStatue++;
                    mPbProgressbar.incrementProgressBy(1);
                    mProgressPercent.setText(currentStatue + "%");
                    // 得到字体的宽度
                    tvWidth = mProgressPercent.getWidth();
                    currentPosition += scrollDistance;
                    //做一个平移动画的效果
                    // 这里加入条件判断
                    if (tvWidth + currentPosition <= width - mProgressPercent.getPaddingRight()) {
                        mProgressPercent.setTranslationX(currentPosition);
                    }
                });
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isRunning = false;
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTING) {
            Toast.makeText(this, "从设置界面返回", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 跳转到设置界面
     */
    private void startSetting() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_SETTING);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivityForResult(intent, REQUEST_CODE_SETTING);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

}

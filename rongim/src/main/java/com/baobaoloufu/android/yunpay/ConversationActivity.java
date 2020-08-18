package com.baobaoloufu.android.yunpay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConversationActivity extends FragmentActivity {

    private TextView timeText;
    private ImageView timeIconSingle;
    private RelativeLayout timeLayout;
    private LinearLayout endLayout;
    private TextView btnHuifang;
    private ProgressDialog progressDialog;


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 888:
                    timeText.setText((String)msg.obj);
                    setTime(false);
                    break;

                case 0:
                    endLayout.setVisibility(View.VISIBLE);
                    timeLayout.setVisibility(View.GONE);
                    timeText.setText("");
                    hide_keyboard_from(ConversationActivity.this, endLayout);
                    handler.removeCallbacksAndMessages(null);
                    setTime(true);
                    break;

                case 1:
                    endLayout.setVisibility(View.GONE);
                    timeLayout.setVisibility(View.VISIBLE);
                    initTime((String) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.conversation);

        ImageView backIcon = findViewById(R.id.back_icon);
        TextView title = findViewById(R.id.title);
        ImageView rightIcon = findViewById(R.id.right_icon);
        endLayout = findViewById(R.id.end_layout);
        btnHuifang = findViewById(R.id.btn_huifang);

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        title.setText(getIntent().getData().getQueryParameter("title"));

        rightIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPop(rightIcon);
            }
        });

        timeLayout = findViewById(R.id.time_bg_layout);
        timeIconSingle = findViewById(R.id.time_icon_single);
        timeText = findViewById(R.id.time_text);

        timeIconSingle.setVisibility(View.GONE);
        String expiryDate = ShareUtils.getValue(this,"expiryDate", "");
        initTime(expiryDate);


        timeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this).setTitle("提示")
                        .setMessage("是否结束咨询?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                initGetData("chatEnd");
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                builder.create().show();
            }
        });

        btnHuifang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                initGetData("chat");
            }
        });


        int status = Integer.valueOf(ShareUtils.getValue(this,"status", "")) ;
        if(status == 1){
            //咨询中
            endLayout.setVisibility(View.GONE);
            timeLayout.setVisibility(View.VISIBLE);
        }else{
            // 已结束
            endLayout.setVisibility(View.VISIBLE);
            timeLayout.setVisibility(View.GONE);
        }
        initWinView();

//        (ConversationFragment)(getSupportFragmentManager().findFragmentById(R.id.conversation))

    }

    private void initTime(String expiryDate){
        if(!TextUtils.isEmpty(expiryDate)){

            long time = 0;
            try{
                time = Long.valueOf(expiryDate);
            }catch (Exception e){
                e.printStackTrace();
                time = 0;
            }
            if(time != 0){
                Date date = new Date();
                long startTime = date.getTime();
                if(startTime > time){
                    timeLayout.setVisibility(View.GONE);
                }else{
                    midTime = (time - startTime) / 1000;
                    setTime(false);
                }
            }else{
                endLayout.setVisibility(View.VISIBLE);
                timeLayout.setVisibility(View.GONE);
                hide_keyboard_from(ConversationActivity.this, endLayout);
            }


        }
    }

    private void initWinView(){

        timeLayout.setOnTouchListener(new View.OnTouchListener() {
            private boolean isDraged = false; //View是否被移动过
            private boolean isDrag = false; //判断是拖动还是点击
            private int lastX = 0;
            private int lastY = 0; //手指在屏幕上的坐标

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                timeLayout.getParent().requestDisallowInterceptTouchEvent(true);
                boolean consumed = false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isDrag = false;
                        isDraged = false;
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX()-lastX;
                        int dy = (int) event.getRawY()-lastY; //手指在屏幕上移动的距离

                        if (isDraged){
                            isDrag = true; //如果已经被拖动过，那么无论本次移动的距离是否为零，都判定本次事件为拖动事件
                        }else {
                            if (dx == 0 && dy == 0) {
                                isDraged = false; //如果移动的距离为零，则认为控件没有被拖动过，灵敏度可以自己控制
                                isDrag = false;
                                break;
                            } else {
                                isDraged = true;
                                isDrag = true;
                            }
                        }
                        int lastYFlag= (int) (v.getTop()+event.getY());//现在的相对Parent触摸坐标点
                        int height=v.getHeight();

                        setRelativeViewLocation(v, timeLayout.getLeft(), lastYFlag - height / 2, timeLayout.getLeft()+timeLayout.getWidth(), lastYFlag + height / 2);
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return isDrag;//此处一定要返回true，否则监听不生效
            }
        });

    }


    private void setRelativeViewLocation(View view, int left, int top, int right, int bottom) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(right - left, bottom - top);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.setMargins(left, top, 0, 0);
        view.setLayoutParams(params);
    }

    private void initGetData(String url){
//        if(progressDialog == null){
//            progressDialog = new ProgressDialog(this);
//        }
//        if (!progressDialog.isShowing()) {
//            progressDialog.show();
//        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //公共请求头
        HttpInterceptor httpInterceptor = new HttpInterceptor(this);

        builder.addInterceptor(httpInterceptor);
        OkHttpClient client = builder.build();

        //第三步创建Rquest
        Request request = new Request.Builder()
                .url(BuildConfig.BASE_URL+ "/sapi/rongyun/"+url+"?to="+ShareUtils.getValue(this, "targetId", ""))
                .build();

        //第四步创建call回调对象
        final Call call = client.newCall(request);
        //第五步发起请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                if (progressDialog != null && progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }

                String result = response.body().string();
                Log.i("result", result);
                try {
                    JSONObject object = new JSONObject(result);
                    int status = object.getInt("status");
                    if(status == 0){
                        Message message = handler.obtainMessage(0);
                        if(TextUtils.equals(url, "chat")){
                            message = handler.obtainMessage(1);
                            JSONObject data = object.getJSONObject("data");
                            message.obj = data.getLong("expiryDate")+"";
                        }
                        handler.sendMessage(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    long midTime;
    private void setTime(boolean isStop){
        if(isStop){
            return;
        }
        if(midTime < 0){
            endLayout.setVisibility(View.VISIBLE);
            timeLayout.setVisibility(View.GONE);
//            Toast.makeText(ConversationActivity.this, "本次回访已结束", Toast.LENGTH_LONG).show();
            return;
        }
        midTime--;
        long hh = midTime / 60 / 60 % 60;
        long mm = midTime / 60 % 60;
        long ss = midTime % 60;

        System.out.println("还剩" + hh + "小时" + mm + "分钟" + ss + "秒");

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                msg.what = 888;
                String time, hStr, mStr, sStr;
                if(hh == 0){
                    hStr = "00";
                }else if(hh < 10){
                    hStr = "0"+hh;
                }
                else{
                    hStr = hh+"";
                }

                if(mm == 0){
                    mStr = "00";
                }else if(mm < 10){
                    mStr = "0"+mm;
                }
                else{
                    mStr = mm+"";
                }

                if(ss == 0){
                    sStr = "00";
                }else if(ss < 10){
                    sStr = "0"+ss;
                }
                else{
                    sStr = ss+"";
                }

                time = hStr+ ":"+mStr+":"+sStr;
                msg.obj = time;
                handler.sendMessage(msg);
            }
        }, 1200);
    }


    private void setPop(View rightIcon){
        View contentView = LayoutInflater.from(this).inflate(R.layout.pop_layout, null);
        PopupWindow window=new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        TextView changeFee = contentView.findViewById(R.id.change_fee);
        TextView clearChat = contentView.findViewById(R.id.clear_chat);

        changeFee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                Intent intent = new Intent();
                intent.setAction("com.rong.setting");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(intent);

            }
        });

        clearChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                RongIM.getInstance().deleteMessages(Conversation.ConversationType.PRIVATE, getIntent().getData().getQueryParameter("targetId"), new RongIMClient.ResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Toast.makeText(ConversationActivity.this, "清除成功", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {

                    }
                });
            }
        });


        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setOutsideTouchable(true);
        window.setTouchable(true);
        window.showAsDropDown(rightIcon, 20, -30);
    }


    public void hide_keyboard_from(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


}

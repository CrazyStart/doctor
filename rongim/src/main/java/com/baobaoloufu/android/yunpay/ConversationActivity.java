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

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.baobaoloufu.android.yunpay.bean.SingleConsumerBean;
import com.baobaoloufu.android.yunpay.dialog.CloseConsultDialog;
import com.baobaoloufu.android.yunpay.dialog.SendConsultDialog;
import com.baobaoloufu.android.yunpay.http.MyObserver;
import com.baobaoloufu.android.yunpay.http.RetrofitUtils;
import com.baobaoloufu.android.yunpay.http.RxHelper;
import com.baobaoloufu.android.yunpay.util.TimeUtils;
import com.google.gson.Gson;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle3.components.support.RxFragmentActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConversationActivity extends FragmentActivity {
    //赠送机会
    private LinearLayout mLayoutSendConsult;
    private TextView mTvTipSendConsult;
    private TextView mTvSendStatus1;
    private LinearLayout mLayoutSendStatus2;
    private TextView mTvSendConsult;
    //聊天文本状态
    private TextView mTvChatStatus;
    private LinearLayout mLayoutChatStatus;
    private TextView mTvConsultCount;
    private TextView mTvConsultExpiryTime;
    //顶部倒计时
    private LinearLayout mLayoutTimeDown;
    //倒计时时间
    private TextView timeText;
    //关闭咨询
    private LinearLayout layout_close_chat;

    private SingleConsumerBean mSingleConsumerBean;

    private LinearLayout endLayout;
    private TextView btnHuifang;
    private ProgressDialog progressDialog;
    private Context mContext;

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
                    mLayoutSendConsult.setVisibility(View.VISIBLE);
                    mLayoutTimeDown.setVisibility(View.GONE);
                    timeText.setText("");
                    hide_keyboard_from(ConversationActivity.this, mLayoutSendConsult);
                    handler.removeCallbacksAndMessages(null);
                    setTime(true);
                    break;
                case 1:
                    mLayoutSendConsult.setVisibility(View.GONE);
                    mLayoutTimeDown.setVisibility(View.VISIBLE);
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

        mLayoutSendConsult = findViewById(R.id.layout_send_consult);
        mTvTipSendConsult = findViewById(R.id.tv_tip_send_consult);
        mTvSendStatus1 = findViewById(R.id.tv_send_consult_status_1);
        mTvSendConsult = findViewById(R.id.tv_send_consult);
        mLayoutSendStatus2 = findViewById(R.id.layout_send_consult_status_2);
        mLayoutChatStatus = findViewById(R.id.layout_chat_status_2);
        mTvConsultCount = findViewById(R.id.tv_consult_count);
        mTvConsultExpiryTime = findViewById(R.id.tv_consult_expiry_time);
        layout_close_chat = findViewById(R.id.layout_close_chat);

        layout_close_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseConsultDialog closeConsultDialog = new CloseConsultDialog(mContext, new CloseConsultDialog.OnDialogClickListener() {
                    @Override
                    public void onCancelClick() {
                        initGetData("chatEnd");
                        mSingleConsumerBean.setStatus(4);
                        refreshBottomView(mSingleConsumerBean);
                    }

                    @Override
                    public void onConfirmClick(int type) {

                    }
                });
                closeConsultDialog.show(getSupportFragmentManager(),"layout_close_chat");
            }
        });

        mTvSendConsult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendConsultDialog sendConsultDialog = new SendConsultDialog(mContext, new SendConsultDialog.OnDialogClickListener() {
                    @Override
                    public void onCancelClick() {

                    }

                    @Override
                    public void onConfirmClick(int type) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("type", String.valueOf(type));
                        map.put("cid", ShareUtils.getValue(mContext, "targetId", ""));
                        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                        RequestBody requestBody = RequestBody.create(JSON, new Gson().toJson(map));

                        RetrofitUtils.getApiUrl().sendConsult(ShareUtils.getValue(mContext, "token", ""),requestBody)
                                .compose(RxHelper.observableIO2Main(mContext))
                                .subscribe(new MyObserver<Object>(mContext) {
                                    @Override
                                    public void onSuccess(Object demo) {
                                        initGetData("chat");
                                        getConsumerChatStatus();
                                    }

                                    @Override
                                    public void onFailure(Throwable e, String errorMsg) {

                                    }
                                });
                    }
                });
                sendConsultDialog.show(getSupportFragmentManager(),"sendConsultDialog");
            }
        });

        mTvChatStatus = findViewById(R.id.tv_chat_status);
        mLayoutTimeDown = findViewById(R.id.layout_count_down);
        timeText = findViewById(R.id.time_text);

        mContext = this;
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



        // TODO: 2020/8/19 结束咨询
//        timeLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this).setTitle("提示")
//                        .setMessage("是否结束咨询?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.dismiss();
//                                initGetData("chatEnd");
//                            }
//                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.dismiss();
//                            }
//                        });
//                builder.create().show();
//            }
//        });

        btnHuifang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                initGetData("chat");
            }
        });


//        int status = Integer.valueOf(ShareUtils.getValue(this,"status", "")) ;
        getConsumerChatStatus();
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
                    mLayoutTimeDown.setVisibility(View.GONE);
                }else{
                    midTime = (time - startTime) / 1000;
                    setTime(false);
                }
            }else{
                mLayoutSendConsult.setVisibility(View.VISIBLE);
                mLayoutTimeDown.setVisibility(View.GONE);
                hide_keyboard_from(ConversationActivity.this, mLayoutSendConsult);
            }
        }
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
            mLayoutSendConsult.setVisibility(View.VISIBLE);
            mLayoutTimeDown.setVisibility(View.GONE);
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
//                Intent intent = new Intent();
//                intent.setAction("com.rong.setting");
//                intent.addCategory(Intent.CATEGORY_DEFAULT);
//                startActivity(intent);

                Intent intent = new Intent(mContext,SetFeeActivity.class);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(intent);
//                intent.setAction("com.rong.setting");
//                intent.addCategory(Intent.CATEGORY_DEFAULT);
//                startActivity(intent);
            }
        });

        clearChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
//                RongIM.getInstance().deleteMessages(Conversation.ConversationType.PRIVATE, getIntent().getData().getQueryParameter("targetId"), new RongIMClient.ResultCallback<Boolean>() {
//                    @Override
//                    public void onSuccess(Boolean aBoolean) {
//                        Toast.makeText(ConversationActivity.this, "清除成功", Toast.LENGTH_LONG).show();
//
//                    }
//
//                    @Override
//                    public void onError(RongIMClient.ErrorCode errorCode) {
//
//                    }
//                });
                String id = ShareUtils.getValue(ConversationActivity.this, "targetId", "");
                EventBus.getDefault().post(new DataSynEvent("detail", id));
                finish();

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


    //获取单个患者的聊天状态
    private void getConsumerChatStatus() {
        RetrofitUtils.getApiUrl().getConsumerChatStatus(ShareUtils.getValue(mContext, "token", ""),ShareUtils.getValue(mContext, "targetId", ""))
                .compose(RxHelper.observableIO2Main(mContext))
                .subscribe(new MyObserver<SingleConsumerBean>(mContext) {
                    @Override
                    public void onSuccess(SingleConsumerBean singleConsumerBean) {
                        if (null == singleConsumerBean) {
                            return;
                        }
                        mSingleConsumerBean = singleConsumerBean;
//                        singleConsumerBean.setStatus(2);
//                        Date date = new Date();
//                        long startTime = date.getTime();
//                        singleConsumerBean.setExpiryDate(String.valueOf(startTime + 2000000));
                        refreshBottomView(singleConsumerBean);
                    }

                    @Override
                    public void onFailure(Throwable e, String errorMsg) {
                    }
                });
    }

    private void refreshBottomView(SingleConsumerBean singleConsumerBean) {
        switch (singleConsumerBean.getStatus()) {
            case 0://新患者
                mLayoutSendConsult.setVisibility(View.VISIBLE);
                mLayoutTimeDown.setVisibility(View.GONE);
                mTvSendStatus1.setVisibility(View.VISIBLE);
                mLayoutSendStatus2.setVisibility(View.GONE);
                mTvTipSendConsult.setText("患者未发起咨询，您可以：");
                break;
            case 1:// 医生赠送服务中
                mLayoutSendConsult.setVisibility(View.GONE);
                mLayoutTimeDown.setVisibility(View.GONE);
                mTvChatStatus.setVisibility(View.VISIBLE);
                mTvChatStatus.setText("医生赠送服务中");
                mLayoutChatStatus.setVisibility(View.GONE);
                break;
            case 2://医生赠送服务中(显示倒计时)
                mLayoutTimeDown.setVisibility(View.VISIBLE);
                mLayoutSendConsult.setVisibility(View.GONE);
                mTvChatStatus.setVisibility(View.VISIBLE);
                mTvChatStatus.setText("医生赠送服务中");
                initTime(singleConsumerBean.getExpiryDate());
                break;
            case 3:
                switch (singleConsumerBean.getType()) {
                    case 1:
                        mLayoutTimeDown.setVisibility(View.VISIBLE);
                        mLayoutSendConsult.setVisibility(View.GONE);
                        mTvChatStatus.setVisibility(View.VISIBLE);
                        mTvChatStatus.setText("在线问诊中");
                        initTime(singleConsumerBean.getExpiryDate());
                        break;
                    case 2:
                        mLayoutTimeDown.setVisibility(View.GONE);
                        mLayoutSendConsult.setVisibility(View.GONE);
                        mTvChatStatus.setVisibility(View.GONE);
                        mLayoutChatStatus.setVisibility(View.VISIBLE);
                        mTvConsultCount.setText(String.valueOf(singleConsumerBean.getRemain_count()));
                        mTvConsultExpiryTime.setText(TimeUtils.getDate2String(singleConsumerBean.getExpiryDate()));
                        break;
                    case 3:
                        mLayoutTimeDown.setVisibility(View.GONE);
                        mLayoutSendConsult.setVisibility(View.GONE);
                        mTvChatStatus.setVisibility(View.VISIBLE);
                        mTvChatStatus.setText("在线问诊中");
                        break;

                }
                break;
            case 4://咨询结束
                mLayoutTimeDown.setVisibility(View.GONE);
                mLayoutSendConsult.setVisibility(View.VISIBLE);
                mTvSendStatus1.setVisibility(View.GONE);
                mLayoutSendStatus2.setVisibility(View.VISIBLE);
                mTvTipSendConsult.setText("问诊已结束，如果患者需要在跟进，你可以");
                break;
            case 5://医生赠送机会
                mLayoutTimeDown.setVisibility(View.GONE);
                mLayoutSendConsult.setVisibility(View.GONE);
                mTvChatStatus.setVisibility(View.GONE);
                mLayoutChatStatus.setVisibility(View.VISIBLE);
                mTvConsultCount.setText(String.valueOf(singleConsumerBean.getRemain_count()));
                mTvConsultExpiryTime.setText(TimeUtils.getDate2String(singleConsumerBean.getExpiryDate()));
                break;
        }
    }

}

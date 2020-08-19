package com.baobaoloufu.android.yunpay;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.baobaoloufu.android.yunpay.bean.PriceBean;
import com.baobaoloufu.android.yunpay.http.MyObserver;
import com.baobaoloufu.android.yunpay.http.RetrofitUtils;
import com.baobaoloufu.android.yunpay.http.RxHelper;
import com.baobaoloufu.android.yunpay.util.JsonUtils;

import butterknife.BindView;

public class SetFeeActivity extends FragmentActivity {
    private Context mContext;
    private TextView tv_price_1,tv_price_2,tv_price_3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set_fee);

        tv_price_1 = findViewById(R.id.tv_price_1);
        tv_price_2 = findViewById(R.id.tv_price_2);
        tv_price_3 = findViewById(R.id.tv_price_3);
        mContext = this;
        getPrice(ShareUtils.getValue(mContext, "token", ""),ShareUtils.getValue(mContext, "targetId", ""));
    }

    private void getPrice(String token,String consumerId) {
        RetrofitUtils.getApiUrl().getPrice(token,consumerId)
                .compose(RxHelper.observableIO2Main(mContext))
                .subscribe(new MyObserver<PriceBean>(mContext) {
                    @Override
                    public void onSuccess(PriceBean priceBean) {
                        if (null == priceBean) {
                            return;
                        }
                        tv_price_1.setText(String.valueOf(priceBean.getPriceInfo().getPrice()));
                        tv_price_2.setText(String.valueOf(priceBean.getPriceInfo().getAsked_price()));
                        tv_price_3.setText(String.valueOf(priceBean.getPriceInfo().getOneonone_price()));
                    }

                    @Override
                    public void onFailure(Throwable e, String errorMsg) {

                    }
                });
    }
}
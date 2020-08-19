package com.baobaoloufu.android.yunpay.api;

import com.baobaoloufu.android.yunpay.bean.PriceBean;
import com.baobaoloufu.android.yunpay.bean.SingleConsumerBean;
import com.baobaoloufu.android.yunpay.constant.ApiConstants;
import com.baobaoloufu.android.yunpay.http.BaseResponse;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    /**
     * 获取单个患者的聊天状态
     */
    @GET("sapi/hcp/consumers/status/single")
    Observable<BaseResponse<SingleConsumerBean>> getConsumerChatStatus(@Header("x-token") String header, @Query("consumer") String consumer);

    /**
     * 赠送聊天机会接口
     */
    @POST("sapi/hcp/hcpConsumerPresent")
    Observable<BaseResponse<Object>> sendConsult(@Header("x-token") String header,@Body RequestBody body);


    /**
     *获取医生的咨询费
     */
    @GET("sapi/hcp/servicePrice")
    Observable<BaseResponse<PriceBean>> getPrice(@Header("x-token") String header, @Query("consumer") String consumer);
}

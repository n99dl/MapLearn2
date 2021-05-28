package com.n99dl.maplearn.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAiMHZ2oI:APA91bG77XO_DycUqiAo87qZM10NL-D9soaN6ytlM4zzhaKC5Jy87N9DD433WPRikqbzqnawmg-B9OK1SYzMdZt_9I5wwDFJpcKY59ZT0HzQV3SGh_S76wbTjZ6JR4xjBzKOs1hwrpUo"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}


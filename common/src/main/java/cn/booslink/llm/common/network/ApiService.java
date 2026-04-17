package cn.booslink.llm.common.network;

import cn.booslink.llm.common.model.ApiResponse;
import cn.booslink.llm.common.model.PkgInfo;
import cn.booslink.llm.common.model.VideoConfig;
import cn.booslink.llm.common.model.request.ApkRequest;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("http://lcts.ottboxer.cn:8088/api/android_lcts/apps/info")
    Single<ApiResponse<PkgInfo>> getApkInfoByPkg(@Body ApkRequest apkRequest);

    @GET("http://ai.ottboxer.cn/api/yuyin/route")
    Single<ApiResponse<VideoConfig>> getVideoRoute(@Query("channel") String channel, @Query("model") String model, @Query("qv") String qv, @Query("iv") String iv);

    @GET("http://ai.ottboxer.cn/api/yuyin/data")
    Single<ApiResponse<VideoConfig>> getVideoData(@Query("key") String key);
}

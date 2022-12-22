package com.mobitant.yesterdaytv.remote;

import com.mobitant.yesterdaytv.item.MyReviewItem;
import com.mobitant.yesterdaytv.item.ReviewItem;
import com.mobitant.yesterdaytv.item.TvInfoItem;
import com.mobitant.yesterdaytv.item.KeepItem;
import com.mobitant.yesterdaytv.item.MemberInfoItem;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface RemoteService {
    // 이공관
    //String BASE_URL = "http://172.19.3.11:3000";
    // 경천관 힐링존
   // String BASE_URL = "http://172.19.6.185:3000";
    // 후생관
    //String BASE_URL = "http://172.17.11.76:3000";
    // 집
    String BASE_URL= "http://192.168.219.193:3000";
    // 학원
    //String BASE_URL = "http://192.168.100.113:3000";

    String MEMBER_ICON_URL = BASE_URL + "/member/";
    String IMAGE_URL = BASE_URL + "/img/";

    //사용자 정보
    @GET("/member/{phone}")
    Call<MemberInfoItem> selectMemberInfo(@Path("phone") String phone);

    @POST("/member/info")
    Call<String> insertMemberInfo(@Body MemberInfoItem memberInfoItem);

    @FormUrlEncoded
    @POST("/member/phone")
    Call<String> insertMemberPhone(@Field("phone") String phone);

    @Multipart
    @POST("/member/icon_upload")
    Call<ResponseBody> uploadMemberIcon(@Part("member_seq") RequestBody memberSeq,
                                        @Part MultipartBody.Part file);

    //TV 정보
    @GET("/item/info/{info_seq}")
    Call<TvInfoItem> selectTvInfo(@Path("info_seq") int foodInfoSeq,
                                  @Query("user_latitude") double userLatitude,
                                  @Query("user_longitude") double userLongitude,
                                  @Query("member_seq") int memberSeq);

    @POST("/item/info")
    Call<String> insertFoodInfo(@Body TvInfoItem infoItem);

    @Multipart
    @POST("/item/info/image")
    Call<ResponseBody> uploadFoodImage(@Part("info_seq") RequestBody infoSeq,
                                       @Part("image_memo") RequestBody imageMemo,
                                       @Part MultipartBody.Part file);

    @GET("/item/list")
    Call<ArrayList<TvInfoItem>> listTvInfo(@Query("member_seq") int memberSeq,
                                           @Query("user_latitude") double userLatitude,
                                           @Query("user_longitude") double userLongitude,
                                           @Query("order_type") String orderType,
                                           @Query("current_page") int currentPage);

    //검색
    @GET("/item/search")
    Call<ArrayList<TvInfoItem>> searchTvInfo(@Query("member_seq") int memberSeq,
                                           @Query("search_content") String searchContent,
                                           @Query("search_type") String searchType,
                                           @Query("current_page") int currentPage,
                                             @Query("user_latitude") double userLatitude,
                                             @Query("user_longitude") double userLongitude);

    //지도
    @GET("/item/map/list")
    Call<ArrayList<TvInfoItem>> listMap(@Query("member_seq") int memberSeq,
                                        @Query("latitude") double latitude,
                                        @Query("longitude") double longitude,
                                        @Query("distance") int distance,
                                        @Query("user_latitude") double userLatitude,
                                        @Query("user_longitude") double userLongitude);

    //즐겨찾기
    @POST("/keep/{member_seq}/{info_seq}")
    Call<String> insertKeep(@Path("member_seq") int memberSeq, @Path("info_seq") int infoSeq);

    @DELETE("/keep/{member_seq}/{info_seq}")
    Call<String> deleteKeep(@Path("member_seq") int memberSeq, @Path("info_seq") int infoSeq);

    @GET("/keep/list")
    Call<ArrayList<KeepItem>> listKeep(@Query("member_seq") int memberSeq,
                                       @Query("user_latitude") double userLatitude,
                                       @Query("user_longitude") double userLongitude);

    //리뷰
    @GET("/review/list")
    Call<ArrayList<ReviewItem>> listReview(@Query("item_seq") int itemSeq,
                                           @Query("current_page") int currentPage);

    @POST("/review/insert")
    Call<String> insertReview(@Body ReviewItem reviewItem);

    @GET("/review/mylist")
    Call<ArrayList<MyReviewItem>> myReview(@Query("member_seq") int memberSeq,
                                             @Query("current_page") int currentPage);

    //추천시스템
    @GET("/recommend/list")
    Call<ArrayList<TvInfoItem>> recommendList(@Query("user_latitude") double userLatitude,
                                             @Query("user_longitude") double userLongitude,
                                             @Query("member_seq") int memberSeq,
                                             @Query("current_page") int currentPage,
                                             @Query("cf_type") String cfType);
    @GET("/recommend/user_based")
    Call<ArrayList<TvInfoItem>> recUserBased(@Query("user_latitude") double userLatitude,
                                             @Query("user_longitude") double userLongitude,
                                             @Query("member_seq") int memberSeq,
                                             @Query("current_page") int currentPage);
}
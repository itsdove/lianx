package com.example.lianx.util;

public class RedisKeyUtil {

    public static final String SPLIT=":";

    public static final String PREFIX_ENTITY_LIKE="like:entity";

    public static final String PREFIX_USER_LIKE="like:user";

    public static final String PREFIX_FOLLOWEE="followee";

    public static final String PREFIX_FOLLOWER="follower";

    public static final String PREFIX_KAPTCHA="kaptcha";

    public static final String PREFIX_TICKET="ticket";

    public static final String PREFIX_USER="user";
    //数据统计
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    //帖子分数
    private static final String PREFIX_POST = "post";
    //热帖
    private static final String PREFIX_HOT_POST = "hot:post";


    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }

    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }

    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }

    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA+SPLIT+owner;
    }

    public static String getTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }

    public static String getUserKey(int userId){
        return PREFIX_USER+SPLIT+userId;
    }
}

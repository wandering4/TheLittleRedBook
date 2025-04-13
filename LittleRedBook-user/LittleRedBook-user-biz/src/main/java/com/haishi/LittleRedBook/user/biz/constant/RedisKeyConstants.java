package com.haishi.LittleRedBook.user.biz.constant;

public class RedisKeyConstants {

    /**
     * 用户角色数据 KEY 前缀
     */
    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";


    /**
     * 用户信息数据 KEY 前缀
     */
    private static final String USER_INFO_KEY_PREFIX = "user:info:";

    /**
     * 用户主页信息数据 KEY 前缀
     */
    private static final String USER_PROFILE_KEY_PREFIX = "user:profile:";


    /**
     * 构建角色主页信息对应的 KEY
     *
     * @param userId
     * @return
     */
    public static String buildUserProfileKey(Long userId) {
        return USER_PROFILE_KEY_PREFIX + userId;
    }


    /**
     * 用户对应的角色集合 KEY
     *
     * @param userId
     * @return
     */
    public static String buildUserRoleKey(Long userId) {
        return USER_ROLES_KEY_PREFIX + userId;
    }


    /**
     * 构建角色对应的权限集合 KEY
     *
     * @param userId
     * @return
     */
    public static String buildUserInfoKey(Long userId) {
        return USER_INFO_KEY_PREFIX + userId;
    }

}

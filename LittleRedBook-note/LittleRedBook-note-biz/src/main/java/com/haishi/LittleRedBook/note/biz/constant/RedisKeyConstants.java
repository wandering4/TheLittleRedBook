package com.haishi.LittleRedBook.note.biz.constant;

public class RedisKeyConstants {

    /**
     * 笔记详情 KEY 前缀
     */
    public static final String NOTE_DETAIL_KEY = "note:detail:";

    /**
     * Roaring Bitmap：用户笔记点赞 前缀
     */
    public static final String R_BITMAP_USER_NOTE_LIKE_LIST_KEY = "rbitmap:note:likes:";


    /**
     * 用户笔记点赞列表 ZSet 前缀
     */
    public static final String USER_NOTE_LIKE_ZSET_KEY = "user:note:likes:";

    /**
     * 布隆过滤器：用户笔记收藏 前缀
     */
    public static final String BLOOM_USER_NOTE_COLLECT_LIST_KEY = "bloom:note:collects:";

    /**
     * Roaring Bitmap：用户笔记收藏 前缀
     */
    public static final String R_BITMAP_USER_NOTE_COLLECT_LIST_KEY = "rbitmap:note:collects:";

    /**
     * 用户笔记收藏列表 ZSet 前缀
     */
    public static final String USER_NOTE_COLLECT_ZSET_KEY = "user:note:collects:";


    /**
     * 构建完整的笔记详情 KEY
     * @param noteId
     * @return
     */
    public static String buildNoteDetailKey(Long noteId) {
        return NOTE_DETAIL_KEY + noteId;
    }

    /**
     * 构建完整的 Roaring Bitmap：用户笔记点赞 KEY
     * @param userId
     * @return
     */
    public static String buildRBitmapUserNoteLikeListKey(Long userId) {
        return R_BITMAP_USER_NOTE_LIKE_LIST_KEY + userId;
    }


    /**
     * 构建完整的用户笔记点赞列表 ZSet KEY
     * @param userId
     * @return
     */
    public static String buildUserNoteLikeZSetKey(Long userId) {
        return USER_NOTE_LIKE_ZSET_KEY + userId;
    }


    /**
     * 构建完整的布隆过滤器：用户笔记收藏 KEY
     * @param userId
     * @return
     */
    public static String buildBloomUserNoteCollectListKey(Long userId) {
        return BLOOM_USER_NOTE_COLLECT_LIST_KEY + userId;
    }

    /**
     * 构建完整的用户笔记收藏列表 ZSet KEY
     * @param userId
     * @return
     */
    public static String buildUserNoteCollectZSetKey(Long userId) {
        return USER_NOTE_COLLECT_ZSET_KEY + userId;
    }

    /**
     * 构建完整的 Roaring Bitmap：用户笔记收藏 KEY
     * @param userId
     * @return
     */
    public static String buildRBitmapUserNoteCollectListKey(Long userId) {
        return R_BITMAP_USER_NOTE_COLLECT_LIST_KEY + userId;
    }

}

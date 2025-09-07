package com.losthiro.ottohubclient.impl;
import com.losthiro.ottohubclient.utils.StringUtils;

/**
 * @Author Hiro
 * @Date 2025/05/22 14:58
 */
public class APIManager {
    public static final String TAG = "APIManager";
    public static final String API_URL="https://api.ottohub.cn/?module=";

    //账户操作类

    public static final class AccountURI {
        private static final String getAccountURI() {
            return StringUtils.strCat(API_URL, "auth&action=");
        }

        public static final String getLoginURI(String uid_email, String pw) {
            return StringUtils.strCat(new String[]{getAccountURI(), "login&uid_email=", uid_email, "&pw=", pw});
        }

        public static final String getLoginURI(long uid_email, String pw) {
            return StringUtils.strCat(new Object[]{getAccountURI(), "login&uid_email=", uid_email, "&pw=", pw});
        }

        public static final String getRegisterURI(String email, String register_verification_code, String pw, String confirm_pw) {
            return StringUtils.strCat(new String[]{getAccountURI(), "register&email=", email, "&register_verification_code=", register_verification_code, "&pw=", pw, "&confirm_pw=", confirm_pw});
        }

        public static final String getPasswordResetURI(String email, String register_verification_code, String pw, String confirm_pw) {
            return StringUtils.strCat(new String[]{getAccountURI(), "passwordreset&email=", email, "&register_verification_code=", register_verification_code, "&pw=", pw, "&confirm_pw=", confirm_pw});
        }

        public static final String getRegisterVerifyURI(String email) {
            return StringUtils.strCat(new String[]{getAccountURI(), "register_verification_code&email=", email});
        }

        public static final String getPasswordResetVerifyURI(String email) {
            return StringUtils.strCat(new String[]{getAccountURI(), "passwordreset_verification_code&email=", email});
        }
    }

    //视频获取类

    public static final class VideoURI {
        public static final int WEEK=7;
        public static final int MONTH=30;
        public static final int QUARTERLY=90;

        public static final int CATEGORY_OTHER=0;
        public static final int CATEGORY_FUN=1;
        public static final int CATEGORY_MAD=2;
        public static final int CATEGORY_VOCALOID=3;
        public static final int CATEGORY_THEATER=4;
        public static final int CATEGORY_GAME=5;
        public static final int CATEGORY_OLD=6;
        public static final int CATEGORY_MUSIC=7;

        private static final String getVideoURI() {
            return StringUtils.strCat(API_URL, "video&action=");
        }

        public static final String getRandomVideoURI(int num) {
            return StringUtils.strCat(new Object[]{getVideoURI(), "random_video_list&num=", num});
        }

        public static final String getNewVideoURI(int offset, int num) {
            return StringUtils.strCat(new Object[]{getVideoURI(), "new_video_list&offset=", offset, "&num=", num});
        }

        //time天数 7 30 90
        public static final String getPopularVideoURI(int time_limit, int offset, int num) {
            return StringUtils.strCat(new Object[]{getVideoURI(), "popular_video_list&time_limit=", time_limit, "&offset=", offset, "&num=", num});
        }

        //category 0其他 1鬼畜 2mad 3人力 4剧场 5游戏 6怀旧 7音乐
        public static final String getCategoryVideoURI(int category, int num) {
            return StringUtils.strCat(new Object[]{getVideoURI(), "category_video_list&category=", category, "&num=", num});
        }

        public static final String getSearchVideoURI(String search_term, int num) {
            return StringUtils.strCat(new Object[]{getVideoURI(), "search_video_list&search_term=", search_term, "&num=", num});
        }

        public static final String getIDvideoURI(long vid) {
            return StringUtils.strCat(new Object[]{getVideoURI(), "id_video_list&vid=", vid});
        }

        public static final String getUserVideo(long uid, int offset, int num) {
            return StringUtils.strCat(new Object[]{getVideoURI(), "user_video_list&uid=", uid, "&offset=", offset, "&num=", num});
        }

        public static final String getAuditVideo(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getVideoURI(), "audit_video_list&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getVideoDetail(long vid) {
            return StringUtils.strCat(new Object[]{getVideoURI(), "get_video_detail&vid=", vid});
        }

        public static final String getVideoDetail(String token, long vid) {
            return StringUtils.strCat(new Object[]{getVideoURI(), "get_video_detail&vid=", vid, "&token=", token});
        }
    }

    //动态管理类

    public static final class BlogURI {
        public static final int WEEK=7;
        public static final int MONTH=30;
        public static final int QUARTERLY=90;

        private static final String getBlogURI() {
            return StringUtils.strCat(API_URL, "blog&action=");
        }

        public static final String getRandomBlogURI(int num) {
            return StringUtils.strCat(new Object[]{getBlogURI(), "random_blog_list&num=", num});
        }

        public static final String getNewBlogURI(int offset, int num) {
            return StringUtils.strCat(new Object[]{getBlogURI(), "new_blog_list&offset=", offset, "&num=", num});
        }

        public static final String getPopularBlogURI(int time_limit, int offset, int num) {
            return StringUtils.strCat(new Object[]{getBlogURI(), "popular_blog_list&time_limit=", time_limit, "&offset=", offset, "&num=", num});
        }

        public static final String getSearchBlogURI(String search_term, int num) {
            return StringUtils.strCat(new Object[]{getBlogURI(), "search_blog_list&search_term=", search_term, "&num=", num});
        }

        public static final String getIDblogURI(long bid) {
            return StringUtils.strCat(new Object[]{getBlogURI(), "id_blog_list&bid=", bid});
        }

        public static final String getUserBlogURI(long uid, int offset, int num) {
            return StringUtils.strCat(new Object[]{getBlogURI(), "user_blog_list&uid=", uid, "&offset=", offset, "&num=", num});
        }

        public static final String getAuditBlogURI(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getBlogURI(), "audit_blog_list&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getBlogDetailURI(long bid) {
            return StringUtils.strCat(new Object[]{getBlogURI(), "get_blog_detail&bid=", bid});
        }

        public static final String getBlogDetailURI(long bid, String token) {
            return StringUtils.strCat(new Object[]{getBlogURI(), "get_blog_detail&bid=", bid, "&token=", token});
        }
    }

    //用户管理类

    public static final class UserURI {
        private static final String getUserURI() {
            return StringUtils.strCat(API_URL, "user&action=");
        }

        public static final String getSearchUserURI(String search_term, int num) {
            return StringUtils.strCat(new Object[]{getUserURI(), "search_user_list&search_term=", search_term, "&num=", num});
        }

        public static final String getIDuserURI(long uid) {
            return StringUtils.strCat(new Object[]{getUserURI(), "id_user_list&uid=", uid});
        }

        public static final String getUserDetail(long uid) {
            return StringUtils.strCat(new Object[]{getUserURI(), "get_user_detail&uid=", uid});
        }
    }

    //关注管理类

    public static final class FollowingURI {
        private static final String getFollowingURI() {
            return StringUtils.strCat(API_URL, "following&action=");
        }

        public static final String getFollowURI(long following_uid, String token) {
            return StringUtils.strCat(new Object[]{getFollowingURI(), "follow&following_uid=", following_uid, "&token=", token});
        }

        public static final String getFollowStatusURI(long following_uid, String token) {
            return StringUtils.strCat(new Object[]{getFollowingURI(), "follow_status&following_uid=", following_uid, "&token=", token});
        }

        public static final String getFollowingListURI(long uid, int offset, int num) {
            return StringUtils.strCat(new Object[]{getFollowingURI(), "following_list&uid=", uid, "&offset=", offset, "&num=", num});
        }

        public static final String getFanListURI(long uid, int offset, int num) {
            return StringUtils.strCat(new Object[]{getFollowingURI(), "fan_list&uid=", uid, "&offset=", offset, "&num=", num});
        }
    }

    //个人信息类

    public static final class MessageURI {
        private static final String getMessageURI() {
            return StringUtils.strCat(API_URL, "im&action=");
        }

        public static final String getNewMessageURI(String token) {
            return StringUtils.strCat(new Object[]{getMessageURI(), "new_message_num&token=", token});
        }

        public static final String getReadMessageURI(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getMessageURI(), "read_message_list&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getUnreadMessageURI(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getMessageURI(), "unread_message_list&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getSentMessageURI(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getMessageURI(), "sent_message_list&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getSendMessageURI(String token, long receiver, String message) {
            return StringUtils.strCat(new Object[]{getMessageURI(), "send_message&token=", token, "&receiver=", receiver, "&message=", message});
        }

        public static final String getReadMessageURI(String token, long message_id) {//类型疑问？
            return StringUtils.strCat(new Object[]{getMessageURI(), "read_message&token=", token, "&msg_id=", message_id});
        }

        public static final String getSystemReaderMessageURI(String token) {
            return StringUtils.strCat(new Object[]{getMessageURI(), "read_all_system_message&token=", token});
        }
    }

    //点赞收藏类

    public static final class EngagementURI {
        private static final String getEngagementURI() {
            return StringUtils.strCat(API_URL, "engagement&action=");
        }

        public static final String getLikeBlogURI(long bid, String token) {
            return StringUtils.strCat(new Object[]{getEngagementURI(), "like_blog&bid=", bid, "&token=", token});
        }

        public static final String getFavoriteBlogURI(long bid, String token) {
            return StringUtils.strCat(new Object[]{getEngagementURI(), "favorite_blog&bid=", bid, "&token=", token});
        }

        public static final String getLikeVideoURI(long vid, String token) {
            return StringUtils.strCat(new Object[]{getEngagementURI(), "like_video&vid=", vid, "&token=", token});
        }

        public static final String getFavouriteVideoURI(long vid, String token) {
            return StringUtils.strCat(new Object[]{getEngagementURI(), "favorite_video&vid=", vid, "&token=", token});
        }
    }

    //申诉管理类

    public static final class ManageURI {
        private static final String getManageURI() {
            return StringUtils.strCat(API_URL, "manage&action=");
        }

        public static final String getDeleteBlogURI(long bid, String token) {
            return StringUtils.strCat(new Object[]{getManageURI(), "delete_blog&bid=", bid, "&token=", token});
        }

        public static final String getAppealBlogURI(long bid, String token) {
            return StringUtils.strCat(new Object[]{getManageURI(), "appeal_blog&bid=", bid, "&token=", token});
        }

        public static final String getDeleteVideoURI(long vid, String token) {
            return StringUtils.strCat(new Object[]{getManageURI(), "delete_video&vid=", vid, "&token=", token});
        }

        public static final String getAppealVideoURI(long vid, String token) {
            return StringUtils.strCat(new Object[]{getManageURI(), "appeal_video&vid=", vid, "&token=", token});
        }
    }

    //审核操作类

    public static final class ModerationURI {
        private static final String getModerationURI() {
            return StringUtils.strCat(API_URL, "moderation&action=");
        }

        public static final String getReportBlogURI(long bid, String token) {
            return StringUtils.strCat(new Object[]{getModerationURI(), "report_blog&bid=", bid, "&token=", token});
        }

        public static final String getApproveBlogURI(long bid, String token) {
            return StringUtils.strCat(new Object[]{getModerationURI(), "approve_blog&bid=", bid, "&token=", token});
        }

        public static final String getReportVideoURI(long vid, String token) {
            return StringUtils.strCat(new Object[]{getModerationURI(), "report_video&vid=", vid, "&token=", token});
        }

        public static final String getApproveVideoURI(long vid, String token) {
            return StringUtils.strCat(new Object[]{getModerationURI(), "approve_video&vid=", vid, "&token=", token});
        }

        public static final String getRejectBlogURI(long bid, String token) {
            return StringUtils.strCat(new Object[]{getModerationURI(), "reject_blog&bid=", bid, "&token=", token});
        }

        public static final String getRejectVideoURI(long vid, String token) {
            return StringUtils.strCat(new Object[]{getModerationURI(), "reject_video&vid=", vid, "&token=", token});
        }
    }

    //评论管理类

    public static final class CommentURI {
        private static final String getCommentURI() {
            return StringUtils.strCat(API_URL, "comment&action=");
        }

        public static final String getBlogCommentURI(long bid, long parent_bcid, String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getCommentURI(), "blog_comment_list&bid=", bid, "&parent_bcid=", parent_bcid, "&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getBlogCommentURI(long bid, long parent_bcid, int offset, int num) {
            return StringUtils.strCat(new Object[]{getCommentURI(), "blog_comment_list&bid=", bid, "&parent_bcid=", parent_bcid, "&offset=", offset, "&num=", num});
        }

        public static final String getVideoCommentURI(long vid, long parent_vcid, String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getCommentURI(), "video_comment_list&vid=", vid, "&parent_vcid=", parent_vcid, "&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getVideoCommentURI(long vid, long parent_vcid, int offset, int num) {
            return StringUtils.strCat(new Object[]{getCommentURI(), "video_comment_list&vid=", vid, "&parent_vcid=", parent_vcid, "&offset=", offset, "&num=", num});
        }

        public static final String getCommentBlogURI(long bid, long parent_bcid, String token, String content) {
            return StringUtils.strCat(new Object[]{getCommentURI(), "comment_blog&bid=", bid, "&parent_bcid=", parent_bcid, "&token=", token, "&content=", content});
        }

        public static final String getCommentVideoURI(long vid, long parent_vcid, String token, String content) {
            return StringUtils.strCat(new Object[]{getCommentURI(), "comment_video&vid=", vid, "&parent_vcid=", parent_vcid, "&token=", token, "&content=", content});
        }

        public static final String getDeleteBlogURI(long bcid, String token) {
            return StringUtils.strCat(new Object[]{getCommentURI(), "delete_blog_comment&bcid=", bcid, "&token=", token});
        }

        public static final String getDeleteVideoURI(long vcid, String token) {
            return StringUtils.strCat(new Object[]{getCommentURI(), "delete_video_comment&vcid=", vcid, "&token=", token});
        }

        public static final String getReportBlogURI(long bcid, String token) {
            return StringUtils.strCat(new Object[]{getCommentURI(), "report_blog_comment&bcid=", bcid, "&token=", token});
        }

        public static final String getReportVideoURI(long vcid, String token) {
            return StringUtils.strCat(new Object[]{getCommentURI(), "report_video_comment&bcid=", vcid, "&token=", token});
        }
    }

    //个人操作类

    public static final class ProfileURI {
        private static final String getProfileURI() {
            return StringUtils.strCat(API_URL, "profile&action=");
        }

        public static final String getFavoriteBlogsURI(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "favorite_blog_list&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getFavoriteVideosURI(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "favorite_video_list&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getNetworkHistoryURI(String token) {
            return StringUtils.strCat(new String[]{getProfileURI(), "history_video_list&token=", token});
        }

        public static final String getUserProfileURI(String token) {
            return StringUtils.strCat(new String[]{getProfileURI(), "user_profile&token=", token});
        }

        public static final String getNameEditURI(String token, String username) {
            return StringUtils.strCat(new String[]{getProfileURI(), "update_username&token=", token, "&username=", username});
        }

        public static final String getPasswordEditURI(String token, String pw) {
            return StringUtils.strCat(new String[]{getProfileURI(), "update_pw&token=", token, "&pw=", pw});
        }

        public static final String getPhoneEditURI(String token, String phone) {
            return StringUtils.strCat(new String[]{getProfileURI(), "update_phone&token=", token, "&phone=", phone});
        }

        public static final String getQQEditURI(String token, long qq) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "update_qq&token=", token, "&qq=", qq});
        }

        public static final String getSexEditURI(String token, String sex) {
            return StringUtils.strCat(new String[]{getProfileURI(), "update_sex&token=", token, "&sex=", sex});
        }

        public static final String getIntroEditURI(String token, String intro) {
            return StringUtils.strCat(new String[]{getProfileURI(), "update_intro&token=", token, "&intro=", intro});
        }

        public static final String getApproveAvatarURI(String token, long uid_of_avatar) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "approve_avatar&token=", token, "&uid_of_avatar=", uid_of_avatar});
        }

        public static final String getRejectAvatarURI(String token, long uid_of_avatar) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "reject_avatar&token=", token, "&uid_of_avatar=", uid_of_avatar});
        }

        public static final String getApproveCoverURI(String token, long uid_of_cover) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "approve_cover&token=", token, "&uid_of_cover=", uid_of_cover});
        }

        public static final String getRejectCoverURI(String token, long uid_of_cover) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "reject_cover&token=", token, "&uid_of_cover=", uid_of_cover});
        }

        public static final String getUserDataURI(String token) {
            return StringUtils.strCat(new String[]{getProfileURI(), "user_data&token=", token});
        }

        public static final String getBlogsManageURI(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "manage_blog_list&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getVideosManageURI(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "manage_video_list&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getAuditAvatarURI(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "update_username&token=", token, "&offset=", offset, "&num=", num});
        }

        public static final String getAuditCoverURI(String token, int offset, int num) {
            return StringUtils.strCat(new Object[]{getProfileURI(), "update_username&token=", token, "&offset=", offset, "&num=", num});
        }
    }

    //发布类

    public static final class CreatorURI {
        public static final String getSubmitBlogURI(String token, String title, String content) {
            return StringUtils.strCat(new String[]{"action=submit_blog&token=", token, "&title=",title, "&content=", content});
        }

        public static final String getSubmitVideoURI(String token, String title, String intro, int type, int category, String tag) {
            return StringUtils.strCat(new Object[]{"action=submit_video&token=", token, "&title=", title, "&intro=", intro, "&type=", type, "&category=", category, "&tag=", tag});
        }
        
        public static final String getUpdateAvatarURI(String token){
            return StringUtils.strCat("action=update_avatar&token=", token);
        }
        
        public static final String getUpdateCoverURI(String token){
            return StringUtils.strCat("action=update_cover&token=", token);
        }
        
        public static final String getSaveBlogURI(String token, String content){
            return StringUtils.strCat(new String[]{"action=save_blog&token=", token, "&content=", content});
        }
        public static final String getLoadBlog(String token){
            return StringUtils.strCat(new String[]{"action=load_blog&token=", token});
        }
    }

    //系统类

    public static final class SystemURI {
        private static final String getSystemURI() {
            return StringUtils.strCat(API_URL, "system&action=");
        }

        public static final String getSlideURI() {
            return StringUtils.strCat(getSystemURI(), "slideshow");
        }

        public static final String getVersionURI() {
            return StringUtils.strCat(getSystemURI(), "version");
        }
    }

    //弹幕类

    public static final class DanmakuURI {
        private static final String getDanmakuURI() {
            return StringUtils.strCat(API_URL, "danmaku&action=");
        }

        public static final String getSendURI(long vid, String token, String text, double time, String mode, String color, String size, String render) {
            return StringUtils.strCat(new Object[]{getDanmakuURI(), "send_danmaku&vid=", vid, "&token=", token, "&text=", text, "&time=", time, "&mode=", mode, "&color=", color, "&font_size=", size, "&render=", render});
        }

        public static final String getListURI(long vid) {
            return StringUtils.strCat(new Object[]{getDanmakuURI(), "get_danmaku&vid=", vid});
        }
    }
}

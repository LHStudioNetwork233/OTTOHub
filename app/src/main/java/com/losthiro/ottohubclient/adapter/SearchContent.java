package com.losthiro.ottohubclient.adapter;

/**
 * @Author Hiro
 * @Date 2025/05/26 18:29
 */
public class SearchContent {
    public static final String TAG = "SearchContent";
    public static final int TYPE_DEF=0;
    public static final int TYPE_USER=1;
    public static final int TYPE_BLOG=2;
    public static final int TYPE_VIDEO=3;
    private int type;
    private Video video;
    private Blog blog;
    private User user;
    private String title;

    public SearchContent(String t) {
        title = t;
        type = TYPE_DEF;
    }

    public SearchContent(Video v) {
        video = v;
        type = TYPE_VIDEO;
    }

    public SearchContent(Blog b) {
        blog = b;
        type = TYPE_BLOG;
    }

    public SearchContent(User u) {
        user = u;
        type = TYPE_USER;
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public Video getVideo() {
        return video;
    }

    public Blog getBlog() {
        return blog;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof SearchContent) {
            SearchContent another=(SearchContent)obj;
            if (another.getType() != getType()) {
                return false;
            }
            switch (another.getType()) {
                case TYPE_USER:
                    return another.getUser().getUID() == getUser().getUID();
                case TYPE_BLOG:
                    return another.getBlog().getBID() == getBlog().getBID();
                case TYPE_VIDEO:
                    return another.getVideo().getVID() == getVideo().getVID();
            }
        }
        return false;
    }
}

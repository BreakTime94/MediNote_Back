package com.medinote.medinote_back_kys.common.paging;

import java.util.Map;

public final class NewsSortWhitelist {

    private NewsSortWhitelist() {}

    /** 사용자 목록용: 공개된 기사 화면에서 허용할 정렬 키 */
    public static final Map<String, String> PUBLIC = Map.of(
            "pubDate", "pubDate",
            "regDate", "regDate",
            "title",   "title",
            "source",  "sourceName",
            "id",      "id"
    );

    /** 관리자 목록용: 관리 화면에서 허용할 정렬 키 */
    public static final Map<String, String> ADMIN = Map.of(
            "pubDate",   "pubDate",
            "regDate",   "regDate",
            "title",     "title",
            "source",    "sourceName",
            "published", "isPublished",
            "type",      "contentType",
            "id",        "id"
    );
}

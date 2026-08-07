package com.chatBox.realtalk.base.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtils {

    private PageUtils() {
    }

    public static Pageable of(int page, int size) {
        int p = Math.max(page, 0);
        int s = size > 0 ? Math.min(size, 100) : 20;
        return PageRequest.of(p, s, Sort.by("createdAt").descending());
    }
}

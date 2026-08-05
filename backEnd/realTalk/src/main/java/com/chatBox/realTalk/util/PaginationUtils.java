package com.chatBox.realTalk.util;

import com.chatBox.realTalk.constant.ApiConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PaginationUtils {

    private PaginationUtils() {
    }

    public static Pageable createPageable(
            Integer page,
            Integer size
    ) {
        int normalizedPage =
                page == null || page < 0
                        ? ApiConstants.DEFAULT_PAGE
                        : page;

        int normalizedSize =
                size == null || size <= 0
                        ? ApiConstants.DEFAULT_PAGE_SIZE
                        : Math.min(
                        size,
                        ApiConstants.MAX_PAGE_SIZE
                );

        return PageRequest.of(
                normalizedPage,
                normalizedSize
        );
    }
}

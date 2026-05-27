package aihub.backend.common.api;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> data,
        PageMeta meta
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), PageMeta.from(page));
    }
}

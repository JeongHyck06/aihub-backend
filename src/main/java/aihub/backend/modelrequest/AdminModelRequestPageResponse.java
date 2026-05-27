package aihub.backend.modelrequest;

import aihub.backend.common.api.PageMeta;

import java.util.List;

public record AdminModelRequestPageResponse(
        AdminModelRequestData data,
        PageMeta meta
) {
    public record AdminModelRequestData(
            ModelRequestStats stats,
            List<ModelRequestResponse> items
    ) {
    }
}

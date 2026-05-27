package aihub.backend.compare;

import java.util.List;

public record CompareResponse(
        List<CompareServiceResponse> services,
        List<CompareRowDescriptor> rows
) {
}

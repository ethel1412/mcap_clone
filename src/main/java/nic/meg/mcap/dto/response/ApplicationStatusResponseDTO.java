package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nic.meg.mcap.entities.Application;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusResponseDTO {

    private boolean personalDetailsComplete;
    private boolean academicDetailsComplete;
    private boolean programmeSelectionComplete;
    private boolean documentsUploadComplete;
    private boolean paymentComplete;
    private boolean isFormLocked;

    public static ApplicationStatusResponseDTO fromEntity(Application app) {
        if (app == null) {
            return ApplicationStatusResponseDTO.builder().build();
        }
        return ApplicationStatusResponseDTO.builder()
                .personalDetailsComplete(app.isPersonalDetailsComplete())
                .academicDetailsComplete(app.isAcademicDetailsComplete())
                .programmeSelectionComplete(app.isProgrammeSelectionComplete())
                .documentsUploadComplete(app.isDocumentsFinalized())
                .paymentComplete(app.isPaymentComplete())
                .isFormLocked(app.isPaymentComplete())
                .build();
    }
}
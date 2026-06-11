package nic.meg.mcap.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequestDTO {
    private String menuName;
    private String iconClass;
    private Integer orderIndex;
    private boolean active;
    private Short parentMenuId; // short to match repository
}

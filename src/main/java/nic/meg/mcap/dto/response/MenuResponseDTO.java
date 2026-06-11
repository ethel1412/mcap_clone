package nic.meg.mcap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponseDTO {

    // --- Common fields for both navigation & admin views ---
    private Short menuId;           // For admin operations
    private String name;            // Menu display name
    private String iconClass;       // Icon CSS
    private Integer orderIndex;     // Order for sorting
    private String url;             // Link URL

    // For hierarchical tree structures (navigation)
    private List<MenuResponseDTO> children;

    // Roles assigned to this menu
    private List<RoleResponseDTO> assignedRoles;

    // --- Convenience Constructors ---

    // For navigation menu tree (no ID/order, but with children)
    public MenuResponseDTO(String name, String iconClass, String url, List<MenuResponseDTO> children) {
        this.name = name;
        this.iconClass = iconClass;
        this.url = url;
        this.children = children;
    }

    // For admin flat listing (with ID/order)
    public MenuResponseDTO(Short menuId, String name, String iconClass, Integer orderIndex) {
        this.menuId = menuId;
        this.name = name;
        this.iconClass = iconClass;
        this.orderIndex = orderIndex;
    }

    // For admin flat listing with roles
    public MenuResponseDTO(Short menuId, String name, String iconClass, Integer orderIndex, List<RoleResponseDTO> assignedRoles) {
        this.menuId = menuId;
        this.name = name;
        this.iconClass = iconClass;
        this.orderIndex = orderIndex;
        this.assignedRoles = assignedRoles;
    }
}

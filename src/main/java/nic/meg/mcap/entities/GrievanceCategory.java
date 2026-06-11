package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "grievance_category", schema = "mcap")
@Getter
@Setter
public class GrievanceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Secure code exposed to the frontend (e.g., "ACC_LOGIN", "TECH_PROB")
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "concerned_role_id", nullable = false)
    private String concernedRoleId;

    @Column(name = "requires_institute", nullable = false)
    private boolean requiresInstitute = false;
}
package nic.meg.mcap.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class ManagementType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Integer managementTypeId;

    @Column(nullable = false, unique = true)
    private String managementTypeName;

}

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

public class AffiliationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer affiliationTypeId;

    @Column(nullable = false,length=30)
    private String affiliationTypeName;

}

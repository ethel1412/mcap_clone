package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One line-item (particular + amount) inside an InstituteSeatFeeStructure.
 */
@Entity
@Table(name = "institute_seat_fee_particular")
@Getter
@Setter
@NoArgsConstructor
public class InstituteSeatFeeParticular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long particularId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private InstituteSeatFeeStructure feeStructure;

    @Column(nullable = false, length = 200)
    private String particularName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Display order for rendering rows in the table */
    @Column(nullable = false)
    private int displayOrder = 0;
}

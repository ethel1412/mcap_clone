package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Links an InstituteSeatFeeStructure to a specific programme OR an entire stream.
 * Exactly one of programmeOffered / stream must be non-null.
 * If streamId is set and programmeOfferedId is null => fee applies to ALL programmes in that stream.
 * If programmeOfferedId is set => fee applies to that specific programme only.
 */
@Entity
@Table(
        name = "institute_seat_fee_scope",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"fee_structure_id", "programme_offered_id"}),
                @UniqueConstraint(columnNames = {"fee_structure_id", "stream_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class InstituteSeatFeeScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scopeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private InstituteSeatFeeStructure feeStructure;

    /** Set when the fee applies to one specific programme. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_offered_id")
    private ProgrammeOffered programmeOffered;

    /** Set when the fee applies to all programmes under a stream. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id")
    private Stream stream;
}

package nic.meg.mcap.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class InstituteSeatFeeStructureRequestDTO {

    /** Optional: present when editing an existing structure */
    private Long feeStructureId;

    @NotBlank(message = "Fee name is required")
    private String feeName;

    @NotEmpty(message = "At least one particular is required")
    @Valid
    private List<SeatFeeParticularDTO> particulars;

    /**
     * Scope: list of programme_offered IDs to attach this fee to.
     * Mutually exclusive with streamIds — client sends one or the other.
     */
    private List<Integer> programmeOfferedIds;

    /**
     * Scope: list of stream IDs. If set, fee applies to every programme in those streams.
     */
    private List<Short> streamIds;
}

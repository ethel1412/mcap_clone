package nic.meg.mcap.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import nic.meg.mcap.entities.AdmissionWindow;
import nic.meg.mcap.entities.AdmissionWindowProgramme;

public interface AdmissionWindowProgrammeRepository extends JpaRepository<AdmissionWindowProgramme, Short> {

	@Modifying
	@Transactional
	void deleteByAdmissionWindow(AdmissionWindow admissionWindow);

	boolean existsByAdmissionWindowAdmissionId(Short admissionId);

	List<AdmissionWindowProgramme> findByAdmissionWindowAdmissionId(Short admissionId);

	List<AdmissionWindowProgramme> findByAdmissionWindowAndIsActiveTrue(AdmissionWindow window);
}
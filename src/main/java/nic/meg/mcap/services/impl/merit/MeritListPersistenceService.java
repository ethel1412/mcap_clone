package nic.meg.mcap.services.impl.merit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nic.meg.mcap.entities.MeritList;
import nic.meg.mcap.entities.MeritListEntry;
import nic.meg.mcap.repositories.MeritListEntryRepository;
import nic.meg.mcap.repositories.MeritListRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MeritListPersistenceService {

    private final MeritListRepository meritListRepository;
    private final MeritListEntryRepository entryRepository;

    public void hardResetByProgrammeRoundPhase(Short admissionWindowId, Short programmeId, String roundType, Integer phaseNo) {
        List<MeritList> existingLists =
                meritListRepository.findAllByAdmissionWindow_AdmissionIdAndProgramme_ProgrammeId(admissionWindowId, programmeId);

        for (MeritList ml : existingLists) {
            if (roundType.equalsIgnoreCase(ml.getRoundType()) && phaseNo != null && phaseNo.equals(ml.getPhaseNo())) {
                entryRepository.deleteByMeritList(ml);
                meritListRepository.delete(ml);
            }
        }
    }

    public MeritList saveMeritList(MeritList meritList) {
        return meritListRepository.save(meritList);
    }

    public void saveEntries(List<MeritListEntry> entries) {
        entryRepository.saveAll(entries);
    }

    public List<MeritListEntry> getEntriesByMeritListId(Long meritListId) {
        return entryRepository.findByMeritListOrderByRank(meritListId);
    }
}

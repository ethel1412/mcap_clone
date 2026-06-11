package nic.meg.mcap.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nic.meg.mcap.entities.SubjectCombinationItem;

public interface SubjectCombinationItemRepository extends JpaRepository<SubjectCombinationItem, Long> {

	List<SubjectCombinationItem> findByCombinationId(Long subjectCombinationHeaderId);

	List<SubjectCombinationItem> findByCombinationIdIn(Collection<Long> subjectCombinationHeaderIds);

	@Query("""
			    select i
			    from SubjectCombinationItem i
			    where i.combination.id in :ids
			    order by i.combination.id asc, i.id asc
			""")
	List<SubjectCombinationItem> findAllByHeaderIds(@Param("ids") Collection<Long> ids);
}

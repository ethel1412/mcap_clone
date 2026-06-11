package nic.meg.mcap.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import nic.meg.mcap.entities.Menu;

public interface MenuRepository extends JpaRepository<Menu, Short> {

    // Used for navigation menu generation for a role
    @Query("""
           SELECT DISTINCT m FROM Role r 
           JOIN r.menus m
           LEFT JOIN FETCH m.pageUrls              
           WHERE r.roleName = :roleName
           AND m.isActive = true
           ORDER BY m.orderIndex ASC
           """)
    List<Menu> findMenusByRoleName(@Param("roleName") String roleName);

    // Optional: You may want a method to get only active menus
    List<Menu> findByIsActiveTrueOrderByOrderIndexAsc();
}
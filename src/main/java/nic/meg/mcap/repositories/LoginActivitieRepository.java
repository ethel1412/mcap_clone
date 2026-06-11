package nic.meg.mcap.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nic.meg.mcap.entities.LoginActivity;

@Repository
public interface LoginActivitieRepository extends JpaRepository<LoginActivity, Long> {

    List<LoginActivity> findByUser_UserIdOrderByTimeDesc(Integer userId);

}

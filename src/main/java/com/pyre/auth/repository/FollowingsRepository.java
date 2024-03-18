package com.pyre.auth.repository;

import com.pyre.auth.entity.Following;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FollowingsRepository extends JpaRepository<Following, UUID> {
}

package com.pyre.auth.repository;

import com.pyre.auth.entity.OauthMember;
import com.pyre.auth.oauth2.OauthId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OauthMemberRepository extends JpaRepository<OauthMember, UUID> {

    Optional<OauthMember> findByOauthId(OauthId oauthId);
}


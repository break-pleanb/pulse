package com.den.pulse.domain.channel.repository;

import com.den.pulse.domain.channel.entity.Channel;
import com.den.pulse.domain.channel.entity.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {

    @Query("""
            select c from Channel c
            where c.project.id = :projectId
              and exists (select 1 from ChannelMember cm where cm.channel = c and cm.user.id = :userId)
            """)
    List<Channel> findByProject_IdAndMemberUserId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    /** 프로젝트 안에서 요청자·상대방 두 사람으로 이루어진 기존 DM 채널을 찾는다 (있으면 재사용, API-SPEC.md 6장). */
    @Query("""
            select c from Channel c
            where c.project.id = :projectId and c.type = :type
              and exists (select 1 from ChannelMember cm where cm.channel = c and cm.user.id = :userId)
              and exists (select 1 from ChannelMember cm where cm.channel = c and cm.user.id = :targetUserId)
            """)
    Optional<Channel> findDmChannel(@Param("projectId") UUID projectId, @Param("type") ChannelType type,
                                     @Param("userId") UUID userId, @Param("targetUserId") UUID targetUserId);

    List<Channel> findByProject_IdAndType(UUID projectId, ChannelType type);
}

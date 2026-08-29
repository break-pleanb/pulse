package com.den.pulse.domain.channel.repository;

import com.den.pulse.domain.channel.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, UUID> {

    boolean existsByChannel_IdAndUser_Id(UUID channelId, UUID userId);

    Optional<ChannelMember> findByChannel_IdAndUser_Id(UUID channelId, UUID userId);

    @Query("select cm.user.id from ChannelMember cm where cm.channel.id = :channelId")
    List<UUID> findUserIdsByChannel_Id(@Param("channelId") UUID channelId);

    @Query("select cm.channel.id as channelId, cm.user.id as userId from ChannelMember cm where cm.channel.id in :channelIds")
    List<ChannelMemberIdsView> findByChannelIdIn(@Param("channelIds") Collection<UUID> channelIds);
}

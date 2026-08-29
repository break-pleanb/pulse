package com.den.pulse.domain.channel.repository;

import com.den.pulse.domain.channel.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByChannel_IdOrderByCreatedAtAsc(UUID channelId);

    long countByChannel_Id(UUID channelId);

    long countByChannel_IdAndCreatedAtAfter(UUID channelId, LocalDateTime after);
}

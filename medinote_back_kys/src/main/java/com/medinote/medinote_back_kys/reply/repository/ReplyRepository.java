package com.medinote.medinote_back_kys.reply.repository;

import com.medinote.medinote_back_kys.reply.domain.en.ReplyTargetType;
import com.medinote.medinote_back_kys.reply.domain.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByLinkTypeAndLinkIdOrderByIdAsc(ReplyTargetType linkType, Long linkId);

    Page<Reply> findByLinkTypeAndLinkId(ReplyTargetType linkType, Long linkId, Pageable pageable);
}

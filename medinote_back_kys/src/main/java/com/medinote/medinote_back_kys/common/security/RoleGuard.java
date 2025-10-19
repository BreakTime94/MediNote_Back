package com.medinote.medinote_back_kys.common.security;

import com.medinote.medinote_back_kys.common.client.MemberClient;
import com.medinote.medinote_back_kys.common.dto.member.MemberInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RoleGuard {

    private final MemberClient memberClient;
    private static final String ADMIN = "ADMIN";

    /** 기존: 관리자만 허용 */
    public void requireAdmin(Long memberId) {
        if (!isAdmin(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 수행할 수 있습니다.");
        }
    }

    /** ✅ 추가 1: 본인만 허용 */
    public void requireSelf(Long requesterMemberId, Long ownerMemberId) {
        if (!Objects.equals(requesterMemberId, ownerMemberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인만 수행할 수 있습니다.");
        }
    }

    /** ✅ 추가 2: 본인 또는 관리자만 허용 */
    public void requireSelfOrAdmin(Long requesterMemberId, Long ownerMemberId) {
        if (Objects.equals(requesterMemberId, ownerMemberId)) return; // 본인 OK
        if (isAdmin(requesterMemberId)) return;                       // 관리자 OK
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 또는 관리자만 수행할 수 있습니다.");
    }

    /** 헬퍼: 관리자 여부 */
    public boolean isAdmin(Long memberId) {
        String role = memberClient.getById(memberId)
                .map(MemberInfoDTO::getRole)
                .orElse("UNKNOWN");
        return ADMIN.equalsIgnoreCase(role);
    }
}

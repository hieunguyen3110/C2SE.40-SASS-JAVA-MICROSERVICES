package org.com.studygroupservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.com.studygroupservice.repository.GroupMemberRepository;
import org.com.studygroupservice.service.GroupMemberService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupMemberServiceImpl implements GroupMemberService {
    private final GroupMemberRepository groupMemberRepository;
    @Override
    public Boolean checkAccountParticipateGroup(Long accountId) throws Exception {
        try{
            Long existAccountInGroup= groupMemberRepository.countByAccountId(accountId);
            return existAccountInGroup > 0;
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }
    }
}

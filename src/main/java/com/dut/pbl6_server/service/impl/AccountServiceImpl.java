package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.Follower;
import com.dut.pbl6_server.entity.enums.AccountRole;
import com.dut.pbl6_server.mapper.AccountMapper;
import com.dut.pbl6_server.repository.fetch_data.AccountsFetchRepository;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.FollowersRepository;
import com.dut.pbl6_server.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("AccountService")
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountsFetchRepository accountsFetchRepository;
    private final AccountsRepository accountsRepository;
    private final FollowersRepository followersRepository;
    private final AccountMapper accountMapper;

    @Override
    public AccountResponse getAccountInfo(Account currentUser) {
        Optional<Account> accountOptional = accountsFetchRepository.findById(currentUser.getId());

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            return accountMapper.toResponse(account);
        } else {
            throw new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND);
        }
    }

    @Override
    public AccountResponse getAccountInfoById(Account currentUser, Long userId) {
        var account = accountsRepository.findById(userId).orElseThrow(() -> new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));

        // Check account's visibility
        return switch (account.getVisibility()) {
            case PUBLIC -> accountMapper.toUserResponse(account);
            case PRIVATE -> throw new BadRequestException(ErrorMessageConstants.ACCOUNT_IS_NOT_AVAILABLE);
            case FRIEND_ONLY -> {
                if (followersRepository.isFollowing(userId, currentUser.getId()))
                    yield accountMapper.toUserResponse(account);
                else
                    throw new BadRequestException(ErrorMessageConstants.ACCOUNT_IS_NOT_AVAILABLE);
            }
        };
    }

    @Override
    public DataWithPage<AccountResponse> searchUser(Account currentUser, String displayName, Pageable pageable) {
        var page = accountsRepository.searchByDisplayName(displayName, pageable);
        return DataWithPage.<AccountResponse>builder()
            .data(page.getContent().stream().map(accountMapper::toUserResponse).toList())
            .pageInfo(PageUtils.makePageInfo(page))
            .build();
    }

    @Override
    public void followUser(Account currentUser, Long userId) {
        var user = accountsRepository.findById(userId).orElseThrow(() -> new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));
        // Check if user is admin
        if (user.getRole() == AccountRole.ADMIN)
            throw new BadRequestException(ErrorMessageConstants.CANNOT_FOLLOW_ADMIN);
        
        // Check if user who is followed is current user
        if (currentUser.getId().equals(userId))
            throw new BadRequestException(ErrorMessageConstants.CANNOT_FOLLOW_YOURSELF);

        // Check if user is already followed
        if (followersRepository.isFollowing(userId, currentUser.getId()))
            throw new BadRequestException(ErrorMessageConstants.ALREADY_FOLLOWED);

        // Follow user (save to database)
        followersRepository.save(new Follower(currentUser, user));

        // TODO: send notification (via stomp websocket)
    }
}

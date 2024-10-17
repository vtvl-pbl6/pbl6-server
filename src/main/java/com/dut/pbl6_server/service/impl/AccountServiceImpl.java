package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.common.util.CommonUtils;
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
import com.dut.pbl6_server.service.NotificationService;
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
    private final NotificationService notificationService;

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
        var account = accountsFetchRepository.findById(userId).orElseThrow(() -> new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));
        Boolean isFollowingByCurrentUser = userId.equals(currentUser.getId()) ? null : followersRepository.isFollowing(userId, currentUser.getId());

        // Check account's visibility
        return switch (account.getVisibility()) {
            case PUBLIC -> accountMapper.toUserInfoResponse(account, isFollowingByCurrentUser);
            case PRIVATE -> throw new BadRequestException(ErrorMessageConstants.ACCOUNT_IS_NOT_AVAILABLE);
            case FRIEND_ONLY -> {
                if (isFollowingByCurrentUser != null && isFollowingByCurrentUser)
                    yield accountMapper.toUserInfoResponse(account, true);
                else
                    throw new BadRequestException(ErrorMessageConstants.ACCOUNT_IS_NOT_AVAILABLE);
            }
        };
    }

    @Override
    public DataWithPage<AccountResponse> searchUser(Account currentUser, String displayName, Pageable pageable) {
        var page = accountsFetchRepository.searchByDisplayName(displayName, pageable);
        return DataWithPage.<AccountResponse>builder()
            .data(page.getContent().stream().map(e ->
                accountMapper.toUserInfoResponse(e, followersRepository.isFollowing(e.getId(), currentUser.getId()))).toList()
            )
            .pageInfo(PageUtils.makePageInfo(page))
            .build();
    }

    @Override
    public DataWithPage<AccountResponse> getFollowers(Account currentUser, Long userId, Pageable pageable) {
        var page = followersRepository.findAllByUserId(userId, pageable);
        return DataWithPage.<AccountResponse>builder()
            .data(page.getContent().stream().map(e -> accountMapper.toUserInfoResponse(e.getFollower(), null)).toList())
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

        // Save follower to database if not exists else set deleted_at to null
        var follower = followersRepository.findByFollowerIdAndUserId(currentUser.getId(), userId).orElse(null);
        if (follower == null)
            follower = new Follower(currentUser, user);
        else if (follower.getDeletedAt() != null)
            follower.setDeletedAt(null);
        else
            throw new BadRequestException(ErrorMessageConstants.ALREADY_FOLLOWED);
        followersRepository.save(follower);

        // send notification (via stomp websocket)
        notificationService.sendNotification(currentUser, user, NotificationType.FOLLOW, null);
    }

    @Override
    public void unfollowUser(Account currentUser, Long userId) {
        var user = accountsRepository.findById(userId).orElseThrow(() -> new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));
        // Check if user is admin
        if (user.getRole() == AccountRole.ADMIN)
            throw new BadRequestException(ErrorMessageConstants.CANNOT_UNFOLLOW_ADMIN);

        // Check if user who is followed is current user
        if (currentUser.getId().equals(userId))
            throw new BadRequestException(ErrorMessageConstants.CANNOT_UNFOLLOW_YOURSELF);

        // Set deleted_at to current time if follower exists else throw exception
        var follower = followersRepository.findByFollowerIdAndUserId(currentUser.getId(), userId).orElse(null);
        if (follower != null && follower.getDeletedAt() == null)
            follower.setDeletedAt(CommonUtils.DateTime.getCurrentTimestamp());
        else
            throw new BadRequestException(ErrorMessageConstants.ALREADY_UNFOLLOWED);
        followersRepository.save(follower);

        // send notification (via stomp websocket)
        notificationService.sendNotification(currentUser, user, NotificationType.UNFOLLOW, null);
    }
}

package com.dut.pbl6_server.service.impl;

import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.PageUtils;
import com.dut.pbl6_server.dto.request.UpdateProfileRequest;
import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.Follower;
import com.dut.pbl6_server.entity.enums.AccountRole;
import com.dut.pbl6_server.entity.enums.AccountStatus;
import com.dut.pbl6_server.mapper.AccountMapper;
import com.dut.pbl6_server.repository.fetch_data.AccountsFetchRepository;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.FilesRepository;
import com.dut.pbl6_server.repository.jpa.FollowersRepository;
import com.dut.pbl6_server.service.AccountService;
import com.dut.pbl6_server.service.CloudinaryService;
import com.dut.pbl6_server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service("AccountService")
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountsFetchRepository accountsFetchRepository;
    private final AccountsRepository accountsRepository;
    private final FollowersRepository followersRepository;
    private final AccountMapper accountMapper;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService;
    private final FilesRepository filesRepository;

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
        var page = followersRepository.findFollowerIds(userId, pageable);
        var accounts = accountsFetchRepository.getAccountsByIds(page.getContent());
        return DataWithPage.<AccountResponse>builder()
            .data(accounts.stream().map(
                e -> accountMapper.toUserInfoResponse(e, e.getFollowers().stream().anyMatch(f -> f.getFollower().getId().equals(currentUser.getId())))
            ).toList())
            .pageInfo(PageUtils.makePageInfo(page))
            .build();
    }

    @Override
    public DataWithPage<AccountResponse> getFollowingUsers(Account currentUser, Long userId, Pageable pageable) {
        var page = followersRepository.findFollowingUserIds(userId, pageable);
        var accounts = accountsFetchRepository.getAccountsByIds(page.getContent());
        return DataWithPage.<AccountResponse>builder()
            .data(accounts.stream().map(e -> accountMapper.toUserInfoResponse(e, true)).toList())
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
        notificationService.sendNotification(currentUser, user, NotificationType.FOLLOW, null, false, false);
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
        notificationService.sendNotification(currentUser, user, NotificationType.UNFOLLOW, null, false, false);
    }

    @Override
    public AccountResponse editProfile(Account currentUser, UpdateProfileRequest request) {
        var account = accountsFetchRepository.findById(currentUser.getId()).orElseThrow(() -> new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));

        // No field is updated
        if (request.hasAllNullFields())
            return accountMapper.toResponse(account);

        var result = accountsRepository.save(accountMapper.toEntity(account, request));
        result.setFollowers(account.getFollowers());
        result.setFollowingUsers(account.getFollowingUsers());
        return accountMapper.toResponse(result);
    }

    @Override
    public AccountResponse uploadAvatar(Account currentUser, MultipartFile avatar) {
        var account = accountsFetchRepository.findById(currentUser.getId()).orElseThrow(() -> new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));
        if (avatar == null)
            throw new BadRequestException(ErrorMessageConstants.AVATAR_IS_REQUIRED);
        var oldFile = account.getAvatarFile();
        // Upload new avatar file
        var file = cloudinaryService.uploadFile(avatar);
        account.setAvatarFile(file);
        var result = accountsRepository.save(account);
        result.setFollowers(account.getFollowers());
        result.setFollowingUsers(account.getFollowingUsers());
        // Delete old avatar file if exists
        if (oldFile != null) cloudinaryService.deleteFiles(List.of(oldFile.getId()));
        return accountMapper.toResponse(result);
    }

    @Override
    public List<AccountResponse> getAccounts() {
        return accountsFetchRepository.getUserAccounts().stream().map(accountMapper::toResponse).toList();
    }

    @Override
    public AccountResponse getAccountInfoByAdmin(Long userId) {
        return accountMapper.toResponse(
            accountsFetchRepository.getByIdAlthoughDeleted(userId)
                .orElseThrow(() -> new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND))
        );
    }

    @Override
    public AccountResponse deactivateAccount(Account admin, Long userId) {
        var account = accountsFetchRepository.getByIdAlthoughDeleted(userId)
            .orElseThrow(() -> new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));

        // Check if user is admin
        if (account.getRole() == AccountRole.ADMIN)
            throw new BadRequestException(ErrorMessageConstants.CANNOT_ACTION_ADMIN_ACCOUNT);

        // Check if user is already deactivated
        if (account.getStatus() == AccountStatus.INACTIVE)
            throw new BadRequestException(ErrorMessageConstants.ACCOUNT_IS_ALREADY_INACTIVE);
        account.setStatus(AccountStatus.INACTIVE);
        account.setDeletedAt(CommonUtils.DateTime.getCurrentTimestamp());
        notificationService.sendNotification(admin, account, NotificationType.DEACTIVATE_ACCOUNT, account, false, false);
        return accountMapper.toResponse(accountsRepository.save(account));
    }

    @Override
    public AccountResponse activateAccount(Account admin, Long userId) {
        var account = accountsFetchRepository.getByIdAlthoughDeleted(userId)
            .orElseThrow(() -> new BadRequestException(ErrorMessageConstants.ACCOUNT_NOT_FOUND));

        // Check if user is admin
        if (account.getRole() == AccountRole.ADMIN)
            throw new BadRequestException(ErrorMessageConstants.CANNOT_ACTION_ADMIN_ACCOUNT);

        // Check if user is already deactivated
        if (account.getStatus() == AccountStatus.ACTIVE)
            throw new BadRequestException(ErrorMessageConstants.ACCOUNT_IS_ALREADY_ACTIVE);
        account.setStatus(AccountStatus.ACTIVE);
        account.setDeletedAt(null);
        notificationService.sendNotification(admin, account, NotificationType.ACTIVATE_ACCOUNT, account, false, false);
        return accountMapper.toResponse(accountsRepository.save(account));
    }
}

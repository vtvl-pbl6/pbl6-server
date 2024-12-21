package com.dut.pbl6_server.service;

import com.dut.pbl6_server.common.model.DataWithPage;
import com.dut.pbl6_server.dto.request.UpdateProfileRequest;
import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.entity.Account;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountService {
    AccountResponse getAccountInfo(Account currentUser);

    AccountResponse getAccountInfoById(Account currentUser, Long userId);

    DataWithPage<AccountResponse> searchUser(Account currentUser, String displayName, Pageable pageable);

    DataWithPage<AccountResponse> getFollowers(Account currentUser, Long userId, Pageable pageable);

    DataWithPage<AccountResponse> getFollowingUsers(Account currentUser, Long userId, Pageable pageable);

    void followUser(Account currentUser, Long userId);

    void unfollowUser(Account currentUser, Long userId);

    AccountResponse editProfile(Account currentUser, UpdateProfileRequest request);

    AccountResponse uploadAvatar(Account currentUser, MultipartFile avatar);

    List<AccountResponse> getAccounts();

    AccountResponse getAccountInfoByAdmin(Long userId);

    AccountResponse deactivateAccount(Account admin, Long userId);

    AccountResponse activateAccount(Account admin, Long userId);
}

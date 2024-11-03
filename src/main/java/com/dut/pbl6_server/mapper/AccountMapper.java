package com.dut.pbl6_server.mapper;

import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.config.SpringMapStructConfig;
import com.dut.pbl6_server.dto.request.UpdateProfileRequest;
import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.Follower;
import org.hibernate.LazyInitializationException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(config = SpringMapStructConfig.class)
public interface AccountMapper {
    String TO_RESPONSE_NAMED = "account_to_response";
    String TO_NOTIFICATION_USER_RESPONSE_NAMED = "account_to_notification_user_response";
    String TO_THREAD_SHARER_RESPONSE_NAMED = "account_to_thread_sharer_response";
    String TO_USER_RESPONSE_NAMED = "account_to_user_response";
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Named(TO_RESPONSE_NAMED)
    @Mapping(source = "followers", target = "followerNum", qualifiedByName = "getFollowers")
    @Mapping(source = "followingUsers", target = "followingUserNum", qualifiedByName = "getFollowingUsers")
    AccountResponse toResponse(Account account);

    @Named(TO_NOTIFICATION_USER_RESPONSE_NAMED)
    @Mapping(source = "status", target = "status", ignore = true)
    @Mapping(source = "role", target = "role", ignore = true)
    @Mapping(source = "birthday", target = "birthday", ignore = true)
    @Mapping(source = "gender", target = "gender", ignore = true)
    @Mapping(source = "bio", target = "bio", ignore = true)
    @Mapping(source = "visibility", target = "visibility", ignore = true)
    @Mapping(source = "language", target = "language", ignore = true)
    @Mapping(source = "createdAt", target = "createdAt", ignore = true)
    @Mapping(source = "updatedAt", target = "updatedAt", ignore = true)
    @Mapping(source = "deletedAt", target = "deletedAt", ignore = true)
    @Mapping(source = "followers", target = "followerNum", ignore = true)
    @Mapping(source = "followingUsers", target = "followingUserNum", ignore = true)
    AccountResponse toNotificationUserResponse(Account account); // Only include [id, email, firstName, lastName, displayName, avatar]

    @Named(TO_THREAD_SHARER_RESPONSE_NAMED)
    @Mapping(source = "email", target = "email", ignore = true)
    @Mapping(source = "firstName", target = "firstName", ignore = true)
    @Mapping(source = "lastName", target = "lastName", ignore = true)
    @Mapping(source = "status", target = "status", ignore = true)
    @Mapping(source = "role", target = "role", ignore = true)
    @Mapping(source = "birthday", target = "birthday", ignore = true)
    @Mapping(source = "gender", target = "gender", ignore = true)
    @Mapping(source = "bio", target = "bio", ignore = true)
    @Mapping(source = "visibility", target = "visibility", ignore = true)
    @Mapping(source = "language", target = "language", ignore = true)
    @Mapping(source = "createdAt", target = "createdAt", ignore = true)
    @Mapping(source = "updatedAt", target = "updatedAt", ignore = true)
    @Mapping(source = "deletedAt", target = "deletedAt", ignore = true)
    @Mapping(source = "followers", target = "followerNum", ignore = true)
    @Mapping(source = "followingUsers", target = "followingUserNum", ignore = true)
    AccountResponse toThreadSharerResponse(Account account); // Only include [id, displayName, avatar]

    @Named(TO_USER_RESPONSE_NAMED)
    @Mapping(source = "account.status", target = "status", ignore = true)
    @Mapping(source = "account.language", target = "language", ignore = true)
    @Mapping(source = "account.createdAt", target = "createdAt", ignore = true)
    @Mapping(source = "account.updatedAt", target = "updatedAt", ignore = true)
    @Mapping(source = "account.deletedAt", target = "deletedAt", ignore = true)
    @Mapping(source = "account.followingUsers", target = "followingUserNum", ignore = true)
    @Mapping(source = "account.followers", target = "followerNum", qualifiedByName = "getFollowers")
    @Mapping(source = "isFollowedByCurrentUser", target = "isFollowedByCurrentUser")
    AccountResponse toUserInfoResponse(Account account, Boolean isFollowedByCurrentUser);

    default Account toEntity(Account account, UpdateProfileRequest request) {
        if (account == null) return null;

        if (request.getFirstName() != null) {
            account.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            account.setLastName(request.getLastName());
        }
        if (request.getBirthday() != null) {
            account.setBirthday(request.getBirthday());
        }
        if (request.getGender() != null) {
            account.setGender(request.getGender());
        }
        if (request.getBio() != null) {
            account.setBio(request.getBio());
        }
        if (request.getVisibility() != null) {
            account.setVisibility(request.getVisibility());
        }
        if (request.getLanguage() != null) {
            account.setLanguage(request.getLanguage());
        }
        return account;
    }

    @Named("getFollowers")
    default Integer getFollowers(List<Follower> followers) {
        try {
            if (CommonUtils.List.isEmptyOrNull(followers)) return null;
            return followers.size();
        } catch (LazyInitializationException e) {
            return null;
        }
    }

    @Named("getFollowingUsers")
    default Integer getFollowingUsers(List<Follower> followingUsers) {
        try {
            if (CommonUtils.List.isEmptyOrNull(followingUsers)) return null;
            return followingUsers.size();
        } catch (LazyInitializationException e) {
            return null;
        }
    }
}

package com.dut.pbl6_server.mapper;

import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.config.SpringMapStructConfig;
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
    String TO_USER_RESPONSE_NAMED = "account_to_user_response";
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Named(TO_RESPONSE_NAMED)
    @Mapping(source = "followers", target = "followers", qualifiedByName = "getFollowers")
    @Mapping(source = "followingUsers", target = "followingUsers", qualifiedByName = "getFollowingUsers")
    AccountResponse toResponse(Account account);

    @Named(TO_NOTIFICATION_USER_RESPONSE_NAMED)
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
    @Mapping(source = "followers", target = "followers", ignore = true)
    @Mapping(source = "followingUsers", target = "followingUsers", ignore = true)
    AccountResponse toNotificationUserResponse(Account account);

    @Named(TO_USER_RESPONSE_NAMED)
    @Mapping(source = "status", target = "status", ignore = true)
    @Mapping(source = "visibility", target = "visibility", ignore = true)
    @Mapping(source = "language", target = "language", ignore = true)
    @Mapping(source = "createdAt", target = "createdAt", ignore = true)
    @Mapping(source = "updatedAt", target = "updatedAt", ignore = true)
    @Mapping(source = "deletedAt", target = "deletedAt", ignore = true)
    @Mapping(source = "followers", target = "followers", ignore = true)
    @Mapping(source = "followingUsers", target = "followingUsers", ignore = true)
    AccountResponse toUserResponse(Account account);

    @Named("getFollowers")
    default List<AccountResponse> getFollowers(List<Follower> followers) {
        try {
            if (CommonUtils.List.isEmptyOrNull(followers)) return null;
            return followers.stream().map(e -> this.toResponse(e.getFollower())).toList();
        } catch (LazyInitializationException e) {
            return null;
        }
    }

    @Named("getFollowingUsers")
    default List<AccountResponse> getFollowingUsers(List<Follower> followingUsers) {
        try {
            if (CommonUtils.List.isEmptyOrNull(followingUsers)) return null;
            return followingUsers.stream().map(e -> this.toResponse(e.getUser())).toList();
        } catch (LazyInitializationException e) {
            return null;
        }
    }
}

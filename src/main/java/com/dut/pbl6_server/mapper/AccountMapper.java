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
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    // TODO: add the toEntity method with account's data from client request as the parameter

    @Named(TO_RESPONSE_NAMED)
    @Mapping(source = "followers", target = "followers", qualifiedByName = "getFollowers")
    @Mapping(source = "followingUsers", target = "followingUsers", qualifiedByName = "getFollowingUsers")
    AccountResponse toResponse(Account account);

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

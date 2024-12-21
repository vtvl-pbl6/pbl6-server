package com.dut.pbl6_server.service;

import com.dut.pbl6_server.common.enums.NotificationType;
import com.dut.pbl6_server.common.exception.BadRequestException;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.dto.request.UpdateProfileRequest;
import com.dut.pbl6_server.dto.respone.AccountResponse;
import com.dut.pbl6_server.entity.Account;
import com.dut.pbl6_server.entity.File;
import com.dut.pbl6_server.entity.Follower;
import com.dut.pbl6_server.entity.enums.AccountStatus;
import com.dut.pbl6_server.entity.enums.Visibility;
import com.dut.pbl6_server.mapper.AccountMapper;
import com.dut.pbl6_server.repository.fetch_data.AccountsFetchRepository;
import com.dut.pbl6_server.repository.jpa.AccountsRepository;
import com.dut.pbl6_server.repository.jpa.FollowersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("dev")
public class AccountServiceTest {
    /* Mock beans and dependencies */
    @MockBean
    private AccountsFetchRepository accountsFetchRepository;
    @MockBean
    private AccountsRepository accountsRepository;
    @MockBean
    private FollowersRepository followersRepository;
    @MockBean
    private AccountMapper accountMapper;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private CloudinaryService cloudinaryService;
    @Autowired
    private AccountService accountService;

    /* Test cases */
    @Test
    void getAccountInfo_ValidAccount_ReturnsCorrectAccountInfo() {
        // Arrange
        Account currentUser = new Account();
        currentUser.setId(1L);
        currentUser.setDisplayName("John Doe");

        Account account = new Account();
        account.setId(1L);
        account.setDisplayName("John Doe");
        account.setVisibility(Visibility.PUBLIC);

        AccountResponse expectedResponse = AccountResponse.builder()
            .id(1L)
            .displayName("John Doe")
            .build();

        // Mock
        when(accountsFetchRepository.findById(currentUser.getId())).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(expectedResponse);

        // Act
        AccountResponse result = accountService.getAccountInfo(currentUser);

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    void getAccountInfo_AccountNotFound_ThrowsBadRequestException() {
        // Arrange
        Account currentUser = new Account();
        currentUser.setId(1L);

        // Mock
        when(accountsFetchRepository.findById(currentUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadRequestException.class, () -> accountService.getAccountInfo(currentUser));
    }

    @Test
    void followUser_ValidAction_FollowsUser() {
        // Arrange
        Account currentUser = new Account();
        currentUser.setId(1L);
        Account userToFollow = new Account();
        userToFollow.setId(2L);

        // Mock
        when(accountsRepository.findById(userToFollow.getId())).thenReturn(Optional.of(userToFollow));
        when(followersRepository.findByFollowerIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        // Act
        accountService.followUser(currentUser, 2L);

        // Assert
        verify(followersRepository).save(any(Follower.class));
        verify(notificationService).sendNotification(currentUser, userToFollow, NotificationType.FOLLOW, null, false, false);
    }

    @Test
    void followUser_AlreadyFollowed_ThrowsBadRequestException() {
        // Arrange
        Account currentUser = new Account();
        currentUser.setId(1L);
        Account userToFollow = new Account();
        userToFollow.setId(2L);

        // Mock
        Follower existingFollower = new Follower(currentUser, userToFollow);
        existingFollower.setDeletedAt(null);
        when(followersRepository.findByFollowerIdAndUserId(1L, 2L)).thenReturn(Optional.of(existingFollower));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> accountService.followUser(currentUser, 2L));
    }

    @Test
    void unfollowUser_ValidAction_UnfollowsUser() {
        // Arrange
        Account currentUser = new Account();
        currentUser.setId(1L);
        Account userToUnfollow = new Account();
        userToUnfollow.setId(2L);

        Follower follower = new Follower(currentUser, userToUnfollow);
        follower.setDeletedAt(null);

        // Mock
        when(accountsRepository.findById(userToUnfollow.getId())).thenReturn(Optional.of(userToUnfollow));
        when(followersRepository.findByFollowerIdAndUserId(1L, 2L)).thenReturn(Optional.of(follower));

        // Act
        accountService.unfollowUser(currentUser, 2L);

        // Assert
        verify(followersRepository).save(any(Follower.class));
        verify(notificationService).sendNotification(currentUser, userToUnfollow, NotificationType.UNFOLLOW, null, false, false);
    }

    @Test
    void unfollowUser_AlreadyUnfollowed_ThrowsBadRequestException() {
        // Arrange
        Account currentUser = new Account();
        currentUser.setId(1L);
        Account userToUnfollow = new Account();
        userToUnfollow.setId(2L);

        Follower follower = new Follower(currentUser, userToUnfollow);
        follower.setDeletedAt(CommonUtils.DateTime.getCurrentTimestamp());

        // Mock
        when(followersRepository.findByFollowerIdAndUserId(1L, 2L)).thenReturn(Optional.of(follower));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> accountService.unfollowUser(currentUser, 2L));
    }

    @Test
    void editProfile_ValidRequest_UpdatesProfile() {
        // Arrange
        Account currentUser = new Account();
        currentUser.setId(1L);
        currentUser.setDisplayName("John Doe");

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Jox");
        request.setLastName("Dox");

        Account account = new Account();
        account.setId(1L);
        account.setFirstName("Jox");
        account.setLastName("Dox");
        account.setDisplayName("John Doe");

        AccountResponse expectedResponse = AccountResponse.builder()
            .id(1L)
            .displayName("John Doe")
            .firstName("Jox")
            .lastName("Dox")
            .build();

        // Mock
        when(accountsFetchRepository.findById(currentUser.getId())).thenReturn(Optional.of(account));
        when(accountMapper.toEntity(account, request)).thenReturn(account);
        when(accountsRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(expectedResponse);

        // Act
        AccountResponse result = accountService.editProfile(currentUser, request);

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    void editProfile_NoFieldsUpdated_ReturnsSameProfile() {
        // Arrange
        Account currentUser = new Account();
        currentUser.setId(1L);
        currentUser.setDisplayName("John Doe");

        UpdateProfileRequest request = new UpdateProfileRequest(); // No fields set

        AccountResponse expectedResponse = AccountResponse.builder()
            .id(1L)
            .displayName("John Doe")
            .build();

        // Mock
        when(accountsFetchRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(accountMapper.toResponse(currentUser)).thenReturn(expectedResponse);

        // Act
        AccountResponse result = accountService.editProfile(currentUser, request);

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    void uploadAvatar_ValidAvatar_UpdatesAvatar() {
        // Arrange
        Account currentUser = new Account();
        currentUser.setId(1L);
        MultipartFile avatar = mock(MultipartFile.class);

        Account account = new Account();
        account.setId(1L);
        account.setAvatarFile(null);  // Initially no avatar

        File newAvatar = File.builder().id(1L).name("new").url("http://cloudinary/image.jpg").build();

        AccountResponse expectedResponse = AccountResponse.builder()
            .id(1L)
            .avatarFile(newAvatar)
            .build();

        // Mock
        when(accountsFetchRepository.findById(currentUser.getId())).thenReturn(Optional.of(account));
        when(cloudinaryService.uploadFile(avatar)).thenReturn(newAvatar);
        when(accountsRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(expectedResponse);

        // Act
        AccountResponse result = accountService.uploadAvatar(currentUser, avatar);

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    void deactivateAccount_ValidAction_DeactivatesAccount() {
        // Arrange
        Account admin = new Account();
        admin.setId(1L);
        Account accountToDeactivate = new Account();
        accountToDeactivate.setId(2L);
        accountToDeactivate.setStatus(AccountStatus.ACTIVE);

        AccountResponse expectedResponse = AccountResponse.builder()
            .id(2L)
            .status(AccountStatus.INACTIVE)
            .build();

        // Mock
        when(accountsFetchRepository.getByIdAlthoughDeleted(accountToDeactivate.getId()))
            .thenReturn(Optional.of(accountToDeactivate));
        when(accountsRepository.save(accountToDeactivate)).thenReturn(accountToDeactivate);
        when(accountMapper.toResponse(accountToDeactivate)).thenReturn(expectedResponse);

        // Act
        AccountResponse result = accountService.deactivateAccount(admin, 2L);

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    void activateAccount_ValidAction_ActivatesAccount() {
        // Arrange
        Account admin = new Account();
        admin.setId(1L);
        Account accountToActivate = new Account();
        accountToActivate.setId(2L);
        accountToActivate.setStatus(AccountStatus.INACTIVE);

        AccountResponse expectedResponse = AccountResponse.builder()
            .id(2L)
            .status(AccountStatus.ACTIVE)
            .build();

        // Mock
        when(accountsFetchRepository.getByIdAlthoughDeleted(accountToActivate.getId()))
            .thenReturn(Optional.of(accountToActivate));
        when(accountsRepository.save(accountToActivate)).thenReturn(accountToActivate);
        when(accountMapper.toResponse(accountToActivate)).thenReturn(expectedResponse);

        // Act
        AccountResponse result = accountService.activateAccount(admin, 2L);

        // Assert
        assertEquals(expectedResponse, result);
    }
}

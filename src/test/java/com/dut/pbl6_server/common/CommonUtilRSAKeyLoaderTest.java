package com.dut.pbl6_server.common;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.dut.pbl6_server.common.util.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class CommonUtilRSAKeyLoaderTest {

    @Test
    void loadPublicKey_FileExists_ReturnsPublicKey() throws Exception {
        // Act
        PublicKey publicKey = CommonUtils.RSAKeyLoader.loadPublicKey(CommonConstants.ACCESS_TOKEN_PUBLIC_KEY_FILE);

        // Assert
        Assertions.assertNotNull(publicKey);
    }

    @Test
    void loadPublicKey_FileDoesNotExist_ThrowsException() {
        // Arrange
        String invalidPublicKeyPath = "invalid-public.key";

        // Act & Assert
        Assertions.assertThrows(Exception.class, () -> CommonUtils.RSAKeyLoader.loadPublicKey(invalidPublicKeyPath));
    }

    @Test
    void loadPrivateKey_FileExists_ReturnsPrivateKey() throws Exception {
        // Act
        PrivateKey privateKey = CommonUtils.RSAKeyLoader.loadPrivateKey(CommonConstants.ACCESS_TOKEN_PRIVATE_KEY_FILE);

        // Assert
        Assertions.assertNotNull(privateKey);
    }

    @Test
    void loadPrivateKey_FileDoesNotExist_ThrowsException() {
        // Arrange
        String invalidPrivateKeyPath = "invalid-private.key";

        // Act & Assert
        Assertions.assertThrows(Exception.class, () -> CommonUtils.RSAKeyLoader.loadPrivateKey(invalidPrivateKeyPath));
    }
}

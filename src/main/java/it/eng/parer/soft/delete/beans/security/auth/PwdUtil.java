/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.soft.delete.beans.security.auth;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;

import it.eng.parer.soft.delete.beans.exceptions.AppGenericRuntimeException;
import it.eng.parer.soft.delete.beans.utils.Costanti.ErrorCategory;

public class PwdUtil {

    private PwdUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final int ITERATIONS = 2048;
    private static final int KEY_LENGTH = 64 * 8;

    /**
     *
     * @deprecated
     *
     *             encodePassword util
     *
     * @param password password to enconde
     *
     * @return encoded password
     */
    @Deprecated(forRemoval = true)
    public static String encodePassword(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(password.getBytes(StandardCharsets.UTF_8.name()), 0, password.length());
            byte[] pwdHash = md.digest();
            return new String(Base64.encodeBase64(pwdHash), StandardCharsets.UTF_8.name());
        } catch (NoSuchAlgorithmException ex) {
            throw new AppGenericRuntimeException(ex, ErrorCategory.INTERNAL_ERROR);
        } catch (UnsupportedEncodingException ex) {
            throw new AppGenericRuntimeException(ex, ErrorCategory.INTERNAL_ERROR);
        }

    }

    public static String encodePBKDF2Password(byte[] binarySalt, String password) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), binarySalt, ITERATIONS,
                KEY_LENGTH);
        SecretKeyFactory skf;
        try {
            skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] pwdHash = skf.generateSecret(spec).getEncoded();
            return new String(Base64.encodeBase64(pwdHash), StandardCharsets.UTF_8.name());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new AppGenericRuntimeException(ex, ErrorCategory.INTERNAL_ERROR);
        } catch (UnsupportedEncodingException ex) {
            throw new AppGenericRuntimeException(ex, ErrorCategory.INTERNAL_ERROR);
        }
    }

    public static String encodeUFT8Base64String(byte[] barray) {
        try {
            return new String(Base64.encodeBase64(barray), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new AppGenericRuntimeException(ex, ErrorCategory.INTERNAL_ERROR);
        }
    }

    public static byte[] decodeUFT8Base64String(String utf8str) {
        try {
            return Base64.decodeBase64(utf8str.getBytes(StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException ex) {
            throw new AppGenericRuntimeException(ex, ErrorCategory.INTERNAL_ERROR);
        }
    }

    public static byte[] generateSalt() {
        try {
            SecureRandom sr = SecureRandom.getInstanceStrong();
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            return salt;
        } catch (NoSuchAlgorithmException ex) {
            throw new AppGenericRuntimeException(ex, ErrorCategory.INTERNAL_ERROR);
        }
    }
}

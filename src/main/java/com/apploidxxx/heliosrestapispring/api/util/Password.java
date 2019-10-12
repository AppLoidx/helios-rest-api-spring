package com.apploidxxx.heliosrestapispring.api.util;

import org.apache.commons.codec.digest.Md5Crypt;

import java.util.Base64;

/**
 * Класс для работы с паролями
 *
 * @author Arthur Kupriyanov
 */
public class Password {
    private static final String salt = Base64.getEncoder().encodeToString("ITMO".getBytes());

    /**
     * Хэширует пароль через {@link Md5Crypt}
     * @param password пароль, который нужно хэшировать
     * @return хэшированный пароль
     */
    public static String hash(String password){
        return Md5Crypt.md5Crypt((password + salt).getBytes());
    }

    /**
     * Проверка равенства паролей.
     * Если хотя бы один из них null возвращается false
     * @param rawPassword проверяемый пароль
     * @param hashedPassword исходный захишированный пароль
     * @return <code>true</code>, если пароли совпадают, иначе <code>false</code>
     */
    public static boolean isEqual(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) return false;
        try {
            return hashedPassword.equals(hash(rawPassword, hashedPassword));

        } catch (IllegalArgumentException e){
            return false;
        }
    }
    private static String hash(String password, String anotherEncryptedPassword){
        return Md5Crypt.md5Crypt((password + salt).getBytes(), anotherEncryptedPassword);
    }
}

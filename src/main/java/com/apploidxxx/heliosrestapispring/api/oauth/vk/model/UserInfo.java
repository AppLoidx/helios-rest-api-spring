package com.apploidxxx.heliosrestapispring.api.oauth.vk.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Arthur Kupriyanov
 */
@Data
@NoArgsConstructor
public class UserInfo {
    private List<VkUser> response;
}

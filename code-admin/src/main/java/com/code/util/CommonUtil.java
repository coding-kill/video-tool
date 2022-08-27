package com.code.util;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @Description 记录有需要但是不归属业务的工具
 * @Author zhangw
 * @Date 2022/4/13 13:42
 * @Version 1.0
 */
public class CommonUtil {

    public static boolean getPermissions() {
        SysUser sysUser = ShiroUtils.getSysUser();
        List<SysRole> roles = sysUser.getRoles();
        if (CollectionUtils.isNotEmpty(roles)) {
            //判断是否包含允许展示备份文件夹的角色
            for (SysRole role : roles) {
                String roleKey = role.getRoleKey();
                if ("bakadmin".equals(roleKey)) {
                    return true;
                }
            }
        }
        return false;
    }
}

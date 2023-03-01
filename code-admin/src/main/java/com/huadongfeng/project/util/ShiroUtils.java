//
// Source huadongfeng recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.huadongfeng.project.util;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;
import com.ruoyi.framework.shiro.realm.UserRealm;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;

public class ShiroUtils {
    public ShiroUtils() {
    }

    public static Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    public static Session getSession() {
        return SecurityUtils.getSubject().getSession();
    }

    public static void logout() {
        getSubject().logout();
    }

    public static SysUser getSysUser() {
        SysUser user = null;
        Object obj = getSubject().getPrincipal();
        if (StringUtils.isNotNull(obj)) {
            user = new SysUser();
            BeanUtils.copyBeanProp(user, obj);
        }

        return user;
    }

    public static void setSysUser(SysUser user) {
        Subject subject = getSubject();
        PrincipalCollection principalCollection = subject.getPrincipals();
        String realmName = (String)principalCollection.getRealmNames().iterator().next();
        PrincipalCollection newPrincipalCollection = new SimplePrincipalCollection(user, realmName);
        subject.runAs(newPrincipalCollection);
    }

    public static void clearCachedAuthorizationInfo() {
        RealmSecurityManager rsm = (RealmSecurityManager)SecurityUtils.getSecurityManager();
        UserRealm realm = (UserRealm)rsm.getRealms().iterator().next();
        realm.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }

    public static Long getUserId() {
        return getSysUser().getUserId();
    }

    public static String getLoginName() {
        return getSysUser().getLoginName();
    }

    public static String getIp() {
        return getSubject().getSession().getHost();
    }

    public static String getSessionId() {
        return String.valueOf(getSubject().getSession().getId());
    }
}

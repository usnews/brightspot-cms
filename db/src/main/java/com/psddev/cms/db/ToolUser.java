package com.psddev.cms.db;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.io.BaseEncoding;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.util.Password;
import com.psddev.dari.util.Settings;

/** User that uses the CMS and other related tools. */
@ToolUi.IconName("object-toolUser")
public class ToolUser extends Record {

    private static final long TOKEN_CHECK_EXPIRE_MILLISECONDS = 30000L;

    @Indexed
    @ToolUi.Note("If left blank, the user will have full access to everything.")
    private ToolRole role;

    @Indexed
    @Required
    private String name;

    @Indexed(unique = true)
    @Required
    private String email;

    @ToolUi.FieldDisplayType("timeZone")
    private String timeZone;

    @ToolUi.FieldDisplayType("password")
    private String password;

    private String phoneNumber;
    private NotificationMethod notifyVia;

    @Indexed
    @ToolUi.DropDown
    private Set<Notification> notifications;

    @ToolUi.Hidden
    private Map<String, Object> settings;

    private Site currentSite;

    @ToolUi.Hidden
    private Schedule currentSchedule;

    private boolean tfaEnabled;
    private String totpSecret;

    @ToolUi.Hidden
    private long lastTotpCounter;

    @Indexed
    @ToolUi.Hidden
    private String totpToken;

    @ToolUi.Hidden
    private long totpTokenTime;

    @Indexed
    @Embedded
    @ToolUi.Hidden
    private List<LoginToken> loginTokens;

    /** Returns the role. */
    public ToolRole getRole() {
        return role;
    }

    /** Sets the role. */
    public void setRole(ToolRole role) {
        this.role = role;
    }

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the email. */
    public String getEmail() {
        return email;
    }

    /** Sets the email. */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the time zone.
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Sets the time zone.
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /** Returns the password. */
    public Password getPassword() {
        return Password.valueOf(password);
    }

    /** Sets the password. */
    public void setPassword(Password password) {
        this.password = password.toString();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public NotificationMethod getNotifyVia() {
        return notifyVia;
    }

    public void setNotifyVia(NotificationMethod notifyVia) {
        this.notifyVia = notifyVia;
    }

    public Set<Notification> getNotifications() {
        if (notifications == null) {
            notifications = new LinkedHashSet<Notification>();
        }
        return notifications;
    }

    public void setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
    }

    /** Returns the settings. */
    public Map<String, Object> getSettings() {
        if (settings == null) {
            settings = new LinkedHashMap<String, Object>();
        }
        return settings;
    }

    /** Sets the settings. */
    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public Site getCurrentSite() {
        if ((currentSite == null &&
                hasPermission("site/global")) ||
                (currentSite != null &&
                hasPermission(currentSite.getPermissionId()))) {
            return currentSite;

        } else {
            for (Site s : Site.Static.findAll()) {
                if (hasPermission(s.getPermissionId())) {
                    return s;
                }
            }

            throw new IllegalStateException("No accessible site!");
        }
    }

    public void setCurrentSite(Site site) {
        this.currentSite = site;
    }

    public Schedule getCurrentSchedule() {
        return currentSchedule;
    }

    public void setCurrentSchedule(Schedule currentSchedule) {
        this.currentSchedule = currentSchedule;
    }

    public boolean isTfaEnabled() {
        return tfaEnabled;
    }

    public void setTfaEnabled(boolean tfaEnabled) {
        this.tfaEnabled = tfaEnabled;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public String getTotpToken() {
        return totpToken;
    }

    public byte[] getTotpSecretBytes() {
        return BaseEncoding.base32().decode(getTotpSecret());
    }

    public void setTotpSecretBytes(byte[] totpSecretBytes) {
        this.totpSecret = BaseEncoding.base32().encode(totpSecretBytes);
    }

    public void setTotpToken(String totpToken) {
        this.totpToken = totpToken;
        this.totpTokenTime = System.currentTimeMillis();
    }

    private static final String TOTP_ALGORITHM = "HmacSHA1";
    private static final long TOTP_INTERVAL = 30000L;

    private int getTotpCode(long counter) {
        try {
            Mac mac = Mac.getInstance(TOTP_ALGORITHM);

            mac.init(new SecretKeySpec(getTotpSecretBytes(), TOTP_ALGORITHM));

            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            int offset = hash[hash.length - 1] & 0xf;
            int binary =
                    ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);

            return binary % 1000000;

        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);

        } catch (InvalidKeyException error) {
            throw new IllegalStateException(error);
        }
    }

    public boolean verifyTotp(int code) {
        long counter = System.currentTimeMillis() / TOTP_INTERVAL - 2;

        for (long end = counter + 5; counter < end; ++ counter) {
            if (counter > lastTotpCounter &&
                    code == getTotpCode(counter)) {
                lastTotpCounter = counter;
                save();
                return true;
            }
        }

        return false;
    }

    /**
     * Returns {@code true} if this user is allowed access to the
     * resources identified by the given {@code permissionId}.
     */
    public boolean hasPermission(String permissionId) {
        ToolRole role = getRole();
        return role != null ? role.hasPermission(permissionId) : true;
    }

    public String generateLoginToken() {
        LoginToken loginToken = new LoginToken();
        getLoginTokens().add(loginToken);
        save();

        return loginToken.getToken();
    }

    public void refreshLoginToken(String token) {
        Iterator<LoginToken> iter = getLoginTokens().iterator();
        while (iter.hasNext()) {
            LoginToken loginToken = iter.next();
            if (loginToken.getToken().equals(token)) {
                loginToken.refreshToken();
            } else if (!loginToken.isValid()) {
                iter.remove();
            }
        }

        save();
    }

    public void removeLoginToken(String token) {
        LoginToken loginToken = getLoginToken(token);
        if (loginToken != null) {
            getLoginTokens().remove(loginToken);
            save();
        }
    }

    public LoginToken getLoginToken(String token) {
        for (LoginToken loginToken : getLoginTokens()) {
            if (loginToken.getToken().equals(token) && loginToken.isValid()) {
                return loginToken;
            }
        }

        return null;
    }

    public List<LoginToken> getLoginTokens() {
        if (loginTokens == null) {
            loginTokens = new ArrayList<LoginToken>();
        }

        return loginTokens;
    }

    public void setLoginTokens(List<LoginToken> loginTokens) {
        this.loginTokens = loginTokens;
    }

    public static class LoginToken extends Record {

        @Indexed
        private String token;
        private Long expireTimestamp;

        public LoginToken() {
            this.token = UUID.randomUUID().toString();

            refreshToken();
        }

        public String getToken() {
            return token;
        }

        public Long getExpireTimestamp() {
            return expireTimestamp;
        }

        public void refreshToken() {
            refreshTokenIfNecessary();
        }

        public boolean refreshTokenIfNecessary() {
            long sessionTimeout = Settings.getOrDefault(long.class, "cms/tool/sessionTimeout", 0L);

            if (sessionTimeout == 0L && (this.expireTimestamp == null || this.expireTimestamp != 0L)) {
                this.expireTimestamp = 0L;
                return true;
            }

            // Only refresh if the expireTimestamp is empty or token was issued over TOKEN_CHECK_EXPIRE_MILLISECONDS ago.
            if (sessionTimeout != 0L &&
                    (this.expireTimestamp == null ||
                            this.expireTimestamp == 0L ||
                            (this.expireTimestamp - sessionTimeout) + TOKEN_CHECK_EXPIRE_MILLISECONDS < System.currentTimeMillis())) {
                this.expireTimestamp = System.currentTimeMillis() + sessionTimeout;
                return true;
            }

            return false;
        }

        public boolean isValid() {
            if (getExpireTimestamp() == null) {
                return false;
            }

            if (getExpireTimestamp() == 0L) {
                return true;
            }

            return getExpireTimestamp() > System.currentTimeMillis();
        }
    }

    public static final class Static {

        private Static() {
        }

        public static ToolUser getByTotpToken(String totpToken) {
            ToolUser user = Query.from(ToolUser.class).where("totpToken = ?", totpToken).first();
            return user != null && user.totpTokenTime + 60000 > System.currentTimeMillis() ? user : null;
        }

        public static ToolUser getByToken(String token) {
            ToolUser user = Query.from(ToolUser.class).where("loginTokens/token = ?", token).first();
            return user != null && user.getLoginToken(token) != null ? user : null;
        }
    }
}

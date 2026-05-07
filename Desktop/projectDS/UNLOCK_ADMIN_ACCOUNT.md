# Unlock Admin Account - Account Locked Error Fix

## Error Message
```
Account has been locked due to multiple failed login attempts
```

## What Happened
The system locks accounts after **5 failed login attempts** as a security feature. The account remains locked for **15 minutes**.

---

## 🚀 Quick Fix (Choose ONE method)

### Method 1: Wait 15 Minutes (EASIEST)
The account will automatically unlock after 15 minutes from the last failed attempt.

- ⏰ **Wait time**: 15 minutes
- ✅ **No action needed**: Just wait and try again

---

### Method 2: Restart the Server (FASTEST)
Restarting the server clears the in-memory lock immediately.

1. **Stop the server**: Press `Ctrl+C` in the server terminal

2. **Start the server again**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"
   ```

3. **Try logging in again** with correct credentials:
   - Username: `admin`
   - Password: `Admin@123`

---

### Method 3: Use Database Script (If account is disabled)
If the account is also disabled in the database:

#### Using phpMyAdmin:
1. Go to `http://localhost/phpmyadmin`
2. Select database `Wolde`
3. Click "SQL" tab
4. Paste this:
```sql
USE Wolde;
UPDATE USERS SET is_active = TRUE WHERE username = 'admin';
SELECT username, is_active FROM USERS WHERE username = 'admin';
```
5. Click "Go"

#### Using Command Line:
```bash
# Windows XAMPP
"C:\xampp\mysql\bin\mysql.exe" -u root -p Wolde < scripts/database/unlock-admin.sql

# macOS XAMPP
/Applications/XAMPP/xamppfiles/bin/mysql -u root -p Wolde < scripts/database/unlock-admin.sql

# Linux XAMPP
/opt/lampp/bin/mysql -u root -p Wolde < scripts/database/unlock-admin.sql
```

---

## 🔐 Account Locking Details

### How It Works
- **Failed Attempts Allowed**: 5 attempts
- **Lockout Duration**: 15 minutes
- **Storage**: In-memory (cleared on server restart)
- **Purpose**: Prevent brute-force attacks

### What Triggers the Lock
1. Entering wrong password 5 times
2. Trying to login with non-existent username 5 times
3. Any combination of failed login attempts

### When Lock Expires
- **Automatically**: After 15 minutes from the last failed attempt
- **Manually**: By restarting the server
- **On Success**: Lock is cleared after successful login

---

## ✅ Verify Account Status

### Check if account is locked (in logs):
Look for this message in server logs:
```
Authentication attempt on locked account: admin
```

### Check if account is active (in database):
```sql
USE Wolde;
SELECT username, is_active FROM USERS WHERE username = 'admin';
```

**Expected result**:
```
+----------+-----------+
| username | is_active |
+----------+-----------+
| admin    |         1 |
+----------+-----------+
```

If `is_active` is `0`, run the unlock script.

---

## 🛡️ Prevent Future Lockouts

### 1. Use Correct Credentials
- **Username**: `admin` (lowercase)
- **Password**: `Admin@123` (case-sensitive)

### 2. Double-Check Before Submitting
- Make sure Caps Lock is OFF
- Verify you're typing the correct password
- Check for extra spaces

### 3. Reset Password If Forgotten
If you've forgotten the password, reset it in the database:

```sql
USE Wolde;

-- Reset admin password to 'Admin@123'
UPDATE USERS 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G'
WHERE username = 'admin';

-- Ensure account is active
UPDATE USERS 
SET is_active = TRUE 
WHERE username = 'admin';
```

### 4. Create Additional Admin Accounts
Don't rely on a single admin account:

1. Login as admin (after unlocking)
2. Go to "User Management"
3. Create a new admin user
4. Use the new account as backup

---

## 🔧 Configuration Options

### Change Lockout Settings
You can modify these values in `AuthenticationService.java`:

```java
// Current settings
private static final int MAX_FAILED_ATTEMPTS = 5;  // Number of attempts
private static final long LOCKOUT_DURATION_MILLIS = 15 * 60 * 1000;  // 15 minutes
```

**To change**:
1. Edit `src/main/java/com/attendance/system/service/AuthenticationService.java`
2. Modify the values (e.g., increase to 10 attempts or 30 minutes)
3. Recompile: `mvn clean compile`
4. Restart server

---

## 🆘 Troubleshooting

### Issue: "Still locked after 15 minutes"
**Solution**: 
- The 15 minutes starts from the LAST failed attempt
- If you keep trying, the timer resets
- Wait 15 minutes WITHOUT trying to login

### Issue: "Still locked after server restart"
**Solution**:
- Check if account is disabled in database
- Run the unlock script
- Verify `is_active = 1` in USERS table

### Issue: "Don't remember the password"
**Solution**:
- Use the password reset SQL above
- Default password is `Admin@123`
- Or recreate the admin account

### Issue: "Want to disable account locking"
**Solution** (NOT RECOMMENDED for production):
1. Edit `AuthenticationService.java`
2. Change `MAX_FAILED_ATTEMPTS` to a very high number (e.g., 999)
3. Or comment out the `isAccountLocked()` check
4. Recompile and restart

---

## 📋 Quick Command Reference

### Restart Server
```bash
# Stop: Ctrl+C
# Start:
mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"
```

### Check Account Status
```sql
USE Wolde;
SELECT username, is_active, created_at FROM USERS WHERE username = 'admin';
```

### Unlock Account
```sql
USE Wolde;
UPDATE USERS SET is_active = TRUE WHERE username = 'admin';
```

### Reset Password
```sql
USE Wolde;
UPDATE USERS 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ePPO4tyh/OpGrrabJefPCt/Nu/G'
WHERE username = 'admin';
```

---

## ✅ Recommended Solution

**For immediate access**:
1. ✅ **Restart the server** (fastest - takes 30 seconds)
2. ✅ **Login with correct credentials**: `admin` / `Admin@123`
3. ✅ **Create a backup admin account** (for future)

**For future prevention**:
1. ✅ **Save credentials** in a secure password manager
2. ✅ **Create multiple admin accounts**
3. ✅ **Be careful when typing passwords**

---

## 📞 Summary

| Method | Time | Difficulty | Recommended |
|--------|------|------------|-------------|
| Wait 15 minutes | 15 min | Easy | ⭐ If not urgent |
| Restart server | 30 sec | Easy | ⭐⭐⭐ Best option |
| Database unlock | 2 min | Medium | If account disabled |

---

**Status**: 🔒 Account Locked  
**Solution**: 🔄 Restart Server (Fastest)  
**Prevention**: 💾 Save correct credentials  

---

**Last Updated**: May 8, 2026

## 🚀 Quick Action

**Right now, do this**:
1. Press `Ctrl+C` in server terminal
2. Run: `mvn exec:java -Dexec.mainClass="com.attendance.system.server.ServerLauncher"`
3. Login with: `admin` / `Admin@123`

**Done!** ✅

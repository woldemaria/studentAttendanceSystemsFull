# Registration Feature - Quick Start Guide

## Overview
The Student Attendance System now includes a self-registration feature that allows students and teachers to create accounts without administrator intervention.

## Quick Start for Users

### Step 1: Launch the Application
```bash
java -jar student-attendance-system-1.0.0.jar
```

### Step 2: Click Register
On the login screen, click the green **"Register"** button.

### Step 3: Fill in Your Information
Complete all required fields:
- **First Name**: Your first name (max 50 characters)
- **Last Name**: Your last name (max 50 characters)
- **Username**: Your login username (3-50 characters, alphanumeric + . _ -)
- **Email**: Your email address (must be valid format)
- **Account Type**: Select "Student" or "Teacher"
- **Password**: Create a strong password (see requirements below)
- **Confirm Password**: Re-enter your password

### Step 4: Create Your Account
Click the **"Register"** button to create your account.

### Step 5: Log In
After successful registration, you'll be redirected to the login screen. Log in with your new username and password.

## Password Requirements

Your password must contain:
- ✓ At least 8 characters
- ✓ At least one uppercase letter (A-Z)
- ✓ At least one lowercase letter (a-z)
- ✓ At least one number (0-9)
- ✓ At least one special character (!@#$%^&*()_+-=[]{}';:"\\|,.<>/?))

### Example Strong Passwords:
- `MyPassword123!`
- `SecurePass@2024`
- `Attendance#System99`

## Common Issues

### "Username already exists"
- The username is already taken
- Try a different username

### "Email address already exists"
- The email is already registered
- Use a different email address
- Or log in if you already have an account

### "Invalid email format"
- Check your email format (should be: user@domain.com)
- Make sure there are no spaces

### "Password must contain..."
- Your password doesn't meet the requirements
- Add the missing character type (uppercase, lowercase, number, or special character)

### "Server error: Unable to create account"
- The server may be temporarily unavailable
- Check your internet connection
- Try again in a few moments

## Tips

### Username Tips
- Use lowercase letters and numbers for simplicity
- Avoid special characters if possible
- Make it memorable but unique
- Examples: `john_smith`, `student.2024`, `teacher-jane`

### Email Tips
- Use your official school or personal email
- Make sure you have access to this email
- Double-check for typos
- Examples: `john.smith@school.edu`, `jane@email.com`

### Password Tips
- Use a mix of character types
- Avoid common words or patterns
- Don't use your username or email
- Consider using a passphrase
- Write it down securely or use a password manager

## After Registration

### First Login
1. Use your username and password to log in
2. You'll see your role-specific dashboard
3. Complete your profile if needed

### Student Dashboard
- View your attendance records
- Check your attendance percentage
- View notifications
- See your enrolled courses

### Teacher Dashboard
- Mark attendance for your classes
- View class attendance reports
- Manage your courses
- View student attendance statistics

## Need Help?

### For Registration Issues
1. Check the error message carefully
2. Review the requirements above
3. Try again with corrected information
4. Contact your system administrator if problems persist

### For Account Access Issues
1. Verify your username and password
2. Check that Caps Lock is off
3. Try resetting your password (contact admin)
4. Contact your system administrator

### For Technical Issues
1. Check your internet connection
2. Verify the server is running
3. Try again in a few moments
4. Contact your system administrator

## Security Reminders

⚠️ **Important Security Tips:**
- Never share your password with anyone
- Don't use the same password as other accounts
- Change your password regularly
- Log out when finished, especially on shared computers
- Be cautious of phishing emails asking for your credentials
- Report suspicious activity to your administrator

## Account Management

### Changing Your Password
1. Log in to your account
2. Go to Account Settings (if available)
3. Select "Change Password"
4. Enter your current password
5. Enter your new password (must meet requirements)
6. Confirm your new password
7. Click "Update"

### Updating Your Profile
1. Log in to your account
2. Go to your dashboard
3. Click "Edit Profile" or "Account Settings"
4. Update your information
5. Click "Save"

### Resetting Your Password
If you forget your password:
1. Click "Forgot Password" on the login screen (if available)
2. Or contact your system administrator
3. Administrator can reset your password
4. You'll receive a temporary password
5. Log in and change to a new password

## System Requirements

### Minimum Requirements
- Java 11 or higher
- 4GB RAM
- 100MB disk space
- Internet connection to server

### Supported Operating Systems
- Windows 10/11
- macOS 10.14+
- Linux (Ubuntu 18.04+)

## Troubleshooting Checklist

- [ ] Internet connection is working
- [ ] Server is running and accessible
- [ ] All required fields are filled
- [ ] Password meets all requirements
- [ ] Username/email are not already registered
- [ ] No special characters in username (except . _ -)
- [ ] Email format is correct
- [ ] Caps Lock is off
- [ ] No extra spaces in fields

## Contact Support

For additional help:
1. Check the full documentation: `REGISTRATION_SYSTEM.md`
2. Review error messages carefully
3. Contact your system administrator
4. Submit a support ticket with:
   - Error message
   - Steps to reproduce
   - Your operating system
   - Browser/application version

---

**Last Updated**: May 6, 2026
**Version**: 1.0.0

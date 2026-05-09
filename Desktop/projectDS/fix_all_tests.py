#!/usr/bin/env python3
"""
Fix all test files to use the new 11-parameter registerUser signature.
Old: registerUser(username, email, firstName, lastName, password, role, classSection)
New: registerUser(username, email, firstName, lastName, password, role, classSection, phoneNumber, gender, photoPath, department)
"""

import re
import sys

def fix_register_user_calls(content):
    """Fix registerUser calls to include 4 additional parameters."""
    
    # Pattern to match registerUser calls with 7 parameters
    # Captures: username, email, firstName, lastName, password, role, classSection
    pattern = r'registerUser\(\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*([^,]+),\s*(UserRole\.\w+),\s*([^)]+)\)'
    
    def replacement(match):
        username = match.group(1)
        email = match.group(2)
        firstName = match.group(3)
        lastName = match.group(4)
        password = match.group(5)
        role = match.group(6)
        classSection = match.group(7)
        
        # Determine department based on role
        if 'TEACHER' in role:
            department = '"Computer Science"'
        else:
            department = 'null'
        
        # Build new call with 11 parameters
        return f'registerUser({username}, {email}, {firstName}, {lastName}, {password}, {role}, {classSection}, null, null, null, {department})'
    
    # Replace all occurrences
    fixed_content = re.sub(pattern, replacement, content)
    
    return fixed_content

def fix_file(filepath):
    """Fix a single test file."""
    print(f"Fixing {filepath}...")
    
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        fixed_content = fix_register_user_calls(content)
        
        if fixed_content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(fixed_content)
            
            # Count changes
            original_calls = len(re.findall(r'registerUser\([^)]+\)', original_content))
            print(f"  ✓ Fixed {filepath}")
            return True
        else:
            print(f"  - No changes needed in {filepath}")
            return False
            
    except Exception as e:
        print(f"  ✗ Error fixing {filepath}: {e}")
        return False

def main():
    """Main function to fix all test files."""
    test_files = [
        'src/test/java/com/attendance/system/server/RegistrationServerTest.java',
        'src/test/java/com/attendance/system/integration/RegistrationIntegrationTest.java'
    ]
    
    print("=" * 60)
    print("Fixing Test Files - registerUser Signature Update")
    print("=" * 60)
    print()
    
    fixed_count = 0
    for filepath in test_files:
        if fix_file(filepath):
            fixed_count += 1
        print()
    
    print("=" * 60)
    print(f"Summary: Fixed {fixed_count} out of {len(test_files)} files")
    print("=" * 60)
    
    return 0 if fixed_count > 0 else 1

if __name__ == '__main__':
    sys.exit(main())

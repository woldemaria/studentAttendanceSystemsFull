#!/usr/bin/env python3
"""
Script to fix all test files by adding classSection parameter to registerUser calls.
"""

import re
import os
import glob

def fix_register_user_calls(content):
    """
    Fix registerUser calls by adding classSection parameter.
    Pattern: registerUser(..., UserRole.STUDENT) -> registerUser(..., UserRole.STUDENT, "A")
    Pattern: registerUser(..., UserRole.TEACHER) -> registerUser(..., UserRole.TEACHER, null)
    """
    # Pattern to match registerUser calls with 6 parameters (missing classSection)
    # Matches: registerUser("user", "email", "first", "last", "pass", UserRole.STUDENT)
    pattern = r'registerUser\(((?:[^()]|\([^()]*\))*?),\s*(UserRole\.(STUDENT|TEACHER|ADMIN))\)'
    
    def replacement(match):
        params = match.group(1)
        role = match.group(2)
        role_type = match.group(3)
        
        # Add appropriate classSection based on role
        if role_type == 'STUDENT':
            return f'registerUser({params}, {role}, "A")'
        else:  # TEACHER or ADMIN
            return f'registerUser({params}, {role}, null)'
    
    # Apply the replacement
    fixed_content = re.sub(pattern, replacement, content)
    
    return fixed_content

def fix_file(filepath):
    """Fix a single test file."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        fixed_content = fix_register_user_calls(content)
        
        if fixed_content != original_content:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(fixed_content)
            print(f"✅ Fixed: {filepath}")
            return True
        else:
            print(f"⏭️  Skipped (no changes): {filepath}")
            return False
    except Exception as e:
        print(f"❌ Error fixing {filepath}: {e}")
        return False

def main():
    """Main function to fix all test files."""
    print("🔧 Fixing test files...")
    print("=" * 60)
    
    # Find all test files
    test_files = glob.glob('src/test/java/**/*Test.java', recursive=True)
    test_files.extend(glob.glob('*Test.java'))
    
    fixed_count = 0
    for filepath in test_files:
        if fix_file(filepath):
            fixed_count += 1
    
    print("=" * 60)
    print(f"✅ Fixed {fixed_count} files out of {len(test_files)} test files")

if __name__ == '__main__':
    main()

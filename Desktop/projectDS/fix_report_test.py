#!/usr/bin/env python3
"""
Script to fix ReportServiceImplTest by adding AttendanceStatus parameter to findWithFilters calls.
"""

import re

def fix_find_with_filters():
    filepath = 'src/test/java/com/attendance/system/service/ReportServiceImplTest.java'
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Pattern to match findWithFilters calls with 4 parameters
    # Replace: findWithFilters(a, b, c, d) with findWithFilters(a, b, c, d, null)
    pattern = r'findWithFilters\(([^)]+)\)'
    
    def replacement(match):
        params = match.group(1)
        # Count commas to determine number of parameters
        param_count = params.count(',') + 1
        
        if param_count == 4:
            # Add null as 5th parameter
            return f'findWithFilters({params}, null)'
        else:
            # Already has 5 parameters or different call
            return match.group(0)
    
    fixed_content = re.sub(pattern, replacement, content)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(fixed_content)
    
    print(f"✅ Fixed: {filepath}")

if __name__ == '__main__':
    fix_find_with_filters()

import os
import re

files = [
    "src/ui/AdminPanel.java",
    "src/ui/AnalyticsSubPanel.java",
    "src/ui/DomainSubPanel.java",
    "src/ui/HistorySubPanel.java",
    "src/ui/HomeSubPanel.java",
    "src/ui/ModernSelectionDialog.java",
    "src/ui/ProfileSubPanel.java",
    "src/ui/ResultPanel.java",
    "src/ui/ResultsHistoryPage.java"
]

def process(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    matches = re.finditer(r'^[ \t]*JScrollPane\s+([a-zA-Z0-9_]+)\s*=\s*new\s+JScrollPane\([^;]*\);[ \t]*\n', content, re.MULTILINE)
    
    offset = 0
    new_content = content
    for m in matches:
        var_name = m.group(1)
        insertion = (
            f"        {var_name}.getVerticalScrollBar().setUI(new ModernScrollBarUI());\n"
            f"        {var_name}.getHorizontalScrollBar().setUI(new ModernScrollBarUI());\n"
            f"        {var_name}.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(8, 0));\n"
            f"        {var_name}.getHorizontalScrollBar().setPreferredSize(new java.awt.Dimension(0, 8));\n"
        )
        pos = m.end() + offset
        new_content = new_content[:pos] + insertion + new_content[pos:]
        offset += len(insertion)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)

for f in files:
    process(f)

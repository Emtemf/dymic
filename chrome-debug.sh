killall chromium 2>/dev/null; sleep 2; snap run chromium --remote-debugging-port=9222 --no-first-run http://localhost:8888/config/template-designer.html

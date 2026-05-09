# ✅ CORRECT COMMANDS TO RUN

## 🎯 You are here: `/home/woldemariam/Desktop/projectDS`

---

## ❌ WRONG (What you tried):
```bash
./start-server-network.sh
```

## ✅ CORRECT (What you should run):
```bash
./scripts/start-server-network.sh
```

**Notice:** Add `scripts/` before the filename!

---

## 📋 ALL CORRECT COMMANDS:

### 1. Start Server:
```bash
./scripts/start-server-network.sh
```

### 2. Start Client:
```bash
./scripts/start-client-network.sh 192.168.1.100
```
*(Replace `192.168.1.100` with your server's IP)*

### 3. Run Setup:
```bash
./scripts/setup-network.sh
```

---

## 🔍 WHY THIS HAPPENS:

Your project structure:
```
/home/woldemariam/Desktop/projectDS/     ← You are here
├── scripts/                              ← Scripts are here
│   ├── start-server-network.sh          ← The file
│   ├── start-client-network.sh
│   └── setup-network.sh
├── src/
├── target/
└── pom.xml
```

When you run `./start-server-network.sh`, bash looks in the **current directory**.
When you run `./scripts/start-server-network.sh`, bash looks in the **scripts subdirectory**.

---

## 🚀 QUICK START (Copy-Paste):

```bash
# Make sure you're in the project directory
cd /home/woldemariam/Desktop/projectDS

# Make scripts executable (one-time)
chmod +x scripts/*.sh

# Start the server
./scripts/start-server-network.sh
```

---

## 💡 ALTERNATIVE: Run from scripts directory

```bash
# Go into scripts directory
cd scripts

# Now you can run without the path
./start-server-network.sh

# Go back to project root
cd ..
```

---

## ✅ VERIFY SCRIPTS EXIST:

```bash
# List all scripts
ls -la scripts/

# Should show:
# start-server-network.sh
# start-client-network.sh
# setup-network.sh
```

---

## 🎯 READY TO START!

Run this now:
```bash
./scripts/start-server-network.sh
```

This will:
1. ✅ Auto-detect your IP
2. ✅ Check Java & MySQL
3. ✅ Configure firewall
4. ✅ Start the server
5. ✅ Show connection info

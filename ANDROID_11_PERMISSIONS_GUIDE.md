# Android 11+ File Access Guide for PnCUtils

**Issue:** Android 11+ restricts access to `/Android/data/` folder even with "All files" permission. The File Swapper feature requires manual steps using a file manager.

**Solution:** Use ZArchiver (or MiXplorer) to move files in/out of the restricted `/Android/data/` folder.

---

## Why This is Needed

- **Android 11+** introduced stricter file access controls
- Apps can't directly write to `/Android/data/` folders even with "All files" permission
- File managers like ZArchiver have special access that bypasses this restriction
- This is a system limitation, not a bug in PnCUtils

---

## Prerequisites

1. **Grant "All files" permission:**
   - Settings → Apps → PnCUtils → Permissions → Files & media
   - Select **"Allow all files"** or **"Manage all files"**
   - Note: This is available on Android 11+

2. **Install ZArchiver** (free on Play Store):
   - [Download ZArchiver](https://play.google.com/store/apps/details?id=ru.zdevs.zarchiver)
   - It can access `/Android/data/` when others can't

---

## Step-by-Step Workflow

### Step 1: Create PnCUtils Folder

**Open PnCUtils app:**
- Tap **"Create PnCUtils Folder"** button
- The app creates `/sdcard/PnCUtils/` folder
- If folder already exists, you'll see "Folder already exists" message

---

### Step 2: Copy Game File to PnCUtils Folder (Using ZArchiver)

**Open ZArchiver:**
- Tap the folder icon at the bottom
- Navigate: Internal storage → Android → data → com.global.tmslg → files → ABAsset

**Copy the game file:**
- Long-press on `1b98f4343ed035646b53cccdb7bd3811.assetbundles`
- Tap **Copy**

**Navigate to PnCUtils folder:**
- Go to: Internal storage → sdcard → PnCUtils

**Paste the file:**
- Long-press empty space
- Tap **Paste**

**Result:** Now you have the original game file in `/sdcard/PnCUtils/`

---

### Step 3: Select File and Modify (Using PnCUtils App)

**In PnCUtils app:**
- Tap **"Pick File (SAF)"**
- Navigate to `/sdcard/PnCUtils/`
- Select `1b98f4343ed035646b53cccdb7bd3811.assetbundles`

**Then tap:**
- **"Replace File"** (the normal button, not the Android 11+ button)
- The app creates a backup and modifies the file

---

### Step 4: Copy Modified File Back to Game (Using ZArchiver)

**Open ZArchiver:**
- Navigate to `/sdcard/PnCUtils/`
- Find the modified `1b98f4343ed035646b53cccdb7bd3811.assetbundles`
- Long-press → **Copy**

**Navigate back to game folder:**
- Android → data → com.global.tmslg → files → ABAsset

**Paste (overwrite):**
- Long-press empty space → **Paste**
- When asked "Replace file?" → tap **Yes/Overwrite**

**Done!** The game now uses your modified file.

---

### Restore the Original

**When you want to restore:**

**In PnCUtils app:**
- Tap **"Restore Original"** (the normal button)
- The backup is restored

**Then in ZArchiver:**
- Navigate to `/sdcard/PnCUtils/`
- Copy `1b98f4343ed035646b53cccdb7bd3811.assetbundles`
- Navigate to `/Android/data/com.global.tmslg/files/ABAsset/`
- Paste (overwrite) the original file back

---

## Quick Reference Button

The **"TUTORIAL FOR ANDROID 11+"** button in the app shows this tutorial in a dialog for quick reference while working.

---

## Restore Original

If you want to restore the backup:

**In PnCUtils:**
- Tap **"Restore Original"**
- The backup in `/sdcard/PnCUtils/` is restored

**Then in ZArchiver:**
- Copy `1b98f4343ed035646b53cccdb7bd3811.assetbundles` from `/sdcard/PnCUtils/`
- Paste back to `/Android/data/com.global.tmslg/files/ABAsset/`

---

## Why This is Needed

- **Android 11+** introduced stricter file access controls
- Apps can't directly write to `/Android/data/` folders even with "All files" permission
- File managers like ZArchiver have special access that bypasses this restriction
- This is a system limitation, not a bug in PnCUtils

---

## Alternative File Managers

If ZArchiver doesn't work, try:
- **MiXplorer** (very reliable for `/Android/data/` access)
- **FX File Manager**
- **X-plore File Manager**

All can access restricted folders and work with PnCUtils.

---

## Troubleshooting

**Q: Can't see `/Android/data/` folder in file manager?**
- A: Enable "Show hidden files" in the manager settings

**Q: "Permission denied" when pasting?**
- A: Grant "All files" permission in app settings (see Prerequisites). This requires Android 11+

**Q: File won't paste back to game folder?**
- A: Make sure you're in the exact correct folder: `/Android/data/com.global.tmslg/files/ABAsset/`

**Q: Why does this happen even on Android 11?**
- A: Starting from Android 11, Google restricted direct app access to `/Android/data/`. Only file managers with special permissions can access these folders.

**Q: Still having issues?**
- A: Join our community or open an issue on GitHub with details

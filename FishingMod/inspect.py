import zipfile
import subprocess
import os

# find the intermediary jar
loom_cache = r"C:\Users\dimal\.gradle\caches\fabric-loom"
jar_path = None
for root, dirs, files in os.walk(loom_cache):
    for file in files:
        if file.startswith("1.21.1") and file.endswith(".jar") and "intermediary" in root:
            jar_path = os.path.join(root, file)
            break
    if jar_path: break

print("Found jar:", jar_path)
if jar_path:
    with zipfile.ZipFile(jar_path, 'r') as z:
        for info in z.infolist():
            if info.filename == "net/minecraft/class_1787.class" or info.filename == "net/minecraft/class_1792.class":
                z.extract(info, r"C:\FishingMine\mc-classes")
                print("Extracted", info.filename)

javap = r"C:\FishingMine\jdk\jdk-21.0.11+10\bin\javap.exe"
print(subprocess.check_output([javap, r"C:\FishingMine\mc-classes\net\minecraft\class_1787.class"]).decode('utf-8'))
print(subprocess.check_output([javap, r"C:\FishingMine\mc-classes\net\minecraft\class_1792.class"]).decode('utf-8'))

import java.util.zip.*;
import java.io.*;

public class Inspect {
    public static void main(String[] args) throws Exception {
        String cache = "C:\\Users\\dimal\\.gradle\\caches\\fabric-loom\\minecraftMaven\\net\\minecraft\\minecraft-merged-intermediary";
        File dir = new File(cache);
        File jar = null;
        for (File f : dir.listFiles()[0].listFiles()) {
            if (f.getName().endsWith(".jar")) jar = f;
        }
        ZipFile zf = new ZipFile(jar);
        InputStream is = zf.getInputStream(zf.getEntry("net/minecraft/class_1787.class"));
        File out = new File("C:\\FishingMine\\class_1787.class");
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            fos.write(buf, 0, len);
        }
        fos.close();
        zf.close();
        
        Process p = Runtime.getRuntime().exec("C:\\FishingMine\\jdk\\jdk-21.0.11+10\\bin\\javap.exe -c C:\\FishingMine\\class_1787.class");
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = r.readLine()) != null) {
            System.out.println(line);
        }
    }
}

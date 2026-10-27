import java.util.jar.*;
import java.io.*;
import java.util.Enumeration;

public class VerifyClasses {
    public static void main(String[] args) throws Exception {
        String jarPath = "C:\\Users\\dimal\\.gradle\\caches\\fabric-loom\\minecraftMaven\\net\\minecraft\\minecraft-merged\\1.21.11-net.fabricmc.yarn.1_21_11.1.21.11+build.6\\minecraft-merged-1.21.11-net.fabricmc.yarn.1_21_11.1.21.11+build.6.jar";
        
        String cls = "net/minecraft/client/render/entity/FishingBobberEntityRenderer.class";
        
        JarFile jar = new JarFile(jarPath);
        String outDir = "C:\\FishingMine\\verify-classes";
        
        JarEntry entry = jar.getJarEntry(cls);
        if (entry != null) {
            String outFile = outDir + "\\" + cls.replace("/", "\\");
            new File(outFile).getParentFile().mkdirs();
            InputStream is = jar.getInputStream(entry);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            fos.close();
            System.out.println("EXTRACTED: " + cls);
        }
        jar.close();
        
        String javap = "C:\\FishingMine\\jdk\\jdk-21.0.11+10\\bin\\javap.exe";
        Process p = Runtime.getRuntime().exec(new String[]{javap, "-c", "-p", outDir + "\\net\\minecraft\\client\\render\\entity\\FishingBobberEntityRenderer.class"});
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = r.readLine()) != null) System.out.println(line);
    }
}

import java.util.zip.ZipFile;
import java.util.Enumeration;
import java.io.File;

public class JarExtractor {
    public static void main(String[] args) throws Exception {
        File home = new File(System.getProperty("user.home"));
        File gradleCaches = new File(home, ".gradle/caches");
        File targetJar = findJar(gradleCaches, "google-genai-1.41.0.jar");
        
        if (targetJar == null) {
            System.out.println("JAR not found");
            return;
        }
        
        System.out.println("Found JAR: " + targetJar.getAbsolutePath());
        ZipFile zip = new ZipFile(targetJar);
        Enumeration<?> entries = zip.entries();
        
        System.out.println("=== LIVE API CLASSES ===");
        while (entries.hasMoreElements()) {
            java.util.zip.ZipEntry entry = (java.util.zip.ZipEntry) entries.nextElement();
            if (entry.getName().startsWith("com/google/genai/") && (entry.getName().toLowerCase().contains("live") || entry.getName().toLowerCase().contains("session")) && entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
                System.out.println(entry.getName().replace("/", ".").replace(".class", ""));
            }
        }
    }
    
    private static File findJar(File dir, String name) {
        if (!dir.exists()) return null;
        if (dir.getName().equals(name)) return dir;
        
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().equals(name)) return f;
                    File found = findJar(f, name);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }
}

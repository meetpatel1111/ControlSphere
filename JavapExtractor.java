import java.io.*;

public class JavapExtractor {
    public static void main(String[] args) throws Exception {
        File home = new File(System.getProperty("user.home"));
        File temp = new File(System.getProperty("java.io.tmpdir"));
        File gradleCaches = new File(home, ".gradle/caches");
        File targetJar = findJar(gradleCaches, "google-genai-1.41.0.jar");
        
        if (targetJar == null) {
            System.out.println("JAR not found");
            return;
        }
        
        String[] classes = {
            "com.google.genai.types.LiveConnectConfig",
            "com.google.genai.types.LiveSendRealtimeInputParameters",
            "com.google.genai.types.LiveServerMessage",
            "com.google.genai.types.LiveSendClientContentParameters"
        };
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(System.getProperty("user.dir"), "javap_out.txt")));
        
        for (String c : classes) {
            writer.write("-------------------------\n");
            writer.write("javap -public " + c + "\n");
            ProcessBuilder pb = new ProcessBuilder("javap", "-public", "-cp", targetJar.getAbsolutePath(), c);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                writer.write(line + "\n");
            }
            p.waitFor();
            writer.write("-------------------------\n");
        }
        writer.close();
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

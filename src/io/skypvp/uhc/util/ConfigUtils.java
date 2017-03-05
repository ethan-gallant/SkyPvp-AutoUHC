package io.skypvp.uhc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigUtils {
	
	public static void copy(InputStream in, File file) {
		try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            
            while((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void save(final File file, final YamlConfiguration config) throws IOException {
		config.save(file);
	}
}

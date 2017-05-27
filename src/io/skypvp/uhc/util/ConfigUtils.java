package io.skypvp.uhc.util;

import io.skypvp.uhc.SkyPVPUHC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class ConfigUtils {
    
    public static SkyPVPUHC main;
	
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
	
    public static ItemStack handleIconString(String itemStackStr) {
        String[] split = itemStackStr.toUpperCase().split(":");
        // Let's split for amount.
        String[] amtSplit = itemStackStr.toUpperCase().split("-");
        try {
            String useSplit = itemStackStr.toUpperCase();
            if(itemStackStr.contains(":")) {
               useSplit = split[0];
            }else if(itemStackStr.contains("-")) {
               useSplit = amtSplit[0];
               //System.out.println(amtSplit[0]);
            }
            
            //System.out.println(itemStackStr);
            //System.out.println(itemStackStr.contains("-"));
            
            Material mat = Material.valueOf(useSplit);
            short data = 0;
            if(split.length == 2) {
                data = Short.valueOf(split[1]);
            }
            
            int amount = 1;
            if(amtSplit.length == 2) {
                amount = Integer.valueOf(amtSplit[1]);
            }

            return new ItemStack(mat, amount, data);
        } catch (IllegalArgumentException | NullPointerException e) {
            main.sendConsoleMessage(ChatColor.DARK_RED + String.format("Material %s does not exist. Please check your config.", split[0]));
            main.disable();
        }
        
        return null;
    }
    
    public static Sound handleSoundString(String soundStr) {
        try {
            Sound snd = Sound.valueOf(soundStr);
            return snd;
        } catch (IllegalArgumentException | NullPointerException e) {
            main.sendConsoleMessage(ChatColor.DARK_RED + String.format("Sound %s does not exist. Please check your config.", soundStr));
            main.disable();
        }
        
        return null;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.library;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author gbl
 * 
 * TODO: Only one instance. First loading plugin wins.
 */

public class DefaultMessageLoader implements MessageLoader {
    
    private final JavaPlugin plugin;
    private YamlConfiguration messages;
    private static DefaultMessageLoader instance=null;
    
    private DefaultMessageLoader(JavaPlugin p) {
        plugin=p;
        messages=null;
        reloadMessages();
    }
    
    // @Override
    public static MessageLoader getInstance(JavaPlugin p) {
        if (instance==null)
            instance=new DefaultMessageLoader(p);
        return instance;
    }
    
    @Override
    public boolean reloadMessages() {
        YamlConfiguration tempMessages=new YamlConfiguration();
        try {
            File file;
            if ((file=new File(plugin.getDataFolder(), "messages.yml")).exists())
                tempMessages.load(file);
            else
                tempMessages.load(new InputStreamReader(plugin.getResource("messages.yml")));
            messages=tempMessages;
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().log(Level.SEVERE, null, e);
            // In case of error, initialze messages, but don't overwrite them.
            if (messages==null)
                messages=tempMessages;
            return false;
        }
    }

    @Override
    public String getMessage(String id, Object... placeholders) {
        String result, formatted;
        if (messages==null)
            return "no messages loaded";
        if ((result=messages.getString(id))==null)
            return "No message defined for "+id;
        try {
            formatted=MessageFormat.format(result, placeholders);
            return formatted;
        } catch (IllegalArgumentException e) {
            return "Problem formatting "+id+" "+result+" with "+placeholders.length+" parameters ";
        }
    }
    
    private void copy(InputStream in, File file) {
        byte[] buf = new byte[1024];
        int len;
        try (OutputStream out = new FileOutputStream(file)) {
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }    
}

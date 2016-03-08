/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.library;

/**
 *
 * @author gbl
 */
public interface MessageLoader {
    // public static MessageLoader getInstance(JavaPlugin p);
    public String getMessage(String id, Object... placeholders);
    public boolean reloadMessages();
}

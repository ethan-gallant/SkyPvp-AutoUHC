package io.skypvp.uhc.player.event;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

import io.skypvp.uhc.player.UHCPlayer;

public class UHCPlayerDamagedByUHCPlayerEvent extends UHCPlayerDamageEvent {
    
    private final UHCPlayer damager;
    
    public UHCPlayerDamagedByUHCPlayerEvent(UHCPlayer damaged, UHCPlayer damager, EntityDamageByEntityEvent evt) {
        super(damaged, evt);
        this.damager = damager;
    }
    
    public UHCPlayer getDamager() {
        return this.damager;
    }
}

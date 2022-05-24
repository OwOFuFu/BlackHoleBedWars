/*
 * BlackHoleBedWars
 * Copyright (c) 2022. YumaHisai
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.yumahisai.blholebw.arena.despawnables;

import com.yumahisai.blholebw.BedWars;
import com.yumahisai.blholebw.api.arena.GameState;
import com.yumahisai.blholebw.api.arena.IArena;
import com.yumahisai.blholebw.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class TargetListener implements Listener {

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent e){
        if (!(e.getTarget() instanceof Player)) return;
        IArena arena = Arena.getArenaByIdentifier(e.getEntity().getWorld().getName());
        Player p = (Player) e.getTarget();
        if (arena == null) return;
        if (!arena.isPlayer(p)) {
            e.setCancelled(true);
            return;
        }
        if (arena.getStatus() != GameState.playing){
            e.setCancelled(true);
            return;
        }
        if (BedWars.nms.isDespawnable(e.getEntity())){
            if (arena.getTeam(p) == BedWars.nms.getDespawnablesList().get(e.getEntity().getUniqueId()).getTeam()){
                e.setCancelled(true);
            }
        }
    }
}

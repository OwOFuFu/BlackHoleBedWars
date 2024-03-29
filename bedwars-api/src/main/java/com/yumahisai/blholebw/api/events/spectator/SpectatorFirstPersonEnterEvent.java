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

package com.yumahisai.blholebw.api.events.spectator;

import com.yumahisai.blholebw.api.arena.IArena;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class SpectatorFirstPersonEnterEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player spectator;
    private final Player target;
    private final IArena arena;
    private boolean cancelled = false;
    private Function<Player, String> title;
    private Function<Player, String> subTitle;
    private int fadeIn = 0;
    private int stay = 40;
    private int fadeOut = 10;

    // A list of all players spectating in first person
    private static List<UUID> spectatingInFirstPerson = new ArrayList<>();

    /**
     * Called when a spectator enters the first person spectating system.
     */
    public SpectatorFirstPersonEnterEvent(@NotNull Player spectator, @NotNull Player target, IArena arena, Function<Player, String> title, Function<Player, String> subtitle) {
        this.spectator = spectator;
        this.target = target;
        this.arena = arena;
        this.title = title;
        this.subTitle = subtitle;
        if (!spectatingInFirstPerson.contains(spectator.getUniqueId()))
            spectatingInFirstPerson.add(spectator.getUniqueId());
    }

    /**
     * Get the spectator
     */
    public Player getSpectator() {
        return spectator;
    }

    /**
     * Get the arena
     */
    public IArena getArena() {
        return arena;
    }

    /**
     * Get the target player
     */
    public Player getTarget() {
        return target;
    }

    /**
     * Check if it is cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Change value
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Function<Player, String> getSubTitle() {
        return subTitle;
    }

    /**
     * Get first person enter subtitle
     */


    public Function<Player, String> getTitle() {
        return title;
    }

    /**
     * Get first person enter title
     */
    public void setTitle(Function<Player, String> title) {
        this.title = title;
    }

    /**
     * Set first person enter title and subtitle. Leave "" for empty msg
     */
    public void setSubTitle(Function<Player, String> subTitle) {
        this.subTitle = subTitle;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public void setStay(int stay) {
        if (stay < 0) return;
        this.stay = stay;
    }

    public void setFadeOut(int fadeOut) {
        if (fadeOut < 0) return;
        this.fadeOut = fadeOut;
    }

    public void setFadeIn(int fadeIn) {
        if (fadeIn < 0) return;
        this.fadeIn = fadeIn;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

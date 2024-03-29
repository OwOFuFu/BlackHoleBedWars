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

package com.yumahisai.blholebw.commands.bedwars.subcmds.sensitive.setup;

import com.yumahisai.blholebw.BedWars;
import com.yumahisai.blholebw.api.command.ParentCommand;
import com.yumahisai.blholebw.api.command.SubCommand;
import com.yumahisai.blholebw.api.configuration.ConfigPath;
import com.yumahisai.blholebw.api.server.SetupType;
import com.yumahisai.blholebw.arena.Misc;
import com.yumahisai.blholebw.arena.SetupSession;
import com.yumahisai.blholebw.configuration.Permissions;
import com.yumahisai.blholebw.configuration.Sounds;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SetBed extends SubCommand {

    public SetBed(ParentCommand parent, String name) {
        super(parent, name);
        setArenaSetupCommand(true);
        setPermission(Permissions.PERMISSION_SETUP_ARENA);
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        SetupSession ss = SetupSession.getSession(p.getUniqueId());
        if (ss == null) {
            //s.sendMessage("§c ▪ §7You're not in a setup session!");
            return false;
        }
        if (args.length == 0) {
            String foundTeam = ss.getNearestTeam();
            if (foundTeam.isEmpty()) {
                p.sendMessage("");
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "Could not find any nearby team.");
                p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Make sure you set the team's spawn first!", ChatColor.LIGHT_PURPLE + "Set a team bed.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));
                p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Or if you set the spawn and it wasn't found automatically try using: /blbw " + getSubCommandName() + " <team>", "Add a team bed.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));
                BedWars.nms.sendTitle(p, " ", ChatColor.RED + "Could not find any nearby team.", 5, 60, 5);
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p);
                ss.displayAvailableTeams();

            } else {
                Bukkit.dispatchCommand(s, getParent().getName() + " " + getSubCommandName() + " " + foundTeam);
            }
        } else {
            if (!(BedWars.nms.isBed(p.getLocation().clone().add(0, -0.5, 0).getBlock().getType()) || BedWars.nms.isBed(p.getLocation().clone().add(0, 0.5, 0).getBlock().getType())
                    || BedWars.nms.isBed(p.getLocation().clone().getBlock().getType()))) {
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "You must stay on a bed while using this command!");
                BedWars.nms.sendTitle(p, " ", ChatColor.RED + "You must stay on a bed.", 5, 40, 5);
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p);
                return true;
            }
            if (ss.getConfig().getYml().get("Team." + args[0]) == null) {
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "This team doesn't exist!");
                if (ss.getConfig().getYml().get("Team") != null) {
                    p.sendMessage(ss.getPrefix() + "Available teams: ");
                    for (String team : Objects.requireNonNull(ss.getConfig().getYml().getConfigurationSection("Team")).getKeys(false)) {
                        p.spigot().sendMessage(Misc.msgHoverClick(ChatColor.LIGHT_PURPLE + " " + '▪' + " " + ss.getTeamColor(team) + team, ChatColor.LIGHT_PURPLE + "Set bed for " + ss.getTeamColor(team) + team, "/" + BedWars.mainCmd + " setBed " + team, ClickEvent.Action.RUN_COMMAND));
                    }
                }
            } else {
                String team = ss.getTeamColor(args[0]) + args[0];
                if (ss.getConfig().getYml().get("Team." + args[0] + ".Bed") != null) {
                    com.yumahisai.blholebw.commands.Misc.removeArmorStand("bed", ss.getConfig().getArenaLoc("Team." + args[0] + ".Bed"), null);
                }
                com.yumahisai.blholebw.commands.Misc.createArmorStand(team + " " + ChatColor.LIGHT_PURPLE + "BED SET", p.getLocation().add(0.5, 0, 0.5), null);
                ss.getConfig().saveArenaLoc("Team." + args[0] + ".Bed", p.getLocation());
                p.sendMessage(ss.getPrefix() + "Bed set for: " + team);

                BedWars.nms.sendTitle(p, " ", ChatColor.GREEN + "Bed set for: " + team, 5, 40, 5);
                Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p);

                if (ss.getSetupType() == SetupType.ASSISTED) {
                    Bukkit.dispatchCommand(p, getParent().getName());
                }
            }
        }
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return new ArrayList<>();
    }

    @Override
    public boolean canSee(CommandSender s, com.yumahisai.blholebw.api.BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (!SetupSession.isInSetupSession(p.getUniqueId())) return false;

        return hasPermission(s);
    }
}

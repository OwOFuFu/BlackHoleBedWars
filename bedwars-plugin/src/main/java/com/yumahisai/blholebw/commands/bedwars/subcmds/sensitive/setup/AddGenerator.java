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

import com.yumahisai.blholebw.api.BedWars;
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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddGenerator extends SubCommand {

    public AddGenerator(ParentCommand parent, String name) {
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
            //s.sendMessage(ChatColor.RED + "You're not in a setup session!");
            return false;
        }

        if (args.length == 0 && ss.getSetupType() == SetupType.ASSISTED) {
            String team = ss.getNearestTeam();
            if (team.isEmpty()) {
                // save emerald or diamond generator if is standing on a block of this type
                if (p.getLocation().add(0, -1, 0).getBlock().getType() == Material.DIAMOND_BLOCK) {
                    Bukkit.dispatchCommand(p, getParent().getName() + " " + getSubCommandName() + " diamond");
                    return true;
                } else if (p.getLocation().add(0, -1, 0).getBlock().getType() == Material.EMERALD_BLOCK) {
                    Bukkit.dispatchCommand(p, getParent().getName() + " " + getSubCommandName() + " emerald");
                    return true;
                }

                // else send usage message
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "Could not find any nearby team.");
                p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Make sure you set the team's spawn first!", ChatColor.LIGHT_PURPLE + "Set a team spawn.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));
                p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Or if you set the spawn and it wasn't found automatically try using: /blbw addGenerator <team>", "Add a team generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));
                p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "Other use: /blbw addGenerator <emerald/ diamond>", "Add an emerald/ diamond generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));
                com.yumahisai.blholebw.BedWars.nms.sendTitle(p, " ", ChatColor.RED + "Could not find any nearby team.", 5, 60, 5);
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p);
                return true;
            }
            // save team generators
            saveTeamGen(p.getLocation(), team, ss, "Iron");
            saveTeamGen(p.getLocation(), team, ss, "Gold");
            saveTeamGen(p.getLocation(), team, ss, "Emerald");

            com.yumahisai.blholebw.commands.Misc.createArmorStand(ChatColor.DARK_PURPLE + "Generator set for team: " + ss.getTeamColor(team) + team, p.getLocation(), ss.getConfig().stringLocationArenaFormat(p.getLocation()));
            p.sendMessage(ss.getPrefix() + "Generator set for team: " + ss.getTeamColor(team) + team);

            Bukkit.dispatchCommand(p, getParent().getName());

            com.yumahisai.blholebw.BedWars.nms.sendTitle(p, " ", ChatColor.GREEN + "Generator set for team: " + ss.getTeamColor(team) + team, 5, 60, 5);
            Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p);
            return true;
        } else if (args.length == 1 && (args[0].equalsIgnoreCase("diamond") || args[0].equalsIgnoreCase("emerald"))) {
            // add emerald or diamond generator to the list if it was not added yet
            List<Location> locations = ss.getConfig().getArenaLocations("generator." + args[0].substring(0, 1).toUpperCase() + args[0].substring(1).toLowerCase());
            for (Location l : locations) {
                if (ss.getConfig().compareArenaLoc(l, p.getLocation())) {
                    p.sendMessage(ss.getPrefix() + ChatColor.RED + "This generator was already set!");
                    com.yumahisai.blholebw.BedWars.nms.sendTitle(p, " ", ChatColor.RED + "This generator was already set!", 5, 30, 5);
                    Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p);
                    return true;
                }
            }
            String gen = args[0].substring(0, 1).toUpperCase() + args[0].substring(1).toLowerCase();

            ArrayList<String> saved;
            if (ss.getConfig().getYml().get("generator." + gen) == null) {
                saved = new ArrayList<>();
            } else {
                saved = (ArrayList<String>) ss.getConfig().getYml().getStringList("generator." + gen);
            }
            saved.add(ss.getConfig().stringLocationArenaFormat(p.getLocation()));

            ss.getConfig().set("generator." + gen, saved);
            p.sendMessage(ss.getPrefix() + gen + " generator was added!");
            com.yumahisai.blholebw.commands.Misc.createArmorStand(ChatColor.DARK_PURPLE + gen + " SET", p.getLocation(), ss.getConfig().stringLocationArenaFormat(p.getLocation()));
            if (ss.getSetupType() == SetupType.ASSISTED) {
                Bukkit.dispatchCommand(p, getParent().getName());
            }
            com.yumahisai.blholebw.BedWars.nms.sendTitle(p, " ", ChatColor.DARK_PURPLE + gen + ChatColor.GREEN + " generator added!", 5, 60, 5);
            Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p);
            return true;
        } else if (args.length >= 1 && (args[0].equalsIgnoreCase("iron") || args[0].equalsIgnoreCase("gold") || args[0].equalsIgnoreCase("upgrade")) && ss.getSetupType() == SetupType.ADVANCED) {
            String team;
            if (args.length == 1) {
                team = ss.getNearestTeam();
            } else {
                team = args[1];
                if (ss.getConfig().getYml().get("Team." + team + ".Color") == null) {
                    p.sendMessage(ss.getPrefix() + ChatColor.RED + "Could not find team: " + team);
                    p.sendMessage(ss.getPrefix() + "Use: /blbw createTeam if you want to create one.");
                    ss.displayAvailableTeams();
                    com.yumahisai.blholebw.BedWars.nms.sendTitle(p, " ", ChatColor.RED + "Could not find any nearby team.", 5, 60, 5);
                    Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p);
                    return true;
                }
            }
            // find nearest team to set the generator else send usage msg
            if (team.isEmpty()) {
                p.sendMessage(ss.getPrefix() + ChatColor.RED + "Could not find any nearby team.");
                p.sendMessage(ss.getPrefix() + "Try using: /blbw addGenerator <iron/ gold/ upgrade> <team>");
                return true;
            }

            String gen = args[0].substring(0, 1).toUpperCase() + args[0].substring(1).toLowerCase();
            if (gen.equalsIgnoreCase("upgrade")) {
                gen = "Emerald";
            }

            com.yumahisai.blholebw.commands.Misc.createArmorStand(ChatColor.DARK_PURPLE + gen + " generator added for team: " + ss.getTeamColor(team) + team, p.getLocation(), ss.getConfig().stringLocationArenaFormat(p.getLocation()));
            p.sendMessage(ss.getPrefix() + gen + " generator added for team: " + ss.getTeamColor(team) + team);
            saveTeamGen(p.getLocation(), team, ss, gen);
            com.yumahisai.blholebw.BedWars.nms.sendTitle(p, " ", ChatColor.DARK_PURPLE + gen + ChatColor.GREEN + " generator for " + ss.getTeamColor(team) + team + ChatColor.GREEN + " was added!", 5, 60, 5);
            Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p);
            return true;
        } else if (args.length == 1 && ss.getSetupType() == SetupType.ASSISTED) {
            String team = args[0];
            if (ss.getConfig().getYml().get("Team." + team + ".Color") == null) {
                p.sendMessage(ss.getPrefix() + "Could not find team: " + ChatColor.RED + team);
                p.sendMessage(ss.getPrefix() + "Use: /blbw createTeam if you want to create one.");
                ss.displayAvailableTeams();
                com.yumahisai.blholebw.BedWars.nms.sendTitle(p, " ", "Could not find team: " + ChatColor.RED + team, 5, 40, 5);
                Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, p);
                return true;
            }

            saveTeamGen(p.getLocation(), team, ss, "Iron");
            saveTeamGen(p.getLocation(), team, ss, "Gold");
            saveTeamGen(p.getLocation(), team, ss, "Emerald");
            com.yumahisai.blholebw.commands.Misc.createArmorStand(ChatColor.DARK_PURPLE + "Generator set for team: " + ss.getTeamColor(team) + team, p.getLocation(), ss.getConfig().stringLocationArenaFormat(p.getLocation()));
            p.sendMessage(ss.getPrefix() + "Generator set for team: " + ss.getTeamColor(team) + team);
            Bukkit.dispatchCommand(p, getParent().getName());

            com.yumahisai.blholebw.BedWars.nms.sendTitle(p, " ", ChatColor.GREEN + "Generator set for team: " + ss.getTeamColor(team) + team, 5, 60, 5);
            Sounds.playSound(ConfigPath.SOUNDS_BOUGHT, p);
            return true;
        }
        if (ss.getSetupType() == SetupType.ASSISTED) {
            p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "/blbw addGenerator (detect team automatically)", "Add a team generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));
            p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "/blbw addGenerator <team>", "Add a team generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));

        }
        if (ss.getSetupType() == SetupType.ADVANCED) {
            p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "/blbw addGenerator <iron/ gold/ upgrade>", "Add a team generator.\nThe team will be detected automatically.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));

            p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "/blbw addGenerator <iron/ gold/ upgrade> <team>", "Add a team generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));
        }
        p.spigot().sendMessage(Misc.msgHoverClick(ss.getPrefix() + "/blbw addGenerator <emerald/ diamond>", "Add an emerald/ diamond generator.", "/" + getParent().getName() + " " + getSubCommandName() + " ", ClickEvent.Action.SUGGEST_COMMAND));
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return Arrays.asList("Diamond", "Emerald", "Iron", "Gold", "Upgrade");
    }

    @Override
    public boolean canSee(CommandSender s, BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (!SetupSession.isInSetupSession(p.getUniqueId())) return false;

        return hasPermission(s);
    }

    /**
     * Save team generator.
     *
     * @param l    location.
     * @param t    team.
     * @param ss   setup session.
     * @param type Iron/ Gold.
     */
    private static void saveTeamGen(Location l, String t, @NotNull SetupSession ss, String type) {
        Object o = ss.getConfig().getYml().get("Team." + t + "." + type);
        List<String> locs;
        if (o == null) {
            locs = new ArrayList<>();
        } else if (o instanceof String) {
            locs = new ArrayList<>();
            locs.add((String) o);
        } else {
            locs = ss.getConfig().getList("Team." + t + "." + type);
        }

        locs.add(ss.getConfig().stringLocationArenaFormat(l));
        ss.getConfig().set("Team." + t + "." + type, locs);
    }
}

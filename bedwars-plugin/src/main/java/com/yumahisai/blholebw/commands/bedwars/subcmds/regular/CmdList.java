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

package com.yumahisai.blholebw.commands.bedwars.subcmds.regular;

import com.yumahisai.blholebw.api.BedWars;
import com.yumahisai.blholebw.api.arena.team.TeamColor;
import com.yumahisai.blholebw.api.command.ParentCommand;
import com.yumahisai.blholebw.api.command.SubCommand;
import com.yumahisai.blholebw.api.configuration.ConfigPath;
import com.yumahisai.blholebw.api.language.Messages;
import com.yumahisai.blholebw.api.server.SetupType;
import com.yumahisai.blholebw.arena.Arena;
import com.yumahisai.blholebw.arena.Misc;
import com.yumahisai.blholebw.arena.SetupSession;
import com.yumahisai.blholebw.commands.bedwars.MainCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

import static com.yumahisai.blholebw.api.language.Language.getList;

public class CmdList extends SubCommand {

    public CmdList(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(11);
        showInList(true);
        setDisplayInfo(Misc.msgHoverClick("§d ▪ §7/" + MainCommand.getInstance().getName() + " " + getSubCommandName() + "         §8 - §d view player cmds", "§fView player commands.", "/" + getParent().getName() + " " + getSubCommandName(), ClickEvent.Action.RUN_COMMAND));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        if (SetupSession.isInSetupSession(p.getUniqueId())) {
            SetupSession ss = SetupSession.getSession(p.getUniqueId());
            Objects.requireNonNull(ss).getConfig().reload();

            boolean waitingSpawn = ss.getConfig().getYml().get("waiting.Loc") != null,
                    pos1 = ss.getConfig().getYml().get("waiting.Pos1") != null,
                    pos2 = ss.getConfig().getYml().get("waiting.Pos2") != null,
                    pos = pos1 && pos2;
            StringBuilder spawnNotSetNames = new StringBuilder();
            StringBuilder bedNotSet = new StringBuilder();
            StringBuilder shopNotSet = new StringBuilder();
            StringBuilder killDropsNotSet = new StringBuilder();
            StringBuilder upgradeNotSet = new StringBuilder();
            StringBuilder spawnNotSet = new StringBuilder();
            StringBuilder generatorNotSet = new StringBuilder();
            int teams = 0;

            if (ss.getConfig().getYml().get("Team") != null) {
                for (String team : ss.getConfig().getYml().getConfigurationSection("Team").getKeys(true)) {
                    if (ss.getConfig().getYml().get("Team." + team + ".Color") == null) continue;
                    ChatColor color = TeamColor.getChatColor(ss.getConfig().getYml().getString("Team." + team + ".Color"));
                    if (ss.getConfig().getYml().get("Team." + team + ".Spawn") == null) {
                        spawnNotSet.append(color).append("▋");
                        spawnNotSetNames.append(color).append(team).append(" ");
                    }
                    if (ss.getConfig().getYml().get("Team." + team + ".Bed") == null) {
                        bedNotSet.append(color).append("▋");
                    }
                    if (ss.getConfig().getYml().get("Team." + team + ".Shop") == null) {
                        shopNotSet.append(color).append("▋");
                    }
                    if (ss.getConfig().getYml().get("Team." + team + "." + ConfigPath.ARENA_TEAM_KILL_DROPS_LOC) == null) {
                        killDropsNotSet.append(color).append("▋");
                    }
                    if (ss.getConfig().getYml().get("Team." + team + ".Upgrade") == null) {
                        upgradeNotSet.append(color).append("▋");
                    }
                    if (ss.getConfig().getYml().get("Team." + team + ".Iron") == null || ss.getConfig().getYml().get("Team." + team + ".Gold") == null) {
                        generatorNotSet.append(color).append("▋");
                    }
                    teams++;
                }
            }
            int emGen = 0, dmGen = 0;
            if (ss.getConfig().getYml().get("generator.Emerald") != null) {
                emGen = ss.getConfig().getYml().getStringList("generator.Emerald").size();
            }
            if (ss.getConfig().getYml().get("generator.Diamond") != null) {
                dmGen = ss.getConfig().getYml().getStringList("generator.Diamond").size();
            }

            String posMsg, group = ChatColor.RED + "(NOT SET)";
            if (pos1 && !pos2) {
                posMsg = ChatColor.RED + "(POS 2 NOT SET)";
            } else if (!pos1 && pos2) {
                posMsg = ChatColor.RED + "(POS 1 NOT SET)";
            } else if (pos1) {
                posMsg = ChatColor.GREEN + "(SET)";
            } else {
                posMsg = ChatColor.GRAY + "(NOT SET) " + ChatColor.ITALIC + "OPTIONAL";
            }

            String g2 = ss.getConfig().getYml().getString("group");
            if (g2 != null) {
                if (!g2.equalsIgnoreCase("default")) {
                    group = ChatColor.GREEN + "(" + g2 + ")";
                }
            }

            int maxInTeam = ss.getConfig().getInt("maxInTeam");

            String setWaitingSpawn = ss.dot() + (waitingSpawn ? ChatColor.STRIKETHROUGH : "") + "setWaitingSpawn" + ChatColor.RESET + " " + (waitingSpawn ? ChatColor.GREEN + "(SET)" : ChatColor.RED + "(NOT SET)");
            String waitingPos = ss.dot() + (pos ? ChatColor.STRIKETHROUGH : "") + "waitingPos 1/2" + ChatColor.RESET + " " + posMsg;
            String setSpawn = ss.dot() + ((spawnNotSet.length() == 0) ? ChatColor.STRIKETHROUGH : "") + "setSpawn <teamName>" + ChatColor.RESET + " " + ((spawnNotSet.length() == 0) ? ChatColor.GREEN + "(ALL SET)" : ChatColor.RED + "(Remaining: " + spawnNotSet + ChatColor.RED + ")");
            String setBed = ss.dot() + ((bedNotSet.toString().length() == 0) ? ChatColor.STRIKETHROUGH : "") + "setBed" + ChatColor.RESET + " " + ((bedNotSet.length() == 0) ? ChatColor.GREEN + "(ALL SET)" : ChatColor.RED + "(Remaining: " + bedNotSet + ChatColor.RED + ")");
            String setShop = ss.dot() + ((shopNotSet.toString().length() == 0) ? ChatColor.STRIKETHROUGH : "") + "setShop" + ChatColor.RESET + " " + ((shopNotSet.length() == 0) ? ChatColor.GREEN + "(ALL SET)" : ChatColor.RED + "(Remaining: " + shopNotSet + ChatColor.RED + ")");
            String setKillDrops = ss.dot() + ((killDropsNotSet.toString().length() == 0) ? ChatColor.STRIKETHROUGH : "") + "setKillDrops" + ChatColor.RESET + " " + ((shopNotSet.length() == 0) ? ChatColor.GREEN + "(ALL SET)" : ChatColor.RED + "(Remaining: " + killDropsNotSet + ChatColor.RED + ")");
            String setUpgrade = ss.dot() + ((upgradeNotSet.toString().length() == 0) ? ChatColor.STRIKETHROUGH : "") + "setUpgrade" + ChatColor.RESET + " " + ((upgradeNotSet.length() == 0) ? ChatColor.GREEN + "(ALL SET)" : ChatColor.RED + "(Remaining: " + upgradeNotSet + ChatColor.RED + ")");
            String addGenerator = ss.dot() + "addGenerator " + ((generatorNotSet.toString().length() == 0) ? "" : ChatColor.RED + "(Remaining: " + generatorNotSet + ChatColor.RED + ") ") + ChatColor.YELLOW + "(" + ChatColor.DARK_GREEN + "E" + emGen + " " + ChatColor.AQUA + "D" + dmGen + ChatColor.YELLOW + ")";
            String setSpectatorSpawn = ss.dot() + (ss.getConfig().getYml().get(ConfigPath.ARENA_SPEC_LOC) == null ? "" : ChatColor.STRIKETHROUGH) + "setSpectSpawn" + ChatColor.RESET + " " + (ss.getConfig().getYml().get(ConfigPath.ARENA_SPEC_LOC) == null ? ChatColor.RED + "(NOT SET)" : ChatColor.GRAY + "(SET)");

            s.sendMessage("");
            s.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + MainCommand.getDot() + ChatColor.DARK_PURPLE + com.yumahisai.blholebw.BedWars.plugin.getDescription().getName() + " v" + com.yumahisai.blholebw.BedWars.plugin.getDescription().getVersion() + ChatColor.GRAY + '-' + " " + ChatColor.GREEN + ss.getWorldName() + " commands");
            p.spigot().sendMessage(Misc.msgHoverClick(setWaitingSpawn, ChatColor.LIGHT_PURPLE + "Set the place where players have\n" + ChatColor.LIGHT_PURPLE + "to wait before the game starts.", "/" + getParent().getName() + " setWaitingSpawn", ss.getSetupType() == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND));
            p.spigot().sendMessage(Misc.msgHoverClick(waitingPos, ChatColor.LIGHT_PURPLE + "Make it so the waiting lobby will disappear at start.\n" + ChatColor.LIGHT_PURPLE + "Select it as a world edit region.", "/" + getParent().getName() + " waitingPos ", ClickEvent.Action.SUGGEST_COMMAND));
            if (ss.getSetupType() == SetupType.ADVANCED) {
                p.spigot().sendMessage(Misc.msgHoverClick(setSpectatorSpawn, ChatColor.LIGHT_PURPLE + "Set where to spawn spectators.", "/" + getParent().getName() + " setSpectSpawn", ClickEvent.Action.RUN_COMMAND));
            }
            p.spigot().sendMessage(Misc.msgHoverClick(ss.dot() + "autoCreateTeams " + ChatColor.DARK_PURPLE + "(auto detect)", ChatColor.LIGHT_PURPLE + "Create teams based on islands colors.", "/" + getParent().getName() + " autoCreateTeams", ClickEvent.Action.SUGGEST_COMMAND));
            p.spigot().sendMessage(Misc.msgHoverClick(ss.dot() + "createTeam <name> <color> " + ChatColor.DARK_PURPLE + "(" + teams + " CREATED)", ChatColor.LIGHT_PURPLE + "Create a team.", "/" + getParent().getName() + " createTeam ", ClickEvent.Action.SUGGEST_COMMAND));
            p.spigot().sendMessage(Misc.msgHoverClick(ss.dot() + "removeTeam <name>", ChatColor.LIGHT_PURPLE + "Remove a team by name.", "/" + com.yumahisai.blholebw.BedWars.mainCmd + " removeTeam ", ClickEvent.Action.SUGGEST_COMMAND));


            p.spigot().sendMessage(Misc.msgHoverClick(setSpawn, ChatColor.LIGHT_PURPLE + "Set a team spawn.\n" + ChatColor.LIGHT_PURPLE + "Teams without a spawn set:\n" + spawnNotSetNames.toString(), "/" + getParent().getName() + " setSpawn ", ClickEvent.Action.SUGGEST_COMMAND));
            p.spigot().sendMessage(Misc.msgHoverClick(setBed, ChatColor.LIGHT_PURPLE + "Set a team's bed location.\n" + ChatColor.LIGHT_PURPLE + "You don't have to specify the team name.", "/" + getParent().getName() + " setBed", ss.getSetupType() == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND));
            p.spigot().sendMessage(Misc.msgHoverClick(setShop, ChatColor.LIGHT_PURPLE + "Set a team's NPC.\n" + ChatColor.LIGHT_PURPLE + "You don't have to specify the team name.\n" + ChatColor.LIGHT_PURPLE + "It will be spawned only when the game starts.", "/" + getParent().getName() + " setShop", ss.getSetupType() == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND));
            p.spigot().sendMessage(Misc.msgHoverClick(setUpgrade, ChatColor.LIGHT_PURPLE + "Set a team's upgrade NPC.\n" + ChatColor.LIGHT_PURPLE + "You don't have to specify the team name.\n" + ChatColor.LIGHT_PURPLE + "It will be spawned only when the game starts.", "/" + getParent().getName() + " setUpgrade", ss.getSetupType() == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND));
            if (ss.getSetupType() == SetupType.ADVANCED) {
                p.spigot().sendMessage(Misc.msgHoverClick(setKillDrops, ChatColor.LIGHT_PURPLE + "Set a the location where to drop\n" + ChatColor.LIGHT_PURPLE + "enemy items after you kill them.", "/" + getParent().getName() + " setKillDrops ", ClickEvent.Action.SUGGEST_COMMAND));
            }
            String genHover = (ss.getSetupType() == SetupType.ADVANCED ? ChatColor.LIGHT_PURPLE + "Add a generator spawn point.\n" + ChatColor.DARK_PURPLE + "/" + getParent().getName() + " addGenerator <Iron/ Gold/ Emerald, Diamond>" :
                    ChatColor.LIGHT_PURPLE + "Add a generator spawn point.\n" + ChatColor.DARK_PURPLE + "Stay in on a team island to set a team generator") + "\n" + ChatColor.LIGHT_PURPLE + "Stay on a diamond block to set the diamond generator.\n" + ChatColor.LIGHT_PURPLE + "Stay on a emerald block to set an emerald generator.";

            p.spigot().sendMessage(Misc.msgHoverClick(addGenerator, genHover, "/" + getParent().getName() + " addGenerator ", ss.getSetupType() == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND));
            p.spigot().sendMessage(Misc.msgHoverClick(ss.dot() + "removeGenerator", genHover, "/" + getParent().getName() + " removeGenerator", ss.getSetupType() == SetupType.ASSISTED ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND));

            if (ss.getSetupType() == SetupType.ADVANCED) {
                p.spigot().sendMessage(Misc.msgHoverClick(ss.dot() + "setMaxInTeam <int> (IS SET TO " + maxInTeam + ")", ChatColor.LIGHT_PURPLE + "Set the max team size.", "/" + com.yumahisai.blholebw.BedWars.mainCmd + " setMaxInTeam ", ClickEvent.Action.SUGGEST_COMMAND));
                p.spigot().sendMessage(Misc.msgHoverClick(ss.dot() + "arenaGroup " + group, ChatColor.LIGHT_PURPLE + "Set the arena group.", "/" + com.yumahisai.blholebw.BedWars.mainCmd + " arenaGroup ", ClickEvent.Action.SUGGEST_COMMAND));
            } else {
                p.spigot().sendMessage(Misc.msgHoverClick(ss.dot() + "setType <type> " + group, ChatColor.LIGHT_PURPLE + "Add the arena to a group.", "/" + getParent().getName() + " setType", ClickEvent.Action.RUN_COMMAND));
            }

            p.spigot().sendMessage(Misc.msgHoverClick(ss.dot() + "save", ChatColor.LIGHT_PURPLE + "Save arena and go back to lobby", "/" + getParent().getName() + " save", ClickEvent.Action.SUGGEST_COMMAND));
        } else {
            TextComponent credits = new TextComponent(ChatColor.BLUE + "" + ChatColor.BOLD + MainCommand.getDot() + " " + ChatColor.DARK_PURPLE + com.yumahisai.blholebw.BedWars.plugin.getName() + " " + ChatColor.GRAY + "v" + com.yumahisai.blholebw.BedWars.plugin.getDescription().getVersion() + " by YumaHisai");
            credits.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, com.yumahisai.blholebw.BedWars.link));
            credits.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Arenas: " + (Arena.getArenas().size() == 0 ? ChatColor.RED + "0" : ChatColor.GREEN + "" + Arena.getArenas().size())).create()));
            ((Player) s).spigot().sendMessage(credits);
            for (String string : getList((Player) s, Messages.COMMAND_MAIN)) {
                s.sendMessage(string);
            }
        }
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, BedWars api) {

        if (s instanceof Player) {
            Player p = (Player) s;
            if (Arena.isInArena(p)) return false;

            if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        }

        return hasPermission(s);
    }
}

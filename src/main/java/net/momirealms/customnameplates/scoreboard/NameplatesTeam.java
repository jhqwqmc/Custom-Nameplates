/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customnameplates.scoreboard;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.momirealms.customnameplates.ConfigManager;
import net.momirealms.customnameplates.CustomNameplates;
import net.momirealms.customnameplates.data.DataManager;
import net.momirealms.customnameplates.data.PlayerData;
import net.momirealms.customnameplates.font.FontCache;
import net.momirealms.customnameplates.hook.ParsePapi;
import net.momirealms.customnameplates.nameplates.NameplateUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Optional;

public class NameplatesTeam {

    private final CustomNameplates plugin;
    private final Player player;
    private final Team team;
    private Component prefix;
    private Component suffix;
    private String prefixText;
    private String suffixText;
    private ChatColor color;

    public Component getPrefix() {return this.prefix;}
    public Component getSuffix() {return this.suffix;}
    public ChatColor getColor() {return this.color;}
    public String getPrefixText() {return prefixText;}
    public String getSuffixText() {return suffixText;}

    public NameplatesTeam(CustomNameplates plugin, Player player) {
        this.color = ChatColor.WHITE;
        this.plugin = plugin;
        this.player = player;
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String name = player.getName();
        this.team = Optional.ofNullable(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name)).orElseGet(() -> scoreboard.registerNewTeam(name));
        team.addEntry(player.getName());
    }

    public void updateNameplates() {
        Optional<PlayerData> playerData = Optional.ofNullable(this.plugin.getDataManager().getOrCreate(this.player.getUniqueId()));
        String nameplate;
        if (playerData.isPresent()){
            nameplate = playerData.get().getEquippedNameplate();
        }else {
            nameplate = "none";
        }
        if (nameplate.equals("none")) {
            if (ConfigManager.MainConfig.placeholderAPI) {
                this.prefix = Component.text(ParsePapi.parsePlaceholders(this.player, ConfigManager.MainConfig.player_prefix));
                this.suffix = Component.text(ParsePapi.parsePlaceholders(this.player, ConfigManager.MainConfig.player_suffix));
                this.prefixText = ParsePapi.parsePlaceholders(this.player, ConfigManager.MainConfig.player_prefix);
                this.suffixText = ParsePapi.parsePlaceholders(this.player, ConfigManager.MainConfig.player_suffix);
            }else {
                this.prefix = Component.text(ConfigManager.MainConfig.player_prefix);
                this.suffix = Component.text(ConfigManager.MainConfig.player_suffix);
                this.prefixText = ConfigManager.MainConfig.player_prefix;
                this.suffixText = ConfigManager.MainConfig.player_suffix;
            }
            this.color = ChatColor.WHITE;
            this.team.setPrefix("");
            return;
        }
        FontCache fontCache = this.plugin.getResourceManager().getNameplateInfo(nameplate);
        if (fontCache == null){
            this.prefix = Component.text("");
            this.suffix = Component.text("");
            this.color = ChatColor.WHITE;
            this.team.setPrefix("");
            DataManager.cache.get(player.getUniqueId()).equipNameplate("none");
            return;
        }
        NameplateUtil nameplateUtil = new NameplateUtil(fontCache);
        String name = this.player.getName();
        String playerPrefix;
        String playerSuffix;
        if (ConfigManager.MainConfig.placeholderAPI) {
            if (!ConfigManager.MainConfig.hidePrefix){
                playerPrefix = ParsePapi.parsePlaceholders(this.player, ConfigManager.MainConfig.player_prefix);
            }else {
                playerPrefix = "";
            }
            if (!ConfigManager.MainConfig.hideSuffix){
                playerSuffix = ParsePapi.parsePlaceholders(this.player, ConfigManager.MainConfig.player_suffix);
            }else {
                playerSuffix = "";
            }
        }else {
            if (!ConfigManager.MainConfig.hidePrefix){
                playerPrefix = ConfigManager.MainConfig.player_prefix;
            }else {
                playerPrefix = "";
            }
            if (!ConfigManager.MainConfig.hideSuffix){
                playerSuffix = ConfigManager.MainConfig.player_suffix;
            }else {
                playerSuffix = "";
            }
        }
        this.prefixText = nameplateUtil.makeCustomNameplate(MiniMessage.miniMessage().stripTags(playerPrefix), name, MiniMessage.miniMessage().stripTags(playerSuffix));
        this.suffixText = nameplateUtil.getSuffixLength(MiniMessage.miniMessage().stripTags(playerPrefix) + name + MiniMessage.miniMessage().stripTags(playerSuffix));
        this.prefix = Component.text(nameplateUtil.makeCustomNameplate(MiniMessage.miniMessage().stripTags(playerPrefix), name, MiniMessage.miniMessage().stripTags(playerSuffix))).font(ConfigManager.MainConfig.key).append(MiniMessage.miniMessage().deserialize(playerPrefix));
        this.suffix = MiniMessage.miniMessage().deserialize(playerSuffix).append(Component.text(nameplateUtil.getSuffixLength(MiniMessage.miniMessage().stripTags(playerPrefix) + name + MiniMessage.miniMessage().stripTags(playerSuffix))).font(ConfigManager.MainConfig.key));
//        this.prefixText = nameplateUtil.makeCustomNameplate(playerPrefix, name, playerSuffix) + playerPrefix;
//        this.suffixText = playerSuffix + nameplateUtil.getSuffixLength(playerPrefix + name + playerSuffix);
//        this.prefix = Component.text(nameplateUtil.makeCustomNameplate(playerPrefix, name, playerSuffix)).font(ConfigManager.MainConfig.key).append(Component.text(playerPrefix).font(Key.key("default")));
//        this.suffix = Component.text(playerSuffix).append(Component.text(nameplateUtil.getSuffixLength(playerPrefix + name + playerSuffix)).font(ConfigManager.MainConfig.key));
        this.color = nameplateUtil.getColor();
        this.team.setPrefix("");
    }
}
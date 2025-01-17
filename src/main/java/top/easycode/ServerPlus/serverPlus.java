package top.easycode.ServerPlus;

import it.unimi.dsi.fastutil.Hash;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class serverPlus extends JavaPlugin implements Listener {
    public HashMap<UUID, Boolean> logged = new HashMap<UUID, Boolean>();
    public HashMap<UUID, UUID> TpaPlayers = new HashMap<UUID, UUID>();
    public HashMap<UUID, String> TpaState = new HashMap<UUID, String>();
    @Override
    public void onEnable() {
        // Plugin startup logic
        try {Files.createDirectories(Paths.get("plugins/ServerPlus"));} catch (IOException ignored) {}
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!(logged.get(uuid))) {
            event.setCancelled(true);
            player.sendMessage("§c§l请先登录或注册§r");
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws IOException {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        logged.put(uuid, false);
        player.sendMessage("§a§l欢迎来到"+getServer().getName()+"§r");
        Files.createDirectories(Paths.get("plugins/ServerPlus/"+uuid));
        try {
            Files.createFile(Paths.get("plugins/ServerPlus/" + uuid + "/data.yml"));
            Files.createFile(Paths.get("plugins/ServerPlus/" + uuid + "/config.yml"));
        }catch (FileAlreadyExistsException ignored){}
        String password = YamlConfiguration.loadConfiguration(new File("plugins/ServerPlus/password.yml")).getString(String.valueOf(uuid));
        if (password == null) {
            player.sendMessage("§2§l请注册：/register <密码> <确认密码>§r");
        }
        else {
            player.sendMessage("§2§l请登录：/login <密码>§r");
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (command.getName().equalsIgnoreCase("register")) {
            if (args.length != 2) {
                player.sendMessage("§c§l命令格式错误§r");
                player.sendMessage("§2用法：/register <密码> <确认密码>§r");
                return false;
            }
            if (!(args[0].equals(args[1]))) {
                player.sendMessage("§c§l两次输入的密码不一致§r");
                return false;
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/ServerPlus/password.yml"));
            config.set(String.valueOf(player.getUniqueId()), args[0]);
            try {config.save(new File("plugins/ServerPlus/password.yml"));}catch (IOException ignored){}
            player.sendMessage("§2§l注册成功§r");
            logged.put(player.getUniqueId(), true);
            return true;
        }
        if (command.getName().equalsIgnoreCase("login")) {
            if (args.length != 1) {
                player.sendMessage("§c§l命令格式错误§r");
                player.sendMessage("§2用法：/login <密码>§r");
                return false;
            }
            String password = YamlConfiguration.loadConfiguration(new File("plugins/ServerPlus/password.yml")).getString(String.valueOf(player.getUniqueId()));
            if (password == null) {
                player.sendMessage("§c§l请先注册§r");
                return false;
            }
            if (!(password.equals(args[0]))) {
                player.sendMessage("§c§l密码错误§r");
                return false;
            }
            player.sendMessage("§2§l登录成功§r");
            logged.put(player.getUniqueId(), true);
            return true;
        }
        if (command.getName().equalsIgnoreCase("tpa")) {
            if (args.length == 1) {
                player.sendMessage("§c§l命令格式错误§r");
                player.sendMessage("§2用法：/tpa <玩家>§r");
                return false;
            }
            Player player2 = getServer().getPlayer(args[0]);
            if (player2 == null) {
                sender.sendMessage("§c§l玩家不存在§r");
                return false;
            }
            player2.sendMessage("§3§l"+player.getName() + "§r§b请求传送到你处");
            player2.sendMessage("§a§o输入 /tpaccept 以接受§r");
            player2.sendMessage("§c§o输入 /tpdeny 以拒绝§r");
            player.sendMessage("§2已发送请求§r");
            TpaPlayers.put(player2.getUniqueId(), player.getUniqueId());
            TpaState.put(player2.getUniqueId(), "tpa");
            return true;

        }
        if (command.getName().equalsIgnoreCase("tpahere")) {
            if (args.length != 1) {
                player.sendMessage("§c§l命令格式错误§r");
                player.sendMessage("§2用法：/tpahere <玩家>§r");
                return false;
            }
            Player player2 = getServer().getPlayer(args[0]);
            if (player2 == null) {
                sender.sendMessage("§c§l玩家不存在§r");
                return false;
            }
            player2.sendMessage("§3§l"+player.getName() + "§r§b请求将你传送到他那里");
            player2.sendMessage("§a§o输入 /tpaccept 以接受§r");
            player2.sendMessage("§c§o输入 /tpdeny 以拒绝§r");
            player.sendMessage("§2已发送请求§r");
            TpaPlayers.put(player2.getUniqueId(), player.getUniqueId());
            TpaState.put(player2.getUniqueId(), "tpahere");
            return true;
        }
        if (command.getName().equalsIgnoreCase("tpaccept")) {
            if (TpaPlayers.get(player.getUniqueId()) == null) {
                player.sendMessage("§c§l没有收到任何传送请求§r");
                return false;
            }
            if (getServer().getPlayer(TpaPlayers.get(player.getUniqueId())) == null) {
                player.sendMessage("§c§l对方已离线§r");
                return false;
            }
            if (TpaState.get(player.getUniqueId()).equals("tpa")) {
                getServer().getPlayer(TpaPlayers.get(player.getUniqueId())).teleport(player);
                player.sendMessage("§2§l传送成功§r");
                getServer().getPlayer(TpaPlayers.get(player.getUniqueId())).sendMessage("§2§l传送成功§r");
            }
            if (TpaState.get(player.getUniqueId()).equals("tpahere")) {
                player.teleport(getServer().getPlayer(TpaPlayers.get(player.getUniqueId())));
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("tpdeny")) {
            if (TpaPlayers.get(player.getUniqueId()) == null) {
                player.sendMessage("§c§l没有收到任何传送请求§r");
                return false;
            }
            if (getServer().getPlayer(TpaPlayers.get(player.getUniqueId())) == null) {
                player.sendMessage("§c§l对方已离线§r");
                return false;
            }
            player.sendMessage("§c§l传送请求已拒绝§r");
            getServer().getPlayer(TpaPlayers.get(player.getUniqueId())).sendMessage("§c§l传送请求已被拒绝§r");
        }
        return false;
    }
}

package samann.bwplugin.airwars.items;

import fr.skytasul.guardianbeam.Laser;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.BwPlugin;
import samann.bwplugin.airwars.AirwarsPlayer;

public class Ghost extends Item {
  private static final int ACTIVE_TICKS = 5 * 20;
  private boolean isActive = false;
  private Location startLocation = null;
  private int ticksLeft = 0;
  private final Runnable activeTick = this::activeTick;
  private final Laser.GuardianLaser laser;

  public Ghost(AirwarsPlayer player) {
    super(player, Material.CHAINMAIL_CHESTPLATE, ChatColor.BLUE, "Sicherung",
            "Aktiviere an einer beliebigen Position. Nach Ablauf der Dauer oder durch erneutes Aktivieren teleportierst du dich zu dieser Position zurück.",
            30 * 20);
    if (player != null) {
      try {
        laser = new Laser.GuardianLaser(player.player.getEyeLocation(), player.player, -1, -1);
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    } else {
      laser = null;
    }
  }

  @Override
  protected void useItem() {
    if (isActive) {
      end();
    } else {
      start();
    }
  }

  private void activeTick() {
    ticksLeft--;
    if (ticksLeft == 0) {
      end();
      return;
    }
    setDurability((double) ticksLeft / ACTIVE_TICKS);
  }

  private void start() {
    isActive = true;
    setEnchanted(true);
    ticksLeft = ACTIVE_TICKS;
    startLocation = player.player.getLocation();
    player.addItemTick(activeTick);
    try {
      laser.moveStart(player.player.getEyeLocation());
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
    laser.start(BwPlugin.instance);
  }

  private void end() {
    player.player.teleport(startLocation);
    player.player.setVelocity(new Vector(0, 0, 0));
    reset();
  }

  @Override
  public void reset() {
    super.reset();
    setDurability(1);
    setEnchanted(false);
    isActive = false;
    player.removeItemTick(activeTick);
    if (laser.isStarted()) laser.stop();
  }

  @Override
  public Progress currentProgress() {
    if (isActive) {
      return new Progress(Progress.State.ACTIVE, ticksLeft, ACTIVE_TICKS);
    } else{
      return super.currentProgress();
    }
  }
}

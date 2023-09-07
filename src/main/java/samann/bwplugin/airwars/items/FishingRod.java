package samann.bwplugin.airwars.items;

import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import samann.bwplugin.airwars.AirwarsPlayer;

import java.util.HashSet;
import java.util.Set;

public class FishingRod extends Item {
  private static final ItemStack ITEM = new ItemStack(Material.FISHING_ROD);
  private static final int ACTIVE_TICKS = 7 * 20;
  private boolean isActive = false;
  private int ticksLeft = 0;
  private final Runnable activeTick = this::activeTick;
  private FishHook hook;
  private static final Set<FishingRod> activeRods = new HashSet<>();

  static {
    var meta = ITEM.getItemMeta();
    meta.setDisplayName("Angel");
    ITEM.setItemMeta(meta);
  }

  public FishingRod(AirwarsPlayer player) {
    super(ITEM, 15 * 20, player);
  }

  @Override
  protected void useItem() {
    if (isActive) {
      end();
    }
  }

  private void activeTick() {
    ticksLeft--;
    if (ticksLeft == 0) {
      reset();
      return;
    }
    setDurability((double) ticksLeft / ACTIVE_TICKS);
  }

  private void start() {
    isActive = true;
    activeRods.add(this);
    setEnchanted(true);
    ticksLeft = ACTIVE_TICKS;
    player.addItemTick(activeTick);
  }

  private void end() {
    var hookedIn = hook.getHookedEntity();
    if (hookedIn != null || hook.isOnGround()) {
      var hookPos = hook.getBoundingBox().getCenter();
      var playerPos = player.player.getLocation().toVector();
      var delta = hookPos.subtract(playerPos.setY(playerPos.getY() + 1));
      var direction = delta.normalize();
      var boost = direction.multiply(3);
      boost.setY(boost.getY() / 2);
      player.player.setVelocity(boost);
    }
    reset();
  }

  @Override
  public void reset() {
    super.reset();
    setDurability(1);
    setEnchanted(false);
    isActive = false;
    activeRods.remove(this);
    player.removeItemTick(activeTick);
    if (hook != null) {
      hook.remove();
      hook = null;
    }
  }

  public void onThrowHook(@NotNull FishHook hook) {
    this.hook = hook;
    start();
  }

  @Override
  public boolean callUseOnRightClick() {
    return isActive;
  }

  public static void onKill(AirwarsPlayer player) {
    for (var rod : activeRods) {
      if (rod.hook.getHookedEntity() == player.player) {
        rod.reset();
      }
    }
  }
}

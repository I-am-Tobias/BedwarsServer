package samann.bwplugin.airwars.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.jetbrains.annotations.NotNull;
import samann.bwplugin.airwars.AirwarsPlayer;

import java.util.HashSet;
import java.util.Set;

public class FishingRod extends Item {
  private static final int ACTIVE_TICKS = 7 * 20;
  private boolean isActive = false;
  private int ticksLeft = 0;
  private final Runnable activeTick = this::activeTick;
  private FishHook hook;
  private static final Set<FishingRod> activeRods = new HashSet<>();
  private boolean hitSomething = false;

  public FishingRod(AirwarsPlayer player) {
    super(player, Material.FISHING_ROD, ChatColor.LIGHT_PURPLE, "Angel",
            "Aktiviere, um den Angelhaken zu werfen. Wenn der Angelhaken auf dem Boden aufliegt oder ein Spieler getroffen wurde, kannst du erneut aktivieren, um dich zum Haken zu ziehen.",
            15 * 20);
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
    boolean hitSomething = hook.getHookedEntity() != null || hook.isOnGround();
    if (hitSomething && !this.hitSomething) {
      player.player.playSound(player.player, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1, 1);
    }
    this.hitSomething = hitSomething;
  }

  private void start() {
    isActive = true;
    activeRods.add(this);
    setEnchanted(true);
    ticksLeft = ACTIVE_TICKS;
    player.addItemTick(activeTick);
    hitSomething = false;
  }

  private void end() {
    if (hitSomething) {
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

  @Override
  public Progress currentProgress() {
    if (isActive) {
      if (hitSomething) {
        return new Progress(Progress.State.READY, ticksLeft, ACTIVE_TICKS);
      } else {
        return new Progress(Progress.State.ACTIVE, ticksLeft, ACTIVE_TICKS);
      }
    } else{
      return super.currentProgress();
    }
  }
}

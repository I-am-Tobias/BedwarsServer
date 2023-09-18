package samann.bwplugin.airwars.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.airwars.AirwarsPlayer;

import java.util.List;

public class Crossbow extends Item {
  private enum State { LOAD, READY, INACTIVE }
  private static final int LOADING_TIME = 25;
  private State state = State.INACTIVE;
  private int loadingTicks = 0;

  public Crossbow(AirwarsPlayer player) {
    super(player, Material.CROSSBOW, ChatColor.DARK_PURPLE, "Armbrust",
            "Schieße einen Pfeil auf einen Gegner ab. Wenn du einen Gegner triffst, erhälst du einen weiteren Schuss, ohne die Abklingzeit abwarten zu müssen.",
            25 * 20);
  }

  @Override
  protected void useItem() {

  }

  @Override
  public void reset() {
    super.reset();
    changeState(State.INACTIVE);
  }

  public void onHit() {
    removeCooldown();
  }

  public void tick() {
    if (isItem(player.player.getInventory().getItemInMainHand())) {
      if (state == State.LOAD) {
        loadingTicks++;
        if (loadingTicks == LOADING_TIME) {
          player.game.world.playSound(player.player, Sound.ITEM_CROSSBOW_LOADING_END, 1, 1);
          changeState(State.READY);
        } else if (loadingTicks == 5) {
          player.game.world.playSound(player.player, Sound.ITEM_CROSSBOW_LOADING_START, 1, 1);
        } else if (loadingTicks == 13) {
          player.game.world.playSound(player.player, Sound.ITEM_CROSSBOW_LOADING_MIDDLE, 1, 1);
        }
      } else if (state == State.INACTIVE && !hasCooldown()) {
        changeState(State.LOAD);
      }
    } else {
      changeState(State.INACTIVE);
    }
  }

  private void changeState(State newState) {
    if (state == newState) return;
    var item = getInventoryItem();
    var meta = item == null ? null : (CrossbowMeta) item.getItemMeta();
    System.out.println("from: " + state.name() + " to: " + newState);
    switch (state) {
      case LOAD -> {

      }
      case READY -> {
        if (meta != null) {
          meta.setChargedProjectiles(null);
          item.setItemMeta(meta);
        }
      }
      case INACTIVE -> {
        removeCooldown();
      }
    }
    state = newState;
    switch (state) {
      case LOAD -> {
        loadingTicks = 0;
        //var serverPlayer = ((CraftPlayer) player.player).getHandle();
        //serverPlayer.startUsingItem(InteractionHand.MAIN_HAND);
        //var crossbowItem = (CrossbowItem) CraftItemStack.asNMSCopy(player.player.getInventory().getItemInMainHand()).getItem();
        //var level = ((CraftWorld) player.game.world).getHandle();
        //crossbowItem.use(level, serverPlayer, InteractionHand.MAIN_HAND);
      }
      case READY -> {
        if (meta != null) {
          meta.setChargedProjectiles(List.of(new ItemStack(Material.ARROW)));
          item.setItemMeta(meta);
        }
      }
      case INACTIVE -> {
        setDurability(1);
      }
    }
  }

  @Override
  public boolean callUseOnRightClick() {
    return false;
  }

  @Override
  public Progress currentProgress() {
    if (state == State.LOAD) {
      return new Progress(Progress.State.ACTIVE, loadingTicks, LOADING_TIME);
    } else {
      return super.currentProgress();
    }
  }
}

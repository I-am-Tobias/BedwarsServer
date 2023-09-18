package samann.bwplugin.airwars.items;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import samann.bwplugin.airwars.AirwarsPlayer;

public class Trident extends Item {
  public Trident(AirwarsPlayer player) {
    super(player, Material.TRIDENT, ChatColor.AQUA, "Dreizack",
            "Ziehe dich mit dem Dreizack in Blickrichtung und stoße Gegner weg, die du währenddessen triffst.",
            25 * 20);
  }

  @Override
  protected void useItem() {
    performRiptide(2);
    reset();
  }

  @Override
  public boolean callUseOnRightClick() {
    return false;
  }

  private void performRiptide(int level) {
    net.minecraft.world.entity.player.Player serverPlayer = ((CraftPlayer)player.player).getHandle();

    //perform riptide
    float f = serverPlayer.getYRot();
    float f1 = serverPlayer.getXRot();
    float f2 = -Mth.sin(f * 0.017453292F) * Mth.cos(f1 * 0.017453292F);
    float f3 = -Mth.sin(f1 * 0.017453292F);
    float f4 = Mth.cos(f * 0.017453292F) * Mth.cos(f1 * 0.017453292F);
    float f5 = Mth.sqrt(f2 * f2 + f3 * f3 + f4 * f4);
    float f6 = 3.0F * ((1.0F + (float)level) / 4.0F);
    f2 *= f6 / f5;
    f3 *= f6 / f5;
    f4 *= f6 / f5;
    player.player.setVelocity(new Vector(f2, f3, f4));
    serverPlayer.startAutoSpinAttack(20);

    if (serverPlayer.onGround) {
      float f7 = 1.1999999F;
      serverPlayer.move(MoverType.SELF, new Vec3(0.0D, f7, 0.0D));
    }

    Sound sound;

    if (level >= 3) {
      sound = Sound.ITEM_TRIDENT_RIPTIDE_3;
    } else if (level == 2) {
      sound = Sound.ITEM_TRIDENT_RIPTIDE_2;
    } else {
      sound = Sound.ITEM_TRIDENT_RIPTIDE_1;
    }

    player.game.world.playSound(player.player.getLocation(), sound, 2, 1);
  }
}

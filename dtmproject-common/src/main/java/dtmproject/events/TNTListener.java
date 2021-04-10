package dtmproject.events;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import java.util.Random;

public class TNTListener implements Listener {

    @EventHandler
    public void onFallingBlockLand(EntityChangeBlockEvent event) {
        if(event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }

        Entity entity = event.getEntity();
        Block block = event.getBlock();
        World world = event.getEntity().getWorld();

        world.playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 2, 1);
        world.spawnParticle(Particle.BLOCK_CRACK, entity.getLocation(), 100, 1, 1, 1, block.getType().getNewData(block.getData()));
        entity.remove();
        event.setCancelled(true);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.setYield(0);

        World world = event.getLocation().getWorld();
        Location location = event.getLocation();

        event.blockList().forEach(block -> {
            Vector velocity = block.getLocation().toVector().subtract(location.toVector());
            velocity.multiply(0.5);
            velocity.add(new Vector(randomInt(0, 50) / 100, 0, randomInt(0, 50) / 100));
            velocity.setY(velocity.getY() > 0 ? velocity.getY() + randomInt(0, 50) / 100.0 : (-1 * velocity.getY()) + randomInt(0, 50) / 100.0);

            FallingBlock fallingBlock = world.spawnFallingBlock(block.getLocation(), block.getType().getNewData(block.getData()));

            fallingBlock.setVelocity(velocity);
            fallingBlock.setDropItem(false);
        });
    }

    private static final Random rand = new Random();

    public static int randomInt(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

}

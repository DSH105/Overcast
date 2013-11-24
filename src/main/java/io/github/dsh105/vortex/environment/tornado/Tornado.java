package io.github.dsh105.vortex.environment.tornado;

import io.github.dsh105.vortex.environment.Environment;
import io.github.dsh105.vortex.logger.Logger;
import io.github.dsh105.vortex.util.BlockData;
import io.github.dsh105.vortex.util.Geometry;
import io.github.dsh105.vortex.util.Particle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;


public class Tornado extends Environment {

    public Vector direction;
    public BlockData lastBlock = new BlockData(Material.WEB, (byte) 0);
    public Location location;
    public float speed;
    public int liveTime;
    public int height = 40;
    public int maxY;
    public int maxBlocks = 100;
    public ArrayDeque<VortexEntity> blocks = new ArrayDeque<VortexEntity>();
    public ArrayDeque<VortexEntity> entities = new ArrayDeque<VortexEntity>();

    public Tornado(Location location, Vector direction, float speed, int liveTime, int height, int maximumBlocks) {
        this.location = location;
        this.direction = direction;
        this.speed = speed;
        this.liveTime = liveTime;
        this.height = height;
        this.maxY = this.location.getBlockY() + height;
        this.maxBlocks = maximumBlocks;
        if (direction != null) {
            direction.normalize().multiply(this.speed);
        }
        this.start();
    }

    @Override
    public void onLive() {
        if (this.direction != null) {
            this.location.add(this.direction);
        }
        Location l = this.getLocation().clone();
        l.setY(l.getY() - 5);
        List<Location> list = Geometry.circle(l, 5, this.maxY / 2, false, true, false);
        if (!list.isEmpty()) {
            Block b = list.get(io.github.dsh105.vortex.VortexPlugin.r().nextInt(list.size())).getBlock();
            VortexEntity fb = new VortexEntity(this, this.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData()), false, VortexEntity.VortexEntityType.BLOCK);
            this.lastBlock = new BlockData(b.getType(), b.getData());
            b.setType(Material.AIR);
            this.blocks.add(fb);
        } else {
            VortexEntity fb = new VortexEntity(this, this.getWorld().spawnFallingBlock(this.getLocation(), this.lastBlock.blockType, this.lastBlock.blockMeta), false, VortexEntity.VortexEntityType.BLOCK);
            this.blocks.add(fb);
        }

        if (this.blocks.size() > this.maxBlocks) {
            this.removeBlock(blocks.getFirst());
        }

        if (this.age >= this.liveTime) {
            this.dissipate();
        }

        List<Entity> entityList = Geometry.getNearbyEntities(this.getLocation(), 4);
        if (entityList != null && !entityList.isEmpty()) {
            for (Entity e : entityList) {
                if (!e.hasMetadata("tornado")) {
                    this.entities.add(new VortexEntity(this, e, false, VortexEntity.VortexEntityType.ENTITY));
                }
            }
        }
    }

    public void removeBlock(VortexEntity vortexEntity) {
        vortexEntity.cancel();
        this.blocks.remove(vortexEntity);
        if (vortexEntity.remove) {
            vortexEntity.entity.remove();
        }
    }

    public void removeMob(VortexEntity vortexEntity) {
        vortexEntity.cancel();
        this.entities.remove(vortexEntity);
        if (vortexEntity.remove) {
            vortexEntity.entity.remove();
        }
    }

    public Location getLocation() {
        return this.location;
    }

    public World getWorld() {
        return this.location.getWorld();
    }

    public void dissipate() {
        this.end();
        Iterator<VortexEntity> i = this.blocks.iterator();
        while (i.hasNext()) {
            VortexEntity ve = i.next();
            this.removeBlock(ve);
            try {
                Particle.CLOUD.sendTo(ve.entity.getLocation());
            } catch (Exception e) {
                Logger.log(Logger.LogLevel.WARNING, "Failed to generate Tornado Cloud particle.", e, true);
            }
        }
    }
}
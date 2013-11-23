package io.github.dsh105.overcast.environment;

import io.github.dsh105.overcast.Overcast;
import org.bukkit.scheduler.BukkitRunnable;

public class EnvironmentTick extends BukkitRunnable {

    protected Environment environment;

    public EnvironmentTick(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run() {
        this.environment.age++;
        this.environment.onLive();
    }
}
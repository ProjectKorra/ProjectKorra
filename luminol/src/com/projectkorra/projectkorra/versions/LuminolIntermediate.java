package com.projectkorra.projectkorra.versions;

import me.earthme.luminol.api.ThreadedRegion;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class LuminolIntermediate {

    public static Object getRegion(Location location) {
        return location.getWorld().getThreadedRegionizer().getAtSynchronized(location);
    }

    public static boolean isRegionActive(@NotNull Object region) {
        //((ThreadedRegion) region).getWorld().getThreadedRegionizer().getAllRegions().forEach(r -> System.out.println(r));

        return ((ThreadedRegion) region).getTickRegionData().getRegionStats().getPlayerCount() > 0;
    }

    public static long getRegionId(@NotNull Object region) {
        return ((ThreadedRegion) region).getId();
    }
}

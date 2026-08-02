package kenshkrix.sssrm;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;

/**
 * Categories a hostile entity can fall into. A swarm profile lists these in
 * priority order; anything not listed is not a valid target for that profile.
 */
public enum TargetClass {
    CAPITAL,
    CRUISER,
    DESTROYER,
    FRIGATE,
    FIGHTER,
    MISSILE;

    /** Returns null for entities that are never valid targets (asteroids, hulks, debris). */
    public static TargetClass of(CombatEntityAPI entity) {
        if (entity instanceof MissileAPI) {
            return MISSILE;
        }
        if (entity instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) entity;
            if (ship.isHulk() || !ship.isAlive()) {
                return null;
            }
            if (ship.isFighter() || ship.isDrone()) {
                return FIGHTER;
            }
            switch (ship.getHullSize()) {
                case CAPITAL_SHIP:
                    return CAPITAL;
                case CRUISER:
                    return CRUISER;
                case DESTROYER:
                    return DESTROYER;
                default:
                    return FRIGATE;
            }
        }
        return null;
    }
}

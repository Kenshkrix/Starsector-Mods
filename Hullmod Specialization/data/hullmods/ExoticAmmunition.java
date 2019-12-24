package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;


public class ExoticAmmunition extends BaseHullMod {
	
	public static final float PROJECTILE_SPEED_MOD = 45f;
	public static final float BALLISTIC_DAMAGE_MOD = 20f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getProjectileSpeedMult().modifyPercent(id,PROJECTILE_SPEED_MOD);
		stats.getBallisticWeaponDamageMult().modifyPercent(id,BALLISTIC_DAMAGE_MOD);
	}
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) BALLISTIC_DAMAGE_MOD + "%";
        if (index == 1) return "" + (int) PROJECTILE_SPEED_MOD + "%";
        return null;
    }
}

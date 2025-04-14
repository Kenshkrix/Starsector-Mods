package data.hullmods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShieldAPI;

public class SecondaryShieldThree extends BaseHullMod {

    private static final int SHIELD_SEGMENTS = 3;
    private static final float FLUX_CAPACITY = 2000f;
    private static final float FLUX_DISSIPATION = 50f;
    private static final float SHIELD_EFFICIENCY = 1.0f;
    private static final float SHIELD_RADIUS = 30;
    private static final float ROTATION_RATE = 0.1f;
    private static final float HARD_DISSIPATION = 0.05f;
    private static final float SHIELD_ARC = (360f / SHIELD_SEGMENTS) + 6f;
    private static final float ANGLE_OFFSET = 360f / SHIELD_SEGMENTS;
    private static final float SMOD_MULT = 0.8f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    @Override
    public void applyEffectsAfterShipAddedToCombatEngine(ShipAPI ship, String id) {
        delay = 1f;
        rotation = 0f;
        ID = id;
    }
    private static final String DRONE_ID = "shieldbubble_alwayson_variant";

    String ID;
    float delay = 1f;
    float rotation;

    public HashMap<ShipAPI, ArrayList<ShipAPI>> shieldsOfShip = new HashMap<>();

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        if (!ship.isAlive()) {
            return;
        }

        if (delay > 0) {
            delay -= amount;
            return;
        } else if (!shieldsOfShip.containsKey(ship)) {
            ArrayList<ShipAPI> shields = new ArrayList();
            shieldsOfShip.put(ship, shields);
            CombatEngineAPI engine = Global.getCombatEngine();
            CombatFleetManagerAPI fleetManager = engine.getFleetManager(ship.getOwner());
            for (int i = 0; i < SHIELD_SEGMENTS; i++) {
                shields.add(makeShieldDrone(fleetManager, ship));
            }
        }
        rotation += ROTATION_RATE;
        if (rotation > 360f) {
            rotation -= 360f;
        }
        for (int i = 0; i < SHIELD_SEGMENTS; i++) {
            ShipAPI currShield = shieldsOfShip.get(ship).get(i);
            if (currShield != null && currShield.isAlive()) {
                currShield.getLocation().set(ship.getLocation());
                currShield.setFacing((ship.getFacing() + (ANGLE_OFFSET * i)) + rotation);
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) FLUX_CAPACITY + " [" + FLUX_CAPACITY / SHIELD_SEGMENTS + "]";
        }
        if (index == 1) {
            return "" + (int) FLUX_DISSIPATION + " [" + FLUX_DISSIPATION / SHIELD_SEGMENTS + "]";
        }
        if (index == 2) {
            return "" + SHIELD_EFFICIENCY;
        }
        if (index == 3) {
            return "" + (int) (HARD_DISSIPATION * 100) + "%";
        }
        if (index == 4) {
            return "+" + (int) SHIELD_RADIUS;
        }
        return null;
    }
    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + SMOD_MULT;
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ship != null && ship.getShield() != null;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Ship has no shields";
    }

    ShipAPI makeShieldDrone(CombatFleetManagerAPI fleetManager, ShipAPI ship) {
        fleetManager.setSuppressDeploymentMessages(true);
        ShipAPI shieldDrone = fleetManager.spawnShipOrWing(DRONE_ID, ship.getLocation(), ship.getFacing());
        shieldDrone.getMutableStats().getFluxCapacity().setBaseValue((FLUX_CAPACITY / SHIELD_SEGMENTS) * (isSMod(ship) ? SMOD_MULT : 1.0f));
        shieldDrone.getMutableStats().getFluxDissipation().setBaseValue((FLUX_DISSIPATION / SHIELD_SEGMENTS) * (isSMod(ship) ? SMOD_MULT : 1.0f));
        shieldDrone.getMutableStats().getHardFluxDissipationFraction().setBaseValue(HARD_DISSIPATION * (isSMod(ship) ? SMOD_MULT : 1.0f));
        shieldDrone.getMutableStats().getShieldUnfoldRateMult().modifyMult(ID, 2f);
        shieldDrone.setShield(ShieldAPI.ShieldType.FRONT, 0f, SHIELD_EFFICIENCY, SHIELD_ARC);
        //shieldDrone.setCollisionRadius(ship.getShield().getRadius() + SHIELD_RADIUS);
        shieldDrone.getShield().setRadius(ship.getShield().getRadius() + SHIELD_RADIUS);
        shieldDrone.setBeingIgnored(true);
        fleetManager.setSuppressDeploymentMessages(false);
        return shieldDrone;
    }
}

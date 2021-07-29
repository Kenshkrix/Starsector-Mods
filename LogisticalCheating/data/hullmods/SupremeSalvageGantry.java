package data.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class SupremeSalvageGantry extends BaseHullMod {
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 100f);
		mag.put(HullSize.DESTROYER, 250f);
		mag.put(HullSize.CRUISER, 300f);
		mag.put(HullSize.CAPITAL_SHIP, 400f);
	}
	
	public static final float BATTLE_SALVAGE_MULT = 1.0f;
	public static final float MIN_CR = 0.1f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.SALVAGE_VALUE_MULT_MOD).modifyFlat(id, (Float) mag.get(hullSize) * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue() + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
		if (index == 4) return "" + (int)Math.round(BATTLE_SALVAGE_MULT * 100f) + "%";
		
		return null;
	}
	
	
}










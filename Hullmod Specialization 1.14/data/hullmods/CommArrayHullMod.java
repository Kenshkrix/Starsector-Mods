package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.CommRelayCondition;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.misc.CommSnifferIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class CommArrayHullmod extends BaseHullMod {

	private static int INTEL_PER_UPDATE = 4;
	private static int DAYS_PER_UPDATE = 3;
	private static float SPEED_MOD = -20f;
	private static float ARMOR_MULT = 0.5f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyFlat(id, SPEED_MOD);
		stats.getArmorBonus().modifyMult(id, ARMOR_MULT);
	}
	private long lastDay = 0;
	
	@Override
	public void advanceInCampaign(FleetMemberAPI member, float amount) {
		
		long currentDay = Global.getSector().getClock().getTimestamp();
		
		if (Global.getSector().getClock().getElapsedDaysSince(lastDay) >= DAYS_PER_UPDATE){
			lastDay = currentDay;
			
			int i = 0;
			for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getCommQueue()){
				if (i < INTEL_PER_UPDATE){
					intel.setForceAddNextFrame(true);
				}
				i++;
			}
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + DAYS_PER_UPDATE;
		if (index == 1) return "" + INTEL_PER_UPDATE;
		return null;
	}
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return (ship.getHullSize() == HullSize.CAPITAL_SHIP || ship.getHullSize() == HullSize.CRUISER);
	}
}

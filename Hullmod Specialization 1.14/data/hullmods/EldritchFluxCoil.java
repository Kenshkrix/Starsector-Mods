package data.hullmods;

import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

@SuppressWarnings("unchecked")
public class EldritchFluxCoil extends BaseHullMod {

	public static final String HULLMOD_NAME = "khs_eldritch_flux_coil";
	public static final float FLUX_CAPACITY = 4000f;
	public static final float FLUX_DISSIPATION = 200f;
	public static final int HARD_FLUX_DISSIPATION_PERCENT = 5;
	
	private static int DAYS_PER_SACRIFICE = 3;
	private static int SACRIFICES_REQUIRED = 1;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (stats == null) return;
		FleetMemberAPI fleetMember = stats.getFleetMember();
		if (fleetMember == null) return;
		FleetDataAPI data = fleetMember.getFleetData();
		if (data == null) return;
		int count = 0;
		for (FleetMemberAPI ship : data.getMembersListCopy()){
			if (ship.getVariant().getHullMods().contains(HULLMOD_NAME)){
				count = count + 1;
			}
		}
		if (count >= 2) return;
		
		stats.getFluxCapacity().modifyFlat(id, FLUX_CAPACITY);
		stats.getFluxDissipation().modifyFlat(id, FLUX_DISSIPATION);
		
		stats.getHardFluxDissipationFraction().modifyFlat(id, (float)HARD_FLUX_DISSIPATION_PERCENT * 0.01f);
	}
	
	private long lastDay = 0;
	
	@Override
	public void advanceInCampaign(FleetMemberAPI member, float amount) {
		
		long currentDay = Global.getSector().getClock().getTimestamp();
		
		if (Global.getSector().getClock().getElapsedDaysSince(lastDay) >= DAYS_PER_SACRIFICE){
			lastDay = currentDay;
			
			member.getFleetData().getFleet().getCargo().removeCrew(SACRIFICES_REQUIRED);
			
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) FLUX_CAPACITY;
		if (index == 1) return "" + (int) FLUX_DISSIPATION;
		if (index == 2) return "" + HARD_FLUX_DISSIPATION_PERCENT + "%";
		if (index == 3) return "" + SACRIFICES_REQUIRED;
		if (index == 4) return "" + DAYS_PER_SACRIFICE;
		return null;
	}


}

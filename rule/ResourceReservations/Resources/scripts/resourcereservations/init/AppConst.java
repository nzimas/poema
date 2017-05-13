package resourcereservations.init;

import com.exponentus.common.init.DefaultAppConst;

import administrator.model.constants.InterfaceType;

public class AppConst extends DefaultAppConst {
	public static String CODE = "rr";
	public static String NAME = "ResourceReservations";
	public static String NAME_ENG = "Resource reservations";
	public static String NAME_RUS = "Резервирование ресурсов";
	public static String NAME_KAZ = "Ресурс брондау";
	public static String BASE_URL = "/" + NAME + "/";
	public static final InterfaceType AVAILABLE_MODE[] = { InterfaceType.SPA };
	public static final String[] ROLES = { "vehicle_reservation_decider" };
}

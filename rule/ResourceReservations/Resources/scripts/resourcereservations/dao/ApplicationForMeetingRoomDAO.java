package resourcereservations.dao;

import com.exponentus.dataengine.exception.DAOException;
import com.exponentus.dataengine.jpa.DAO;
import com.exponentus.scripting._Session;
import resourcereservations.model.ApplicationForMeetingRoom;

import java.util.UUID;

public class ApplicationForMeetingRoomDAO extends DAO<ApplicationForMeetingRoom, UUID> {

    public ApplicationForMeetingRoomDAO(_Session session) throws DAOException {
        super(ApplicationForMeetingRoom.class, session);
    }
}

package workflow.task;

import java.util.Date;
import java.util.List;

import com.exponentus.appenv.AppEnv;
import com.exponentus.dataengine.exception.DAOException;
import com.exponentus.exception.SecureException;
import com.exponentus.scripting._Session;
import com.exponentus.scripting.event.Do;
import com.exponentus.scriptprocessor.constants.Trigger;
import com.exponentus.scriptprocessor.tasks.Command;

import reference.dao.TagDAO;
import reference.model.Tag;
import workflow.dao.AssignmentDAO;
import workflow.dao.filter.AssignmentFilter;
import workflow.model.Assignment;
import workflow.model.constants.ControlStatusType;

@Command(name = "expired_tracking", trigger = Trigger.EVERY_NIGHT)
public class ExpiredTracking extends Do {
	private static final String EXPIRED_TAG_NAME = "expired";
	private Date current = new Date();
	private Tag tag;
	private AssignmentDAO tDao;

	@Override
	public void doTask(AppEnv appEnv, _Session session) {
		try {
			TagDAO tagDAO = new TagDAO(session);
			tag = tagDAO.findByName(EXPIRED_TAG_NAME);
			if (tag != null) {
				tDao = new AssignmentDAO(session);
				AssignmentFilter filter = new AssignmentFilter();
				filter.setControlStatusType(ControlStatusType.PROCESSING);
				//processTask(appEnv, tDao.findViewPage(filter, new SortParams(), 0, 0).getResult(), session);
			} else {
				logger.warning("The tag \"" + EXPIRED_TAG_NAME + "\" did not find in Reference");
			}
		} catch (DAOException e) {
			logger.exception(e);
		}

	}

	private void processTask(AppEnv env, List<Assignment> tasks, _Session session) {
		for (Assignment task : tasks) {
			if (current.after(task.getDueDate())) {
				if (!task.getTags().contains(tag)) {
					List<Tag> tags = task.getTags();
					tags.add(tag);
					task.setTags(tags);
					try {
						tDao.update(task);
						logger.info("The task \"" + task.getTitle() + "\" was marked as \"" + tag.getName() + "\"");
						//sendNotify(env, session, task);
					} catch (SecureException | DAOException e) {
						setError(e);
					}
				}
			} else {
				if (task.getTags().contains(tag)) {
					List<Tag> tags = task.getTags();
					tags.remove(tag);
					task.setTags(tags);
					try {
						tDao.update(task);
						logger.info("The task \"" + task.getTitle() + "\" was unmarked as \"" + tag.getName() + "\"");
					} catch (SecureException | DAOException e) {
						setError(e);
					}
				}
			}
		}
	}

}

package projects.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.exponentus.appenv.AppEnv;
import com.exponentus.dataengine.exception.DAOException;
import com.exponentus.dataengine.exception.DAOExceptionType;
import com.exponentus.exception.SecureException;
import com.exponentus.localization.constants.LanguageCode;
import com.exponentus.scripting._Session;
import com.exponentus.scripting.event.Do;
import com.exponentus.scriptprocessor.tasks.Command;

import reference.dao.TagDAO;
import reference.model.Tag;

@Command(name = "fill_tags")
public class FillTags extends Do {
	private static final String CATEGORY_NAME = "software_developing_task";
	
	@Override
	public void doTask(AppEnv appEnv, _Session ses) {

		List<Tag> entities = new ArrayList<Tag>();
		
		Tag entity = new Tag();
		entity.setName("starred");
		Map<LanguageCode, String> name = new HashMap<LanguageCode, String>();
		name.put(LanguageCode.ENG, "Starred");
		name.put(LanguageCode.RUS, "Избранный");
		name.put(LanguageCode.POR, "Estrelou");
		entity.setLocName(name);
		entity.setColor("#008080");
		entity.setCategory(CATEGORY_NAME);
		entities.add(entity);
		
		entity = new Tag();
		entity.setName("bug");
		name = new HashMap<LanguageCode, String>();
		name.put(LanguageCode.ENG, "Bug");
		name.put(LanguageCode.RUS, "Ошибка");
		name.put(LanguageCode.POR, "Erro");
		entity.setLocName(name);
		entity.setColor("#ff0000");
		entity.setCategory(CATEGORY_NAME);
		entities.add(entity);
		
		entity = new Tag();
		entity.setName("enhancement");
		name = new HashMap<LanguageCode, String>();
		name.put(LanguageCode.ENG, "Enhancement");
		name.put(LanguageCode.RUS, "Улучшение");
		name.put(LanguageCode.POR, "Aprimoramento");
		entity.setLocName(name);
		entity.setColor("#0080ff");
		entity.setCategory(CATEGORY_NAME);
		entities.add(entity);
		
		entity = new Tag();
		entity.setName("expired");
		name = new HashMap<>();
		name.put(LanguageCode.ENG, "Overdued");
		name.put(LanguageCode.RUS, "Просроченный");
		name.put(LanguageCode.KAZ, "Mерзімі өткен");
		entity.setLocName(name);
		entity.setColor("#db0000");
		entity.setCategory(CATEGORY_NAME);
		entity.setHidden(true);
		entities.add(entity);

		try {
			TagDAO dao = new TagDAO(ses);
			for (Tag entry : entities) {
				try {
					if (dao.add(entry) != null) {
						logger.info(entry.getName() + " added");
					}
				} catch (DAOException e) {
					if (e.getType() == DAOExceptionType.UNIQUE_VIOLATION) {
						logger.warning("a data is already exists (" + e.getAddInfo() + "), record was skipped");
					} else if (e.getType() == DAOExceptionType.NOT_NULL_VIOLATION) {
						logger.warning("a value is null (" + e.getAddInfo() + "), record was skipped");
					} else {
						logger.exception(e);
					}
				} catch (SecureException e) {
					logger.exception(e);
				}
			}
		} catch (DAOException e) {
			logger.exception(e);
		}
		logger.info("done...");
	}
	
}

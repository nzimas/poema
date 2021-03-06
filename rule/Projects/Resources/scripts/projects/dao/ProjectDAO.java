package projects.dao;

import com.exponentus.dataengine.exception.DAOException;
import com.exponentus.dataengine.jpa.DAO;
import com.exponentus.dataengine.jpa.ViewPage;
import com.exponentus.scripting.SortParams;
import com.exponentus.scripting._Session;
import projects.dto.ProjectViewEntry;
import projects.model.Project;
import projects.model.constants.ProjectStatusType;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.UUID;

public class ProjectDAO extends DAO<Project, UUID> {

    public ProjectDAO(_Session session) throws DAOException {
        super(Project.class, session);
    }

    public ViewPage<ProjectViewEntry> findViewPage(SortParams sortParams, ProjectStatusType status, int pageNum, int pageSize) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        try {
            CriteriaQuery<ProjectViewEntry> cq = cb.createQuery(ProjectViewEntry.class);
            Root<Project> root = cq.from(Project.class);
            Join atts = root.join("attachments", JoinType.LEFT);

            CriteriaQuery<Long> countRootCq = cb.createQuery(Long.class);
            Root<Project> countRoot = countRootCq.from(Project.class);

            Predicate condition = null;
            Predicate countCondition = null;

            if (!user.isSuperUser()) {
                condition = cb.and(root.get("readers").in(user.getId()));
                countCondition = cb.and(countRoot.get("readers").in(user.getId()));
            }

            if (status != null) {
                if (condition == null) {
                    condition = cb.and(cb.equal(root.get("status"), status));
                    countCondition = cb.and(cb.equal(countRoot.get("status"), status));
                } else {
                    condition = cb.and(cb.equal(root.get("status"), status), condition);
                    countCondition = cb.and(cb.equal(countRoot.get("status"), status), countCondition);
                }
            }

            cq.select(cb.construct(
                    ProjectViewEntry.class,
                    root.get("id"),
                    root.get("name"),
                    root.get("status"),
                    root.get("primaryLanguage"),
                    root.get("customer").get("name"),
                    root.get("manager"),
                    root.get("programmer"),
                    root.get("tester"),
                    root.get("startDate"),
                    root.get("finishDate"),
                    root.get("comment"),
                    cb.count(atts)
            ))
                    .distinct(true)
                    .groupBy(root, root.get("customer").get("name"))
                    .orderBy(collectSortOrder(cb, root, sortParams));

            countRootCq.select(cb.count(countRoot));

            if (condition != null) {
                cq.where(condition);
                countRootCq.where(countCondition);
            }

            TypedQuery<ProjectViewEntry> typedQuery = em.createQuery(cq);
            TypedQuery<Long> countQuery = em.createQuery(countRootCq);

            long count = countQuery.getSingleResult();
            int maxPage = pageable(typedQuery, count, pageNum, pageSize);

            List<ProjectViewEntry> result = typedQuery.getResultList();

            return new ViewPage<>(result, count, maxPage, pageNum);
        } finally {
            em.close();
        }
    }
}

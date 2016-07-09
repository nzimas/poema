package projects.page;

import com.exponentus.common.dao.AttachmentDAO;
import com.exponentus.common.model.Attachment;
import com.exponentus.exception.SecureException;
import com.exponentus.localization.LanguageCode;
import com.exponentus.scripting._Session;
import com.exponentus.scripting._Validation;
import com.exponentus.scripting._WebFormData;
import com.exponentus.scripting.event._DoForm;
import org.eclipse.persistence.exceptions.DatabaseException;
import projects.dao.CommentDAO;
import projects.dao.TaskDAO;
import projects.model.Comment;
import projects.model.Task;

import java.util.List;

public class Comments extends _DoForm {

    @Override
    public void doGET(_Session session, _WebFormData formData) {
        String taskId = formData.getValueSilently("taskId");
        if (taskId.isEmpty()) {
            setBadRequest();
            return;
        }

        TaskDAO taskDAO = new TaskDAO(session);
        Task task = taskDAO.findById(taskId);
        if (task == null) {
            setBadRequest();
            return;
        }

        CommentDAO commentDAO = new CommentDAO(session);
        int page = formData.getNumberValueSilently("page", 1);
        List<Comment> comments = commentDAO.findTaskComments(task, page, 20);
        addContent(comments);
    }

    @Override
    public void doPOST(_Session session, _WebFormData formData) {
        String taskId = formData.getValueSilently("taskId");
        if (taskId.isEmpty()) {
            setBadRequest();
            return;
        }

        saveComment(session, taskId, "");
    }

    @Override
    public void doPUT(_Session session, _WebFormData formData) {
        String taskId = formData.getValueSilently("taskId");
        String commentId = formData.getValueSilently("commentId");
        if (taskId.isEmpty() || commentId.isEmpty()) {
            setBadRequest();
            return;
        }

        saveComment(session, taskId, commentId);
    }

    @Override
    public void doDELETE(_Session session, _WebFormData formData) {
        String commentId = formData.getValueSilently("commentId");
        if (commentId.isEmpty()) {
            setBadRequest();
            return;
        }

        if (formData.containsField("attachmentId")) {
            String attachmentId = formData.getValueSilently("attachmentId");
            deleteAttachment(session, commentId, attachmentId);
        } else {
            deleteComment(session, commentId);
        }
    }

    private void saveComment(_Session session, String taskId, String commentId) {
        try {
            TaskDAO taskDAO = new TaskDAO(session);
            Task task = taskDAO.findById(taskId);
            if (task == null) {
                setBadRequest();
                return;
            }

            _Validation ve = validateComment(formData, session.getLang());
            if (ve.hasError()) {
                setBadRequest();
                setValidation(ve);
                return;
            }

            CommentDAO commentDAO = new CommentDAO(session);
            Comment comment = new Comment();
            comment.setTask(task);
            comment.setComment(formData.getValueSilently("comment"));
            comment.setAttachments(getActualAttachments(comment.getAttachments()));

            commentDAO.add(comment);

        } catch (DatabaseException e) {
            error(e);
            setBadRequest();
        } catch (SecureException e) {
            setError(e);
        }
    }

    private void deleteComment(_Session session, String commentId) {
        CommentDAO commentDAO = new CommentDAO(session);
        Comment comment = commentDAO.findById(commentId);

        try {
            commentDAO.delete(comment);
        } catch (SecureException e) {
            setError(e);
        }
    }

    private void deleteAttachment(_Session session, String commentId, String attachmentId) {
        if (commentId.isEmpty() || attachmentId.isEmpty()) {
            return;
        }

        CommentDAO commentDAO = new CommentDAO(session);
        Comment comment = commentDAO.findById(commentId);

        AttachmentDAO attachmentDAO = new AttachmentDAO(session);
        Attachment attachment = attachmentDAO.findById(attachmentId);
        comment.getAttachments().remove(attachment);

        try {
            commentDAO.update(comment);
        } catch (SecureException e) {
            setError(e);
        }
    }

    private _Validation validateComment(_WebFormData formData, LanguageCode lang) {
        _Validation ve = new _Validation();

        if (formData.getValueSilently("comment").isEmpty()) {
            ve.addError("comment", "required", getLocalizedWord("field_is_empty", lang));
        }

        return ve;
    }
}

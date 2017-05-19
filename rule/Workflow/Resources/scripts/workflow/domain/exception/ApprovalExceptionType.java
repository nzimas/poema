package workflow.domain.exception;

public enum ApprovalExceptionType {
	WRONG_STATUS, BLOCK_TYPE_ERROR, BLOCK_NOT_FOUND, WRONG_BLOCK_TYPE, APPROVER_NOT_FOUND_IN_BLOCK, THERE_IS_NO_COMMENT, APPROVER_IS_NOT_SET, APPROVER_ALREADY_HAS_DECISION, INTERNAL_ERROR, BLOCK_IS_NULL
}

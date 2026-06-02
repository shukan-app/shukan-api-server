CREATE FUNCTION cascade_company_soft_delete_to_children()
	RETURNS TRIGGER
	LANGUAGE plpgsql
AS
$$
BEGIN
	IF OLD.deleted_at IS NULL AND NEW.deleted_at IS NOT NULL THEN
		UPDATE tasks
		SET deleted_at = NEW.deleted_at,
		    updated_at = NEW.updated_at
		WHERE company_id = NEW.id
		  AND deleted_at IS NULL;
		
		UPDATE events
		SET deleted_at = NEW.deleted_at,
		    updated_at = NEW.updated_at
		WHERE company_id = NEW.id
		  AND deleted_at IS NULL;
	END IF;
	
	RETURN NEW;
END;
$$;

CREATE TRIGGER companies_soft_delete_cascade_children
	AFTER UPDATE OF deleted_at ON companies
	FOR EACH ROW
	EXECUTE FUNCTION cascade_company_soft_delete_to_children();

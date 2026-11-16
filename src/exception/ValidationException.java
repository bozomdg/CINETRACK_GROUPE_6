package exception;

/**
 * Exception levée lors d'une violation de règle métier ou de validation dans la couche Service.
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}


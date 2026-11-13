package exception;

/**
 * Exception levée lors d'une violation de règle métier ou de validation dans la couche Service.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}


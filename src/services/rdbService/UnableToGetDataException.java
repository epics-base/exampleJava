package services.rdbService;

@SuppressWarnings("serial")
public class UnableToGetDataException extends Exception 
{
	public
    UnableToGetDataException()
    {
        super();
    }

	public UnableToGetDataException(String _message) {
		super(_message);
	}
	
	public UnableToGetDataException(String _message, Throwable _cause) {
		super(_message, _cause);
	}
	
}




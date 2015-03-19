package image.medical.idcm.decode;

public class DecodeError extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 9197232637721402161L;

    public DecodeError() {
        super();
    }

    public DecodeError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DecodeError(String detailMessage) {
        super(detailMessage);
    }

    public DecodeError(Throwable throwable) {
        super(throwable);
    }

    
}

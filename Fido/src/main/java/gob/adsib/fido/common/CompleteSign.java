package gob.adsib.fido.common;

import java.util.List;

/**
 *
 * @author MEFP
 */
public class CompleteSign {
    private String payload;
    private List<Signs> signatures;
 	
    /**
    * @return the payload
    */
    public String getPayload() {
        return payload;
    }
	
    /**
    * @param payload the payload to set
    */
    public void setPayload(String payload) {
        this.payload = payload;
    }
	
    /**
    * @return the signatures
    */
    public List<Signs> getSignatures() {
        return signatures;
    }

    /**
    * @param signatures the signatures to set
    */
    public void setSignatures(List<Signs> signatures) {
       this.signatures = signatures;
    }
}

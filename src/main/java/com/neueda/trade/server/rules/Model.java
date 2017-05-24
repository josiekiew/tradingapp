package com.neueda.trade.server.rules;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neueda.trade.server.FatalTradeException;
import com.neueda.trade.server.TradeException;
import com.neueda.trade.server.model.TradeState;

/**
 * Utility class for managing the trade business rules.
 * 
 * @author Neueda
 *
 */
@Component
public class Model {
	
	/**
	 * Jackson mapper object use for conversion to/from JSON
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Java Validation object used to check annotations on the DTOs
	 */
	@Autowired
	private Validator validator;


	/**
	 * Explicit DTO validation.
	 * @param <T> Type of DTO to validate
	 * @param obj DTO to validate using validation annotations in the class
	 * @return validated object
	 * @throws InjectException if validation fails, exception holds the first annotation validation message
	 */
	public <T> T validate(@Validated T obj) {
		Set<ConstraintViolation<T>> errors = validator.validate(obj);
		if (errors.size() != 0) {
			for (ConstraintViolation<T> error : errors) {
				throw new TradeException(error.getMessage());
			}
		}
		return obj;
	}

    /**
     * Convenience method to check that DAO.findById() methods returns a single row.
	 * @param <T> Type of DTO to validate
	 * @param <C> Type of unique id (typically int or String)
     * @param list DTO rows returned from the query 
     * @param id query ID - used in the error message
     * @param type - DTO type name used in the error message
     * @return single DTO
     * @throws InjectException if failed to find entry 
     * @throws FatalTradeException more than one row matched (DB is corrupted)
     */
    public static final <T, C> T validateFindUnique(List<T> list, C id, String type) {
        // test most likely scenario first
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() == 0) {
            throw new TradeException("Cannot find %s with id %s", type, id);
        } else {
            // defensive programming
            throw new FatalTradeException("Found duplicated %s id %s", type, id);
        }
    }
    
    private ThreadLocal<SimpleDateFormat> df = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
    
    /**
     * Helper method to ensure placement time format parse is consistent
     * @param ptime string hold transaction placement time
     * @return date converted from the string
     * @throws InjectException if the date is invalid
     */
    public Date parseTime(String ptime) {
    	try {
    		return df.get().parse(ptime);
    	}
    	catch (ParseException ex) {
    		throw new TradeException("Invalid trade time %s", ex.getMessage());
    	}
    }
  
	/**
	 * Wrapper to handle errors when converting from JSON messages.
	 * The object is not validated as it is created
	 * 
	 * @param json JSON string
	 * @param type class type expected
	 * @return object of the indicated class
	 * @throws FatalTradeException if the conversion fails
	 */
	public static <T> T fromJson(String json, Class<T> type) {
    	try {
    		return mapper.readValue(json, type);
    	}
    	catch (IOException ex) {
    		throw new FatalTradeException("Cannot convert from json (%s) to %s: %s", json, type.getName(), ex.getMessage());
    	}
	}
	
	/**
	 * Wrapper to handle errors when converting to JSON
	 * @param obj
	 * @return
	 */
	public static <T> String toJson(T obj) {
    	try {
    		return mapper.writeValueAsString(obj);
    	}
    	catch (IOException ex) {
    		throw new FatalTradeException("Cannot create json from %s: %s", obj, ex.getMessage());
    	}
	}

    // TODO: Extension solutions start here    

	/**
	 * state table defining allowed state changes
	 */
    @SuppressWarnings("serial")
    public static final HashMap<TradeState, TradeState[]> allowedStates = new HashMap<TradeState, TradeState[]>() {{
        put(TradeState.Accept, new TradeState[] {TradeState.Place, TradeState.Modify} );
        put(TradeState.Cancel, new TradeState[] {TradeState.Place, TradeState.Modify, TradeState.Accept} );
        put(TradeState.Deny, new TradeState[] {TradeState.Place, TradeState.Modify} );
        put(TradeState.Execute, new TradeState[] {TradeState.Accept} );
        put(TradeState.Modify, new TradeState[] {TradeState.Place, TradeState.Modify} );
        put(TradeState.Reject, new TradeState[] {TradeState.Execute} );
        put(TradeState.Settle, new TradeState[] {TradeState.Execute} );
    }};

}

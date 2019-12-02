package za.co.wethinkcode.constraints;

import za.co.wethinkcode.App;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MarketIdValidator implements ConstraintValidator<MarketId, String> {

     private String message;

        @Override
        public void initialize(MarketId marketId) {

        }

        @Override
        public boolean isValid(String marketId, ConstraintValidatorContext context) {

            if (marketId.length() != 6) {
                message = "MarketId must be 6 characters.";
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
                return false;
            }
            if (!marketExists(marketId)) {
                message = "MarketId is not on record.";
                context.disableDefaultConstraintViolation();
                //build new violation message and add it
                context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
                return false;
            }
            if (!marketId.matches("[A-Z0-9]+")) {
                message = "MarketId must be alphanumeric.";
                context.disableDefaultConstraintViolation();
                //build new violation message and add it
                context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
                return false;
            }
            return true;
        }

        private boolean marketExists(String marketId) {

            if (App.marketInstruments.getInstruments().containsKey(marketId))
                return true;
            return false;
    }
}

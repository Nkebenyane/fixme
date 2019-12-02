package za.co.wethinkcode.broker;

import jdk.vm.ci.code.site.Mark;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import za.co.wethinkcode.App;
import za.co.wethinkcode.broker.InvalidInputException;
import za.co.wethinkcode.constraints.MarketId;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
public class Transaction {

	@MarketId()
	private String marketId;
	@Pattern(regexp = "buy|sell|BUY|SELL", message = "Must be buy or sell.")
	private String buyOrSell;
	private String instrument;
	@Pattern(regexp = "[0-9]+", message = "Must be a number")
	private String orderQty;
	
	
	private Transaction(String marketId, String buyOrSell, String instrument, String orderQty) {
		this.marketId = marketId;
		this.buyOrSell = buyOrSell;
		this.instrument = instrument;
		this.orderQty = orderQty;
	}
	
	public static Transaction buildTransaction(String msg) throws InvalidInputException {
		// Later: regex to split on all whitespace
		String[] args = msg.split("\\s");
		Transaction t;
		if (args.length == 4) {
			t = new Transaction(args[0], args[1], args[2], args[3]);
		} else {
			App.flipSwitch();
			throw new InvalidInputException("Transaction message must have 4 fields, for example: \n[marketId] [buyOrSell] [instrument] [orderQTY]");
		}
		validateTransaction(t);
		return t;
	}

	private static void validateTransaction(Transaction t) throws InvalidInputException {
		ValidatorFactory validatorFactory = Validation
				.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();

		Set<ConstraintViolation<Transaction>> constraintViolations = validator.validate(t);

		if (constraintViolations.size() > 0) {
			System.out.println("Your input is invalid:\n[marketId] [buyOrSell] [instrument] [orderQTY]");
			Set<String> violationMessages = new HashSet<String>();

			for (ConstraintViolation<Transaction> constraintViolation : constraintViolations) {
				violationMessages.add(constraintViolation.getPropertyPath() + " : " + constraintViolation.getMessage());
			}
			System.out.println(StringUtils.join(violationMessages, "\n"));
			App.flipSwitch();
			throw new InvalidInputException("");
		}
	}
}

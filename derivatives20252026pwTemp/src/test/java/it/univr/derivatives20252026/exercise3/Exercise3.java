package it.univr.derivatives20252026.exercise3;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Set;
import java.util.TreeMap;

import it.univr.derivatives.marketdataprovider.MarketDataProvider;
import net.finmath.exception.CalculationException;
import net.finmath.fouriermethod.calibration.BoundConstraint;
import net.finmath.fouriermethod.calibration.CalibratedModel;
import net.finmath.fouriermethod.calibration.ScalarParameterInformationImplementation;
import net.finmath.fouriermethod.calibration.CalibratedModel.OptimizationResult;
import net.finmath.fouriermethod.calibration.models.CalibratableHestonModel;
import net.finmath.fouriermethod.products.EuropeanOption;
import net.finmath.fouriermethod.products.smile.EuropeanOptionSmileByCarrMadan;
import net.finmath.functions.HestonModel;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.volatilities.OptionSurfaceData;
import net.finmath.modelling.descriptor.HestonModelDescriptor;
import net.finmath.optimizer.OptimizerFactory;
import net.finmath.optimizer.OptimizerFactoryLevenbergMarquardt;
import net.finmath.optimizer.SolverException;
import net.finmath.time.daycount.DayCountConvention;
import net.finmath.time.daycount.DayCountConvention_ACT_365;
import net.finmath.time.businessdaycalendar.BusinessdayCalendar;
import net.finmath.time.businessdaycalendar. BusinessdayCalendarExcludingTARGETHolidays;

public class Exercise3_1 {
	
	
public static void main(String[] args) throws SolverException, CalculationException {
		
		
		
		// Load Market Data
	
		TreeMap<LocalDate, OptionSurfaceData> marketData = MarketDataProvider.getVolatilityDataContainer();
		
		// All available dates
		Set<LocalDate> keys = marketData.keySet();
		BusinessdayCalendar targetCalendar = new BusinessdayCalendarExcludingTARGETHolidays();	
		
		// Step 1: choose a reference day and calibrate the model.
		
		LocalDate today = LocalDate.of(2009, 5, 2);
		
		/*
		 * The while loop verifies whether the selected date is a valid business day. 
		 * If the reference date falls on a non-business day, 
		 * the loop advances to the next available trading day.
		 */
		while(true) 
		{
		if(!targetCalendar.isBusinessday(today)) {
			today = today.plusDays(1);
		}
		else
		{
			break; 
		}
		}
		
		
		
		double volatility = 0.15;
		double theta = 0.04;
		double kappa = 1.0;
		double xi = 0.5;
		double rho = -0.5;
		
		
		OptionSurfaceData data = marketData.get(today);
		DiscountCurve discountCurve = data.getDiscountCurve();
		DiscountCurve equityFowardCurve = data.getEquityForwardCurve();
		final double initialValue = equityFowardCurve.getValue(0.0);


		HestonModelDescriptor hestonModelDescription = new HestonModelDescriptor(
				today, 
				initialValue, 
				discountCurve,
				discountCurve,
				volatility, 
				theta, 
				kappa, 
				xi, 
				rho);
		
		final ScalarParameterInformationImplementation volatilityInformation = new ScalarParameterInformationImplementation(true, new BoundConstraint(0.01,1.0));
		final ScalarParameterInformationImplementation thetaInformation = new ScalarParameterInformationImplementation(true, new BoundConstraint(0.01,1.0));
		final ScalarParameterInformationImplementation kappaInformation = new ScalarParameterInformationImplementation(true, new BoundConstraint(0.01,20.0));
		final ScalarParameterInformationImplementation xiInformation = new ScalarParameterInformationImplementation(true, new BoundConstraint(0.01,5));
		final ScalarParameterInformationImplementation rhoInformation = new ScalarParameterInformationImplementation(true, new BoundConstraint(-0.99,0.99));
		
		final CalibratableHestonModel model = new CalibratableHestonModel( 
				hestonModelDescription,
				volatilityInformation,
				thetaInformation,
				kappaInformation,
				xiInformation,
				rhoInformation,
				false);


		final OptimizerFactory optimizerFactory = new OptimizerFactoryLevenbergMarquardt(300 /* maxIterations */, 2 /* maxThreads */);
		final double maturityForPricer = 30.0/252;
		final EuropeanOptionSmileByCarrMadan pricer = new EuropeanOptionSmileByCarrMadan(maturityForPricer, new double [] {initialValue});
		final double[] currentParameters = new double[] { volatility, theta, kappa, xi, rho};
		final double[] parameterStep = new double[] { 0.001,0.001,0.001,0.001,0.001};
		
		
		final CalibratedModel problem = new CalibratedModel(data, model, optimizerFactory, pricer , currentParameters,parameterStep);
		final OptimizationResult result = problem.getCalibration();
		final HestonModelDescriptor calibratedHestonDescriptor = (HestonModelDescriptor) result.getModel().getModelDescriptor();
		
		volatility = calibratedHestonDescriptor.getVolatility();
		theta = calibratedHestonDescriptor.getTheta();
		kappa = calibratedHestonDescriptor.getKappa();
		xi = calibratedHestonDescriptor.getXi();
		rho = calibratedHestonDescriptor.getRho();

		
		
		
		
		// Step 2 we sell today a call option on DAX with maturity 2 months and strike equals to 95% of spot price

		// Defines the duration of the hedging strategy
		int months = 2;
		// Defines the maturity of the option
		double optionMaturity = (double)months/12.0;
		// Defines the strike of the option
		double optionStrike = initialValue*0.95;
		// Retrieves the risk-free rate of the curve
		double riskFreeRate = ((net.finmath.marketdata.model.curves.DiscountCurveInterpolation) discountCurve).getZeroRate(optionMaturity);
		
		System.out.println("RISKFREERATE " + riskFreeRate);
		System.out.println(volatility);
		System.out.println(theta);
		System.out.println(kappa);
		System.out.println(xi);
		System.out.println(rho);
		
		// Create the Heston Model
		net.finmath.fouriermethod.models.HestonModel modelHS = 
				new net.finmath.fouriermethod.models.HestonModel (initialValue, riskFreeRate, volatility, riskFreeRate, theta, kappa, xi , rho);
		// We create the Call Option
		EuropeanOption callOptionOnDax = new EuropeanOption(optionMaturity, optionStrike);
		
		// We compute the pricing of the call using Heston Model
		double sellingPrice = callOptionOnDax.getValue(0.0, modelHS);
		System.out.println("We sell an option with maturity " + months  + " months, strike  " + 
		                   optionStrike +" spot price " + initialValue + "and price "+ sellingPrice);
		
		System.out.println();
		
		// This is when we stop the hedging portfolio
		LocalDate maturity = today.plusMonths(months);	
		
		/*
		 * The while loop verifies whether the selected maturity is a valid business day. 
		 * If the maturity falls on a non-business day, 
		 * the loop advances to the next available trading day
		 */
		while(true) {
			if(!targetCalendar.isBusinessday(maturity)) {
				maturity = maturity.plusDays(1);
			}else {
				break; 
			}
			}
		// Defines the day count convention
		DayCountConvention daycountConvention = new DayCountConvention_ACT_365();
		
		/*
		 * Step 3: delta Hedge
		 */
		
		// Defines the value of numeraire asset a time 0
		double bankAccountAtTimeIndex = 1.0;
		// Defines the values of underlying asset at time 0
		double underlyingAtTimeIndex = 0.0;
		
		// Calculates the initial quantity of the numeraire units
		double amountOfBankAccount = sellingPrice/bankAccountAtTimeIndex;
		// Initializes the quantity of the underlying asset at time zero
		double amountOfUnderlyingAsset = 0.0;
		// Initializes the delta of Heston Model
		double delta = 0.0;
	
		
		// Counts the number of hedging days 
		int hedgingDays = 0;
		
		LocalDate previousDate = today;
		/*
		 * This loop executes the dynamic hedging strategy from the initial time t = 0
		 * up to time t = T-1 (the trading day immediately preceding maturity).
		 */
		
		for(LocalDate currentDay : keys) {
			if(currentDay.isBefore(today)) continue;
			if(currentDay.plusDays(1).isAfter(maturity)) break;
			
			// Calculates the year fraction corresponding to the time step Δt
			double dayCountFraction = daycountConvention.getDaycountFraction(previousDate, currentDay);
			
			// Gets the current value of the underlying from the data
			OptionSurfaceData currentDayData = marketData.get(currentDay);
			underlyingAtTimeIndex = currentDayData.getEquityForwardCurve().getValue(0.0);
			
			// Updates the numeraire to include the latest data
			bankAccountAtTimeIndex *= Math.exp(riskFreeRate*dayCountFraction);
			
			// Time To maturity for the delta calculation
			double timeToMaturity = daycountConvention.getDaycountFraction(currentDay, maturity);
			
			// Calculates the delta 
			delta = HestonModel.hestonOptionDelta(100.0, riskFreeRate, 0.0, volatility, theta, kappa, xi, rho, timeToMaturity, optionStrike/underlyingAtTimeIndex*100);
			
			// The new number of stocks and the change of delta
			double newNumberOfStock = delta;
			double stocksToBuy = newNumberOfStock - amountOfUnderlyingAsset;
			
			// The variation of the numeraire required to finance the trade
			double numeraireToSell = stocksToBuy*underlyingAtTimeIndex/bankAccountAtTimeIndex;
			// The updated quantity of the numeraire asset
			double newNumberOfNumeraireAsset = amountOfBankAccount - numeraireToSell;
			
			System.out.println();
			System.out.println("Date: " + currentDay);
			System.out.println("Quantity of asset: " + newNumberOfStock);
			System.out.println("Stocks to buy: " + stocksToBuy);
			System.out.println("Quantity of numeraire: " + newNumberOfNumeraireAsset);
			System.out.println("Numeraire to sell: " + numeraireToSell);
			System.out.println();
			
			// Updates the portfolio holdings for the next step
			amountOfBankAccount = newNumberOfNumeraireAsset;
			amountOfUnderlyingAsset = newNumberOfStock;
		
			// Updates the reference date for the next iteration
			previousDate = currentDay;
			
			// Updates the counter 
			hedgingDays++;
			
		}
		
		System.out.println("Hedging days: " + hedgingDays);
		
		// Retrieves the market data available at maturity
		OptionSurfaceData finalDayData = marketData.get(maturity);
		// Retrieves the underlying spot price at maturity (S_T)
		double underlyingAtMaturity  = finalDayData.getEquityForwardCurve().getValue(0.0);
		
		// Calculates the final year fraction corresponding to the last time step Δt = T - (T-1)
		double finalTimeStep = daycountConvention.getDaycountFraction(previousDate, maturity);
		// Computes the numeraire at maturity
		bankAccountAtTimeIndex *= Math.exp(riskFreeRate * finalTimeStep);
		
		// Calculates the portfolio value
		double portfolioValue = (amountOfBankAccount*bankAccountAtTimeIndex) + (amountOfUnderlyingAsset*underlyingAtMaturity);
		
		// Calculate the payoff of the option at maturity
		double payoffAtMaturity = Math.max(underlyingAtMaturity-optionStrike, 0.0);
		
		System.out.println();
		
		System.out.println("Underlying at maturity " + underlyingAtMaturity);
		System.out.println("Strike of the option " + optionStrike);
		System.out.println("The final value of the portfolio is " + portfolioValue);
		System.out.println("The payoff of the option is " + payoffAtMaturity);
		System.out.println("The hedging error is  " + (portfolioValue - payoffAtMaturity));
		
	}

}




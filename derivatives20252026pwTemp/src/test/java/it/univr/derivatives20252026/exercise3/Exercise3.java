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

public class Exercise3 {

	public static void main(String[] args) throws SolverException, CalculationException {
		
		
		/*
		 * Load Market Data
		 */
		TreeMap<LocalDate, OptionSurfaceData> marketData = MarketDataProvider.getVolatilityDataContainer();
		
		//all available dates
		Set<LocalDate> keys = marketData.keySet();
				
		
		//Step 1: choose a reference day and calibrate the model.
		LocalDate today = LocalDate.of(2006, 1, 2);
	


		//Step 1 Create a Heston model
		
		
		
		//calibration code here
		
		
		
		
		//Step 2 We sell today one option with maturity 1 month
		int months = 2;
		double optionMaturity = 0.0;
		double optionStrike = 0.0;
		
		
		//System.out.println("We sell an option with maturity " + months  + " months, strike  " + optionStrike +" and price "+ sellingPrice);

		//System.out.println();
		
		//This is when we stop the hedging portfolio
		LocalDate maturity = today.plusMonths(months);		
		DayCountConvention daycountConvention = new DayCountConvention_ACT_365();
		
		
		
		/*
		 * Step 3: delta Hedge
		 */
		
		//just initialize your holdings.
		//double bankAccountAtTimeIndex = ;
		
		
		//double amountOfBankAccount = ;	
		//double amountOfUnderlyingAsset = ;
		
		DecimalFormat formatter = new DecimalFormat("0000.##");
		
		/*
		 * main loop
		 */
		LocalDate previousDate = today;
		for(LocalDate currentDay : keys) {
			
			if(currentDay.plusDays(1).isAfter(maturity)) {
				break;
			}
			
			//Calculate the Day Count Fraction
			double dayCountFraction = daycountConvention.getDaycountFraction(previousDate, currentDay);
			
			//Get the current value of the underlying from the data
			
			//Update the bank account to include the latest data			
			
			//Time To maturity for the delta calculation
			double timeToMaturity = daycountConvention.getDaycountFraction(currentDay, maturity);
			
			//Calculate the delta
			
			//the new number of stocks and the change of delta
			
			//the change in the bank account
		
			previousDate = currentDay;
			
			
		}
		
		double underlyingAtMaturity = 0.0;
		
		double portfolioValue = 0.0;
		
		double payoffAtMaturity = 0.0;
		
		System.out.println();
		
		System.out.println("Underlying at maturity " + underlyingAtMaturity);
		System.out.println("Strike of the option " + optionStrike);
		System.out.println("The final value of the portfolio is " + portfolioValue);
		System.out.println("The payoff of the option is " + payoffAtMaturity);
		System.out.println("The hedging error is  " + (portfolioValue - payoffAtMaturity));
		
	}

}

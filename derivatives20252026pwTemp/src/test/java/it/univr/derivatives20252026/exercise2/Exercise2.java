package it.univr.derivatives20252026.exercise2;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import it.univr.derivatives.marketdataprovider.MarketDataProvider;
import net.finmath.exception.CalculationException;
import net.finmath.fouriermethod.calibration.BoundConstraint;
import net.finmath.fouriermethod.calibration.CalibratedModel;
import net.finmath.fouriermethod.calibration.ScalarParameterInformationImplementation;
import net.finmath.fouriermethod.calibration.CalibratedModel.OptimizationResult;
import net.finmath.fouriermethod.calibration.models.CalibratableHestonModel;
import net.finmath.fouriermethod.models.CharacteristicFunctionModel;
import net.finmath.fouriermethod.models.HestonModel;
import net.finmath.fouriermethod.products.EuropeanOption;
import net.finmath.fouriermethod.products.smile.EuropeanOptionSmileByCarrMadan;
import net.finmath.integration.SimpsonRealIntegrator;
import net.finmath.integration.TrapezoidalRealIntegrator;
import net.finmath.marketdata.model.curves.DiscountCurve;
import net.finmath.marketdata.model.volatilities.OptionSurfaceData;
import net.finmath.modelling.descriptor.HestonModelDescriptor;
import net.finmath.optimizer.OptimizerFactory;
import net.finmath.optimizer.OptimizerFactoryLevenbergMarquardt;
import net.finmath.optimizer.SolverException;

public class Exercise2 {

	public static void main(String[] args) throws SolverException, CalculationException {
		/*
		 * Application of the Carr Madan formula for the
		 * static replication of a Variance swap.
		 */
		
		
		
		
		//Step 1 Create a Heston model
		
		
	
		
		/*
		 * Get the calibrated parameters
		 */
		/*volatility = calibratedHestonDescriptor.getVolatility();
		theta = calibratedHestonDescriptor.getTheta();
		kappa = calibratedHestonDescriptor.getKappa();
		xi = calibratedHestonDescriptor.getXi();
		rho = calibratedHestonDescriptor.getRho(); */
		
		/*
		 * Use the closed-form formula for the price of the variance swap
		 */
		double maturity = 30.0/252;
		double fairVarSwapRate = 0.0;
		
		System.out.println("The fair Variance Swap Rate is " + fairVarSwapRate);
		
		
		
		
		
		/*
		 * Second approach
		 * 
		 */
		
		
		
		/*
		TrapezoidalRealIntegrator integratorCalls2 = 
				new TrapezoidalRealIntegrator(daxForwardPrice*1.01, 4*daxForwardPrice, 300);*/
		
		
		
		/*DoubleUnaryOperator integrandCalls2 = x ->
		
		{
			try {
				return new net.finmath.fouriermethod.products.
						EuropeanOption(maturity, x)
						.getValue(modelHS)/(x*x);
			} catch (CalculationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0.0;
		};*/
		
	}

}

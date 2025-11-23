package it.univr.derivatives.varioustests;

import java.time.LocalDate;
import java.util.TreeMap;

import it.univr.derivatives.marketdataprovider.MarketDataProvider;
import it.univr.derivatives.utils.TimeSeries;
import net.finmath.marketdata.model.volatilities.OptionSurfaceData;

public class TimeSeriesTest {

	public static void main(String[] args) throws Exception {
		
		TimeSeries daxTimeSeries = MarketDataProvider.getDaxData();
		
		daxTimeSeries.plot("Dax Index");
		
		
		TimeSeries logReturns = daxTimeSeries.computeLogReturns();
		
		logReturns.plot("Dax Returns");
		
		TimeSeries valatilitySurface = new TimeSeries();

	}
}
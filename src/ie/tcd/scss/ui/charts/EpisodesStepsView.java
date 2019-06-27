package ie.tcd.scss.ui.charts;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class EpisodesStepsView extends JPanel /*implements EnvironmentListener */{


	private static final long serialVersionUID = 7345652429090425458L;

	protected XYSeries stepsSerie;
	protected XYSeries bestSerie;
	protected XYSeries exploitationSerie;

	protected JFreeChart chart;

	public EpisodesStepsView() {


		XYSeriesCollection seriesCollection = new XYSeriesCollection();



		this.stepsSerie = new XYSeries("Steps per episode while learning");

		seriesCollection.addSeries(stepsSerie);


		this.bestSerie = new XYSeries("Best steps per episode while learning");

		seriesCollection.addSeries(bestSerie);


		this.exploitationSerie = new XYSeries("Exploitaiton steps");

		seriesCollection.addSeries(exploitationSerie);


		XYDataset dataSet = seriesCollection;

		chart = ChartFactory.createXYLineChart("Steps per episode", "Episode", "Steps", dataSet);


		ChartPanel myChart = new ChartPanel(chart);


		XYPlot plot = chart.getXYPlot();
		ValueAxis range = plot.getRangeAxis();

		range.setRange(0.0, 10100);


		this.setLayout(new BorderLayout());
		this.add(myChart,BorderLayout.CENTER);
		this.validate();

		this.validate();
	}




}



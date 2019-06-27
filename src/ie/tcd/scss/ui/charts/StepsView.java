package ie.tcd.scss.ui.charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYInterval;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ie.tcd.scss.agents.Agent;
import ie.tcd.scss.ui.listeners.TrainableObserver;


import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class StepsView extends JPanel implements TrainableObserver {


	private static final long serialVersionUID = 7345652429090425458L;

	protected XYSeries stepsSerie;
	protected XYSeries bestStepsSerie;

	protected JFreeChart chart;

	protected long trainings = 0;

	private HashMap<Integer, XYSeries> series = new HashMap<Integer, XYSeries>();

	XYSeriesCollection seriesCollection;

	private static int number = 0;

	public StepsView(String title, String subtitle) {


		this.seriesCollection = new XYSeriesCollection();


		this.stepsSerie = new XYSeries("Steps per episode");

		seriesCollection.addSeries(stepsSerie);


		this.bestStepsSerie = new XYSeries("Best performance so far");

		seriesCollection.addSeries(bestStepsSerie);

		XYDataset dataSet = seriesCollection;

		chart = ChartFactory.createXYLineChart(title, "Episode" + subtitle, "Steps", dataSet);



		ChartPanel myChart = new ChartPanel(chart);


		XYPlot plot = chart.getXYPlot();
		ValueAxis range = plot.getRangeAxis();


		Color background = new Color(230, 230, 230);
		plot.setBackgroundPaint(background);

		range.setRange(0.0, 10100);


		this.setLayout(new BorderLayout());
		this.add(myChart,BorderLayout.CENTER);
		this.validate();

	}



	@Override
	public void trainingResult(long episode, long steps, long bestScoreEver, int representation) {

		this.stepsSerie.add(episode,steps,true);

		this.bestStepsSerie.add(trainings, bestScoreEver, true);
	}



	@Override
	public void updatePolicy(Agent agent) {
	}


	@Override
	public void save(String title) {

		File file = new File("StepView_" + title+ ".pdf");

		try  { 
			OutputStream out = new FileOutputStream(file);
			com.itextpdf.text.Rectangle pagesize = new com.itextpdf.text.Rectangle(this.getWidth(), this.getHeight() ); 
			Document document = new Document( pagesize, 50, 50, 50, 50 ); 
			PdfWriter writer = PdfWriter.getInstance( document, out ); 
			document.open(); 
			PdfContentByte cb = writer.getDirectContent(); 
			PdfTemplate tp = cb.createTemplate( this.getWidth(), this.getHeight() ); 
			Graphics2D g2 = tp.createGraphics( this.getWidth(), this.getHeight(), new DefaultFontMapper() ); 
			java.awt.geom.Rectangle2D r2D = new java.awt.geom.Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight() ); 
			chart.draw(g2, r2D); 
			g2.dispose(); 
			cb.addTemplate(tp, 0, 0); 
			document.close(); 
		} 
		catch (Exception ex) { 
			ex.printStackTrace();
		} 

	}



}



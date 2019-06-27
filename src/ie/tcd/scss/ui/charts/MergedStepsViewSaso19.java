package ie.tcd.scss.ui.charts;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYInterval;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.SortOrder;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;


import ie.tcd.scss.agents.Agent;
import ie.tcd.scss.ui.listeners.TrainableObserver;


public class MergedStepsViewSaso19 extends JPanel implements TrainableObserver {

	protected static String SEPARATOR = ",";

	private static final long serialVersionUID = 7345652429090425458L;

	private static int number = 0;

	protected JFreeChart chart;

	protected long trainings = 0;

	private ArrayList<XYSeries> series = new ArrayList<XYSeries>();

	XYSeriesCollection seriesCollection;

	public MergedStepsViewSaso19() {


		this.seriesCollection = new XYSeriesCollection();


		XYSeries stepsSerie1 = new XYSeries("Sensor-based state space (Grid)");

		this.series.add(stepsSerie1);


		XYSeries stepsSerie2 = new XYSeries("ML-GNG");

		this.series.add(stepsSerie2);


		XYSeries stepsSerie3 = new XYSeries("Con-RL");

		this.series.add(stepsSerie3);

		XYSeries stepsSerie4 = new XYSeries("GNG-Q");

		this.series.add(stepsSerie4);



		seriesCollection.addSeries(stepsSerie3);

		seriesCollection.addSeries(stepsSerie4);


		seriesCollection.addSeries(stepsSerie2);

		seriesCollection.addSeries(stepsSerie1);



		XYDataset dataSet = seriesCollection;

		chart = ChartFactory.createXYLineChart("", "Episode", "Steps", dataSet);

		java.awt.Font font = new java.awt.Font("Cambria", 1, 24);

		java.awt.Font font2 = new java.awt.Font("Cambria", 0, 20);

		java.awt.Font font3 = new java.awt.Font("Cambria", 1, 24);



		ChartPanel myChart = new ChartPanel(chart);


		XYPlot plot = chart.getXYPlot();


		plot.setBackgroundPaint(Color.white);

		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);

		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);

		ValueAxis range = plot.getRangeAxis();

		range.setLabelFont(font);

		range.setTickLabelFont(font2);

		range.setRange(0.0, 10100);

		range = plot.getDomainAxis();

		range.setLabelFont(font);

		range.setTickLabelFont(font2);

		range.setRange(0, 410);


		chart.getLegend().setItemFont(font3);

		chart.getLegend().setSortOrder(SortOrder.DESCENDING);


		Color red = new Color(215,25,28);
		plot.getRenderer().setSeriesPaint(0, red);

		Color green = new Color(241,182,218);
		plot.getRenderer().setSeriesPaint(1, green);

		Color blue = new Color(44, 123, 182);

		Color yellow = new Color(253,174,97);//51,160,44);

		plot.getRenderer().setSeriesPaint(2, yellow);


		plot.getRenderer().setSeriesPaint(3, blue);




		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

		renderer.setLegendLine(new Line2D.Double(-20d,0d,20d,0d));

		renderer.setDrawSeriesLineAsPath(true);

		Stroke stroke1 = new BasicStroke(2.5f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 1.0f, new float[] { 3.0f }, 0.0f);
		Stroke stroke2 = new BasicStroke(
				2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
				1.0f, new float[] {10.0f, 5.0f}, 0.0f
				);

		Stroke stroke3 = new BasicStroke(2.5f);

		Stroke stroke4 = new BasicStroke(
				2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
				1.0f, new float[] {15.0f, 5.0f, 2.0f, 5.0f}, 0.0f
				);

		plot.getRenderer().setSeriesStroke(0, stroke2);

		plot.getRenderer().setSeriesStroke(1, stroke1);
		plot.getRenderer().setSeriesStroke(2, stroke3);
		plot.getRenderer().setSeriesStroke(3, stroke4);


		plot.setSeriesRenderingOrder(SeriesRenderingOrder.REVERSE);


		this.setLayout(new BorderLayout());
		this.add(myChart,BorderLayout.CENTER);
		this.validate();

	}



	@Override
	public void trainingResult(long episode, long steps, long bestScoreEver, int representation) {

		if(representation>=2)
			representation=2;


		if(representation==-1)
			representation=3;

		this.series.get(representation).add(episode,steps,true);


	}



	@Override
	public void updatePolicy(Agent agent) {

	}



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

		this.toCSV(title);

	}



	private void toCSV(String title) {
		File csvFile = new File("MergedSteps"+title+".csv");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));


			//first write the header:
			String line = "";


			line += "Episode";
			line += SEPARATOR;

			for(XYSeries series : this.series) {

				line += series.getKey();
				line += SEPARATOR;

			}

			line+="\n";


			writer.append(line);
			writer.flush();


			String plots = "";

			//then write the data
			for(int episode = 0 ; episode <400 ; ++episode) {

				plots += episode;
				plots += SEPARATOR;

				for(XYSeries series : this.series) {

					plots += Double.min((double) series.getY(episode),10000.0);
					plots += SEPARATOR;

				}

				plots+="\n";


			}


			writer.append(plots);
			writer.flush();

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void loadfromCSV(String title) {
		File csvFile = new File(title);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(csvFile));

			//skip first line
			reader.readLine();

			for(int i = 0; i<400; ++i) {

				String line = reader.readLine();

				String[] values = line.split(SEPARATOR);

				for(int j=0;j<4;++j) {

					double steps = Double.parseDouble(values[j+1]);

					this.series.get(j).add(i, steps);
				}

			}


			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}



package ie.tcd.scss.ui.charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import ie.tcd.scss.agents.discretization.clustering.algorithms.gng.Node;
import ie.tcd.scss.learning.Action;
import ie.tcd.scss.ui.listeners.AggregatorListener;


public class GNGView extends JPanel implements AggregatorListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1326023920073268788L;

	JFreeChart chart;
	
	private ArrayList<Action> actions;

	private XYSeriesCollection seriesCollection;
	
	public GNGView(ArrayList<Action> actions, int granularityX, int granularityY) {

		this.actions = actions;
		
		this.seriesCollection = new XYSeriesCollection();

		XYSeries series1 = new XYSeries("left");
		seriesCollection.addSeries(series1);


		XYSeries series2 = new XYSeries("neutral");
		seriesCollection.addSeries(series2);

		XYSeries series3 = new XYSeries("right");
		seriesCollection.addSeries(series3);


			
		XYDataset dataSet = this.seriesCollection;
	
		chart = ChartFactory.createScatterPlot("", "States indices x",  "States indices y", dataSet);
		
		
		java.awt.Font font = new java.awt.Font("Cambria", 1, 24);

		java.awt.Font font2 = new java.awt.Font("Cambria", 0, 20);

		java.awt.Font font3 = new java.awt.Font("Cambria", 1, 24);

		
		XYPlot plot = chart.getXYPlot();
		ValueAxis range = plot.getRangeAxis();
		range.setRange(-0.5, granularityX+0.5);
		range.setLabelFont(font);

		range.setTickLabelFont(font2);
		
		range = plot.getDomainAxis();
		range.setRange(-0.5, granularityY+0.5);
		range.setLabelFont(font);

		range.setTickLabelFont(font2);

		ChartPanel myChart = new ChartPanel(chart);
		
		chart.getLegend().setItemFont(font3);
		
		

		Color green = new Color(56, 242, 24);
		plot.getRenderer().setSeriesPaint(0, green);

		plot.getRenderer().setSeriesShape(0, new Rectangle2D.Double(-5,-5,10,10));
		plot.getRenderer().setSeriesShape(1, new Ellipse2D.Double(-5,-5,10,10));
		plot.getRenderer().setSeriesShape(2, ShapeUtilities.createUpTriangle(6));
	
		
		Color background = new Color(230, 230, 230);
		plot.setBackgroundPaint(background);
		
		this.setLayout(new BorderLayout());
		this.add(myChart,BorderLayout.CENTER);
		this.validate();
		
	}
	

	@Override
	public void update(ArrayList<Node> nodes) {

		
		for(int i=0; i<this.actions.size();++i) {
			this.seriesCollection.getSeries(i).clear();
		}

	

		for(Node n : nodes) {
			this.seriesCollection.getSeries(this.actions.indexOf(n.getAction())).add(n.getPosition().get(0),n.getPosition().get(1),true);
		}
		
		
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

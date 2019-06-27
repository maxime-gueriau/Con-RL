package ie.tcd.scss.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class ResultsGenerator {

	private String filename;

	protected BufferedWriter writer;

	protected static String SEPARATOR = ",";


	public ResultsGenerator(String folder, String name) {

		File directory = null;

		if(folder!=null && folder!="") {
			directory = new File(folder);
			if(!directory.exists()) {
				directory.mkdir();
			}
		} 
		try {
			if(directory!=null)
				this.filename = directory.getCanonicalPath() + File.separator + name + "_results"+".csv";
			else 
				this.filename = "results_" + name + ".csv";
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		File file = new File(this.filename);
		try {
			this.writer = new BufferedWriter(new FileWriter(file));

		} catch (IOException e) {
			e.printStackTrace();
		}



		//first write the header:
		String line = "";

		line += "run";
		line += SEPARATOR;

		line += "simulation best score";
		line += SEPARATOR;

		line += "simulation best episode";
		line += SEPARATOR;
		
		line += "grid best score";
		line += SEPARATOR;

		line += "grid best episode";
		line += SEPARATOR;

		line += "gng best score";
		line += SEPARATOR;

		line += "gng best episode";
		line += SEPARATOR;

		line += "gng corresponding episode";
		//line += SEPARATOR;

		//end the line
		line+="\n";

		try {
			this.writer.append(line);
			this.writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addLine(long run, long simulationBestScore, long simulationBestEpisode, long gridBestScore, long gridBestEpisode, long gngBestScore, long gngBestEpisode, long gngCorrespondingEpisode) {
		//start with an empty line
		String line = "";
		
		// fill it with data
		line += run;
		line += SEPARATOR;
		
		line += simulationBestScore;
		line += SEPARATOR;
		
		line += simulationBestEpisode;
		line += SEPARATOR;
		
		
		line += gridBestScore;
		line += SEPARATOR;
		
		line += gridBestEpisode;
		line += SEPARATOR;
		
		line += gngBestScore;
		line += SEPARATOR;
		
		line += gngBestEpisode;
		line += SEPARATOR;
		
		line += gngCorrespondingEpisode;
		//line += SEPARATOR;
		
		//end the line
		line+="\n";
		try {
			this.writer.append(line);
			this.writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
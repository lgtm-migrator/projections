package projections.Tools.MemoryUsage;



import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.MenuBar;
import java.awt.Stroke;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import projections.analysis.GenericLogReader;
import projections.analysis.ProjDefs;
import projections.analysis.ThreadManager;
import projections.gui.IntervalChooserPanel;
import projections.gui.MainWindow;
import projections.gui.OrderedIntList;
import projections.gui.ProjectionsWindow;
import projections.gui.RangeDialog;
import projections.gui.U;
import projections.gui.Util;
import projections.misc.LogEntryData;

public class MemoryUsageWindow extends ProjectionsWindow {

	// Temporary hardcode. This variable will be assigned appropriate
	// meaning in future versions of Projections that support multiple
	// runs.
	private int myRun = 0;

	/** For each PE record a set of (time, memusage) pairs */
	TreeMap<Long,XYSeries> memorySamples; 

	private MemoryUsageWindow thisWindow;
	private MainWindow mainWindow;
	private long intervalSize;

	Vector<String> availableStepStrings;
	Vector<Long> availableStepTimes;
	
	IntervalChooserPanel intervalPanel;

	public MemoryUsageWindow(MainWindow mainWindow)
	{
		super(mainWindow);
		thisWindow = this;
		this.mainWindow = mainWindow;

		memorySamples = new TreeMap<Long,XYSeries>(); 

		setForeground(Color.lightGray);
		setTitle("Memory Usage - " + MainWindow.runObject[myRun].getFilename() + ".sts");

		createMenus();
		pack();
		showDialog();
	}



	private void createMenus()
	{
		MenuBar mbar = new MenuBar();

		mbar.add(Util.makeMenu("File", new Object[]
		                                          {
				"Close"
		                                          }, this));
		mbar.add(Util.makeMenu("Modify", new Object[]
		                                            {
				"Set Range"
		                                            }, this));
		setMenuBar(mbar);
	} 

	public void showDialog()
	{
		try {
			if (dialog == null) {
				intervalPanel = new IntervalChooserPanel();
				dialog = new RangeDialog(this, "Select Range", intervalPanel, false);
			}
			dialog.displayDialog();
			if (!dialog.isCancelled()) {
				final OrderedIntList pes = dialog.getSelectedProcessors();
				final long startTime = dialog.getStartTime();
				final long endTime = dialog.getEndTime();
				intervalSize = intervalPanel.getIntervalSize();
				final long startInterval = intervalPanel.getStartInterval();
				final long endInterval = intervalPanel.getEndInterval();

				final SwingWorker worker = new SwingWorker() {
					public Object doInBackground() {
						// Load memory usages here
						thisWindow.loadData(startTime, endTime, pes, intervalSize, startInterval, endInterval);
						return null;
					}

					public void done() {
						thisWindow.createPlot();
					}
				};
				worker.execute();
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}



	private void createPlot() {
		thisWindow.setVisible(false);

		Iterator<Long> pes = memorySamples.keySet().iterator();
		XYSeriesCollection seriesCollection = new XYSeriesCollection();
		int totalSampleCount = 0;
		while(pes.hasNext()){
			Long pe = pes.next();
			XYSeries series = memorySamples.get(pe);
			totalSampleCount += series.getItemCount();
			seriesCollection.addSeries(series);
		}

		if(totalSampleCount == 0){
			JOptionPane.showMessageDialog(this, "No memory usage entries found in log files. Add calls to traceMemoryUsage() in the application", "Warning", JOptionPane.WARNING_MESSAGE);
			Container windowPane = thisWindow.getContentPane();
			windowPane.removeAll();
			windowPane.setLayout(new BorderLayout());
			windowPane.add(new JLabel("No memory log entries found!"), BorderLayout.CENTER);
			thisWindow.setSize(400,400);
			thisWindow.validate();
			thisWindow.repaint();
			thisWindow.setVisible(true);
			return;
		}

		JFreeChart chart = ChartFactory.createScatterPlot(
				"Memory Usage (at " + U.humanReadableString(intervalSize) + " resolution)",
				"Time (us)",
				"MB",
				seriesCollection,
				PlotOrientation.VERTICAL,
				true,
				true,
				false
		) ;


		XYDotRenderer renderer = new XYDotRenderer();
		renderer.setDotWidth(2);
		renderer.setDotHeight(2);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setRenderer(renderer);


		// Add markers to mark each iteration:
		determineStepsFromPEZero();
		for(int i=0; i<availableStepTimes.size(); i++){
			ValueMarker m = new ValueMarker(availableStepTimes.elementAt(i), Color.darkGray, new BasicStroke(2.0f) );
//			m.setLabel(this.availableStepStrings.elementAt(i));
			plot.addDomainMarker(m);
		}

		// Put the chart in a JPanel that we can use inside our program's GUI
		ChartPanel chartpanel = new ChartPanel(chart);
		chart.setBackgroundPaint(Color.white);

		chartpanel.setPreferredSize(new Dimension(1100,700));

		Container windowPane = thisWindow.getContentPane();
		windowPane.removeAll();
		windowPane.setLayout(new BorderLayout());
		windowPane.add(chartpanel, BorderLayout.CENTER);

		thisWindow.pack();
		thisWindow.setVisible(true);
	}


	private void loadData(final long startTime, final long endTime, final OrderedIntList processorList, long intervalSize, long startInterval, long endInterval) {

		if( MainWindow.runObject[myRun].hasLogFiles() || MainWindow.runObject[myRun].hasSumDetailFiles() ) {
			// Do parallel loading because we have full logs

			// Create a list of worker threads
			LinkedList<Thread> readyReaders = new LinkedList<Thread>();

			while (processorList.hasMoreElements()) {
				int nextPe = processorList.nextElement();
				readyReaders.add( new ThreadedFileReader(nextPe, myRun, intervalSize, startInterval, endInterval));
			}

			// Determine a component to show the progress bar with
			Component guiRootForProgressBar = null;
			if(thisWindow!=null && thisWindow.isVisible()) {
				guiRootForProgressBar = thisWindow;
			} else if(mainWindow!=null && mainWindow.isVisible()){
				guiRootForProgressBar = mainWindow;
			} else if(MainWindow.runObject[myRun].guiRoot!=null && MainWindow.runObject[myRun].guiRoot.isVisible()){
				guiRootForProgressBar = MainWindow.runObject[myRun].guiRoot;
			}

			// Pass this list of threads to a class that manages/runs the threads nicely
			ThreadManager threadManager = new ThreadManager("Loading Time Profile in Parallel", readyReaders, guiRootForProgressBar, true);
			threadManager.runThreads();

			memorySamples = new TreeMap(); 

			Iterator<Thread> titer = readyReaders.iterator();
			while(titer.hasNext()){
				ThreadedFileReader r = (ThreadedFileReader) titer.next();
				memorySamples.put(r.getPe(), r.getMemorySamples());
			}


		}

	}

	
	private Vector<Long> determineStepsFromPEZero() {
		// Labels containing the user notes found in the log
		availableStepStrings = new Vector<String>();
		availableStepTimes = new Vector<Long>();

		if (!(MainWindow.runObject[myRun].hasLogData())){
			return availableStepTimes;
		}

		try{
			int PE = 0;
			GenericLogReader reader = new GenericLogReader(PE, MainWindow.runObject[myRun].getVersion());

			int c = 0;
			while (true) {
				LogEntryData data = reader.nextEvent();

				if(data.type == ProjDefs.USER_SUPPLIED_NOTE){
					if(data.note.contains("***")){
						String pruned = data.note.replace("*** ", "");
						availableStepStrings.add("" + (c++) + ": " + pruned);
						availableStepTimes.add(data.time);
					}
				}
			}

		} catch (Exception e) {
		}

		return availableStepTimes;

	}




}

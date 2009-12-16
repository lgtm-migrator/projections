package projections.Tools.Timeline;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


/** A class that analyzes the user supplied data ranges, and displays a table of the results */
class UserSuppliedAnalyzer extends JFrame {
	private Data data;
	
	
	UserSuppliedAnalyzer(Data data){
		this.data = data;
		createLayout();
	}
	

	private void createLayout(){
		setTitle("Analysis of user supplied values(timesteps)");

		TreeMap<Integer, Long> parameterMinTime = new TreeMap();
		TreeMap<Integer, Long> parameterMaxTime = new TreeMap();
		
		
		/* TreeMap<Integer,LinkedList<EntryMethodObject> > */ 
		Iterator iter = data.allEntryMethodObjects.values().iterator();

		while(iter.hasNext()){
			List objs = (List) iter.next();
			Iterator objIter = objs.iterator();
			while(objIter.hasNext()){
				EntryMethodObject obj = (EntryMethodObject) objIter.next();
				Integer param = obj.userSuppliedData;
				long start = obj.getBeginTime();
				long end = obj.getEndTime();
				
				if(param != null){
					if(parameterMinTime.containsKey(param)){
						// update the minimum seen for the user supplied parameter param
						Long oldval = parameterMinTime.get(param);
						if(start < oldval){
							parameterMinTime.put(param, new Long(start));
						}

						// update the maximum seen for the user supplied parameter param
						oldval = parameterMaxTime.get(param);
						if(end > oldval){
							parameterMaxTime.put(param, new Long(end));
						}						
					}
					else {
						// first time we see the values, just insert them
						parameterMinTime.put(param, new Long(start));
						parameterMaxTime.put(param, new Long(end));
					}
				}

			}

		}


		
		// create a table of the data
		Vector columnNames = new Vector();
		columnNames.add(new String("User Supplied Value(timestep)"));
		columnNames.add(new String("Earliest Begin Time"));
		columnNames.add(new String("Latest End Time"));
		columnNames.add(new String("Duration"));
		columnNames.add(new String("Last Begin to This Begin"));
		columnNames.add(new String("Last End to This End"));
		
		Vector data  = new Vector();

		
		Iterator i = parameterMinTime.keySet().iterator();
		Long prevMin=null;
		Long prevMax=null;
		while(i.hasNext()){
			Integer param = (Integer) i.next();
			Long min = parameterMinTime.get(param);
			Long max = parameterMaxTime.get(param);
			Vector row = new Vector();
			row.add(param);
			row.add(min);
			row.add(max);
			row.add(max-min);
		
			if(prevMin==null)
				row.add("");
			else
				row.add(min-prevMin);
			
			if(prevMax==null)
				row.add("");
			else
				row.add(max-prevMax);
			
			prevMin = min;
			prevMax = max;
			data.add(row);	
		}
		
		// If we didn't find any values, put some other text in the table
		
		if(parameterMinTime.size() == 0){
			JLabel msg = new JLabel("<html><body>No User Supplied Values Found in the currently loaded timeline.<br> Try selecting a different time range, or add calls to <br><font color=blue><tt>traceUserSuppliedData(int value)</tt></font> to the program.</body></html>");
			JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
			
			Vector row = new Vector();
			row.add(new String("No data found"));
			row.add(new String(""));
			row.add(new String(""));
			row.add(new String(""));
			row.add(new String(""));
			row.add(new String(""));
			data.add(row);
		}
				
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);

		JTable table = new JTable(tableModel);

		// put the table into a scrollpane

		JScrollPane scroller = new JScrollPane(table);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// put the scrollpane into our guiRoot

		this.setContentPane(scroller);
		
		// Display it all

		pack();
		setSize(800,400);
		setVisible(true);
		
	}
	
	

}

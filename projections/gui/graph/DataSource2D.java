/**
 * DataSource2D-- provide simple 2D data to be graphed.
 **/

package projections.gui.graph;

import projections.gui.*;
import java.awt.Color;
import java.awt.Component;

public class DataSource2D extends DataSource
{
    private String title;
    private double[][] data; /*The data to be graphed*/
    private int xValues;
    private PopUpAble parent;
    
    public DataSource2D(String title_, double[][] data_) {
	title=title_;
	data=data_;
	xValues = data.length;
	parent = null;
    }
  
    public DataSource2D(String title_, double[][] data_, 
			PopUpAble parent_) {
	title=title_;
	data=data_;
	parent=parent_;
	xValues = data.length;
    }
  
    public String[] getPopup(int xVal, int yVal) {
	if (parent == null) {
	    return null;	
	} 
	return parent.getPopup(xVal, yVal);
    }

    public String getTitle() {
	return title;
    }

    public int getIndexCount() {
	return xValues;
    }
	
    public int getValueCount() {
	// assuming that all x values have equal number of 
	// corresponding y values 
	return data[0].length; 
    }  
  
    public void getValues(int index,double[] values)
    {
	for(int j=0;j<data[index].length;j++)
	    values[j]=(double)data[index][j];
    }
}

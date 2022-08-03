import java.util.ArrayList;
import java.util.Collections;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.HyperStackConverter;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;

public class Analyze_Particles_ColorMap_Plugin implements PlugIn{
	/** Version **/
	String version= "v1.0.0";
	
	/** Date **/
	String date= "220802";
	
	/** Stores the active ResultsTable **/
	ResultsTable rt=null; 
	
	/** Stores the active RoiManager **/
	RoiManager rm=null;
	
	/** Stores the ImagePlus to work on **/
	ImagePlus ip=null; 
	
	/** Stores the name of the parameter to draw **/
	String paramName=null; 
	
	/** Stores the parameter to draw **/
	double[] param=null; 
	
	/** Stores the LUT to be used **/
	String lut="Grays";
	
	@Override
	public void run(String arg) {
		if(performChecks()) {
			if(GUI()) {
				createColorMap().show();
			}
		}
	}
	
	/**
	 * Stores a reference to the active ResultsTable and RoiManager and performs coherence checks 
	 * @return true if both are non null and have the same number of elements, false otherwise
	 */
	@SuppressWarnings("static-access")
	private boolean performChecks() {
		rt=ResultsTable.getResultsTable();
		rm=RoiManager.getInstance();
		
		//Is Results Table opened, not empty and visible ?
		if(rt==null || rt.getResultsWindow()==null) {
			IJ.error("Missing a ResultsTable");
			return false;
		}
		
		if(rt.getCounter()==0) {
			IJ.error("The ResultsTable should not be empty");
			return false;
		}
		
		//Is RoiManager opened and not empty ?
		if(rm==null) {
			IJ.error("Missing a RoiManager");
			return false;
		}
		
		if(rm.getCount()==0) {
			IJ.error("The RoiManager should not be empty");
			return false;
		}
		
		//Are the number of lines coherent with the number of ROIs ?
		if(rt.getCounter()!=rm.getCount()) {
			IJ.error("The RoiManager and the ResultsTable should have the same number of items");
			return false;
		}
		
		//At least one image should be opened
		if(WindowManager.getImageCount()==0) {
			IJ.error("At least one image should be opened");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Displays the GUI and stores the user's choices
	 */
	public boolean GUI() {
		String[] images=WindowManager.getImageTitles();
		
		//Removes the "Label" header if it exists
		ArrayList<String> headersList=new ArrayList<String>();
		Collections.addAll(headersList, rt.getHeadings());
		headersList.remove("Label");
		String[] headers=headersList.toArray(new String[headersList.size()]);
		
		String[] luts=IJ.getLuts();
		
		//Displays the GUI
		GenericDialog gd=new GenericDialog("Analyze Particles ColorMap");
		
		gd.addChoice("Reference_image", images, WindowManager.getCurrentImage().getTitle());
		gd.addChoice("Parameter_to_draw", headers, headers[0]);
		gd.addChoice("LUT_to_use", luts, "Grays");
		
		gd.addMessage("");
		gd.addMessage("<html><small><i><p>Analyze Particles ColorMap, "+version+"/"+date+"</p>Infos/bug report: <a href=\"mailto:fabrice.cordelieres@gmail.com\">fabrice.cordelieres@gmail.com</a></i></small></html>");
		gd.showDialog();
		
		//Retrieves and stores parameters
		ip=WindowManager.getImage(gd.getNextChoice());
		paramName=gd.getNextChoice();
		param=rt.getColumn(paramName);	
		lut=gd.getNextChoice();
		
		return gd.wasOKed();
	}
	
	/**
	 * Sets the ImagePlus on which to work
	 * @param ip the input ImagePlus
	 */
	public void setImagePlus(ImagePlus ip) {
		this.ip=ip;
	}
	
	/**
	 * Sets the parameter/column name to be used to create the colorMap
	 * @param paramName the parameter/column name to be used to create the colorMap
	 */
	public void setParamName(String paramName) {
		this.paramName=paramName;
		param=rt.getColumn(paramName);
	}
	
	/**
	 * Sets the name of the LUT to be used to create the colorMap
	 * @param lutName the name of the LUT to be used to create the colorMap (default: Grays)
	 */
	public void setLUT(String lutName) {
		this.lut=lutName;
	}
	
	
	public ImagePlus createColorMap() {
		if(ip==null || paramName==null || param==null) {
			IJ.error("Parameters have not been set (either the image or the parameter's name");
			return null;
		}
		
		//Creates the output image based on the input image
		int[] dimensions=ip.getDimensions();
		ImagePlus out=NewImage.createFloatImage(ip.getTitle()+"_colorMap_for_"+paramName, dimensions[0], dimensions[1], dimensions[3]*dimensions[4], NewImage.FILL_BLACK);
		
		//Only converts to an hyperstack if needed
		if(out.getNSlices()>1) out=HyperStackConverter.toHyperStack(out, 1, dimensions[3], dimensions[4]);
		
		//Colors all ROIs based on the relevant parameter
		for(int i=0; i<rm.getCount(); i++) {
			IJ.showStatus("Processing ROI "+(i+1)+"/"+(rm.getCount()+1));
			Roi roi=rm.getRoi(i);
			int zPos=roi.getZPosition();
			int tPos=roi.getTPosition();
			out.setPositionWithoutUpdate(1, zPos, tPos);
			out.setRoi(roi);
			IJ.run(out, "Add...", "value="+param[i]);
			out.resetRoi();
		}
		
		out.resetDisplayRange();
		IJ.run(out, lut, "");
		return out;
	}
}

package moskitt4me.projectmanager.core.sorters;

import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.BreakdownElement;
import org.eclipse.epf.uma.WorkBreakdownElement;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class ProcessSorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		
		if(e1 instanceof WorkBreakdownElement &&
				e2 instanceof WorkBreakdownElement) {
			
			WorkBreakdownElement wbe1 = (WorkBreakdownElement) e1;
			WorkBreakdownElement wbe2 = (WorkBreakdownElement) e2;
			
			Activity parent1 = wbe1.getSuperActivities();
			Activity parent2 = wbe2.getSuperActivities();
			
			if(parent1.equals(parent2)) {
				
				for(BreakdownElement e : parent1.getBreakdownElements()) {
					if(e.getGuid().equals(wbe1.getGuid())) {
						return -1;
					}
					if(e.getGuid().equals(wbe2.getGuid())) {
						return 1;
					}
				}
				
				return 0;
			}
		}
		
		return super.compare(viewer, e1, e2);
	}
}

package moskitt4me.projectmanager.core.providers;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class ProcessSelectionProvider implements ISelectionProvider {

	private final ListenerList fSelectionChangedListeners;
	private ISelection fSelection;

	public ProcessSelectionProvider() {
		fSelectionChangedListeners = new ListenerList();
	}

	public ISelection getSelection() {
		return fSelection;
	}

	public void setSelection(ISelection selection) {
		fSelection = selection;

		Object[] listeners = fSelectionChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((ISelectionChangedListener) listeners[i])
					.selectionChanged(new SelectionChangedEvent(this, selection));
		}
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionChangedListeners.remove(listener);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionChangedListeners.add(listener);
	}

}
